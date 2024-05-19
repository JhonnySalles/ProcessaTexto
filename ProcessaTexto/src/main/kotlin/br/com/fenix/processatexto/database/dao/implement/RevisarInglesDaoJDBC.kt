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


class RevisarInglesDaoJDBC(conexao: Conexao) : RevisarDao, RepositoryDaoBase<UUID?, Revisar>(conexao) {

    private val LOGGER = LoggerFactory.getLogger(RevisarInglesDaoJDBC::class.java)

    companion object {
        private const val INSERT = "INSERT IGNORE INTO revisar (id, vocabulario, leitura, portugues, revisado, isAnime, isManga) VALUES (?,?,?,?,?,?,?);"
        private const val UPDATE = "UPDATE revisar SET leitura = ?, portugues = ?, revisado = ?, isAnime = ?, isManga = ? WHERE vocabulario = ?;"
        private const val DELETE = "DELETE FROM revisar WHERE vocabulario = ?;"
        private const val SELECT = "SELECT id, vocabulario, leitura, portugues, aparece, revisado, isAnime, isManga, isNovel FROM revisar "
        private const val SELECT_PALAVRA = SELECT + "WHERE vocabulario = ?;"
        private const val SELECT_ID = SELECT + "WHERE id = ?;"
        private const val EXIST = "SELECT vocabulario FROM revisar WHERE vocabulario = ?;"
        private const val IS_VALIDO = "SELECT palavra FROM valido WHERE palavra LIKE ?;"
        private const val SELECT_ALL = SELECT + "WHERE 1 > 0;"
        private const val SELECT_TRADUZIR = SELECT + "WHERE revisado = false"
        private const val SELECT_QUANTIDADE_RESTANTE = "SELECT COUNT(*) AS Quantidade FROM revisar"
        private const val SELECT_REVISAR = SELECT + "WHERE %s ORDER BY aparece DESC LIMIT 1"
        private const val SELECT_REVISAR_PESQUISA = SELECT + "WHERE vocabulario = ? LIMIT 1"
        private const val INCREMENTA_VEZES_APARECE = "UPDATE revisar SET aparece = (aparece + 1) WHERE vocabulario = ?;"
        private const val SET_ISMANGA = "UPDATE revisar SET isManga = ? WHERE vocabulario = ?;"
        private const val SET_ISNOVEL = "UPDATE revisar SET isNovel = ? WHERE vocabulario = ?;"
    }

    @get:Override
    override val tipo: Database get() = Database.INGLES

    @Throws(SQLException::class)
    override fun insert(obj: Revisar) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)
            st.setString(1, obj.getId().toString())
            st.setString(2, obj.vocabulario)
            st.setString(3, obj.leitura)
            st.setString(4, obj.portugues)
            st.setBoolean(5, obj.isRevisado)
            st.setBoolean(6, obj.isAnime)
            st.setBoolean(7, obj.isManga)
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
            st.setString(1, obj.leitura)
            st.setString(2, obj.portugues)
            st.setBoolean(3, obj.isRevisado)
            st.setBoolean(4, obj.isAnime)
            st.setBoolean(5, obj.isManga)
            st.setString(6, obj.vocabulario)
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
    override fun select(vocabulario: String, base: String): Optional<Revisar> = select(vocabulario)

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
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", rs.getString("portugues"), "", rs.getInt("aparece"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"),
                        rs.getBoolean("isNovel")
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
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", rs.getString("portugues"), "", rs.getInt("aparece"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"),
                        rs.getBoolean("isNovel")
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
    override fun selectAll(): MutableList<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ALL)
            rs = st.executeQuery()
            val list: MutableList<Revisar> = mutableListOf()
            while (rs.next()) {
                list.add(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", rs.getString("portugues"), "", rs.getInt("aparece"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"),
                        rs.getBoolean("isNovel")
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
    override fun selectTraduzir(quantidadeRegistros: Int): MutableList<Revisar> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_TRADUZIR + if (quantidadeRegistros > 0) " LIMIT $quantidadeRegistros" else " LIMIT 1000")
            rs = st.executeQuery()
            val list: MutableList<Revisar> = mutableListOf()
            while (rs.next())
                list.add(
                    Revisar(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", rs.getString("portugues"), "", rs.getInt("aparece"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"),
                        rs.getBoolean("isNovel")
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
        try {
            st = conn.prepareStatement(EXIST)
            st.setString(1, vocabulario)
            rs = st.executeQuery()
            if (rs.next())
                return true
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
        return false
    }

    override fun isValido(vocabulario: String): String {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(IS_VALIDO)
            st.setString(1, vocabulario)
            rs = st.executeQuery()
            if (rs.next())
                vocabulario.lowercase(Locale.getDefault())
            else
                ""
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            ""
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun selectFrases(select: String): MutableList<String> {
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
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", rs.getString("portugues"), "", rs.getInt("aparece"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"),
                        rs.getBoolean("isNovel")
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
    override fun selectSimilar(vocabulario: String, ingles: String): MutableList<Revisar> = arrayListOf()

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
        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
        rs.getString("leitura"), "", rs.getString("portugues"), "", rs.getInt("aparece"),
        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"),
        rs.getBoolean("isNovel")
    )

}