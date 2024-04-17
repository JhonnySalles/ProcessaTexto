package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Database;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class VocabularioInglesDaoJDBC implements VocabularioDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularioInglesDaoJDBC.class);

    private final Connection conn;

    final private String INSERT = "INSERT IGNORE INTO vocabulario (id, vocabulario, leitura, portugues) VALUES (?,?,?,?);";
    final private String UPDATE = "UPDATE vocabulario SET leitura = ?, portugues = ? WHERE vocabulario = ?;";
    final private String DELETE = "DELETE FROM vocabulario WHERE vocabulario = ?;";
    final private String SELECT = "SELECT id, vocabulario, leitura, portugues FROM vocabulario WHERE vocabulario = ?;";
    final private String SELECT_ID = "SELECT id, vocabulario, leitura, portugues FROM vocabulario WHERE id = ?;";
    final private String EXIST = "SELECT id, vocabulario FROM vocabulario WHERE vocabulario = ?;";
    final private String INSERT_EXCLUSAO = "INSERT IGNORE INTO exclusao (palavra) VALUES (?)";
    final private String SELECT_ALL_EXCLUSAO = "SELECT palavra FROM exclusao";
    final private String SELECT_EXCLUSAO = "SELECT palavra FROM exclusao WHERE palavra = ? ";

    final private String SELECT_ENVIO = "SELECT id, vocabulario, leitura, portugues FROM vocabulario WHERE atualizacao >= ?;";

    public VocabularioInglesDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Database getTipo() {
        return Database.INGLES;
    }

    @Override
    public void insert(Vocabulario obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getId().toString());
            st.setString(2, obj.getVocabulario());
            st.setString(3, obj.getLeitura());
            st.setString(4, obj.getPortugues());

            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void update(Vocabulario obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getLeitura());
            st.setString(2, obj.getPortugues());
            st.setString(3, obj.getVocabulario());

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
    public void delete(Vocabulario obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(DELETE);

            st.setString(1, obj.getVocabulario());

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
    public Vocabulario select(String vocabulario, String base) throws ExcessaoBd {
        return select(vocabulario);
    }

    @Override
    public Vocabulario select(String vocabulario) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT);
            st.setString(1, vocabulario);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", "", rs.getString("portugues"));
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
    public Vocabulario select(UUID id) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_ID);
            st.setString(1, id.toString());
            rs = st.executeQuery();

            if (rs.next()) {
                return new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", "", rs.getString("portugues"));
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
    public boolean exist(String vocabulario) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(EXIST);
            st.setString(1, vocabulario);
            rs = st.executeQuery();

            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return false;
    }

    @Override
    public void insertExclusao(String palavra) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INSERT_EXCLUSAO, Statement.RETURN_GENERATED_KEYS);
            st.setString(1, palavra.toLowerCase());

            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public Set<String> selectExclusao() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_ALL_EXCLUSAO);
            rs = st.executeQuery();

            Set<String> list = new HashSet<String>();

            while (rs.next())
                list.add(rs.getString("palavra"));

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
    public List<Vocabulario> selectEnvio(LocalDateTime ultimo) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_ENVIO);
            st.setTimestamp(1, Util.convertToTimeStamp(ultimo));
            rs = st.executeQuery();

            List<Vocabulario> list = new ArrayList<>();

            while (rs.next()) {
                list.add(new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", "", rs.getString("portugues")));
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
    public boolean existeExclusao(String palavra, String basico) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_EXCLUSAO);
            st.setString(1, palavra.toLowerCase());
            rs = st.executeQuery();

            if (rs.next())
                return true;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return false;
    }

    @Override
    public List<Vocabulario> selectAll() throws ExcessaoBd {
        return null;
    }

}
