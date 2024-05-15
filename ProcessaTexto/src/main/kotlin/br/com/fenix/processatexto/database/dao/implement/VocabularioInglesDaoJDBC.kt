package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Database
import br.com.fenix.processatexto.model.messages.Mensagens
import br.com.fenix.processatexto.util.Utils
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.time.LocalDateTime
import java.util.*


class VocabularioInglesDaoJDBC(conexao: Conexao) : VocabularioDao, RepositoryDaoBase<UUID?, Vocabulario>(conexao) {

    private val LOGGER = LoggerFactory.getLogger(VocabularioInglesDaoJDBC::class.java)

    companion object {
        private const val INSERT = "INSERT IGNORE INTO vocabulario (id, vocabulario, leitura, portugues) VALUES (?,?,?,?);"
        private const val UPDATE = "UPDATE vocabulario SET leitura = ?, portugues = ? WHERE vocabulario = ?;"
        private const val DELETE = "DELETE FROM vocabulario WHERE vocabulario = ?;"
        private const val SELECT = "SELECT id, vocabulario, leitura, portugues FROM vocabulario WHERE vocabulario = ?;"
        private const val SELECT_ID = "SELECT id, vocabulario, leitura, portugues FROM vocabulario WHERE id = ?;"
        private const val EXIST = "SELECT id, vocabulario FROM vocabulario WHERE vocabulario = ?;"
        private const val INSERT_EXCLUSAO = "INSERT IGNORE INTO exclusao (palavra) VALUES (?)"
        private const val SELECT_ALL_EXCLUSAO = "SELECT palavra FROM exclusao"
        private const val SELECT_EXCLUSAO = "SELECT palavra FROM exclusao WHERE palavra = ? "
        private const val SELECT_ENVIO = "SELECT id, vocabulario, leitura, portugues FROM vocabulario WHERE atualizacao >= ?;"
    }

    @get:Override
    override val tipo: Database get() = Database.INGLES

    @Throws(SQLException::class)
    override fun insert(obj: Vocabulario) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)
            st.setString(1, obj.getId().toString())
            st.setString(2, obj.vocabulario)
            st.setString(3, obj.leitura)
            st.setString(4, obj.portugues)
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
    override fun update(obj: Vocabulario) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS)
            st.setString(1, obj.leitura)
            st.setString(2, obj.portugues)
            st.setString(3, obj.vocabulario)
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
    override fun delete(obj: Vocabulario) {
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
    override fun select(vocabulario: String, base: String): Optional<Vocabulario> = select(vocabulario)

    @Throws(SQLException::class)
    override fun select(vocabulario: String): Optional<Vocabulario> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT)
            st.setString(1, vocabulario)
            rs = st.executeQuery()
            if (rs.next()) {
                Optional.of(
                    Vocabulario(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", "", rs.getString("portugues")
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
    override fun select(id: UUID?): Optional<Vocabulario> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ID)
            st.setString(1, id.toString())
            rs = st.executeQuery()
            if (rs.next()) {
                Optional.of(
                    Vocabulario(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", "", rs.getString("portugues")
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

    @Throws(SQLException::class)
    override fun insertExclusao(palavra: String) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT_EXCLUSAO, Statement.RETURN_GENERATED_KEYS)
            st.setString(1, palavra.lowercase(Locale.getDefault()))
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
    override fun selectExclusao(): Set<String> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ALL_EXCLUSAO)
            rs = st.executeQuery()
            val list: MutableSet<String> = mutableSetOf()
            while (rs.next())
                list.add(rs.getString("palavra"))
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
    override fun selectEnvio(ultimo: LocalDateTime): List<Vocabulario> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ENVIO)
            st.setString(1, Utils.convertToString(ultimo))
            rs = st.executeQuery()
            val list: MutableList<Vocabulario> = mutableListOf()
            while (rs.next()) {
                list.add(
                    Vocabulario(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", "", rs.getString("portugues")
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
    override fun existeExclusao(palavra: String, basico: String): Boolean {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_EXCLUSAO)
            st.setString(1, palavra.lowercase(Locale.getDefault()))
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

    @Throws(SQLException::class)
    override fun selectAll(): MutableList<Vocabulario> = mutableListOf()

    override fun toEntity(rs: ResultSet): Vocabulario = Vocabulario(
        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
        rs.getString("leitura"), "", "", rs.getString("portugues")
    )

}