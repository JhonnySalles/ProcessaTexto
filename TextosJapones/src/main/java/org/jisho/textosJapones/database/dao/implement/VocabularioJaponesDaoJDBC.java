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

public class VocabularioJaponesDaoJDBC implements VocabularioDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularioJaponesDaoJDBC.class);

    private final Connection conn;

    final private String INSERT = "INSERT IGNORE INTO vocabulario (id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles) VALUES (?,?,?,?,?,?,?);";
    final private String UPDATE = "UPDATE vocabulario SET forma_basica = ?, leitura = ?, leitura_novel = ?, portugues = ?, ingles = ? WHERE vocabulario = ?;";
    final private String DELETE = "DELETE FROM vocabulario WHERE vocabulario = ?;";
    final private String SELECT = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE vocabulario = ? OR forma_basica = ?;";
    final private String SELECT_PALAVRA = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE vocabulario = ?;";
    final private String SELECT_ID = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE id = ?;";
    final private String EXIST = "SELECT vocabulario FROM vocabulario WHERE vocabulario = ?;";
    final private String SELECT_ALL = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE forma_basica = '' OR leitura = '';";
    final private String INSERT_EXCLUSAO = "INSERT IGNORE INTO exclusao (palavra) VALUES (?)";
    final private String SELECT_ALL_EXCLUSAO = "SELECT palavra FROM exclusao";
    final private String SELECT_EXCLUSAO = "SELECT palavra FROM exclusao WHERE palavra = ? or palavra = ? ";

    final private String SELECT_ENVIO_VOCABULARIO = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles FROM vocabulario WHERE atualizacao >= ?;";
    final private String SELECT_ENVIO_EXCLUSAO = "SELECT palavra FROM exclusao WHERE atualizacao >= ?;";


    public VocabularioJaponesDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Database getTipo() {
        return Database.JAPONES;
    }

    @Override
    public void insert(Vocabulario obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            int index = 0;
            st.setString(++index, obj.getId().toString());
            st.setString(++index, obj.getVocabulario());
            st.setString(++index, obj.getFormaBasica());
            st.setString(++index, obj.getLeitura());
            st.setString(++index, obj.getLeituraNovel());
            st.setString(++index, obj.getPortugues());
            st.setString(++index, obj.getIngles());

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

            int index = 0;
            st.setString(++index, obj.getFormaBasica());
            st.setString(++index, obj.getLeitura());
            st.setString(++index, obj.getLeituraNovel());
            st.setString(++index, obj.getPortugues());
            st.setString(++index, obj.getIngles());
            st.setString(++index, obj.getVocabulario());

            st.executeUpdate();
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
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT);
            st.setString(1, vocabulario);
            st.setString(2, base);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues"));
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
    public Vocabulario select(String vocabulario) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_PALAVRA);
            st.setString(1, vocabulario);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues"));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return new Vocabulario(vocabulario);
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
                return new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues"));
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
    public List<Vocabulario> selectAll() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_ALL);
            rs = st.executeQuery();

            List<Vocabulario> list = new ArrayList<>();

            while (rs.next()) {
                list.add(new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues")));
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
            st.setString(1, palavra);

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
    public List<Vocabulario> selectEnvioVocabulario(LocalDateTime ultimo) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_ENVIO_VOCABULARIO);
            st.setString(1, Util.convertToString(ultimo));
            rs = st.executeQuery();

            List<Vocabulario> list = new ArrayList<>();

            while (rs.next()) {
                list.add(new Vocabulario(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("ingles"), rs.getString("portugues")));
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
    public Set<String> selectExclusaoEnvio(LocalDateTime ultimo) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_ENVIO_EXCLUSAO);
            st.setString(1, Util.convertToString(ultimo));
            rs = st.executeQuery();

            Set<String> list = new HashSet<>();

            while (rs.next())
                list.add(rs.getString(1));

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
            st.setString(1, palavra);
            st.setString(2, basico);
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

}
