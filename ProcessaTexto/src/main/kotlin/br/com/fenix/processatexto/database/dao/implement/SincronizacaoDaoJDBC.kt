package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.database.dao.SincronizacaoDao
import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.messages.Mensagens
import br.com.fenix.processatexto.util.Utils
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*


class SincronizacaoDaoJDBC(conexao: Conexao) : SincronizacaoDao, RepositoryDaoBase<Long, Sincronizacao>(conexao) {

    private val LOGGER = LoggerFactory.getLogger(SincronizacaoDaoJDBC::class.java)

    companion object {
        private const val UPDATE = "UPDATE sincronizacao SET envio = ?, recebimento = ? WHERE conexao = ?;"
        private const val SELECT = "SELECT conexao, envio, recebimento FROM sincronizacao WHERE conexao = ?;"
    }

    @Throws(SQLException::class)
    override fun update(obj: Sincronizacao) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS)
            var index = 0
            st.setString(++index, Utils.convertToString(obj.envio))
            st.setString(++index, Utils.convertToString(obj.recebimento))
            st.setString(++index, obj.conexao.toString())
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
    override fun select(tipo: Conexao): Optional<Sincronizacao> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT)
            st.setString(1, tipo.toString())
            rs = st.executeQuery()
            if (rs.next())
                Optional.of(
                    Sincronizacao(
                        rs.getLong("id"),
                        Conexao.valueOf(rs.getString("conexao")),
                        Utils.convertToDateTime(rs.getString("envio")),
                        Utils.convertToDateTime(rs.getString("recebimento"))
                    )
                ) else
                Optional.empty()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }

    override fun toEntity(rs: ResultSet): Sincronizacao = Sincronizacao(rs.getLong("id"),
        Conexao.valueOf(rs.getString("conexao")), Utils.convertToDateTime(rs.getString("envio")),
        Utils.convertToDateTime(rs.getString("recebimento"))
    )

}