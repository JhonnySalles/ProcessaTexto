package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.ComicInfoDao
import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.messages.Mensagens
import br.com.fenix.processatexto.model.entities.comicinfo.AgeRating
import org.slf4j.LoggerFactory
import java.sql.*
import java.util.*


class ComicInfoJDBC(conexao: Conexao) : ComicInfoDao, RepositoryDaoBase<UUID?, ComicInfo>(conexao) {

    companion object {
        private const val INSERT = "INSERT IGNORE INTO comicinfo (id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
        private const val UPDATE = "UPDATE comicinfo SET comic = ?, idMal = ?, series = ?, title = ?, publisher = ?, genre = ?, imprint = ?, seriesGroup = ?, storyArc = ?, maturityRating = ?, alternativeSeries = ?, language = ? WHERE id = ?;"
        private const val SELECT = "SELECT id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language FROM comicinfo WHERE comic like ? AND language = ? ;"
    }

    private val LOGGER = LoggerFactory.getLogger(ComicInfoJDBC::class.java)

    override fun toEntity(rs: ResultSet): ComicInfo = ComicInfo(
        UUID.fromString(rs.getString("id")), rs.getLong("idMal"), rs.getString("comic"), rs.getString("title"), rs.getString("series"), rs.getString("publisher"),
        rs.getString("alternativeSeries"), rs.getString("storyArc"), rs.getString("seriesGroup"), rs.getString("imprint"),
        rs.getString("genre"), rs.getString("language"), if (rs.getString("maturityRating") != null) AgeRating.valueOf(rs.getString("maturityRating")) else null
    )

    @Throws(SQLException::class)
    override fun insert(obj: ComicInfo) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)
            var index = 0
            st.setString(++index, UUID.randomUUID().toString())
            st.setString(++index, obj.comic)

            if (obj.idMal == null)
                st.setNString(++index, null)
            else
                st.setLong(++index, obj.idMal!!)

            st.setString(++index, obj.series)
            st.setString(++index, obj.title)
            st.setString(++index, obj.publisher)
            st.setString(++index, obj.genre)
            st.setString(++index, obj.imprint)
            st.setString(++index, obj.seriesGroup)
            st.setString(++index, obj.storyArc)
            st.setNString(++index, if (obj.ageRating == null) null else obj.ageRating.toString())
            st.setString(++index, obj.alternateSeries)
            st.setString(++index, obj.languageISO)

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
    override fun update(obj: ComicInfo) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS)
            var index = 0

            st.setString(++index, obj.comic)

            if (obj.idMal == null)
                st.setNString(++index, null)
            else
                st.setLong(++index, obj.idMal!!)

            st.setString(++index, obj.series)
            st.setString(++index, obj.title)
            st.setString(++index, obj.publisher)
            st.setString(++index, obj.genre)
            st.setString(++index, obj.imprint)
            st.setString(++index, obj.seriesGroup)
            st.setString(++index, obj.storyArc)
            st.setNString(++index, if (obj.ageRating == null) null else obj.ageRating.toString())
            st.setString(++index, obj.alternateSeries)
            st.setString(++index, obj.languageISO)

            st.setString(++index, obj.getId().toString())
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
    override fun select(comic: String, linguagem: String): Optional<ComicInfo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            st = conn.prepareStatement(SELECT)
            st.setString(1, comic)
            st.setString(2, linguagem)
            rs = st.executeQuery()
            return if (rs.next())
                Optional.of(toEntity(rs))
            else
                Optional.empty<ComicInfo>()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            throw SQLException(Mensagens.BD_ERRO_SELECT)
        } finally {
            JdbcFactory.closeStatement(st)
            JdbcFactory.closeResultSet(rs)
        }
    }
}