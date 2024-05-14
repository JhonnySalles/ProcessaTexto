package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.controller.EstatisticaController
import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.EstatisticaDao
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.model.entities.processatexto.Estatistica
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.messages.Mensagens
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*


class EstatisticaDaoJDBC(conexao: Conexao) : EstatisticaDao, RepositoryDao<UUID?, Estatistica>(conexao) {

    companion object {
        private const val INSERT =
            "INSERT IGNORE INTO estatistica (id, kanji, leitura, tipo, quantidade, percentual, media, percentual_medio, cor_sequencial) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
        private const val UPDATE =
            "UPDATE estatistica SET tipo = ?, quantidade = ?, percentual = ?, media = ?, percentual_medio = ?, cor_sequencial = ? WHERE kanji = ? AND leitura = ? ;"
        private const val DELETE = "DELETE FROM estatistica WHERE kanji = ? AND leitura = ? ;"
        private const val SELECT =
            "SELECT id, kanji, leitura, tipo, quantidade, percentual, media, percentual_medio, cor_sequencial FROM estatistica WHERE kanji = ? AND leitura = ? ;"
        private const val SELECT_KANJI = "SELECT id, kanji, leitura, tipo, quantidade, percentual, media, percentual_medio, cor_sequencial FROM estatistica WHERE kanji = ? ;"
        private const val SELECT_ALL = "SELECT id, kanji, leitura, tipo, quantidade, percentual, media, percentual_medio, cor_sequencial FROM estatistica WHERE 1 > 0;"
        private const val PESQUISA = "SELECT id, sequencia, word, read_info, frequency, tabela FROM words_kanji_info WHERE word LIKE "
    }

    private val LOGGER = LoggerFactory.getLogger(EstatisticaDaoJDBC::class.java)

    override fun toEntity(rs: ResultSet): Estatistica = Estatistica(
        UUID.fromString(rs.getString("id")), rs.getString("kanji"), rs.getString("tipo"), rs.getString("leitura"),
        rs.getDouble("quantidade"), rs.getFloat("percentual"), rs.getDouble("media"),
        rs.getFloat("percentual_medio"), rs.getInt("cor_sequencial")
    )

    @Throws(SQLException::class)
    override fun insert(obj: Estatistica) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.getId().toString())
            st.setString(++index, obj.kanji)
            st.setString(++index, obj.leitura)
            st.setString(++index, obj.tipo)
            st.setDouble(++index, obj.quantidade)
            st.setFloat(++index, obj.percentual)
            st.setDouble(++index, obj.media)
            st.setFloat(++index, obj.percentMedia)
            st.setInt(++index, obj.corSequencial)

            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                throw SQLException(Mensagens.BD_ERRO_INSERT)
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
    override fun update(obj: Estatistica) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS)

            var index = 0
            st.setString(++index, obj.tipo)
            st.setDouble(++index, obj.quantidade)
            st.setFloat(++index, obj.percentual)
            st.setDouble(++index, obj.media)
            st.setFloat(++index, obj.percentMedia)
            st.setInt(++index, obj.corSequencial)
            st.setString(++index, obj.kanji)
            st.setString(++index, obj.leitura)

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
    override fun delete(obj: Estatistica) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(DELETE)
            st.setString(1, obj.kanji)
            st.setString(2, obj.leitura)
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
    override fun select(kanji: String, leitura: String): Optional<Estatistica> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            st = conn.prepareStatement(SELECT)
            st.setString(1, kanji)
            st.setString(2, leitura)
            rs = st.executeQuery()
            return if (rs.next())
                Optional.of(toEntity(rs))
            else
                Optional.empty<Estatistica>()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    @Throws(SQLException::class)
    override fun selectAll(): MutableList<Estatistica> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ALL)
            rs = st.executeQuery()
            val list: MutableList<Estatistica> = mutableListOf()
            while (rs.next())
                list.add(toEntity(rs))
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
    override fun select(kanji: String): MutableList<Estatistica> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_KANJI)
            st.setString(1, kanji)
            rs = st.executeQuery()
            val list: MutableList<Estatistica> = mutableListOf()
            while (rs.next())
                list.add(toEntity(rs))
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
    override fun pesquisa(pesquisa: String): List<EstatisticaController.Tabela> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(PESQUISA + "'%" + pesquisa + "%'")
            rs = st.executeQuery()
            val list: MutableList<EstatisticaController.Tabela> = mutableListOf()
            while (rs.next())
                list.add(EstatisticaController.Tabela(rs.getString("word"), rs.getString("read_info"), rs.getString("tabela"), true))
            list
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

}