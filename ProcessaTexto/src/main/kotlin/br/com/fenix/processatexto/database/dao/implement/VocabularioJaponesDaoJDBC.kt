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


class VocabularioJaponesDaoJDBC(conexao: Conexao) : VocabularioDao, RepositoryDaoBase<UUID?, Vocabulario>(conexao) {

    private val LOGGER = LoggerFactory.getLogger(VocabularioJaponesDaoJDBC::class.java)

    companion object {
        private const val INSERT = "INSERT IGNORE INTO vocabulario (id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles) VALUES (?,?,?,?,?,?,?);"
        private const val UPDATE = "UPDATE vocabulario SET forma_basica = ?, leitura = ?, leitura_novel = ?, portugues = ?, ingles = ? WHERE vocabulario = ?;"
        private const val DELETE = "DELETE FROM vocabulario WHERE vocabulario = ?;"
        private const val SELECT = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE vocabulario = ? OR forma_basica = ?;"
        private const val SELECT_PALAVRA = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE vocabulario = ?;"
        private const val SELECT_ID = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE id = ?;"
        private const val EXIST = "SELECT vocabulario FROM vocabulario WHERE vocabulario = ?;"
        private const val SELECT_ALL = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE forma_basica = '' OR leitura = '';"
        private const val INSERT_EXCLUSAO = "INSERT IGNORE INTO exclusao (palavra) VALUES (?)"
        private const val SELECT_ALL_EXCLUSAO = "SELECT palavra FROM exclusao"
        private const val SELECT_EXCLUSAO = "SELECT palavra FROM exclusao WHERE palavra = ? or palavra = ? "
        private const val SELECT_ENVIO = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE atualizacao >= ?;"
    }

    @get:Override
    override val tipo: Database get() = Database.JAPONES

    @Throws(SQLException::class)
    override fun insert(obj: Vocabulario) {
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
            st.setString(++index, obj.formaBasica)
            st.setString(++index, obj.leitura)
            st.setString(++index, obj.leituraNovel)
            st.setString(++index, obj.portugues)
            st.setString(++index, obj.ingles)
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
    override fun select(vocabulario: String, base: String): Optional<Vocabulario> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT)
            st.setString(1, vocabulario)
            st.setString(2, base)
            rs = st.executeQuery()
            if (rs.next()) {
                Optional.of(
                    Vocabulario(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues")
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
    override fun select(vocabulario: String): Optional<Vocabulario> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_PALAVRA)
            st.setString(1, vocabulario)
            rs = st.executeQuery()
            if (rs.next()) {
                Optional.of(
                    Vocabulario(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues")
                    )
                )
            } else
                Optional.of(Vocabulario(vocabulario))
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
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues")
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
    override fun selectAll(): MutableList<Vocabulario> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ALL)
            rs = st.executeQuery()
            val list: MutableList<Vocabulario> = mutableListOf()
            while (rs.next()) {
                list.add(
                    Vocabulario(
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues")
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
            st.setString(1, palavra)
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
                        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues")
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
            st.setString(1, palavra)
            st.setString(2, basico)
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

    override fun toEntity(rs: ResultSet): Vocabulario = Vocabulario(
        UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues")
    )

}