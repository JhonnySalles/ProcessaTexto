package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.MangaDao
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.mangaextractor.*
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.messages.Mensagens
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.*
import java.util.*
import javax.imageio.ImageIO


class MangaDaoJDBC(conexao: Conexao, base: String) : MangaDao {

    private val LOGGER = LoggerFactory.getLogger(MangaDaoJDBC::class.java)

    companion object {
        private const val CREATE_TABELA = "CALL create_table('%s');"
        private const val TABELA_VOLUME = "_volumes"
        private const val TABELA_CAPITULO = "_capitulos"
        private const val TABELA_PAGINA = "_paginas"
        private const val TABELA_TEXTO = "_textos"
        private const val TABELA_VOCABULARIO = "_vocabularios"
        private const val TABELA_CAPA = "_capas"

        private const val CREATE_TRIGGER_INSERT = "CREATE TRIGGER tr_%s_insert BEFORE INSERT ON %s" +
                "  FOR EACH ROW BEGIN" +
                "    IF (NEW.id IS NULL OR NEW.id = '') THEN" +
                "      SET new.id = UUID();" +
                "    END IF;" +
                "  END"
        private const val CREATE_TRIGGER_UPDATE = "CREATE TRIGGER tr_%s_update BEFORE UPDATE ON %s" +
                "  FOR EACH ROW BEGIN" +
                "    SET new.Atualizacao = NOW();" +
                "  END"

        private const val UPDATE_VOLUMES = "UPDATE %s_volumes SET manga = , volume = , linguagem = , arquivo = , is_processado =  WHERE id = "
        private const val UPDATE_CAPITULOS = "UPDATE %s_capitulos SET manga = , volume = , capitulo = , linguagem = , is_extra = , scan =  WHERE id = "
        private const val UPDATE_CAPITULOS_COM_VOLUME = "UPDATE %s_capitulos SET id_volume = , manga = , volume = , capitulo = , linguagem = , is_extra = , scan =  WHERE id = "
        private const val UPDATE_PAGINAS = "UPDATE %s_paginas SET nome = , numero = , hash_pagina =  WHERE id = "
        private const val UPDATE_TEXTO = "UPDATE %s_textos SET sequencia = , texto = , posicao_x1 = , posicao_y1 = , posicao_x2 = , posicao_y2 =  WHERE id = "
        private const val UPDATE_CAPA = "UPDATE %s_capas SET manga = , volume = , linguagem = , arquivo = , extensao = , capa =  WHERE id = "
        private const val UPDATE_VOLUMES_CANCEL = "UPDATE %s_volumes SET is_processado = 0 WHERE id = "

        private const val INSERT_VOLUMES = "INSERT INTO %s_volumes (id, manga, volume, linguagem, arquivo, is_processado) VALUES (,,,,,)"
        private const val INSERT_CAPITULOS = "INSERT INTO %s_capitulos (id, id_volume, manga, volume, capitulo, linguagem, scan, is_extra, is_raw) VALUES (,,,,,,,,)"
        private const val INSERT_PAGINAS = "INSERT INTO %s_paginas (id, id_capitulo, nome, numero, hash_pagina) VALUES (,,,,)"
        private const val INSERT_TEXTO = "INSERT INTO %s_textos (id, id_pagina, sequencia, texto, posicao_x1, posicao_y1, posicao_x2, posicao_y2) VALUES (,,,,,,,)"
        private const val INSERT_CAPA = "INSERT INTO %s_capas (id, id_volume, manga, volume, linguagem, arquivo, extensao, capa) VALUES (,,,,,,,)"

        private const val DELETE_VOLUMES = "DELETE v FROM %s_volumes AS v %s"
        private const val DELETE_CAPITULOS = "DELETE c FROM %s_capitulos AS c INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s"
        private const val DELETE_PAGINAS = ("DELETE p FROM %s_paginas p "
                + "INNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s")
        private const val DELETE_TEXTOS = ("DELETE t FROM %s_textos AS t INNER JOIN %s_paginas AS p ON p.id = t.id_pagina "
                + "INNER JOIN %s_capitulos AS c ON c.id = p.id_capitulo INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s")
        private const val DELETE_CAPAS = "DELETE c FROM %s_capas AS c INNER JOIN %s_volumes AS v ON v.id = c.id_volume %s"

        private const val SELECT_VOLUMES = "SELECT VOL.id, VOL.manga, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL %s WHERE %s GROUP BY VOL.id ORDER BY VOL.manga, VOL.linguagem, VOL.volume"
        private const val SELECT_CAPITULOS = ("SELECT CAP.id, CAP.manga, CAP.volume, CAP.capitulo, CAP.linguagem, CAP.scan, CAP.is_extra, CAP.is_raw "
                + "FROM %s_capitulos CAP %s WHERE id_volume =  AND %s GROUP BY CAP.id ORDER BY CAP.linguagem, CAP.volume, CAP.is_extra, CAP.capitulo")
        private const val SELECT_PAGINAS = "SELECT id, nome, numero, hash_pagina FROM %s_paginas WHERE id_capitulo =  AND %s "
        private const val SELECT_TEXTOS = "SELECT id, sequencia, texto, posicao_x1, posicao_y1, posicao_x2, posicao_y2 FROM %s_textos WHERE id_pagina =  "
        private const val SELECT_CAPAS = "SELECT id, manga, volume, linguagem, arquivo, extensao, capa FROM %s_capas WHERE id_volume =  "
        private const val FIND_VOLUME =
            "SELECT VOL.id, VOL.manga, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL WHERE manga =  AND volume =  AND linguagem =  LIMIT 1"
        private const val SELECT_VOLUME = "SELECT VOL.id, VOL.manga, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_Processado FROM %s_volumes VOL WHERE id = "
        private const val SELECT_CAPITULO = ("SELECT CAP.id, CAP.manga, CAP.volume, CAP.capitulo, CAP.linguagem, CAP.scan, CAP.is_extra, CAP.is_raw "
                + "FROM %s_capitulos CAP WHERE id = ")
        private const val SELECT_PAGINA = "SELECT id, nome, numero, hash_pagina FROM %s_paginas WHERE id = "
        private const val SELECT_CAPA = "SELECT id, manga, volume, linguagem, arquivo, extensao, capa FROM %s_capas WHERE id =  "
        private const val SELECT_TABELAS = ("SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
                + "FROM information_schema.tables WHERE table_schema = '%s' AND Table_Name NOT LIKE '%%exemplo%%' "
                + "AND Table_Name LIKE '%%_volumes' AND %s GROUP BY Tabela ")

        private const val SELECT_LISTA_TABELAS = ("SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
                + " FROM information_schema.tables WHERE table_schema = '%s' AND %s "
                + " AND Table_Name LIKE '%%_volumes%%' GROUP BY Tabela ")
        private const val DELETE_VOCABULARIO = "DELETE FROM %s_vocabularios WHERE %s = ;"
        private const val INSERT_VOCABULARIO = ("INSERT INTO %s_vocabularios (%s, id_vocabulario) "
                + " VALUES (,);")
        private const val SELECT_VOCABUALARIO = "SELECT id_vocabulario FROM %s_vocabularios WHERE %s "
        private const val UPDATE_PROCESSADO = "UPDATE %s_volumes SET is_processado = 1 WHERE id = "
    }

    private val conn: Connection = JdbcFactory.getFactory(conexao)
    private val vocab: VocabularioDao
    private val schema: String

    init {
        schema = base
        vocab = VocabularioExternoDaoJDBC(conexao)
    }

    private fun getLinguagem(vararg linguagem: Language?): List<Language> {
        val list: MutableList<Language> = mutableListOf()
        for (lang in linguagem)
            lang?.let { list.add(it) }
        return list.toList()
    }


    @Throws(SQLException::class)
    override fun updateVolume(base: String, obj: MangaVolume) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_VOLUMES, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.manga)
            st.setInt(++index, obj.volume!!)
            st.setString(++index, obj.lingua.sigla)
            st.setString(++index, obj.arquivo)
            st.setBoolean(++index, obj.processado)
            st.setString(++index, obj.id.toString())

            insertVocabulario(base, obj.id, null, null, obj.vocabularios)
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                println("Nenhum registro atualizado.")
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun updateCapitulo(base: String, obj: MangaCapitulo) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_CAPITULOS, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.manga)
            st.setInt(++index, obj.volume!!)
            st.setFloat(++index, obj.capitulo!!)
            st.setString(++index, obj.lingua.sigla)
            st.setBoolean(++index, obj.isExtra)
            st.setString(++index, obj.scan)
            st.setString(++index, obj.id.toString())

            insertVocabulario(base, null, obj.id, null, obj.vocabularios)
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                println("Nenhum registro atualizado.")
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun updateCapitulo(base: String, IdVolume: UUID, obj: MangaCapitulo) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_CAPITULOS_COM_VOLUME, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, IdVolume.toString())
            st.setString(++index, obj.manga)
            st.setInt(++index, obj.volume!!)
            st.setFloat(++index, obj.capitulo!!)
            st.setString(++index, obj.lingua.sigla)
            st.setBoolean(++index, obj.isExtra)
            st.setString(++index, obj.scan)
            st.setString(++index, obj.id.toString())

            insertVocabulario(base, null, obj.id, null, obj.vocabularios)
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                println("Nenhum registro atualizado.")
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun insertVocabulario(base: String, idVolume: UUID?, idCapitulo: UUID?, idPagina: UUID?, vocabulario: Set<VocabularioExterno>) {
        var st: PreparedStatement? = null
        try {
            if (idVolume == null && idCapitulo == null && idPagina == null) return
            val campo = if (idVolume != null) "id_volume" else if (idCapitulo != null) "id_capitulo" else "id_pagina"
            val id: UUID = idVolume ?: (idCapitulo ?: (idPagina ?: return))

            clearVocabulario(base, campo, id)

            for (vocab in vocabulario) {
                insertNotExists(vocab)
                st = conn.prepareStatement(String.format(INSERT_VOCABULARIO, base, campo), Statement.RETURN_GENERATED_KEYS)
                st.setString(1, id.toString())
                st.setString(2, vocab.getId().toString())
                st.executeUpdate()
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    private fun clearVocabulario(base: String, campo: String, id: UUID) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(DELETE_VOCABULARIO, base, campo))
            st.setString(1, id.toString())
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    private fun insertNotExists(vocabulario: VocabularioExterno) {
        if (!vocab.exist(vocabulario.getId().toString()))
            vocab.insert(vocabulario)
    }

    @Throws(SQLException::class)
    private fun selectVocabulario(base: String, where: String): MutableSet<VocabularioExterno> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_VOCABUALARIO, base, where))
            rs = st.executeQuery()
            val list: MutableSet<VocabularioExterno> = mutableSetOf()
            while (rs.next())
                list.add(vocab.select(rs.getString("id_vocabulario")) as VocabularioExterno)
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun updatePagina(base: String, obj: MangaPagina) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_PAGINAS, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.nomePagina)
            st.setInt(++index, obj.numero)
            st.setString(++index, obj.hash)
            st.setString(++index, obj.id.toString())

            insertVocabulario(base, null, null, obj.id, obj.vocabularios)
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_UPDATE)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun updateTexto(base: String, obj: MangaTexto) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_TEXTO, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setInt(++index, obj.sequencia)
            st.setString(++index, obj.texto)
            st.setInt(++index, obj.x1)
            st.setInt(++index, obj.y1)
            st.setInt(++index, obj.x2)
            st.setInt(++index, obj.y2)
            st.setString(++index, obj.id.toString())

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_UPDATE)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun updateCapa(base: String, obj: MangaCapa) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_CAPA, base), Statement.RETURN_GENERATED_KEYS)
            var index = 0
            st.setString(++index, obj.manga)
            st.setInt(++index, obj.volume!!)
            st.setString(++index, obj.lingua.sigla)
            st.setString(++index, obj.arquivo)
            st.setString(++index, obj.extenssao)

            val baos = ByteArrayOutputStream()
            ImageIO.write(obj.imagem, obj.extenssao, baos)
            st.setBinaryStream(++index, ByteArrayInputStream(baos.toByteArray()))
            st.setString(++index, obj.id.toString())

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            throw RuntimeException(e)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun updateCancel(base: String, obj: MangaVolume) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_VOLUMES_CANCEL, base))
            st.setString(1, obj.id.toString())
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE_CANCEL)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    fun selectVolumes(base: String, todos: Boolean, apenasJapones: Boolean): MutableList<MangaVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = "VOL.linguagem = 'ja'"
            if (!todos)
                condicao += " AND is_processado = 0 "
            st = conn.prepareStatement(String.format(SELECT_VOLUMES, base, "", condicao))
            rs = st.executeQuery()
            val list: MutableList<MangaVolume> = mutableListOf()
            while (rs.next())
                list.add(
                    MangaVolume(
                        UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getString("arquivo"),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"'),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), apenasJapones),
                        selectCapas(base, UUID.fromString(rs.getString("id")))
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    fun selectVolumes(base: String, todos: Boolean, manga: String, volume: Int, capitulo: Float, linguagem: List<Language>?, inverterTexto: Boolean): MutableList<MangaVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            val inner = ""
            var condicao = " 1>0 "
            if (linguagem != null && linguagem.isNotEmpty()) {
                var lang = ""
                for (lg in linguagem)
                    lang += " VOL.linguagem = '" + lg.sigla + "' OR "
                condicao += " AND (" + lang.substring(0, lang.lastIndexOf(" OR ")) + ")"
            }
            if (!todos)
                condicao += " AND VOL.is_processado = 0 "
            if (manga.trim().isNotEmpty())
                condicao += " AND VOL.manga LIKE " + '"' + manga.trim() + '"'
            if (volume > 0)
                condicao += " AND VOL.volume = $volume"
            st = conn.prepareStatement(String.format(SELECT_VOLUMES, base, inner, condicao))
            rs = st.executeQuery()
            val list: MutableList<MangaVolume> = mutableListOf()
            while (rs.next())
                list.add(
                    MangaVolume(
                        UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getString("arquivo"),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"'),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), capitulo, linguagem, inverterTexto),
                        selectCapas(base, UUID.fromString(rs.getString("id")))
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    private fun selectCapitulosTransferir(base: String, tabela: String, idOldVolume: String): MutableList<MangaCapitulo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_CAPITULOS, base + tabela, "", "1>0"))
            st.setString(1, idOldVolume)
            rs = st.executeQuery()
            val list: MutableList<MangaCapitulo> = mutableListOf()
            while (rs.next()) {
                val id: UUID = UUID.randomUUID()
                list.add(
                    MangaCapitulo(
                        id, rs.getString("manga"), rs.getInt("volume"), rs.getFloat("capitulo"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getString("scan"), rs.getBoolean("is_extra"),
                        rs.getBoolean("is_raw"), selectPaginasTransferir(base, tabela, rs.getString("id"))
                    )
                )
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    fun selectCapitulos(base: String, idVolume: UUID, apenasJapones: Boolean): MutableList<MangaCapitulo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            val inner = ""
            var where = "1>0"
            if (apenasJapones)
                where = "CAP.linguagem = 'ja'"

            st = conn.prepareStatement(String.format(SELECT_CAPITULOS, base, inner, where))
            st.setString(1, idVolume.toString())
            rs = st.executeQuery()
            val list: MutableList<MangaCapitulo> = mutableListOf()
            while (rs.next())
                list.add(
                    MangaCapitulo(
                        UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                        rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem"))!!, rs.getString("scan"),
                        rs.getBoolean("is_extra"), rs.getBoolean("is_raw"),
                        selectVocabulario(base, "id_capitulo = " + '"' + UUID.fromString(rs.getString("id")) + '"'),
                        selectPaginas(base, UUID.fromString(rs.getString("id")), inverterTexto = false, selectVocabulario = false)
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    fun selectCapitulos(base: String, idVolume: UUID, capitulo: Float?, linguagem: List<Language>?, inverterTexto: Boolean): MutableList<MangaCapitulo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            val inner = ""
            var condicao = " 1>0 "
            if (linguagem != null && linguagem.isNotEmpty()) {
                var lang = ""
                for (lg in linguagem)
                    lang += " CAP.linguagem = '" + lg.sigla + "' OR "
                condicao += " AND (" + lang.substring(0, lang.lastIndexOf(" OR ")) + ")"
            }
            if (capitulo != null && capitulo > 0)
                condicao += " AND CAP.capitulo = $capitulo"
            st = conn.prepareStatement(String.format(SELECT_CAPITULOS, base, inner, condicao))
            st.setString(1, idVolume.toString())
            rs = st.executeQuery()
            val list: MutableList<MangaCapitulo> = mutableListOf()
            while (rs.next())
                list.add(
                    MangaCapitulo(
                        UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                        rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem"))!!, rs.getString("scan"),
                        rs.getBoolean("is_extra"), rs.getBoolean("is_raw"),
                        selectVocabulario(base, "id_capitulo = " + '"' + UUID.fromString(rs.getString("id")) + '"'),
                        selectPaginas(base, UUID.fromString(rs.getString("id")), inverterTexto, true)
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    private fun selectPaginasTransferir(base: String, tabela: String, idOldCapitulo: String): MutableList<MangaPagina> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_PAGINAS, base + tabela, "1>0"))
            st.setString(1, idOldCapitulo)
            rs = st.executeQuery()
            val list: MutableList<MangaPagina> = mutableListOf()
            while (rs.next()) {
                val id: UUID = UUID.randomUUID()
                list.add(
                    MangaPagina(
                        id, rs.getString("nome"), rs.getInt("numero"), rs.getString("hash_pagina"),
                        selectTextosTransferir(base, tabela, rs.getString("id"))
                    )
                )
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    fun selectPaginas(base: String, idCapitulo: UUID, inverterTexto: Boolean, selectVocabulario: Boolean): MutableList<MangaPagina> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_PAGINAS, base, "1>0"))
            st.setString(1, idCapitulo.toString())
            rs = st.executeQuery()
            val list: MutableList<MangaPagina> = mutableListOf()
            while (rs.next())
                list.add(
                    MangaPagina(
                        UUID.fromString(rs.getString("id")), rs.getString("nome"), rs.getInt("numero"),
                        rs.getString("hash_pagina"), selectTextos(base, UUID.fromString(rs.getString("id")), inverterTexto),
                        if (selectVocabulario) selectVocabulario(base, "id_pagina = " + '"' + UUID.fromString(rs.getString("id")) + '"') else HashSet()
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    private fun selectTextosTransferir(base: String, tabela: String, idOldPagina: String): MutableList<MangaTexto> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_TEXTOS, base + tabela))
            st.setString(1, idOldPagina)
            rs = st.executeQuery()
            val list: MutableList<MangaTexto> = mutableListOf()
            while (rs.next())
                list.add(
                    MangaTexto(
                        UUID.randomUUID(), rs.getString("texto"), rs.getInt("sequencia"),
                        rs.getInt("posicao_x1"), rs.getInt("posicao_y1"), rs.getInt("posicao_x2"),
                        rs.getInt("posicao_y2")
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    private fun selectTextos(base: String, idPagina: UUID, inverterTexto: Boolean): MutableList<MangaTexto> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var order = ""
            if (inverterTexto)
                order = " ORDER BY sequencia DESC"
            st = conn.prepareStatement(String.format(SELECT_TEXTOS, base) + order)
            st.setString(1, idPagina.toString())
            rs = st.executeQuery()
            val list: MutableList<MangaTexto> = mutableListOf()
            var sequencia = 1
            while (rs.next())
                list.add(
                    MangaTexto(
                        UUID.fromString(rs.getString("id")), rs.getString("texto"), sequencia++, rs.getInt("posicao_x1"),
                        rs.getInt("posicao_y1"), rs.getInt("posicao_x2"), rs.getInt("posicao_y2")
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    private fun selectCapas(base: String, idVolume: UUID): MangaCapa? {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_CAPAS, base))
            st.setString(1, idVolume.toString())
            rs = st.executeQuery()
            if (rs.next()) {
                val input = ByteArrayInputStream(rs.getBinaryStream("capa").readAllBytes())
                val image: BufferedImage = ImageIO.read(input)
                MangaCapa(
                    UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                    Language.getEnum(rs.getString("linguagem"))!!, rs.getString("arquivo"), rs.getString("extensao"),
                    image
                )
            } else
                null
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            throw RuntimeException(e)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun selectVolume(base: String, manga: String, volume: Int, linguagem: Language): Optional<MangaVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(FIND_VOLUME, base))
            st.setString(1, manga)
            st.setInt(2, volume)
            st.setString(3, linguagem.sigla)
            LOGGER.info(st.toString())
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    MangaVolume(
                        UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getString("arquivo"),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"'),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), false),
                        selectCapas(base, UUID.fromString(rs.getString("id")))
                    )
                )
            else
                Optional.empty<MangaVolume>()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun selectVolume(base: String, id: UUID): Optional<MangaVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_VOLUME, base))
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    MangaVolume(
                        UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getString("arquivo"),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"'),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), false),
                        selectCapas(base, UUID.fromString(rs.getString("id")))
                    )
                )
            else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun selectCapitulo(base: String, id: UUID): Optional<MangaCapitulo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_CAPITULO, base))
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    MangaCapitulo(
                        UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                        rs.getFloat("capitulo"), Language.getEnum(rs.getString("linguagem"))!!, rs.getString("scan"),
                        rs.getBoolean("is_extra"), rs.getBoolean("is_raw"),
                        selectVocabulario(base, "id_capitulo = " + '"' + UUID.fromString(rs.getString("id")) + '"'),
                        selectPaginas(base, UUID.fromString(rs.getString("id")), inverterTexto = false, selectVocabulario = false)
                    )
                )
            else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun selectPagina(base: String, id: UUID): Optional<MangaPagina> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_PAGINA, base))
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    MangaPagina(
                        UUID.fromString(rs.getString("id")), rs.getString("nome"), rs.getInt("numero"),
                        rs.getString("hash_pagina"), selectTextos(base, UUID.fromString(rs.getString("id")), false), HashSet()
                    )
                )
            else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun selectCapa(base: String, id: UUID): Optional<MangaCapa> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_CAPA, base))
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next()) {
                val input = ByteArrayInputStream(rs.getBinaryStream("capa").readAllBytes())
                val image: BufferedImage = ImageIO.read(input)
                Optional.of(
                    MangaCapa(
                        UUID.fromString(rs.getString("id")), rs.getString("manga"), rs.getInt("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getString("arquivo"), rs.getString("extenssao"),
                        image
                    )
                )
            } else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            throw RuntimeException(e)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun selectAll(base: String): MutableList<MangaTabela> {
        return selectAll(base, "", 0, 0f, null)
    }

    @Throws(SQLException::class)
    override fun selectAll(base: String?, manga: String, volume: Int, capitulo: Float, linguagem: Language?): MutableList<MangaTabela> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = "1>0 "
            if (base != null && base.trim().isNotEmpty())
                condicao += " AND Table_Name LIKE '%" + base.trim() + "%'"
            st = conn.prepareStatement(String.format(SELECT_TABELAS, schema, condicao))
            rs = st.executeQuery()
            val list: MutableList<MangaTabela> = mutableListOf()
            while (rs.next()) {
                val volumes: MutableList<MangaVolume> = selectVolumes(rs.getString("Tabela"), true, manga, volume, capitulo, getLinguagem(linguagem), false)
                if (volumes.isNotEmpty())
                    list.add(MangaTabela(rs.getString("Tabela"), volumes))
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun selectTabelas(todos: Boolean): List<MangaTabela> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_TABELAS, schema, "1>0"))
            rs = st.executeQuery()
            val list: MutableList<MangaTabela> = ArrayList()
            while (rs.next()) {
                val volumes = selectVolumes(rs.getString("Tabela"), todos, true)
                if (volumes.isNotEmpty())
                    list.add(MangaTabela(rs.getString("Tabela"), volumes))
            }
            list.toList()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, manga: String): List<MangaTabela> {
        return selectTabelas(todos, isLike, base, linguagem, manga, 0, 0f)
    }


    @Throws(SQLException::class)
    override fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, manga: String, volume: Int): List<MangaTabela> {
        return selectTabelas(todos, isLike, base, linguagem, manga, volume, 0f)
    }


    @Throws(SQLException::class)
    override fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, manga: String, volume: Int, capitulo: Float): List<MangaTabela> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = "1>0 "
            if (base.trim().isNotEmpty()) {
                condicao += if (isLike) " AND Table_Name LIKE '%" + base.trim() + "%'" else " AND Table_Name LIKE '" + base.trim() + "_volumes'"
            }
            st = conn.prepareStatement(String.format(SELECT_TABELAS, schema, condicao))
            rs = st.executeQuery()
            val list: MutableList<MangaTabela> = mutableListOf()
            while (rs.next()) {
                val volumes = selectVolumes(rs.getString("Tabela"), todos, manga, volume, capitulo, getLinguagem(linguagem), false)
                if (volumes.isNotEmpty())
                    list.add(MangaTabela(rs.getString("Tabela"), volumes))
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun deletarVocabulario(base: String) {
        var stVolume: PreparedStatement? = null
        var stVocabulario: PreparedStatement? = null
        try {
            stVocabulario = conn.prepareStatement(String.format("DELETE FROM %s_vocabulario", base))
            stVolume = conn.prepareStatement(String.format("UPDATE %s_volumes SET is_processado = 0", base))
            conn.autoCommit = false
            conn.beginRequest()
            stVocabulario.executeUpdate()
            stVolume.executeUpdate()
            conn.commit()
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            println(stVocabulario.toString())
            println(stVolume.toString())
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(stVocabulario)
            JdbcFactory.closeStatement(stVolume)
        }
    }


    @Throws(SQLException::class)
    override fun deleteVolume(base: String, obj: MangaVolume) {
        var stVolume: PreparedStatement? = null
        var stCapa: PreparedStatement? = null
        var stCapitulo: PreparedStatement? = null
        var stPagina: PreparedStatement? = null
        var stTexto: PreparedStatement? = null
        try {
            var where = "WHERE "
            where += if (obj.id != null)
                " v.id = " + '"' + obj.id.toString() + '"'
            else
                " v.manga = '" + obj.manga + "' AND v.volume = " + obj.volume.toString() + " AND v.linguagem = '" + obj.lingua.sigla + "'"
            
            stTexto = conn.prepareStatement(String.format(DELETE_TEXTOS, base, base, base, base, where))
            stPagina = conn.prepareStatement(String.format(DELETE_PAGINAS, base, base, base, where))
            stCapitulo = conn.prepareStatement(String.format(DELETE_CAPITULOS, base, base, where))
            stCapa = conn.prepareStatement(String.format(DELETE_CAPAS, base, base, where))
            stVolume = conn.prepareStatement(String.format(DELETE_VOLUMES, base, where))
            
            conn.autoCommit = false
            conn.beginRequest()
            stTexto.executeUpdate()
            stPagina.executeUpdate()
            stCapitulo.executeUpdate()
            stCapa.executeUpdate()
            stVolume.executeUpdate()
            conn.commit()
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            println(stTexto.toString())
            println(stPagina.toString())
            println(stCapitulo.toString())
            println(stCapa.toString())
            println(stVolume.toString())
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(stTexto)
            JdbcFactory.closeStatement(stPagina)
            JdbcFactory.closeStatement(stCapitulo)
            JdbcFactory.closeStatement(stCapa)
            JdbcFactory.closeStatement(stVolume)
        }
    }


    @Throws(SQLException::class)
    override fun deleteCapitulo(base: String, obj: MangaCapitulo) {
        var stCapitulo: PreparedStatement? = null
        var stPagina: PreparedStatement? = null
        var stTexto: PreparedStatement? = null
        try {
            val where = "WHERE c.id = '" + obj.id.toString() + "'"
            
            stTexto = conn.prepareStatement(String.format(DELETE_TEXTOS, base, base, base, base, where))
            stPagina = conn.prepareStatement(String.format(DELETE_PAGINAS, base, base, base, where))
            stCapitulo = conn.prepareStatement(String.format(DELETE_CAPITULOS, base, base, where))
            
            conn.autoCommit = false
            conn.beginRequest()
            stTexto.executeUpdate()
            stPagina.executeUpdate()
            stCapitulo.executeUpdate()
            conn.commit()
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            println(stTexto.toString())
            println(stPagina.toString())
            println(stCapitulo.toString())
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(stTexto)
            JdbcFactory.closeStatement(stPagina)
            JdbcFactory.closeStatement(stCapitulo)
        }
    }


    @Throws(SQLException::class)
    override fun deletePagina(base: String, obj: MangaPagina) {
        var stPagina: PreparedStatement? = null
        var stTexto: PreparedStatement? = null
        try {
            val where = "WHERE p.id = '" + obj.id.toString() + "'"
            stTexto = conn.prepareStatement(String.format(DELETE_TEXTOS, base, base, base, base, where))
            stPagina = conn.prepareStatement(String.format(DELETE_PAGINAS, base, base, base, where))
            
            conn.autoCommit = false
            conn.beginRequest()
            stTexto.executeUpdate()
            stPagina.executeUpdate()
            conn.commit()
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            println(stTexto.toString())
            println(stPagina.toString())
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(stTexto)
            JdbcFactory.closeStatement(stPagina)
        }
    }


    @Throws(SQLException::class)
    override fun deleteTexto(base: String, obj: MangaTexto) {
        var stTexto: PreparedStatement? = null
        try {
            val where = "WHERE t.id = '" + obj.id.toString() + "'"
            stTexto = conn.prepareStatement(String.format(DELETE_TEXTOS, base, base, base, base, where))

            conn.autoCommit = false
            conn.beginRequest()
            stTexto.executeUpdate()
            conn.commit()
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            println(stTexto.toString())
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(stTexto)
        }
    }


    @Throws(SQLException::class)
    override fun deleteCapa(base: String, obj: MangaCapa) {
        var stCapa: PreparedStatement? = null
        try {
            val where = "WHERE c.id = '" + obj.id.toString() + "'"
            stCapa = conn.prepareStatement(String.format(DELETE_CAPAS, base, base, where))
            conn.autoCommit = false
            conn.beginRequest()
            stCapa.executeUpdate()
            conn.commit()
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            println(stCapa.toString())
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(stCapa)
        }
    }


    @Throws(SQLException::class)
    override fun insertVolume(base: String, obj: MangaVolume): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(INSERT_VOLUMES, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, obj.manga)
            st.setInt(++index, obj.volume!!)
            st.setString(++index, obj.lingua.sigla)
            st.setString(++index, obj.arquivo)
            st.setBoolean(++index, obj.processado)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else {
                insertVocabulario(base, obj.id, null, null, obj.vocabularios)
                obj.id!!
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun insertCapitulo(base: String, idVolume: UUID, obj: MangaCapitulo): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(INSERT_CAPITULOS, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, idVolume.toString())
            st.setString(++index, obj.manga)
            st.setInt(++index, obj.volume!!)
            st.setFloat(++index, obj.capitulo!!)
            st.setString(++index, obj.lingua.sigla)
            st.setString(++index, obj.scan)
            st.setBoolean(++index, obj.isExtra)
            st.setBoolean(++index, obj.isRaw)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else {
                insertVocabulario(base, null, obj.id, null, obj.vocabularios)
                obj.id!!
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun insertPagina(base: String, idCapitulo: UUID, obj: MangaPagina): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(INSERT_PAGINAS, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, idCapitulo.toString())
            st.setString(++index, obj.nomePagina)
            st.setInt(++index, obj.numero)
            st.setString(++index, obj.hash)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else {
                insertVocabulario(base, null, null, obj.id, obj.vocabularios)
                obj.id!!
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun insertTexto(base: String, idPagina: UUID, obj: MangaTexto): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(INSERT_TEXTO, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, idPagina.toString())
            st.setInt(++index, obj.sequencia)
            st.setString(++index, obj.texto)
            st.setInt(++index, obj.x1)
            st.setInt(++index, obj.y1)
            st.setInt(++index, obj.x2)
            st.setInt(++index, obj.y2)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else
                obj.id!!
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun insertCapa(base: String, idVolume: UUID, obj: MangaCapa): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(DELETE_CAPAS, base, base, "WHERE c.id_volume = '$idVolume'"))
            conn.autoCommit = false
            conn.beginRequest()
            st.executeUpdate()
            conn.commit()
            
            st = conn.prepareStatement(String.format(INSERT_CAPA, base), Statement.RETURN_GENERATED_KEYS)
            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, idVolume.toString())
            st.setString(++index, obj.manga)
            st.setInt(++index, obj.volume!!)
            st.setString(++index, obj.lingua.sigla)
            st.setString(++index, obj.arquivo)
            st.setString(++index, obj.extenssao)
            
            val baos = ByteArrayOutputStream()
            ImageIO.write(obj.imagem, obj.extenssao, baos)
            st.setBinaryStream(++index, ByteArrayInputStream(baos.toByteArray()))
            
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else
                obj.id!!
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            throw RuntimeException(e)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun selectDadosTransferir(base: String, tabela: String): MutableList<MangaVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_VOLUMES, base + tabela, "", "1>0"))
            rs = st.executeQuery()
            val list: MutableList<MangaVolume> = mutableListOf()
            while (rs.next()) {
                val id: UUID = UUID.randomUUID()
                list.add(
                    MangaVolume(
                        id, rs.getString("manga"), rs.getInt("volume"), Language.getEnum(rs.getString("linguagem"))!!,
                        rs.getString("arquivo"), selectCapitulosTransferir(base, tabela, rs.getString("id"))
                    )
                )
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }


    @Throws(SQLException::class)
    override fun getTabelasTransferir(base: String, tabela: String): MutableList<String> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var filtrar = "1 > 0"
            if (tabela.trim() !== "*")
                filtrar = "Table_Name = '%%$tabela%%'"
            st = conn.prepareStatement(String.format(SELECT_LISTA_TABELAS, base, filtrar))
            rs = st.executeQuery()
            val list: MutableList<String> = mutableListOf()
            while (rs.next())
                list.add(rs.getString("Tabela"))
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    private fun createTriggers(nome: String) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(CREATE_TRIGGER_INSERT, nome, nome))
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
        try {
            st = conn.prepareStatement(String.format(CREATE_TRIGGER_UPDATE, nome, nome))
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun createTabela(baseDestino: String) {
        var nome: String = baseDestino.trim()
        if (nome.contains(".")) nome = baseDestino.substring(baseDestino.indexOf(".")).replace(".", "")
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(CREATE_TABELA, nome))
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
        createTriggers(nome + TABELA_VOLUME)
        createTriggers(nome + TABELA_CAPITULO)
        createTriggers(nome + TABELA_PAGINA)
        createTriggers(nome + TABELA_TEXTO)
        createTriggers(nome + TABELA_CAPA)
        try {
            st = conn.prepareStatement(String.format(CREATE_TRIGGER_UPDATE, nome + TABELA_VOCABULARIO, nome + TABELA_VOCABULARIO))
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun updateProcessado(base: String, id: UUID) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_PROCESSADO, base), Statement.RETURN_GENERATED_KEYS)
            st.setString(1, id.toString())
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_UPDATE)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }


    @Throws(SQLException::class)
    override fun selectTabelasJson(base: String?, manga: String, volume: Int, capitulo: Float, linguagem: Language, inverterTexto: Boolean): List<MangaTabela> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = "1>0 "
            if (base != null && base.trim().isNotEmpty())
                condicao += " AND Table_Name LIKE '%" + base.trim() + "%'"

            st = conn.prepareStatement(String.format(SELECT_TABELAS, schema, condicao))
            rs = st.executeQuery()
            val list: MutableList<MangaTabela> = mutableListOf()
            while (rs.next()) {
                val volumes: MutableList<MangaVolume> = selectVolumes(rs.getString("Tabela"), true, manga, volume, capitulo, getLinguagem(linguagem), inverterTexto)
                if (volumes.isNotEmpty())
                    list.add(MangaTabela(rs.getString("Tabela"), volumes))
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @get:Throws(SQLException::class)
    @get:Override
    override val tabelas: List<String>
        get() {
            var st: PreparedStatement? = null
            var rs: ResultSet? = null
            return try {
                st = conn.prepareStatement(String.format(SELECT_LISTA_TABELAS, schema, "1 > 0"))
                rs = st.executeQuery()
                val list: MutableList<String> = mutableListOf()
                while (rs.next())
                    list.add(rs.getString("Tabela"))
                list
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_SELECT)
            } finally {
                JdbcFactory.closeStatement(st)
                JdbcFactory.closeResultSet(rs)
            }
        }

}