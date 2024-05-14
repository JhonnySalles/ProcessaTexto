package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.KanjiDao
import br.com.fenix.processatexto.database.dao.RepositoryDao
import br.com.fenix.processatexto.model.entities.processatexto.Kanji
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.messages.Mensagens
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*


class KanjiDaoJDBC(conexao: Conexao) : KanjiDao, RepositoryDao<UUID?, Kanji>(conexao) {

    companion object {
        private const val SELECT = "SELECT id, kanji, palavra, significado FROM kanjax_pt WHERE kanji = ?;"
    }

    private val LOGGER = LoggerFactory.getLogger(KanjiDaoJDBC::class.java)

    override fun toEntity(rs: ResultSet): Kanji = Kanji(UUID.fromString(rs.getString("id")),
        rs.getString("kanji"), rs.getString("palavra"), rs.getString("significado")
    )

    @Throws(SQLException::class)
    override fun select(kanji: String): Optional<Kanji> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            st = conn.prepareStatement(SELECT)
            st.setString(1, kanji)
            rs = st.executeQuery()
            return if (rs.next())
                Optional.of(toEntity(rs))
            else
                Optional.empty<Kanji>()
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