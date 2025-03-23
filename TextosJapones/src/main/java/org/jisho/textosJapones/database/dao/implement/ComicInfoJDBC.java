package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.ComicInfoDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.comicinfo.AgeRating;
import org.jisho.textosJapones.model.entities.comicinfo.ComicInfo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ComicInfoJDBC implements ComicInfoDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComicInfoJDBC.class);

    private final Connection conn;

    final private static String INSERT = "INSERT IGNORE INTO comicinfo (id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    final private static String UPDATE = "UPDATE comicinfo SET comic = ?, idMal = ?, series = ?, title = ?, publisher = ?, genre = ?, imprint = ?, seriesGroup = ?, storyArc = ?, maturityRating = ?, alternativeSeries = ?, language = ? WHERE id = ?;";
    final private static String SELECT = "SELECT id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language FROM comicinfo WHERE comic like ? AND language = ? ;";

    final private static String SELECT_BY_ID_OR_COMIC = "SELECT id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language FROM comicinfo WHERE id = ? OR (language = ? AND (UPPER(comic) LIKE ? or UPPER(series) LIKE ? or UPPER(title) LIKE ?));";
    final private static String SELECT_ENVIO = "SELECT id, comic, idMal, series, title, publisher, genre, imprint, seriesGroup, storyArc, maturityRating, alternativeSeries, language FROM comicinfo WHERE atualizacao = ?";


    public ComicInfoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(ComicInfo obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            int index = 0;
            st.setString(++index, UUID.randomUUID().toString());
            st.setString(++index, obj.getComic());
            if (obj.getIdMal() == null)
                st.setNString(++index, null);
            else
                st.setLong(++index, obj.getIdMal());
            st.setString(++index, obj.getSeries());
            st.setString(++index, obj.getTitle());
            st.setString(++index, obj.getPublisher());
            st.setString(++index, obj.getGenre());
            st.setString(++index, obj.getImprint());
            st.setString(++index, obj.getSeriesGroup());
            st.setString(++index, obj.getStoryArc());
            st.setString(++index, obj.getAgeRating() == null ? null : obj.getAgeRating().toString());
            st.setString(++index, obj.getAlternateSeries());
            st.setString(++index, obj.getLanguageISO());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                LOGGER.info(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void update(ComicInfo obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

            int index = 0;
            st.setString(++index, obj.getComic());
            if (obj.getIdMal() == null)
                st.setNString(++index, null);
            else
                st.setLong(++index, obj.getIdMal());
            st.setString(++index, obj.getSeries());
            st.setString(++index, obj.getTitle());
            st.setString(++index, obj.getPublisher());
            st.setString(++index, obj.getGenre());
            st.setString(++index, obj.getImprint());
            st.setString(++index, obj.getSeriesGroup());
            st.setString(++index, obj.getStoryArc());
            st.setString(++index, obj.getAgeRating() == null ? null : obj.getAgeRating().toString());
            st.setString(++index, obj.getAlternateSeries());
            st.setString(++index, obj.getLanguageISO());
            st.setString(++index, obj.getId().toString());
            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                LOGGER.info(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public ComicInfo select(String comic, String linguagem) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT);
            st.setString(1, comic);
            st.setString(2, linguagem);
            rs = st.executeQuery();

            if (rs.next()) {
                return new ComicInfo(UUID.fromString(rs.getString("id")), rs.getLong("idMal"), rs.getString("comic"), rs.getString("title"), rs.getString("series"), rs.getString("publisher"),
                        rs.getString("alternativeSeries"), rs.getString("storyArc"), rs.getString("seriesGroup"), rs.getString("imprint"),
                        rs.getString("genre"), rs.getString("language"), rs.getString("maturityRating") != null ? AgeRating.valueOf(rs.getString("maturityRating")) : null);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return null;
    }

    @Override
    public ComicInfo select(UUID id, String comic, String linguagem) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_BY_ID_OR_COMIC);
            Integer index = 0;
            st.setString(++index, id.toString());
            st.setString(++index, linguagem);
            st.setString(++index, comic.toUpperCase());
            st.setString(++index, comic.toUpperCase());
            st.setString(++index, comic.toUpperCase());
            rs = st.executeQuery();

            if (rs.next()) {
                return new ComicInfo(UUID.fromString(rs.getString("id")), rs.getLong("idMal"), rs.getString("comic"), rs.getString("title"), rs.getString("series"), rs.getString("publisher"),
                        rs.getString("alternativeSeries"), rs.getString("storyArc"), rs.getString("seriesGroup"), rs.getString("imprint"),
                        rs.getString("genre"), rs.getString("language"), rs.getString("maturityRating") != null ? AgeRating.valueOf(rs.getString("maturityRating")) : null);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return null;
    }

    @Override
    public List<ComicInfo> selectEnvio(LocalDateTime ultimo) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_ENVIO);
            st.setString(1, Util.convertToString(ultimo));
            rs = st.executeQuery();

            List<ComicInfo> list = new ArrayList<>();

            while (rs.next()) {
                list.add(new ComicInfo(UUID.fromString(rs.getString("id")), rs.getLong("idMal"), rs.getString("comic"), rs.getString("title"), rs.getString("series"), rs.getString("publisher"),
                        rs.getString("alternativeSeries"), rs.getString("storyArc"), rs.getString("seriesGroup"), rs.getString("imprint"),
                        rs.getString("genre"), rs.getString("language"), rs.getString("maturityRating") != null ? AgeRating.valueOf(rs.getString("maturityRating")) : null));
            }
            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }
}
