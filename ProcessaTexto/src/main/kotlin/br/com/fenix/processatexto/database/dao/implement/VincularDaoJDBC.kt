package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.database.dao.VincularDao
import br.com.fenix.processatexto.model.entities.mangaextractor.*
import br.com.fenix.processatexto.model.entities.processatexto.Vinculo
import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.messages.Mensagens
import br.com.fenix.processatexto.util.Utils
import br.com.fenix.processatexto.util.configuration.Configuracao
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import java.util.stream.Collectors


class VincularDaoJDBC(conexao: Conexao) : VincularDao, RepositoryDao<UUID?, Vinculo>(conexao) {

    private val LOGGER = LoggerFactory.getLogger(VincularDaoJDBC::class.java)

    companion object {
        private const val INSERT_VINCULO =
            "INSERT INTO %s_vinculo (volume, original_arquivo, original_linguagem, id_volume_original, vinculado_arquivo, vinculado_linguagem, id_volume_vinculado, data_criacao, ultima_alteracao) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
        private const val UPDATE_VINCULO =
            "UPDATE %s_vinculo SET volume = ?, original_arquivo = ?, original_linguagem = ?, id_volume_original = ?, vinculado_arquivo = ?, vinculado_linguagem = ?, id_volume_vinculado = ?, ultima_alteracao = ? WHERE id = ? ;"
        private const val DELETE_VINCULO = "DELETE FROM %s_vinculo WHERE id = ? ;"
        private const val SELECT_ID_VOLUME = "SELECT id FROM %s_volumes WHERE manga = '%s' AND volume = %s AND linguagem = '%s' LIMIT 1"
        private const val SELECT_VINCULO_CAMPOS =
            "SELECT vi.id, vi.volume, vi.original_arquivo, vi.original_linguagem, vi.id_volume_original, vi.vinculado_arquivo, vi.vinculado_linguagem, vi.id_volume_vinculado, vi.data_criacao, vi.ultima_alteracao FROM %s_vinculo vi "
        private const val SELECT_VINCULO = SELECT_VINCULO_CAMPOS
        private const val SELECT_VINCULO_ARQUIVO = SELECT_VINCULO_CAMPOS + "WHERE volume = ? AND original_arquivo = ? AND vinculado_arquivo = ? ;"
        private const val SELECT_VINCULO_LINGUAGEM = SELECT_VINCULO_CAMPOS + "WHERE volume = ? AND original_linguagem = ? AND vinculado_linguagem = ? ;"
        private const val SELECT_VINCULO_INNER_VOLUME = SELECT_VINCULO_CAMPOS + "INNER JOIN %s_volumes vo ON vo.id = vi.id_volume_original " + "WHERE 1 > 0 "
        private const val INSERT_PAGINA =
            ("INSERT IGNORE INTO %s_vinculo_pagina (id_vinculo, original_nome, original_pasta, original_pagina, original_paginas, original_pagina_dupla, id_original_pagina, "
                    + "  vinculado_direita_nome, vinculado_direita_pasta, vinculado_direita_pagina, vinculado_direita_paginas, vinculado_direita_pagina_dupla, id_vinculado_direita_pagina, "
                    + "  vinculado_esquerda_nome, vinculado_esquerda_pasta, vinculado_esquerda_pagina, vinculado_esquerda_paginas, vinculado_esquerda_pagina_dupla, id_vinculado_esquerda_paginas, "
                    + "  imagem_dupla) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);")
        private const val UPDATE_PAGINA =
            ("UPDATE %s_vinculo_pagina SET id_vinculo = ?, original_nome = ?, original_pasta = ?, original_pagina = ?, original_paginas = ?, original_pagina_dupla = ?, id_original_pagina = ?, "
                    + "  vinculado_direita_nome = ?, vinculado_direita_pasta = ?, vinculado_direita_pagina = ?, vinculado_direita_paginas = ?, vinculado_direita_pagina_dupla = ?, id_vinculado_direita_pagina = ?,"
                    + "  vinculado_esquerda_nome = ?, vinculado_esquerda_pasta = ?, vinculado_esquerda_pagina = ?, vinculado_esquerda_paginas = ?, vinculado_esquerda_pagina_dupla = ?, id_vinculado_esquerda_paginas = ?,"
                    + "  imagem_dupla = ? WHERE id = ? ;")
        private const val DELETE_PAGINA = "DELETE FROM %s_vinculo_pagina WHERE id_vinculo = ? ;"
        private const val SELECT_PAGINA = ("SELECT id, original_nome, original_pasta, original_pagina, original_paginas, original_pagina_dupla, id_original_pagina, \n"
                + "  vinculado_direita_nome, vinculado_direita_pasta, vinculado_direita_pagina, vinculado_direita_paginas, vinculado_direita_pagina_dupla, id_vinculado_direita_pagina,\n"
                + "  vinculado_esquerda_nome, vinculado_esquerda_pasta, vinculado_esquerda_pagina, vinculado_esquerda_paginas, vinculado_esquerda_pagina_dupla, id_vinculado_esquerda_paginas,\n"
                + "  imagem_dupla FROM %s_vinculo_pagina WHERE id_vinculo = ? ;")
        private const val INSERT_PAGINA_NAO_VINCULADA =
            "INSERT IGNORE INTO %s_vinculo_pagina_nao_vinculado (id_vinculo, nome, pasta, pagina, paginas, pagina_dupla, id_vinculado_pagina) VALUES (?, ?, ?, ?, ?, ?, ?);"
        private const val DELETE_PAGINA_NAO_VINCULADA = "DELETE FROM %s_vinculo_pagina_nao_vinculado WHERE id_vinculo = ? ;"
        private const val SELECT_PAGINA_NAO_VINCULADA =
            "SELECT id, nome, pasta, pagina, paginas, pagina_dupla, id_vinculado_pagina FROM %s_vinculo_pagina_nao_vinculado WHERE id_vinculo = ? ;"

        private const val EXIST_TABELA_VOCABULARIO = ("SELECT Table_Name AS Tabela "
                + " FROM information_schema.tables WHERE table_schema = '%s' "
                + " AND Table_Name LIKE '%%_vocabulario%%' AND Table_Name LIKE '%%%s%%' GROUP BY Tabela ")
        private const val EXIST_TABELA_VINCULO = ("SELECT Table_Name AS Tabela "
                + " FROM information_schema.tables WHERE table_schema = '%s' "
                + " AND Table_Name LIKE '%%_vinculo%%' AND Table_Name LIKE '%%%s%%' GROUP BY Tabela ")
        private const val SELECT_MANGAS = "SELECT Manga FROM %s_volumes WHERE linguagem = '%s' GROUP BY manga ORDER BY manga"
        private const val CREATE_TABELA_VINCULO = ("CREATE TABLE %s_vinculo (" + "  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL,"
                + "  volume int(11) DEFAULT NULL," + "  original_arquivo VARCHAR(250) DEFAULT NULL,"
                + "  original_linguagem VARCHAR(4) DEFAULT NULL," + "  id_volume_original VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
                + "  vinculado_arquivo VARCHAR(250) DEFAULT NULL," + "  vinculado_linguagem VARCHAR(4) DEFAULT NULL,"
                + "  id_volume_vinculado VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL," + "  data_criacao DATETIME DEFAULT NULL,"
                + "  ultima_alteracao DATETIME DEFAULT NULL," + "  PRIMARY KEY (id),"
                + "  KEY %s_original_volume_fk (id_volume_original),"
                + "  KEY %s_vinculado_volume_fk (id_volume_vinculado),"
                + "  CONSTRAINT %s_vinculo_original_volume_fk FOREIGN KEY (id_volume_original) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE,"
                + "  CONSTRAINT %s_vinculo_vinculado_volume_fk FOREIGN KEY (id_volume_vinculado) REFERENCES %s_volumes (id) ON DELETE CASCADE ON UPDATE CASCADE"
                + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci")
        private const val CREATE_TABELA_VINCULO_PAGINA = ("CREATE TABLE %s_vinculo_pagina ("
                + "  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL," + "  id_vinculo VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
                + "  original_nome VARCHAR(250) DEFAULT NULL," + "  original_pasta VARCHAR(500) DEFAULT NULL,"
                + "  original_pagina INT(11) DEFAULT NULL," + "  original_paginas INT(11) DEFAULT NULL,"
                + "  original_pagina_dupla TINYINT(1) DEFAULT NULL," + "  id_original_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
                + "  vinculado_direita_nome VARCHAR(250) DEFAULT NULL,"
                + "  vinculado_direita_pasta VARCHAR(500) DEFAULT NULL,"
                + "  vinculado_direita_pagina INT(11) DEFAULT NULL," + "  vinculado_direita_paginas INT(11) DEFAULT NULL,"
                + "  vinculado_direita_pagina_dupla TINYINT(1) DEFAULT NULL,"
                + "  id_vinculado_direita_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
                + "  vinculado_esquerda_nome VARCHAR(250) DEFAULT NULL,"
                + "  vinculado_esquerda_pasta VARCHAR(500) DEFAULT NULL,"
                + "  vinculado_esquerda_pagina INT(11) DEFAULT NULL," + "  vinculado_esquerda_paginas INT(11) DEFAULT NULL,"
                + "  vinculado_esquerda_pagina_dupla TINYINT(1) DEFAULT NULL,"
                + "  id_vinculado_esquerda_paginas VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL," + "  imagem_dupla TINYINT(1) DEFAULT NULL,"
                + "  PRIMARY KEY (id)," + "  KEY %s_vinculo_fk (id_vinculo),"
                + "  KEY %s_original_pagina_fk (id_original_pagina),"
                + "  KEY %s_direita_pagina_fk (id_vinculado_direita_pagina),"
                + "  KEY %s_esquerda_pagina_fk (id_vinculado_esquerda_paginas),"
                + "  CONSTRAINT %s_vinculado_direita_pagina_fk FOREIGN KEY (id_vinculado_direita_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
                + "  CONSTRAINT %s_vinculado_esquerda_pagina_fk FOREIGN KEY (id_vinculado_esquerda_paginas) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
                + "  CONSTRAINT %s_vinculado_original_pagina_fk FOREIGN KEY (id_original_pagina) REFERENCES %s_paginas (id) ON DELETE CASCADE ON UPDATE CASCADE,"
                + "  CONSTRAINT %s_vinculo_vinculo_pagina_fk FOREIGN KEY (id_vinculo) REFERENCES %s_vinculo (id) ON DELETE CASCADE ON UPDATE CASCADE"
                + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci")
        private const val CREATE_TABELA_NAO_VINCULADOS = ("CREATE TABLE %s_vinculo_pagina_nao_vinculado ("
                + "  id VARCHAR(36) COLLATE utf8mb4_unicode_ci NOT NULL," + "  id_vinculo VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
                + "  nome VARCHAR(250) DEFAULT NULL," + "  pasta VARCHAR(500) DEFAULT NULL,"
                + "  pagina INT(11) DEFAULT NULL," + "  paginas INT(11) DEFAULT NULL,"
                + "  pagina_dupla TINYINT(1) DEFAULT NULL," + "  id_vinculado_pagina VARCHAR(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL,"
                + "  PRIMARY KEY (id)," + "  KEY %s_vinculado_pagina_fk (id_vinculado_pagina),"
                + "  KEY %s_vinculo_fk (id_vinculo),"
                + "  CONSTRAINT %s_vinculo_nao_vinculado_fk FOREIGN KEY (id_vinculo) REFERENCES %s_vinculo (id) ON DELETE CASCADE ON UPDATE CASCADE,"
                + "  CONSTRAINT %s_nao_vinculado_pagina_fk FOREIGN KEY (id_vinculado_pagina) REFERENCES %s_vinculo_pagina (id) ON DELETE CASCADE ON UPDATE CASCADE"
                + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci")
        private const val SELECT_TABELAS = ("SELECT REPLACE(Table_Name, '_vinculo', '') AS Tabela "
                + "FROM information_schema.tables WHERE table_schema = '%s' AND Table_Name NOT LIKE '%%exemplo%%' "
                + "AND Table_Name LIKE '%%_vinculo' AND %s GROUP BY Tabela ")
    }

    private val BASE_MANGA: String
    private val mangaDao = DaoFactory.createMangaDao()

    @Throws(SQLException::class)
    private fun insertVinculados(base: String, idVinculo: UUID, pagina: VinculoPagina) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(INSERT_PAGINA, BASE_MANGA + base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, idVinculo.toString())
            st.setString(++index, pagina.originalNomePagina)
            st.setString(++index, pagina.originalPathPagina)
            st.setInt(++index, pagina.originalPagina)
            st.setInt(++index, pagina.originalPaginas)
            st.setBoolean(++index, pagina.isOriginalPaginaDupla)

            if (pagina.mangaPaginaOriginal != null)
                st.setString(++index, pagina.mangaPaginaOriginal!!.id.toString())
            else
                st.setNString(++index, null)

            st.setString(++index, pagina.vinculadoDireitaNomePagina)
            st.setString(++index, pagina.vinculadoDireitaPathPagina)
            st.setInt(++index, pagina.vinculadoDireitaPagina)
            st.setInt(++index, pagina.vinculadoDireitaPaginas)
            st.setBoolean(++index, pagina.isVinculadoDireitaPaginaDupla)

            if (pagina.mangaPaginaDireita != null)
                st.setString(++index, pagina.mangaPaginaDireita!!.id.toString())
            else
                st.setNString(++index, null)

            st.setString(++index, pagina.vinculadoEsquerdaNomePagina)
            st.setString(++index, pagina.vinculadoEsquerdaPathPagina)
            st.setInt(++index, pagina.vinculadoEsquerdaPagina)
            st.setInt(++index, pagina.vinculadoEsquerdaPaginas)
            st.setBoolean(++index, pagina.isVinculadoEsquerdaPaginaDupla)

            if (pagina.mangaPaginaEsquerda != null)
                st.setString(++index, pagina.mangaPaginaEsquerda!!.id.toString())
            else
                st.setNString(++index, null)

            st.setBoolean(++index, pagina.isImagemDupla)
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else {
                val rs: ResultSet = st.generatedKeys
                if (rs.next())
                    pagina.setId(rs.getLong(1))
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
    private fun insertNaoVinculados(base: String, idVinculo: UUID, pagina: VinculoPagina) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(INSERT_PAGINA_NAO_VINCULADA, BASE_MANGA + base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, idVinculo.toString())
            st.setString(++index, pagina.vinculadoEsquerdaNomePagina)
            st.setString(++index, pagina.vinculadoEsquerdaPathPagina)
            st.setInt(++index, pagina.vinculadoEsquerdaPagina)
            st.setInt(++index, pagina.vinculadoEsquerdaPaginas)
            st.setBoolean(++index, pagina.isVinculadoEsquerdaPaginaDupla)

            if (pagina.mangaPaginaEsquerda != null)
                st.setString(++index, pagina.mangaPaginaEsquerda!!.id.toString())
            else
                st.setNString(++index, null)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else {
                val rs: ResultSet = st.generatedKeys
                if (rs.next())
                    pagina.setId(rs.getLong(1))
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
    override fun insert(base: String, obj: Vinculo): UUID {
        var st: PreparedStatement? = null
        return try {
            conn.autoCommit = false
            conn.beginRequest()
            st = conn.prepareStatement(String.format(INSERT_VINCULO, BASE_MANGA + base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setInt(++index, obj.volume)
            st.setString(++index, obj.nomeArquivoOriginal)
            st.setString(++index, obj.linguagemOriginal.sigla)
            st.setString(++index, obj.volumeOriginal!!.id.toString())
            st.setString(++index, obj.nomeArquivoVinculado)
            st.setString(++index, obj.linguagemVinculado!!.sigla)
            st.setString(++index, obj.volumeVinculado!!.id.toString())
            st.setString(++index, Utils.convertToString(obj.dataCriacao))
            st.setString(++index, Utils.convertToString(obj.ultimaAlteracao))

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
            } else {
                val rs: ResultSet = st.generatedKeys
                if (rs.next()) {
                    obj.setId(UUID.fromString(rs.getString(1)))
                    for (pagina in obj.vinculados)
                        insertVinculados(base, obj.getId()!!, pagina)

                    for (pagina in obj.naoVinculados)
                        insertNaoVinculados(base, obj.getId()!!, pagina)

                    conn.commit()
                    obj.getId()!!
                } else
                    throw SQLException(Mensagens.BD_ERRO_INSERT)
            }
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(st)
        }
    }

    private var volumeOriginal: MangaVolume? = null
    private var volumeVinculado: MangaVolume? = null

    @Throws(SQLException::class)
    private fun selectPagina(base: String?, id: UUID?, volume: MangaVolume?): MangaPagina? {
        if (base == null || id == null)
            return null
        var pagina: MangaPagina? = null
        if (volume != null) {
            val capitulo = volume.capitulos.stream()
                .filter { cp -> cp.paginas.stream().anyMatch { pg -> pg.id!!.compareTo(id) === 0 } }.findFirst()
            if (capitulo.isPresent)
                pagina = capitulo.get().paginas.stream().filter { pg -> pg.id!!.compareTo(id) === 0 }.findFirst()
                    .get()
        }
        if (pagina == null)
            pagina = mangaDao?.selectPagina(base, id)?.orElseGet { null }
        return pagina
    }

    @Throws(SQLException::class)
    private fun selectVinculados(base: String, idVinculo: UUID): MutableList<VinculoPagina> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_PAGINA, BASE_MANGA + base))
            st.setString(1, idVinculo.toString())
            rs = st.executeQuery()
            val list: MutableList<VinculoPagina> = mutableListOf()
            while (rs.next()) {
                val mangaPaginaOriginal = selectPagina(base, UUID.fromString(rs.getString("id_original_pagina")), volumeOriginal)
                val mangaPaginaDireita: MangaPagina? = selectPagina(base, UUID.fromString(rs.getString("id_vinculado_direita_pagina")), volumeVinculado)
                val mangaPaginaEsquerda: MangaPagina? = selectPagina(base, UUID.fromString(rs.getString("id_vinculado_esquerda_paginas")), volumeVinculado)
                list.add(
                    VinculoPagina(
                        rs.getLong("id"), rs.getString("original_nome"),
                        rs.getString("original_pasta"), rs.getInt("original_pagina"), rs.getInt("original_paginas"),
                        rs.getBoolean("original_pagina_dupla"), rs.getString("vinculado_direita_nome"),
                        rs.getString("vinculado_direita_pasta"), rs.getInt("vinculado_direita_pagina"),
                        rs.getInt("vinculado_direita_paginas"), rs.getBoolean("vinculado_direita_pagina_dupla"),
                        rs.getString("vinculado_esquerda_nome"), rs.getString("vinculado_esquerda_pasta"),
                        rs.getInt("vinculado_esquerda_pagina"), rs.getInt("vinculado_esquerda_paginas"),
                        rs.getBoolean("vinculado_esquerda_pagina_dupla"), mangaPaginaOriginal, mangaPaginaDireita,
                        mangaPaginaEsquerda, rs.getBoolean("imagem_dupla"), false
                    )
                )
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    private fun selectNaoVinculados(base: String, idVinculo: UUID): MutableList<VinculoPagina> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_PAGINA_NAO_VINCULADA, BASE_MANGA + base))
            st.setString(1, idVinculo.toString())
            rs = st.executeQuery()
            val list: MutableList<VinculoPagina> = mutableListOf()
            while (rs.next())
                list.add(
                    VinculoPagina(
                        rs.getLong(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5),
                        rs.getBoolean(6), selectPagina(base, UUID.fromString(rs.getString(7)), volumeVinculado), true
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    private fun selectVolume(base: String?, id: UUID?): MangaVolume? {
        return if (base == null || id == null) null else mangaDao?.selectVolume(base, id)?.orElseGet { null }
    }

    @Throws(SQLException::class)
    override fun select(base: String, id: UUID): Optional<Vinculo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_VINCULO, BASE_MANGA + base) + " WHERE id = ? ")
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next()) {
                volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)))
                volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)))
                val obj = Vinculo(
                    UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
                    Language.getEnum(rs.getString(4))!!, volumeOriginal, rs.getString(6),
                    Language.getEnum(rs.getString(7))!!, volumeVinculado, dataCriacao = Utils.convertToDateTime(rs.getString(9)),
                    ultimaAlteracao = Utils.convertToDateTime(rs.getString(10))
                )
                obj.vinculados = selectVinculados(base, obj.getId()!!)
                obj.naoVinculados = selectNaoVinculados(base, obj.getId()!!)
                Optional.of(obj)
            } else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            volumeOriginal = null
            volumeVinculado = null
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun select(
        base: String,
        volume: Int,
        mangaOriginal: String,
        original: Language?,
        arquivoOriginal: String,
        mangaVinculado: String,
        vinculado: Language?,
        arquivoVinculado: String
    ): Optional<Vinculo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var sql: String = String.format(SELECT_VINCULO, BASE_MANGA + base) + " WHERE volume = ? "

            if (mangaOriginal.isNotEmpty() && original != null)
                sql += (" AND id_volume_original = (" + String.format(SELECT_ID_VOLUME, BASE_MANGA + base, mangaOriginal, volume, original.sigla)) + ")"

            if (mangaVinculado.isNotEmpty() && vinculado != null)
                sql += " AND id_volume_vinculado = (" + String.format(SELECT_ID_VOLUME, BASE_MANGA + base, mangaVinculado, volume, vinculado.sigla) + ")"

            if (original != null)
                sql += " AND original_linguagem = '" + original.sigla + "'"

            if (vinculado != null)
                sql += " AND vinculado_linguagem = '" + vinculado.sigla + "'"

            if (arquivoOriginal.isNotEmpty())
                sql += " AND original_arquivo = '$arquivoOriginal'"

            if (arquivoVinculado.isNotEmpty())
                sql += " AND vinculado_arquivo = '$arquivoVinculado'"

            st = conn.prepareStatement(sql)
            st.setInt(1, volume)
            rs = st.executeQuery()
            if (rs.next()) {
                volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)))
                volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)))
                val obj = Vinculo(
                    UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
                    Language.getEnum(rs.getString(4))!!, volumeOriginal, rs.getString(6),
                    Language.getEnum(rs.getString(7))!!, volumeVinculado, dataCriacao = Utils.convertToDateTime(rs.getString(9)),
                    ultimaAlteracao = Utils.convertToDateTime(rs.getString(10))
                )
                obj.vinculados = selectVinculados(base, obj.getId()!!)
                obj.naoVinculados = selectNaoVinculados(base, obj.getId()!!)
                Optional.of(obj)
            } else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            volumeOriginal = null
            volumeVinculado = null
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun select(base: String, volume: Int, mangaOriginal: String, arquivoOriginal: String, mangaVinculado: String, arquivoVinculado: String): Optional<Vinculo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_VINCULO_ARQUIVO, BASE_MANGA + base))
            st.setInt(1, volume)
            st.setString(2, arquivoOriginal)
            st.setString(3, arquivoVinculado)

            rs = st.executeQuery()
            if (rs.next()) {
                volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)))
                volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)))
                val obj = Vinculo(
                    UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
                    Language.getEnum(rs.getString(4))!!, volumeOriginal, rs.getString(6),
                    Language.getEnum(rs.getString(7))!!, volumeVinculado, dataCriacao = Utils.convertToDateTime(rs.getString(9)),
                    ultimaAlteracao = Utils.convertToDateTime(rs.getString(10))
                )
                obj.vinculados = selectVinculados(base, obj.getId()!!)
                obj.naoVinculados = selectNaoVinculados(base, obj.getId()!!)
                Optional.of(obj)
            } else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            volumeOriginal = null
            volumeVinculado = null
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun select(base: String, volume: Int, mangaOriginal: String, original: Language, mangaVinculado: String, vinculado: Language): Optional<Vinculo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_VINCULO_LINGUAGEM, BASE_MANGA + base))
            st.setInt(1, volume)
            st.setString(2, original.sigla)
            st.setString(3, vinculado.sigla)
            rs = st.executeQuery()
            if (rs.next()) {
                volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)))
                volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)))

                val obj = Vinculo(
                    UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
                    Language.getEnum(rs.getString(4))!!, volumeOriginal, rs.getString(6),
                    Language.getEnum(rs.getString(7))!!, volumeVinculado, dataCriacao = Utils.convertToDateTime(rs.getString(9)),
                    ultimaAlteracao = Utils.convertToDateTime(rs.getString(10))
                )

                obj.vinculados = selectVinculados(base, obj.getId()!!)
                obj.naoVinculados = selectNaoVinculados(base, obj.getId()!!)

                Optional.of(obj)
            } else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            volumeOriginal = null
            volumeVinculado = null
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    fun select(base: String, manga: String?, volume: Int?, capitulo: Float?, linguagem: Language?, isValidaTabela: Boolean): MutableList<Vinculo> {
        if (isValidaTabela && !existTabelaVinculo(base)) return ArrayList<Vinculo>()
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var sql: String = String.format(SELECT_VINCULO_INNER_VOLUME, BASE_MANGA + base, BASE_MANGA + base)
            if (manga != null && manga.trim().isNotEmpty())
                sql += " AND vo.manga LIKE '$manga'"
            if (volume != null && volume > 0)
                sql += " AND vo.volume = $volume"

            /*
             * if (capitulo != null && capitulo > -1) sql +=
             * " AND vo.vinculado_linguagem = '" + vinculado.sigla + "'"; 
             */

            if (linguagem != null)
                sql += " AND vo.linguagem = '" + linguagem.sigla + "'"
            st = conn.prepareStatement(sql)
            rs = st.executeQuery()
            val list: MutableList<Vinculo> = mutableListOf()
            while (rs.next()) {
                volumeOriginal = selectVolume(base, UUID.fromString(rs.getString(5)))
                volumeVinculado = selectVolume(base, UUID.fromString(rs.getString(8)))

                val obj = Vinculo(
                    UUID.fromString(rs.getString(1)), base, rs.getInt(2), rs.getString(3),
                    Language.getEnum(rs.getString(4))!!, volumeOriginal, rs.getString(6),
                    Language.getEnum(rs.getString(7))!!, volumeVinculado, dataCriacao = Utils.convertToDateTime(rs.getString(9)),
                    ultimaAlteracao = Utils.convertToDateTime(rs.getString(10))
                )
                obj.vinculados = selectVinculados(base, obj.getId()!!)
                obj.naoVinculados = selectNaoVinculados(base, obj.getId()!!)

                list.add(obj)
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            volumeOriginal = null
            volumeVinculado = null
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    private fun deleteVinculado(base: String, idVinculo: UUID) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(DELETE_PAGINA, BASE_MANGA + base))
            st.setString(1, idVinculo.toString())
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_DELETE)
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_DELETE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    private fun deleteNaoVinculado(base: String, idVinculo: UUID) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(DELETE_PAGINA_NAO_VINCULADA, BASE_MANGA + base))
            st.setString(1, idVinculo.toString())
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_DELETE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun delete(base: String, obj: Vinculo) {
        var st: PreparedStatement? = null
        try {
            conn.autoCommit = false
            conn.beginRequest()
            deleteVinculado(base, obj.getId()!!)
            deleteNaoVinculado(base, obj.getId()!!)
            st = conn.prepareStatement(String.format(DELETE_VINCULO, BASE_MANGA + base))
            st.setString(1, obj.getId().toString())
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_DELETE)
            }
            conn.commit()
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_DELETE)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    private fun updateVinculados(base: String, idVinculo: UUID, pagina: VinculoPagina) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(String.format(UPDATE_PAGINA, BASE_MANGA + base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, idVinculo.toString())
            st.setString(++index, pagina.originalNomePagina)
            st.setString(++index, pagina.originalPathPagina)
            st.setInt(++index, pagina.originalPagina)
            st.setInt(++index, pagina.originalPaginas)
            st.setBoolean(++index, pagina.isOriginalPaginaDupla)

            if (pagina.mangaPaginaOriginal != null)
                st.setString(++index, pagina.mangaPaginaOriginal!!.id.toString())
            else
                st.setNString(++index, null)

            st.setString(++index, pagina.vinculadoDireitaNomePagina)
            st.setString(++index, pagina.vinculadoDireitaPathPagina)
            st.setInt(++index, pagina.vinculadoDireitaPagina)
            st.setInt(++index, pagina.vinculadoDireitaPaginas)
            st.setBoolean(++index, pagina.isVinculadoDireitaPaginaDupla)

            if (pagina.mangaPaginaDireita != null)
                st.setString(++index, pagina.mangaPaginaDireita!!.id.toString())
            else
                st.setNString(++index, null)

            st.setString(++index, pagina.vinculadoEsquerdaNomePagina)
            st.setString(++index, pagina.vinculadoEsquerdaPathPagina)
            st.setInt(++index, pagina.vinculadoEsquerdaPagina)
            st.setInt(++index, pagina.vinculadoEsquerdaPaginas)
            st.setBoolean(++index, pagina.isVinculadoEsquerdaPaginaDupla)

            if (pagina.mangaPaginaEsquerda != null)
                st.setString(++index, pagina.mangaPaginaEsquerda!!.id.toString())
            else
                st.setNString(++index, null)

            st.setBoolean(++index, pagina.isImagemDupla)
            st.setLong(++index, pagina.getId()!!)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_UPDATE)
            } else {
                val rs: ResultSet = st.generatedKeys
                if (rs.next())
                    pagina.setId(rs.getLong(1))
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
    private fun updateNaoVinculados(base: String, idVinculo: UUID, pagina: VinculoPagina) = insertNaoVinculados(base, idVinculo, pagina)

    @Throws(SQLException::class)
    override fun update(base: String, obj: Vinculo) {
        var st: PreparedStatement? = null
        try {
            conn.autoCommit = false
            conn.beginRequest()
            st = conn.prepareStatement(String.format(UPDATE_VINCULO, BASE_MANGA + base), Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setInt(++index, obj.volume)
            st.setString(++index, obj.nomeArquivoOriginal)
            st.setString(++index, obj.linguagemOriginal.sigla)
            st.setString(++index, obj.volumeOriginal!!.id.toString())
            st.setString(++index, obj.nomeArquivoVinculado)
            st.setString(++index, obj.linguagemVinculado!!.sigla)
            st.setString(++index, obj.volumeVinculado!!.id.toString())
            st.setString(++index, Utils.convertToString(obj.ultimaAlteracao))
            st.setString(++index, obj.getId().toString())

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_UPDATE)
            } else {
                for (pagina in obj.vinculados)
                    updateVinculados(base, obj.getId()!!, pagina)
                deleteNaoVinculado(base, obj.getId()!!)
                for (pagina in obj.naoVinculados)
                    updateNaoVinculados(base, obj.getId()!!, pagina)
            }
            conn.commit()
        } catch (e: SQLException) {
            try {
                conn.rollback()
            } catch (e1: SQLException) {
                e1.printStackTrace()
            }
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_UPDATE)
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    private fun existTabelaVinculo(nome: String): Boolean {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(EXIST_TABELA_VINCULO, BASE_MANGA.substring(0, BASE_MANGA.length - 1), nome))
            rs = st.executeQuery()
            rs.next()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun createTabelas(nome: String): Boolean {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            st = conn.prepareStatement(String.format(EXIST_TABELA_VOCABULARIO, BASE_MANGA.substring(0, BASE_MANGA.length - 1), nome))
            rs = st.executeQuery()
            if (!rs.next())
                return false
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }

        if (existTabelaVinculo(nome))
            return false

        st = null
        try {
            st = conn.prepareStatement(
                String.format(
                    CREATE_TABELA_VINCULO, BASE_MANGA + nome, nome, nome, nome,
                    BASE_MANGA + nome, nome, BASE_MANGA + nome
                )
            )
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }

        st = null
        try {
            st = conn.prepareStatement(
                String.format(
                    CREATE_TABELA_VINCULO_PAGINA, BASE_MANGA + nome, nome, nome, nome,
                    nome, nome, BASE_MANGA + nome, nome, BASE_MANGA + nome, nome, BASE_MANGA + nome, nome,
                    BASE_MANGA + nome
                )
            )
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }

        st = null
        try {
            st = conn.prepareStatement(
                String.format(
                    CREATE_TABELA_NAO_VINCULADOS, BASE_MANGA + nome, nome, nome, nome,
                    BASE_MANGA + nome, nome, BASE_MANGA + nome
                )
            )
            st.execute()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_CREATE_DATABASE)
        } finally {
            JdbcFactory.closeStatement(st)
        }
        return true
    }

    @get:Throws(SQLException::class)
    @get:Override
    override val tabelas: List<String> get() = mangaDao?.tabelas!!

    @Throws(SQLException::class)
    override fun getMangas(base: String, linguagem: Language): MutableList<String> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(String.format(SELECT_MANGAS, BASE_MANGA + base, linguagem.sigla))
            rs = st.executeQuery()
            val list: MutableList<String> = ArrayList()
            while (rs.next())
                list.add(rs.getString("Manga"))
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    private var sequencia: Int = 0

    init {
        BASE_MANGA = Configuracao.database + "."
    }

    @Throws(SQLException::class)
    private fun selectVinculo(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language, isValidaTabela: Boolean): MutableList<MangaVinculo> {
        val vinculos: List<Vinculo> = select(base, manga, volume, capitulo, linguagem, isValidaTabela)
        vinculos.stream().forEach { vol ->
            vol.volumeOriginal?.capitulos?.parallelStream()?.forEach { c -> c.paginas.parallelStream().forEach { p -> p.sequencia = -1 } }
            vol.volumeVinculado?.capitulos?.parallelStream()?.forEach { c -> c.paginas.parallelStream().forEach { p -> p.sequencia = -1 } }
        }
        vinculos.stream().forEach { vol ->
            sequencia = 0
            vol.vinculados.stream().forEach { pg ->
                sequencia += 1
                if (pg.mangaPaginaOriginal != null)
                    pg.mangaPaginaOriginal!!.sequencia = sequencia
                if (pg.mangaPaginaEsquerda != null)
                    pg.mangaPaginaEsquerda!!.sequencia = sequencia
                if (pg.mangaPaginaDireita != null)
                    pg.mangaPaginaDireita!!.sequencia = sequencia
            }
        }
        val mangas: MutableList<MangaVinculo> = mutableListOf()
        val volumes: MutableSet<MangaVolume> = mutableSetOf()
        vinculos.parallelStream().forEach { volumes.add(it.volumeOriginal!!) }
        volumes.parallelStream().forEach { vol ->
            val vinculados = vinculos.parallelStream().filter { it.volumeOriginal!! == vol }
                .map { m -> m.volumeVinculado!! }.collect(Collectors.toList())
            mangas.add(MangaVinculo(vol, vinculados))
        }
        return mangas
    }

    @Throws(SQLException::class)
    override fun selectVinculo(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language): MutableList<MangaVinculo> =
        selectVinculo(base, manga, volume, capitulo, linguagem, true)


    @Override
    @Throws(SQLException::class)
    override fun selectTabelasJson(base: String, manga: String, volume: Int, capitulo: Float, linguagem: Language): MutableList<MangaTabela> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            var condicao = "1>0 "
            if (base != null && base.trim().isNotEmpty())
                condicao += " AND Table_Name LIKE '%" + base.trim() + "%'"
            st = conn.prepareStatement(String.format(SELECT_TABELAS, BASE_MANGA.substring(0, BASE_MANGA.length - 1), condicao))
            rs = st.executeQuery()
            val list: MutableList<MangaTabela> = mutableListOf()
            while (rs.next()) {
                val vinculo = selectVinculo(rs.getString("Tabela"), manga, volume, capitulo, linguagem, false)
                if (vinculo.size > 0)
                    list.add(MangaTabela(rs.getString("Tabela"), mutableListOf(), vinculo))
            }
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    override fun toEntity(rs: ResultSet): Vinculo {
        TODO("Not yet implemented")
    }

}