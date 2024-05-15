package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Database
import br.com.fenix.processatexto.model.messages.Mensagens
import org.slf4j.LoggerFactory
import java.sql.*
import java.time.LocalDateTime
import java.util.*


class VocabularioExternoDaoJDBC(conexao: Conexao) : VocabularioDao, RepositoryDaoBase<UUID?, Vocabulario>(conexao) {

    private val LOGGER = LoggerFactory.getLogger(VocabularioExternoDaoJDBC::class.java)

    companion object {
        private const val INSERT = "INSERT IGNORE INTO _vocabularios (id, palavra, leitura, portugues, ingles, revisado) VALUES (?,?,?,?,?,?);"
        private const val UPDATE = "UPDATE _vocabularios SET palavra = ?, leitura = ?, portugues = ?, ingles = ?, revisado = ? WHERE id = ?;"
        private const val DELETE = "DELETE FROM _vocabularios WHERE id = ?;"
        private const val SELECT_ALL = "SELECT id, palavra, leitura, portugues, ingles, revisado FROM _vocabularios"
        private const val SELECT = "$SELECT_ALL WHERE id = ?;"
        private const val EXIST = "SELECT id FROM _vocabularios WHERE id = ?;"
    }

    @get:Override
    override val tipo: Database get() = Database.EXTERNO

    @Throws(SQLException::class)
    override fun insert(obj: Vocabulario) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)
            var index = 0
            st.setString(++index, obj.getId().toString())
            st.setString(++index, (obj as VocabularioExterno).palavra)
            st.setString(++index, obj.leitura)
            st.setString(++index, obj.portugues)
            st.setString(++index, obj.ingles)
            st.setBoolean(++index, obj.revisado)
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
            var index = 0
            st.setString(++index, (obj as VocabularioExterno).palavra)
            st.setString(++index, obj.leitura)
            st.setString(++index, obj.portugues)
            st.setString(++index, obj.ingles)
            st.setBoolean(++index, obj.revisado)
            st.setString(++index, obj.getId().toString())
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
    override fun select(vocabulario: String, base: String): Optional<Vocabulario> = Optional.empty()

    @Throws(SQLException::class)
    override fun select(id: String): Optional<Vocabulario> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT)
            st.setString(1, id)
            rs = st.executeQuery()
            if (rs.next()) {
                Optional.of(
                    VocabularioExterno(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("palavra"),
                        rs.getString("portugues"),
                        rs.getString("ingles"),
                        rs.getString("leitura"),
                        rs.getBoolean("revisado")
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
    override fun select(id: UUID?): Optional<Vocabulario> = Optional.empty()

    override fun exist(id: String): Boolean {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            st = conn.prepareStatement(EXIST)
            st.setString(1, id)
            rs = st.executeQuery()
            return rs.next()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
        return false
    }

    @Throws(SQLException::class)
    override fun selectExclusao(): Set<String> = setOf()

    @Throws(SQLException::class)
    override fun selectEnvio(ultimo: LocalDateTime): List<Vocabulario> = listOf()

    override fun insertExclusao(palavra: String) {
        TODO("Not yet implemented")
    }

    override fun existeExclusao(palavra: String, basico: String) : Boolean = false

    @Throws(SQLException::class)
    override fun selectAll(): MutableList<Vocabulario> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ALL)
            rs = st.executeQuery()
            val list: MutableList<Vocabulario> = mutableListOf()
            while (rs.next()) list.add(
                VocabularioExterno(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("palavra"),
                    rs.getString("portugues"),
                    rs.getString("ingles"),
                    rs.getString("leitura"),
                    rs.getBoolean("revisado")
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

    override fun toEntity(rs: ResultSet): Vocabulario = VocabularioExterno(
        UUID.fromString(rs.getString("id")),
        rs.getString("palavra"), rs.getString("portugues"), rs.getString("ingles"),
        rs.getString("leitura"), rs.getBoolean("revisado")
    )
}