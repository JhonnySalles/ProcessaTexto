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

public class RevisarJaponesDaoJDBC implements RevisarDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevisarJaponesDaoJDBC.class);

    private final Connection conn;

    final private String INSERT = "INSERT IGNORE INTO revisar (id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles, revisado, isAnime, isManga, isNovel) VALUES (?,?,?,?,?,?,?,?,?,?,?);";
    final private String UPDATE = "UPDATE revisar SET forma_basica = ?, leitura = ?, leitura_novel = ?, portugues = ?, ingles = ?, revisado = ?, isAnime = ?, isManga = ?, isNovel = ? WHERE vocabulario = ?;";
    final private String DELETE = "DELETE FROM revisar WHERE vocabulario = ?;";
    final private String SELECT = "SELECT id, vocabulario, forma_basica, leitura, leitura_novel, portugues, ingles, revisado, isAnime, isManga, isNovel FROM revisar ";
    final private String SELECT_FORMA = SELECT + "WHERE vocabulario = ? OR forma_basica = ?;";
    final private String SELECT_PALAVRA = SELECT + "WHERE vocabulario = ?;";
    final private String SELECT_ID = SELECT + "WHERE id = ?;";
    final private String EXIST = "SELECT vocabulario FROM revisar WHERE vocabulario = ?;";
    final private String SELECT_ALL = SELECT + "WHERE 1 > 0;";
    final private String SELECT_TRADUZIR = SELECT + "WHERE revisado = false";
    final private String SELECT_QUANTIDADE_RESTANTE = "SELECT COUNT(*) AS Quantidade FROM revisar";
    final private String SELECT_REVISAR = SELECT + "WHERE %s ORDER BY aparece DESC LIMIT 1";
    final private String SELECT_REVISAR_PESQUISA = SELECT + "WHERE vocabulario = ? or forma_basica = ? LIMIT 1";
    final private String SELECT_SIMILAR = SELECT + "WHERE vocabulario <> ? AND ingles <> '' AND ingles = ?";
    final private String INCREMENTA_VEZES_APARECE = "UPDATE revisar SET aparece = (aparece + 1) WHERE vocabulario = ?;";
    final private String SET_ISMANGA = "UPDATE revisar SET isManga = ? WHERE vocabulario = ?;";
    final private String SET_ISNOVEL = "UPDATE revisar SET isNovel = ? WHERE vocabulario = ?;";

    public RevisarJaponesDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Database getTipo() {
        return Database.JAPONES;
    }

    @Override
    public void insert(Revisar obj) throws ExcessaoBd {
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
            st.setBoolean(++index, obj.getRevisado().isSelected());
            st.setBoolean(++index, obj.isAnime());
            st.setBoolean(++index, obj.isManga());
            st.setBoolean(++index, obj.isNovel());

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

            int index = 0;
            st.setString(++index, obj.getFormaBasica());
            st.setString(++index, obj.getLeitura());
            st.setString(++index, obj.getLeituraNovel());
            st.setString(++index, obj.getPortugues());
            st.setString(++index, obj.getIngles());
            st.setBoolean(++index, obj.getRevisado().isSelected());
            st.setBoolean(++index, obj.isAnime());
            st.setBoolean(++index, obj.isManga());
            st.setBoolean(++index, obj.isNovel());
            st.setString(++index, obj.getVocabulario());

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
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_FORMA);
            st.setString(1, vocabulario);
            st.setString(2, base);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel"));
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
    public Revisar select(String vocabulario) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement(SELECT_PALAVRA);
            st.setString(1, vocabulario);
            rs = st.executeQuery();

            if (rs.next()) {
                return new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel"));
            }
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return new Revisar(vocabulario);
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
                return new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel"));
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
                list.add(new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")));
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
                list.add(new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")));

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
        // TODO Auto-generated method stub
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
                st.setString(2, pesquisar);
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
                return new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel"));
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
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_SIMILAR);
            st.setString(1, vocabulario);
            st.setString(2, ingles);
            rs = st.executeQuery();

            List<Revisar> list = new ArrayList<>();

            while (rs.next())
                list.add(new Revisar(UUID.fromString(rs.getString("id")), rs.getString("vocabulario"), rs.getString("forma_basica"),
                        rs.getString("leitura"), rs.getString("leitura_novel"), rs.getString("portugues"), rs.getString("ingles"),
                        rs.getBoolean("revisado"), rs.getBoolean("isAnime"), rs.getBoolean("isManga"), rs.getBoolean("isNovel")));

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
