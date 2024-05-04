package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.enums.Database;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VocabularioExternoDaoJDBC implements VocabularioDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(VocabularioExternoDaoJDBC.class);

    private final Connection conn;

    final private String INSERT = "INSERT IGNORE INTO _vocabularios (id, palavra, leitura, portugues, ingles, revisado) VALUES (?,?,?,?,?,?);";
    final private String UPDATE = "UPDATE _vocabularios SET palavra = ?, leitura = ?, portugues = ?, ingles = ?, revisado = ? WHERE id = ?;";
    final private String DELETE = "DELETE FROM _vocabularios WHERE id = ?;";
    final private String SELECT_ALL = "SELECT id, palavra, leitura, portugues, ingles, revisado FROM _vocabularios";
    final private String SELECT = SELECT_ALL + " WHERE id = ?;";
    final private String EXIST = "SELECT id FROM _vocabularios WHERE id = ?;";

    public VocabularioExternoDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Database getTipo() {
        return Database.EXTERNO;
    }

    @Override
    public void insert(Vocabulario obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            int index = 0;
            st.setString(++index, obj.getId().toString());
            st.setString(++index, ((VocabularioExterno) obj).getPalavra());
            st.setString(++index, obj.getLeitura());
            st.setString(++index, obj.getPortugues());
            st.setString(++index, obj.getIngles());
            st.setBoolean(++index, ((VocabularioExterno) obj).getRevisado());

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
            st.setString(++index, ((VocabularioExterno) obj).getPalavra());
            st.setString(++index, obj.getLeitura());
            st.setString(++index, obj.getPortugues());
            st.setString(++index, obj.getIngles());
            st.setBoolean(++index, ((VocabularioExterno) obj).getRevisado());
            st.setString(++index, obj.getId().toString());

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
        return null;
    }

    @Override
    public Vocabulario select(String id) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT);
            st.setString(1, id);
            rs = st.executeQuery();

            if (rs.next()) {
                return new VocabularioExterno(UUID.fromString(rs.getString("id")), rs.getString("palavra"), rs.getString("portugues"),
                        rs.getString("ingles"), rs.getString("leitura"), rs.getBoolean("revisado"));
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
        return null;
    }

    @Override
    public boolean exist(String id) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(EXIST);
            st.setString(1, id);
            rs = st.executeQuery();
            return rs.next();
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

    }

    @Override
    public Set<String> selectExclusao() throws ExcessaoBd {
        return null;
    }

    @Override
    public List<Vocabulario> selectEnvioVocabulario(LocalDateTime ultimo) throws ExcessaoBd {
        return new ArrayList<>();
    }

    @Override
    public Set<String> selectExclusaoEnvio(LocalDateTime ultimo) throws ExcessaoBd {
        return null;
    }

    @Override
    public boolean existeExclusao(String palavra, String basico) throws ExcessaoBd {
        return false;
    }

    @Override
    public List<Vocabulario> selectAll() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_ALL);
            rs = st.executeQuery();

            List<Vocabulario> list = new ArrayList<>();

            while (rs.next())
                list.add(new VocabularioExterno(UUID.fromString(rs.getString("id")), rs.getString("palavra"), rs.getString("portugues"),
                        rs.getString("ingles"), rs.getString("leitura"), rs.getBoolean("revisado")));
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
