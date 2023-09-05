package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.controller.EstatisticaController.Tabela;
import org.jisho.textosJapones.database.dao.EstatisticaDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Estatistica;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EstatisticaDaoJDBC implements EstatisticaDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(EstatisticaDaoJDBC.class);

    private final Connection conn;

    final private static String INSERT = "INSERT IGNORE INTO estatistica (id, kanji, leitura, tipo, quantidade, percentual, media, percentual_medio, cor_sequencial) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
    final private static String UPDATE = "UPDATE estatistica SET tipo = ?, quantidade = ?, percentual = ?, media = ?, percentual_medio = ?, cor_sequencial = ? WHERE kanji = ? AND leitura = ? ;";
    final private static String DELETE = "DELETE FROM estatistica WHERE kanji = ? AND leitura = ? ;";
    final private static String SELECT = "SELECT id, kanji, leitura, tipo, quantidade, percentual, media, percentual_medio, cor_sequencial FROM estatistica WHERE kanji = ? AND leitura = ? ;";
    final private static String SELECT_KANJI = "SELECT id, kanji, leitura, tipo, quantidade, percentual, media, percentual_medio, cor_sequencial FROM estatistica WHERE kanji = ? ;";
    final private static String SELECT_ALL = "SELECT id, kanji, leitura, tipo, quantidade, percentual, media, percentual_medio, cor_sequencial FROM estatistica WHERE 1 > 0;";

    final private static String PESQUISA = "SELECT id, sequencia, word, read_info, frequency, tabela FROM words_kanji_info WHERE word LIKE ";

    public EstatisticaDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Estatistica obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getId().toString());
            st.setString(2, obj.getKanji());
            st.setString(3, obj.getLeitura());
            st.setString(4, obj.getTipo());
            st.setDouble(5, obj.getQuantidade());
            st.setFloat(6, obj.getPercentual());
            st.setDouble(7, obj.getMedia());
            st.setFloat(8, obj.getPercentMedia());
            st.setInt(9, obj.getCorSequencial());

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
    public void update(Estatistica obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getTipo());
            st.setDouble(2, obj.getQuantidade());
            st.setFloat(3, obj.getPercentual());
            st.setDouble(4, obj.getMedia());
            st.setFloat(5, obj.getPercentMedia());
            st.setInt(6, obj.getCorSequencial());
            st.setString(7, obj.getKanji());
            st.setString(8, obj.getLeitura());
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
    public void delete(Estatistica obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(DELETE);

            st.setString(1, obj.getKanji());
            st.setString(2, obj.getLeitura());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                LOGGER.info(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public Estatistica select(String kanji, String leitura) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT);
            st.setString(1, kanji);
            st.setString(2, leitura);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Estatistica(UUID.fromString(rs.getString("id")), rs.getString("kanji"), rs.getString("tipo"), rs.getString("leitura"),
                        rs.getDouble("quantidade"), rs.getFloat("percentual"), rs.getDouble("media"),
                        rs.getFloat("percentual_medio"), rs.getInt("cor_sequencial"));
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
    public List<Estatistica> selectAll() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_ALL);
            rs = st.executeQuery();

            List<Estatistica> list = new ArrayList<>();

            while (rs.next()) {
                list.add(new Estatistica(UUID.fromString(rs.getString("id")), rs.getString("kanji"), rs.getString("tipo"),
                        rs.getString("leitura"), rs.getDouble("quantidade"), rs.getFloat("percentual"),
                        rs.getDouble("media"), rs.getFloat("percentual_medio"), rs.getInt("cor_sequencial")));
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

    @Override
    public List<Estatistica> select(String kanji) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_KANJI);
            st.setString(1, kanji);
            rs = st.executeQuery();

            List<Estatistica> list = new ArrayList<>();

            while (rs.next()) {
                list.add(new Estatistica(UUID.fromString(rs.getString("id")), rs.getString("kanji"), rs.getString("tipo"), rs.getString("leitura"),
                        rs.getDouble("quantidade"), rs.getFloat("percentual"), rs.getDouble("media"),
                        rs.getFloat("percentual_medio"), rs.getInt("cor_sequencial")));
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

    @Override
    public List<Tabela> pesquisa(String pesquisa) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(PESQUISA + "'%" + pesquisa + "%'");
            rs = st.executeQuery();

            List<Tabela> list = new ArrayList<>();

            while (rs.next())
                list.add(new Tabela(rs.getString("word"), rs.getString("read_info"), rs.getString("tabela"), true));

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
