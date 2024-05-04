package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.RevisarDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.enums.Database;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RevisarInglesDaoJDBC implements RevisarDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevisarInglesDaoJDBC.class);

    private final Connection conn;

    final private String INSERT = "INSERT IGNORE INTO revisar (id, vocabulario, leitura, portugues, revisado, isAnime, isManga) VALUES (?,?,?,?,?,?,?);";
    final private String UPDATE = "UPDATE revisar SET leitura = ?, portugues = ?, revisado = ?, isAnime = ?, isManga = ? WHERE vocabulario = ?;";
    final private String DELETE = "DELETE FROM revisar WHERE vocabulario = ?;";
    final private String SELECT = "SELECT id, vocabulario, leitura, portugues, revisado, isAnime, isManga, isNovel FROM revisar ";
    final private String SELECT_PALAVRA = SELECT + "WHERE vocabulario = ?;";
    final private String SELECT_ID = SELECT + "WHERE id = ?;";
    final private String EXIST = "SELECT vocabulario FROM revisar WHERE vocabulario = ?;";
    final private String IS_VALIDO = "SELECT palavra FROM valido WHERE palavra LIKE ?;";
    final private String SELECT_ALL = SELECT + "WHERE 1 > 0;";
    final private String SELECT_TRADUZIR = SELECT + "WHERE revisado = false";
    final private String SELECT_QUANTIDADE_RESTANTE = "SELECT COUNT(*) AS Quantidade FROM revisar";
    final private String SELECT_REVISAR = SELECT + "WHERE %s ORDER BY aparece DESC LIMIT 1";
    final private String SELECT_REVISAR_PESQUISA = SELECT + "WHERE vocabulario = ? LIMIT 1";
    final private String INCREMENTA_VEZES_APARECE = "UPDATE revisar SET aparece = (aparece + 1) WHERE vocabulario = ?;";
    final private String SET_ISMANGA = "UPDATE revisar SET isManga = ? WHERE vocabulario = ?;";
    final private String SET_ISNOVEL = "UPDATE revisar SET isNovel = ? WHERE vocabulario = ?;";

    public RevisarInglesDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Database getTipo() {
        return Database.INGLES;
    }

    @Override
    public void insert(Revisar obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getId().toString());
            st.setString(2, obj.getVocabulario());
            st.setString(3, obj.getLeitura());
            st.setString(4, obj.getPortugues());
            st.setBoolean(5, obj.getRevisado().isSelected());
            st.setBoolean(6, obj.isAnime());
            st.setBoolean(7, obj.isManga());

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
    public void update(Revisar obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(UPDATE, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getLeitura());
            st.setString(2, obj.getPortugues());
            st.setBoolean(3, obj.getRevisado().isSelected());
            st.setBoolean(4, obj.isAnime());
            st.setBoolean(5, obj.isManga());
            st.setString(6, obj.getVocabulario());

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
    public void delete(Revisar obj) throws ExcessaoBd {
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
    public void delete(String vocabulario) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(DELETE);

            st.setString(1, vocabulario);

            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_DELETE);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public Revisar select(String vocabulario, String base) throws ExcessaoBd {
        return select(vocabulario);
    }

    @Override
    public Revisar select(String vocabulario) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_PALAVRA);
            st.setString(1, vocabulario);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"),"", rs.getString("portugues"), "", rs.getBoolean("revisado"),
                        rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel"));
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
    public Revisar select(UUID id) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_ID);
            st.setString(1, id.toString());
            rs = st.executeQuery();

            if (rs.next()) {
                return new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"),"", rs.getString("portugues"), "", rs.getBoolean("revisado"),
                        rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel"));
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
    public List<Revisar> selectAll() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_ALL);
            rs = st.executeQuery();

            List<Revisar> list = new ArrayList<>();

            while (rs.next()) {
                list.add(new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", rs.getString("portugues"), "", rs.getBoolean("revisado"),
                        rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")));
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
    public List<Revisar> selectTraduzir(Integer quantidadeRegistros) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_TRADUZIR + (quantidadeRegistros > 0 ? " LIMIT " + quantidadeRegistros : " LIMIT 1000"));
            rs = st.executeQuery();

            List<Revisar> list = new ArrayList<>();

            while (rs.next())
                list.add(new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"),"", rs.getString("portugues"), "", rs.getBoolean("revisado"),
                        rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")));

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
    public String isValido(String vocabulario) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(IS_VALIDO);
            st.setString(1, vocabulario);
            rs = st.executeQuery();

            if (rs.next())
                return vocabulario.toLowerCase();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return null;
    }

    @Override
    public List<String> selectFrases(String select) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(select);
            rs = st.executeQuery();

            List<String> list = new ArrayList<>();

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
    public String selectQuantidadeRestante() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_QUANTIDADE_RESTANTE);
            rs = st.executeQuery();

            if (rs.next())
                return rs.getString("Quantidade");
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return "0";
    }

    @Override
    public Revisar selectRevisar(String pesquisar, Boolean isAnime, Boolean isManga, Boolean isNovel) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            if (!pesquisar.trim().isEmpty()) {
                st = conn.prepareStatement(SELECT_REVISAR_PESQUISA);
                st.setString(1, pesquisar);
            } else {
                String parametro = "";

                if (isAnime)
                    parametro += "isAnime = true AND ";

                if (isManga)
                    parametro += "isManga = true AND ";

                if (isNovel)
                    parametro += "isNovel = true AND ";

                if (parametro.isEmpty())
                    parametro = "1>0";
                else
                    parametro = parametro.substring(0, parametro.length()-4);

                st = conn.prepareStatement(String.format(SELECT_REVISAR, parametro));
            }

            rs = st.executeQuery();

            if (rs.next())
                return new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), "",
                        rs.getString("leitura"), "", rs.getString("portugues"), "", rs.getBoolean("revisado"),
                        rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel"));
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
    public List<Revisar> selectSimilar(String vocabulario, String ingles) throws ExcessaoBd {
        return new ArrayList<>();
    }

    @Override
    public void incrementaVezesAparece(String vocabulario) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INCREMENTA_VEZES_APARECE);
            st.setString(1, vocabulario);
            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void setIsManga(Revisar obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(SET_ISMANGA);
            st.setBoolean(1, obj.isManga());
            st.setString(2, obj.getVocabulario());
            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void setIsNovel(Revisar obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(SET_ISNOVEL);
            st.setBoolean(1, obj.isNovel());
            st.setString(2, obj.getVocabulario());
            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
        }
    }

}
