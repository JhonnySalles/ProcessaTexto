package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.NovelDao
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.novelextractor.NovelCapitulo
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.messages.Mensagens
import org.jisho.textosJapones.model.entities.novelextractor.*
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.sql.*
import java.util.*
import javax.imageio.ImageIO
import kotlin.RuntimeException as RuntimeException1


class NovelDaoJDBC(conexao: Conexao, base: String) : NovelDao {

    private val LOGGER = LoggerFactory.getLogger(NovelDaoJDBC::class.java)

    companion object {
        private const val CREATE_TABELA = "CALL create_table('%s');"
        private const val TABELA_VOLUME = "_volumes"
        private const val TABELA_CAPITULO = "_capitulos"
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
        private const val INSERT_VOLUMES =
            "INSERT INTO %s_volumes (id, novel, titulo, titulo_alternativo, descricao, editora, volume, linguagem, arquivo, is_processado) VALUES (?,?,?,?,?,?,?,?,?,?)"
        private const val INSERT_CAPITULOS = "INSERT INTO %s_capitulos (id, id_volume, novel, volume, capitulo, descricao, sequencia, linguagem) VALUES (?,?,?,?,?,?,?,?)"
        private const val INSERT_TEXTO = "INSERT INTO %s_textos (id, id_capitulo, sequencia, texto) VALUES (?,?,?,?)"
        private const val INSERT_CAPA = "INSERT INTO %s_capas (id, id_volume, novel, volume, linguagem, arquivo, extensao, capa) VALUES (?,?,?,?,?,?,?,?)"
        private const val DELETE_VOLUMES = "CALL delete_volume('%s', '%s');"
        private const val SELECT_VOLUMES =
            "SELECT VOL.id, VOL.novel, VOL.titulo, VOL.titulo_alternativo, VOL.serie, VOL.descricao, VOL.editora, VOL.autor,VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_favorito, VOL.is_Processado" +
                    " FROM %s_volumes VOL WHERE %s GROUP BY VOL.id ORDER BY VOL.novel, VOL.linguagem, VOL.volume"
        private const val SELECT_CAPITULOS = ("SELECT CAP.id, CAP.novel, CAP.volume, CAP.capitulo, CAP.descricao, CAP.sequencia, CAP.linguagem "
                + "FROM %s_capitulos CAP WHERE id_volume = ? AND %s GROUP BY CAP.id ORDER BY CAP.linguagem, CAP.volume")
        private const val SELECT_TEXTOS = "SELECT id, sequencia, texto FROM %s_textos WHERE id_capitulo = ? "
        private const val SELECT_CAPA = "SELECT id, novel, volume, linguagem, arquivo, extensao, capa FROM %s_capas WHERE id_volume = ? "
        private const val FIND =
            "SELECT VOL.id, VOL.novel, VOL.titulo, VOL.titulo_alternativo, VOL.serie, VOL.descricao, VOL.editora, VOL.autor, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_favorito, VOL.is_Processado FROM %s_volumes VOL"
        private const val FIND_VOLUME = "$FIND WHERE novel = ? AND volume = ? AND linguagem = ? LIMIT 1"
        private const val FIND_ARQUIVO = "$FIND WHERE arquivo = ? AND linguagem = ? LIMIT 1"
        private const val SELECT_VOLUME =
            "SELECT VOL.id, VOL.novel, VOL.titulo, VOL.titulo_alternativo, VOL.serie, VOL.descricao, VOL.editora, VOL.autor, VOL.volume, VOL.linguagem, VOL.arquivo, VOL.is_favorito, VOL.is_Processado FROM %s_volumes VOL WHERE id = ?"
        private const val SELECT_CAPITULO = "SELECT CAP.id, CAP.novel, CAP.volume, CAP.capitulo, CAP.descricao, CAP.sequencia, CAP.linguagem FROM %s_capitulos CAP WHERE id = ?"
        private const val UPDATE_VOLUMES_CANCEL = "UPDATE %s_volumes SET is_processado = 0 WHERE id = ?"
        private const val SELECT_TABELAS = ("SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
                + "FROM information_schema.tables WHERE table_schema = '%s' AND Table_Name NOT LIKE '%%exemplo%%' "
                + "AND Table_Name LIKE '%%_volumes' AND %s GROUP BY Tabela ")
        private const val SELECT_LISTA_TABELAS = ("SELECT REPLACE(Table_Name, '_volumes', '') AS Tabela "
                + " FROM information_schema.tables WHERE table_schema = '%s' AND %s "
                + " AND Table_Name LIKE '%%_volumes%%' GROUP BY Tabela ")
        private const val DELETE_VOCABULARIO = "DELETE FROM %s_vocabularios WHERE %s = ?;"
        private const val INSERT_VOCABULARIO = ("INSERT INTO %s_vocabularios (%s, id_vocabulario) "
                + " VALUES (?,?);")
        private const val SELECT_VOCABUALARIO = "SELECT id_vocabulario FROM %s_vocabularios WHERE %s "
        private const val UPDATE_PROCESSADO = "UPDATE %s_volumes SET is_processado = 1 WHERE id = ?"
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
    override fun insertVocabulario(base: String, idVolume: UUID?, idCapitulo: UUID?, vocabulario: MutableSet<VocabularioExterno>) {
        var st: PreparedStatement? = null
        try {
            if (idVolume == null && idCapitulo == null)
                return

            val campo = if (idVolume != null) "id_volume" else "id_capitulo"
            val id = idVolume ?: idCapitulo!!
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
    override fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language?, novel: String): MutableList<NovelTabela> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = "1>0 "
            if (base.trim().isNotEmpty()) {
                condicao += if (isLike)
                    " AND Table_Name LIKE '%" + base.trim() + "%'"
                else
                    " AND Table_Name LIKE '" + base.trim() + "_volumes'"
            }

            st = conn.prepareStatement(String.format(SELECT_TABELAS, schema, condicao))
            rs = st.executeQuery()
            val list: MutableList<NovelTabela> = mutableListOf()
            while (rs.next()) {
                val volumes = selectVolumes(rs.getString("Tabela"), todos, novel, 0, getLinguagem(linguagem))
                if (volumes.isNotEmpty())
                    list.add(NovelTabela(rs.getString("Tabela"), volumes))
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
    private fun selectVocabulario(base: String?, where: String): MutableSet<VocabularioExterno> {
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
    fun selectVolumes(base: String, todos: Boolean, novel: String, volume: Int, linguagem: List<Language>): MutableList<NovelVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = " 1>0 "
            if (linguagem.isNotEmpty()) {
                var lang = ""
                for (lg in linguagem)
                    lang += " VOL.linguagem = '" + lg.sigla + "' OR "
                condicao += " AND (" + lang.substring(0, lang.lastIndexOf(" OR ")) + ")"
            }

            if (!todos)
                condicao += " AND VOL.is_processado = 0 "

            if (novel.trim().isNotEmpty())
                condicao += " AND VOL.novel LIKE " + '"' + novel.trim() + '"'

            if (volume > 0)
                condicao += " AND VOL.volume = $volume"

            st = conn.prepareStatement(String.format(SELECT_VOLUMES, base, condicao))
            rs = st.executeQuery()
            val list: MutableList<NovelVolume> = mutableListOf()
            while (rs.next())
                list.add(
                    NovelVolume(
                        UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getString("titulo"), rs.getString("titulo_alternativo"), rs.getString("serie"), rs.getString("descricao"),
                        rs.getString("arquivo"), rs.getString("editora"), rs.getString("autor"), rs.getFloat("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getBoolean("is_favorito"),
                        selectCapa(base, UUID.fromString(rs.getString("id"))).orElse(null), rs.getBoolean("is_processado"),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), linguagem),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"')
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
    fun selectCapitulos(base: String, idVolume: UUID, linguagem: List<Language>): MutableList<NovelCapitulo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = " 1>0 "
            if (linguagem.isNotEmpty()) {
                var lang = ""
                for (lg in linguagem)
                    lang += " CAP.linguagem = '" + lg.sigla + "' OR "
                condicao += " AND (" + lang.substring(0, lang.lastIndexOf(" OR ")) + ")"
            }
            st = conn.prepareStatement(String.format(SELECT_CAPITULOS, base, condicao))
            st.setString(1, idVolume.toString())
            rs = st.executeQuery()
            val list: MutableList<NovelCapitulo> = mutableListOf()
            while (rs.next())
                list.add(
                    NovelCapitulo(
                        UUID.fromString(rs.getString("id")), rs.getString("novel"), rs.getFloat("volume"),
                        rs.getFloat("capitulo"), rs.getString("descricao"), rs.getInt("sequencia"), Language.getEnum(rs.getString("linguagem"))!!,
                        selectTextos(base, UUID.fromString(rs.getString("id"))), selectVocabulario(base, "id_capitulo = " + '"' + UUID.fromString(rs.getString("id")) + '"')
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
    fun selectTextos(base: String, idCapitulo: UUID): MutableList<NovelTexto> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_TEXTOS, base))
            st.setString(1, idCapitulo.toString())
            rs = st.executeQuery()
            val list: MutableList<NovelTexto> = mutableListOf()
            while (rs.next())
                list.add(NovelTexto(UUID.fromString(rs.getString("id")), rs.getString("texto"), rs.getInt("sequencia")))
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
    fun selectCapa(base: String, idNovel: UUID): Optional<NovelCapa> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_CAPA, base))
            st.setString(1, idNovel.toString())
            rs = st.executeQuery()
            if (rs.next()) {
                val input: InputStream = ByteArrayInputStream(rs.getBinaryStream("capa").readAllBytes())
                val image: BufferedImage = ImageIO.read(input)
                Optional.of(
                    NovelCapa(
                        UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getFloat("volume"), Language.getEnum(rs.getString("linguagem"))!!,
                        rs.getString("arquivo"), rs.getString("extensao"), image
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
            throw RuntimeException1(e)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun selectVolume(base: String, novel: String, volume: Int, linguagem: Language): Optional<NovelVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(FIND_VOLUME, base))
            st.setString(1, novel)
            st.setInt(2, volume)
            st.setString(3, linguagem.sigla)
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    NovelVolume(
                        UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getString("titulo"), rs.getString("titulo_alternativo"), rs.getString("serie"), rs.getString("descricao"),
                        rs.getString("arquivo"), rs.getString("editora"), rs.getString("autor"), rs.getFloat("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getBoolean("is_favorito"),
                        selectCapa(base, UUID.fromString(rs.getString("id"))).orElse(null), rs.getBoolean("is_processado"),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), getLinguagem(linguagem)),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"')
                    )
                ) else
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
    override fun selectVolume(base: String, arquivo: String, linguagem: Language): Optional<NovelVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(FIND_ARQUIVO, base))
            st.setString(1, arquivo)
            st.setString(2, linguagem.sigla)
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    NovelVolume(
                        UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getString("titulo"), rs.getString("titulo_alternativo"), rs.getString("serie"), rs.getString("descricao"),
                        rs.getString("arquivo"), rs.getString("editora"), rs.getString("autor"), rs.getFloat("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getBoolean("is_favorito"),
                        selectCapa(base, UUID.fromString(rs.getString("id"))).orElse(null), rs.getBoolean("is_processado"), mutableListOf(), mutableSetOf()
                    )
                ) else
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
    override fun selectVolume(base: String, id: UUID): Optional<NovelVolume> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_VOLUME, base))
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    NovelVolume(
                        UUID.fromString(rs.getString("id")), rs.getString("novel"),
                        rs.getString("titulo"), rs.getString("titulo_alternativo"), rs.getString("serie"), rs.getString("descricao"),
                        rs.getString("arquivo"), rs.getString("editora"), rs.getString("autor"), rs.getFloat("volume"),
                        Language.getEnum(rs.getString("linguagem"))!!, rs.getBoolean("is_favorito"),
                        selectCapa(base, UUID.fromString(rs.getString("id"))).orElse(null), rs.getBoolean("is_processado"),
                        selectCapitulos(base, UUID.fromString(rs.getString("id")), listOf()),
                        selectVocabulario(base, "id_volume = " + '"' + UUID.fromString(rs.getString("id")) + '"')
                    )
                ) else
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
    override fun selectCapitulo(base: String, id: UUID): Optional<NovelCapitulo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_CAPITULO, base))
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    NovelCapitulo(
                        UUID.fromString(rs.getString("id")), rs.getString("novel"), rs.getFloat("volume"),
                        rs.getFloat("capitulo"), rs.getString("descricao"), rs.getInt("sequencia"), Language.getEnum(rs.getString("linguagem"))!!,
                        selectTextos(base, UUID.fromString(rs.getString("id"))), selectVocabulario(base, "id_capitulo = " + '"' + UUID.fromString(rs.getString("id")) + '"')
                    )
                ) else
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
    override fun selectAll(base: String): MutableList<NovelTabela> = selectAll(base, "", 0, 0f, null)

    @Throws(SQLException::class)
    override fun selectAll(base: String, novel: String, volume: Int, capitulo: Float, linguagem: Language?): MutableList<NovelTabela> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = "1>0 "

            if (base.trim().isNotEmpty())
                condicao += " AND Table_Name LIKE '%" + base.trim() + "%'"

            st = conn.prepareStatement(String.format(SELECT_TABELAS, schema, condicao))
            rs = st.executeQuery()
            val list: MutableList<NovelTabela> = mutableListOf()
            while (rs.next()) {
                val volumes: MutableList<NovelVolume> = selectVolumes(rs.getString("Tabela"), true, novel, volume, getLinguagem(linguagem))
                if (volumes.isNotEmpty())
                    list.add(NovelTabela(rs.getString("Tabela"), volumes))
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
    override fun deleteVocabulario(base: String) {
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
    override fun updateCancel(base: String, obj: NovelVolume) {
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
    override fun deleteVolume(base: String, obj: NovelVolume) {
        var stVolume: PreparedStatement? = null
        try {
            stVolume = conn.prepareStatement(String.format(DELETE_VOLUMES, base, '"' + obj.id.toString() + '"'))
            stVolume.executeUpdate()
        } catch (e: SQLException) {
            println(stVolume.toString())
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_DELETE)
        } finally {
            JdbcFactory.closeStatement(stVolume)
        }
    }

    @Throws(SQLException::class)
    override fun insertVolume(base: String, obj: NovelVolume): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(INSERT_VOLUMES, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, obj.novel)
            st.setString(++index, obj.titulo)
            st.setString(++index, obj.tituloAlternativo)
            st.setString(++index, obj.descricao)
            st.setString(++index, obj.editora)
            st.setFloat(++index, obj.volume)
            st.setString(++index, obj.lingua.sigla)
            st.setString(++index, obj.arquivo)
            st.setBoolean(++index, obj.processado)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else {
                obj.capa?.let {
                    it.id = insertCapa(base, obj.id!!, it)
                }
                insertVocabulario(base, obj.id, null, obj.vocabularios)
                for (capitulo in obj.capitulos)
                    insertCapitulo(base, obj.id!!, capitulo)
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
    override fun insertCapitulo(base: String, idVolume: UUID, obj: NovelCapitulo): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(INSERT_CAPITULOS, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, idVolume.toString())
            st.setString(++index, obj.novel)
            st.setFloat(++index, obj.volume)
            st.setFloat(++index, obj.capitulo)
            st.setString(++index, obj.descricao)
            st.setInt(++index, obj.sequencia)
            st.setString(++index, obj.lingua.sigla)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else {
                insertVocabulario(base, null, obj.id, obj.vocabularios)
                for (texto in obj.textos)
                    insertTexto(base, obj.id!!, texto)
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
    override fun insertTexto(base: String, idCapitulo: UUID, obj: NovelTexto): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(INSERT_TEXTO, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, idCapitulo.toString())
            st.setInt(++index, obj.sequencia)
            st.setString(++index, obj.texto)

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
    fun insertCapa(base: String?, idVolume: UUID, obj: NovelCapa): UUID {
        var st: PreparedStatement? = null
        return try {
            st = conn.prepareStatement(String.format(INSERT_CAPA, base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.id.toString())
            st.setString(++index, idVolume.toString())
            st.setString(++index, obj.novel)
            st.setFloat(++index, obj.volume)
            st.setString(++index, obj.lingua.sigla)
            st.setString(++index, obj.arquivo)
            st.setString(++index, obj.extenssao)

            val baos = ByteArrayOutputStream()
            ImageIO.write(obj.imagem, "jpg", baos)
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
            throw RuntimeException1(e)
        } finally {
            JdbcFactory.closeStatement(st)
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
    override fun selectTabela(base: String): String {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_LISTA_TABELAS, schema, " Table_Name = '" + base + "_volumes'"))
            rs = st.executeQuery()
            var tabela = ""
            if (rs.next())
                tabela = rs.getString("Tabela")
            tabela
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