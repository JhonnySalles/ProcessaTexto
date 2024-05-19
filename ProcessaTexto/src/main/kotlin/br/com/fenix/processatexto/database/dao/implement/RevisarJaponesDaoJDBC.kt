package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Database
import br.com.fenix.processatexto.model.messages.Mensagens
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*


class RevisarJaponesDaoJDBC(conexao: Conexao) : RevisarDao, RepositoryDaoBase<UUID?, Revisar>(conexao) {

    private val LOGGER = LoggerFactory.getLogger(RevisarJaponesDaoJDBC::class.java)

    companion object {
        private const val INSERT = "INSERT IGNORE INTO revisar (id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles, revisado, isAnime, isManga, isNovel) VALUES (?,?,?,?,?,?,?,?,?,?,?);"
        private const val UPDATE = "UPDATE revisar SET forma_basica = ?, leitura = ?, leitura_novel = ?, portugues = ?, ingles = ?, revisado = ?, isAnime = ?, isManga = ?, isNovel = ? WHERE vocabulario = ?;"
        private const val DELETE = "DELETE FROM revisar WHERE vocabulario = ?;"
        private const val SELECT = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles, aparece, revisado, isAnime, isManga, isNovel FROM revisar "
        private const val SELECT_FORMA = SELECT + "WHERE vocabulario = ? OR forma_basica = ?;"
        private const val SELECT_PALAVRA = SELECT + "WHERE vocabulario = ?;"
        private const val SELECT_ID = SELECT + "WHERE id = ?;"
        private const val EXIST = "SELECT vocabulario FROM revisar WHERE vocabulario = ?;"
        private const val SELECT_ALL = SELECT + "WHERE 1 > 0;"
        private const val SELECT_TRADUZIR = SELECT + "WHERE revisado = false"
        private const val SELECT_QUANTIDADE_RESTANTE = "SELECT COUNT(*) AS Quantidade FROM revisar"
        private const val SELECT_REVISAR = SELECT + "WHERE %s ORDER BY aparece DESC LIMIT 1"
        private const val SELECT_REVISAR_PESQUISA = SELECT + "WHERE vocabulario = ? or forma_basica = ? LIMIT 1"
        private const val SELECT_SIMILAR = SELECT + "WHERE vocabulario <> ? AND ingles <> '' AND ingles = ?"
        private const val INCREMENTA_VEZES_APARECE = "UPDATE revisar SET aparece = (aparece + 1) WHERE vocabulario = ?;"
        private const val SET_ISMANGA = "UPDATE revisar SET isManga = ? WHERE vocabulario = ?;"
        private const val SET_ISNOVEL = "UPDATE revisar SET isNovel = ? WHERE vocabulario = ?;"
    }

    @get:Override
    override val tipo: Database get() = Database.JAPONES

    @Throws(SQLException::class)
    override fun insert(obj: Revisar) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.getId().toString())
            st.setString(++index, obj.vocabulario)
            st.setString(++index, obj.formaBasica)
            st.setString(++index, obj.leitura)
            st.setString(++index, obj.leituraNovel)
            st.setString(++index, obj.portugues)
            st.setString(++index, obj.ingles)
            st.setBoolean(++index, obj.isRevisado)
            st.setBoolean(++index, obj.isAnime)
            st.setBoolean(++index, obj.isManga)
            st.setBoolean(++index, obj.isNovel)

            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            LOGGER.info(st.toString())
            throw SQLException(Mensagens.BD_ERRO_INSERT)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun update(obj: Revisar) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.formaBasica)
            st.setString(++index, obj.leitura)
            st.setString(++index, obj.leituraNovel)
            st.setString(++index, obj.portugues)
            st.setString(++index, obj.ingles)
            st.setBoolean(++index, obj.isRevisado)
            st.setBoolean(++index, obj.isAnime)
            st.setBoolean(++index, obj.isManga)
            st.setBoolean(++index, obj.isNovel)
            st.setString(++index, obj.vocabulario)

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
    override fun delete(obj: Revisar) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(DELETE)
            st.setString(1, obj.vocabulario)
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
    override fun delete(vocabulario: String) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(DELETE)
            st.setString(1, vocabulario)
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
    override fun select(vocabulario: String, base: String): Optional<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_FORMA)
            st.setString(1, vocabulario)
            st.setString(2, base)
            rs = st.executeQuery()
            if (rs.next()) {
                Optional.of(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getInt("aparece"), rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")
                    )
                )
            } else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun select(vocabulario: String): Optional<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_PALAVRA)
            st.setString(1, vocabulario)
            rs = st.executeQuery()
            if (rs.next()) {
                Optional.of(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getInt("aparece"), rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")
                    )
                )
            } else
                Optional.of(Revisar(vocabulario))
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun select(id: UUID): Optional<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ID)
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next()) {
                Optional.of(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getInt("aparece"), rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")
                    )
                )
            } else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun selectAll(): List<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ALL)
            rs = st.executeQuery()
            val list: MutableList<Revisar> = mutableListOf()
            while (rs.next()) {
                list.add(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getInt("aparece"), rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")
                    )
                )
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

    @Throws(SQLException::class)
    override fun selectTraduzir(quantidadeRegistros: Int): List<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_TRADUZIR + if (quantidadeRegistros > 0) " LIMIT $quantidadeRegistros" else " LIMIT 1000")
            rs = st.executeQuery()
            val list: MutableList<Revisar> = mutableListOf()
            while (rs.next())
                list.add(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getInt("aparece"), rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    override fun exist(vocabulario: String): Boolean {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(EXIST)
            st.setString(1, vocabulario)
            rs = st.executeQuery()
            rs.next()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            false
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    override fun isValido(vocabulario: String): String {
        TODO("Not yet implemented")
    }

    @Throws(SQLException::class)
    override fun selectFrases(select: String): List<String> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(select)
            rs = st.executeQuery()
            val list: MutableList<String> = mutableListOf()
            while (rs.next())
                list.add(rs.getString(1))
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun selectQuantidadeRestante(): String {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            st = conn.prepareStatement(SELECT_QUANTIDADE_RESTANTE)
            rs = st.executeQuery()
            if (rs.next())
                return rs.getString("Quantidade")
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
        return "0"
    }

    @Throws(SQLException::class)
    override fun selectRevisar(pesquisar: String, isAnime: Boolean, isManga: Boolean, isNovel: Boolean): Optional<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            if (pesquisar.trim().isNotEmpty()) {
                st = conn.prepareStatement(SELECT_REVISAR_PESQUISA)
                st.setString(1, pesquisar)
                st.setString(2, pesquisar)
            } else {
                var parametro = ""
                if (isAnime)
                    parametro += "isAnime = true AND "
                if (isManga)
                    parametro += "isManga = true AND "
                if (isNovel)
                    parametro += "isNovel = true AND "

                parametro = if (parametro.isEmpty())
                    "1>0"
                else
                    parametro.substring(0, parametro.length - 4)

                st = conn.prepareStatement(String.format(SELECT_REVISAR, parametro))
            }
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getInt("aparece"), rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")
                    )
                )
            else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun selectSimilar(vocabulario: String, ingles: String): MutableList<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_SIMILAR)
            st.setString(1, vocabulario)
            st.setString(2, ingles)
            rs = st.executeQuery()
            val list: MutableList<Revisar> = mutableListOf()
            while (rs.next())
                list.add(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getInt("aparece"), rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")
                    )
                )
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun incrementaVezesAparece(vocabulario: String) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INCREMENTA_VEZES_APARECE)
            st.setString(1, vocabulario)
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun setIsManga(obj: Revisar) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(SET_ISMANGA)
            st.setBoolean(1, obj.isManga)
            st.setString(2, obj.vocabulario)
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    @Throws(SQLException::class)
    override fun setIsNovel(obj: Revisar) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(SET_ISNOVEL)
            st.setBoolean(1, obj.isNovel)
            st.setString(2, obj.vocabulario)
            st.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        } finally {
            JdbcFactory.closeStatement(st)
        }
    }

    override fun toEntity(rs: ResultSet): Revisar = Revisar(
        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
        rs.getInt("aparece"), rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")
    )

}