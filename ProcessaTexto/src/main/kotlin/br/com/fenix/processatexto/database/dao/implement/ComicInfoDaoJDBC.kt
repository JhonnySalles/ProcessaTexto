package br.com.fenix.processatexto.database.dao.implement

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.dao.ComicInfoDao
import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.model.entities.comicinfo.AgeRating
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.messages.Mensagens
import br.com.fenix.processatexto.util.Utils
import org.slf4j.LoggerFactory
import java.sql.*
import java.time.LocalDateTime
import java.util.*


class ComicInfoDaoJDBC(conexao: Conexao) : ComicInfoDao, RepositoryDaoBase<UUID?, ComicInfo>(conexao) {

    companion object {
        private const val INSERT: String = "INSERT IGNORE INTO comicinfo (id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
        private const val UPDATE: String = "UPDATE comicinfo SET comic = ?, idMal = ?, series = ?, title = ?, publisher = ?, genre = ?, imprint = ?, seriesGroup = ?, storyArc = ?, maturityRating = ?, alternativeSeries = ?, language = ? WHERE id = ?;"
        private const val SELECT: String = "SELECT id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language FROM comicinfo"
        private const val DELETE: String = "DELETE FROM comicinfo WHERE id = ?;"

        private const val SELECT_BY_COMIC_AND_LANGUAGE = "SELECT id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language FROM comicinfo WHERE comic like ? AND language = ? ;"
        private const val SELECT_BY_ID_OR_COMIC = "SELECT id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language FROM comicinfo WHERE id = ? OR (language = ? AND (UPPER(comic) LIKE ? or UPPER(series) LIKE ? or UPPER(title) LIKE ?));"
        private const val SELECT_ENVIO = "SELECT id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language FROM comicinfo WHERE atualizacao >= ?"
    }

    private val LOGGER = LoggerFactory.getLogger(ComicInfoDaoJDBC::class.java)

    override fun toEntity(rs: ResultSet): ComicInfo = ComicInfo(
        UUID.fromString(rs.getString("id")), rs.getLong("idMal"), rs.getString("comic"), rs.getString("title"), rs.getString("series"), rs.getString("publisher"),
        rs.getString("alternativeSeries"), rs.getString("storyArc"), rs.getString("seriesGroup"), rs.getString("imprint"),
        rs.getString("genre"), rs.getString("language"), if (rs.getString("maturityRating") != null) AgeRating.valueOf(rs.getString("maturityRating")) else null
    )

    override fun toID(id: String?): UUID? = if (id != null) UUID.fromString(id) else null

    override fun getCustomParam(param: Objects): String {
        TODO("Not yet implemented")
    }

    @Throws(SQLException::class)
    override fun save(obj: ComicInfo) {
        if (obj.getId() == null)
            insertManual(obj)
        else
            updateManual(obj)
    }

    @Throws(SQLException::class)
    private fun insertManual(obj: ComicInfo) {
        var st: PreparedStatement? = null
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)
            obj.setId(UUID.randomUUID())

            var index = 0
            st.setString(++index, obj.getId().toString())
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
            st.setNString(++index, if (obj.ageRating == null) null else obj.ageRating!!.name)
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
    private fun updateManual(obj: ComicInfo) {
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
            st.setNString(++index, if (obj.ageRating == null) null else obj.ageRating!!.name)
            st.setString(++index, obj.alternateSeries)
            st.setString(++index, obj.languageISO)

            st.setString(++index, obj.getId().toString())
            val rowsAffected: Int = st.executeUpdate()
            if (rowsAffected < 1) {
                LOGGER.info(st.toString())
                //throw SQLException(Mensagens.BD_ERRO_UPDATE)
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
    override fun find(comic: String, linguagem: String): Optional<ComicInfo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            st = conn.prepareStatement(SELECT_BY_COMIC_AND_LANGUAGE)
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

    override fun find(id: UUID, comic: String, linguagem: String): Optional<ComicInfo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            st = conn.prepareStatement(SELECT_BY_ID_OR_COMIC)
            var index = 0
            st.setString(++index, id.toString())
            st.setString(++index, linguagem)
            st.setString(++index, comic.uppercase(Locale.getDefault()))
            st.setString(++index, comic.uppercase(Locale.getDefault()))
            st.setString(++index, comic.uppercase(Locale.getDefault()))
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

    override fun findEnvio(ultimo: LocalDateTime): List<ComicInfo> {
        var st: PreparedStatement? = null
        var rs: ResultSet? = null
        return try {
            st = conn.prepareStatement(SELECT_ENVIO)
            st.setString(1, Utils.convertToString(ultimo))
            rs = st.executeQuery()
            val list: MutableList<ComicInfo> = mutableListOf()
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
}