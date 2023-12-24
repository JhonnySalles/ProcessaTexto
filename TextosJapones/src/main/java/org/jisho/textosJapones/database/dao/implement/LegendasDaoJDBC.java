package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.LegendasDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.entities.subtitle.Legenda;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LegendasDaoJDBC implements LegendasDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegendasDaoJDBC.class);

    private final Connection conn;
    private final Connection connDeckSubtitle;
    private final String schema;

    final private String SELECT_LISTA_TABELAS = "SELECT Table_Name AS Tabela "
            + " FROM information_schema.tables WHERE table_schema = '%s' AND %s "
            + " AND Table_Name != '_sql' GROUP BY Tabela ";

    final private String CREATE_TABELA = "CALL create_table('%s');";

    final private String CREATE_TRIGGER_INSERT =
            "CREATE TRIGGER tr_%s_insert BEFORE INSERT ON %s" +
                    "  FOR EACH ROW BEGIN" +
                    "    IF (NEW.id IS NULL OR NEW.id = '') THEN" +
                    "      SET new.id = UUID();" +
                    "    END IF;" +
                    "  END";

    final private String CREATE_TRIGGER_UPDATE =
            "CREATE TRIGGER tr_%s_update BEFORE UPDATE ON %s" +
                    "  FOR EACH ROW BEGIN" +
                    "    SET new.Atualizacao = NOW();" +
                    "  END";

    final private String INSERT_FILA = "INSERT INTO fila_sql (id, select_sql, update_sql, delete_sql, vocabulario, isExporta, isLimpeza) VALUES (?, ?, ?, ?, ?, ?, ?);";
    final private String UPDATE_FILA = "UPDATE fila_sql SET select_sql = ?, update_sql = ?, delete_sql = ?, vocabulario = ?, isExporta = ?, isLimpeza = ? WHERE id = ?";
    final private String SELECT_FILA = "SELECT id, sequencial, select_sql, update_sql, delete_sql, vocabulario, isExporta, isLimpeza FROM fila_sql";
    final private String EXISTS_FILA = "SELECT id, sequencial, select_sql, update_sql, delete_sql, vocabulario, isExporta, isLimpeza FROM fila_sql WHERE delete_sql = ?";

    final private static String INSERT = "INSERT INTO %s (Episodio, Linguagem, TempoInicial, TempoFinal, Texto, Traducao, Vocabulario) VALUES (?, ?, ?, ?, ?, ?, ?);";
    final private static String UPDATE = "UPDATE %s SET Episodio = ?, Linguagem = ?, TempoInicial = ?, TempoFinal = ?, Texto = ?, Traducao = ?, Vocabulario = ? WHERE id = ?;";
    final private static String SELECT = "SELECT id, Episodio, Linguagem, TempoInicial, TempoFinal, Texto, Traducao, Vocabulario FROM %s WHERE 1 > 0 ;";



    public LegendasDaoJDBC(Connection conn, Connection decksubtitle, String base) {
        this.conn = conn;
        this.connDeckSubtitle = decksubtitle;
        this.schema = base;
    }

    @Override
    public List<String> getTabelas() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = connDeckSubtitle.prepareStatement(
                    String.format(SELECT_LISTA_TABELAS, schema, "1 > 0"));
            rs = st.executeQuery();

            List<String> list = new ArrayList<>();

            while (rs.next())
                list.add(rs.getString("Tabela"));

            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public void createTabela(String base) throws ExcessaoBd {
        String nome = base.trim();

        PreparedStatement st = null;
        try {
            st = connDeckSubtitle.prepareStatement(String.format(CREATE_TABELA, nome));
            st.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
        } finally {
            DB.closeStatement(st);
        }

        try {
            st = connDeckSubtitle.prepareStatement(String.format(CREATE_TRIGGER_INSERT, nome, nome));
            st.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
        } finally {
            DB.closeStatement(st);
        }

        try {
            st = connDeckSubtitle.prepareStatement(String.format(CREATE_TRIGGER_UPDATE, nome, nome));
            st.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_CREATE_DATABASE);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void insert(String tabela, Legenda obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = connDeckSubtitle.prepareStatement(String.format(INSERT, tabela), Statement.RETURN_GENERATED_KEYS);

            Integer index = 0;
            st.setInt(++index, obj.getEpisodio());
            st.setString(++index, obj.getLinguagem().getSigla().toUpperCase());
            st.setString(++index, obj.getTempo());
            st.setNString(++index, "");
            st.setString(++index, obj.getTexto());
            st.setString(++index, obj.getTraducao());
            st.setString(++index, obj.getVocabulario());
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
    public void update(String tabela, Legenda obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = connDeckSubtitle.prepareStatement(String.format(UPDATE, tabela), Statement.RETURN_GENERATED_KEYS);

            Integer index = 0;
            st.setString(++index, obj.getId().toString());
            st.setInt(++index, obj.getEpisodio());
            st.setString(++index, obj.getLinguagem().getSigla().toUpperCase());
            st.setString(++index, obj.getTempo());
            st.setNString(++index, null);
            st.setString(++index, obj.getTexto());
            st.setString(++index, obj.getTraducao());
            st.setString(++index, obj.getVocabulario());
            st.setString(++index, obj.getId().toString());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                LOGGER.info(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
            };
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_INSERT);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void comandoUpdate(String update, Processar obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(update, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getVocabulario());
            st.setString(2, obj.getId());

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
    public List<Processar> comandoSelect(String select) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(select);
            rs = st.executeQuery();

            List<Processar> list = new ArrayList<>();

            while (rs.next())
                list.add(new Processar(rs.getString(1), rs.getString(2)));

            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public void comandoInsert(FilaSQL fila) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(INSERT_FILA, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, UUID.randomUUID().toString());
            st.setString(2, fila.getSelect());
            st.setString(3, fila.getUpdate());
            st.setString(4, fila.getDelete());
            st.setString(5, fila.getVocabulario());
            st.setBoolean(6, fila.isExporta());
            st.setBoolean(7, fila.isLimpeza());

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
    public void comandoUpdate(FilaSQL fila) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(UPDATE_FILA, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, fila.getSelect());
            st.setString(2, fila.getUpdate());
            st.setString(3, fila.getDelete());
            st.setString(4, fila.getVocabulario());
            st.setBoolean(5, fila.isExporta());
            st.setBoolean(6, fila.isLimpeza());
            st.setString(7, fila.getId().toString());

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
    public Boolean existFila(String deleteSql) throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(EXISTS_FILA);
            st.setString(1, deleteSql);
            rs = st.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public List<FilaSQL> comandoSelect() throws ExcessaoBd {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {

            st = conn.prepareStatement(SELECT_FILA);
            rs = st.executeQuery();

            List<FilaSQL> list = new ArrayList<>();

            while (rs.next())
                list.add(new FilaSQL(UUID.fromString(rs.getString("id")), rs.getLong("sequencial"), rs.getString("select_sql"),
                        rs.getString("update_sql"), rs.getString("delete_sql"), rs.getString("vocabulario"),
                        rs.getBoolean("isExporta"), rs.getBoolean("isLimpeza")));

            return list;
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public void comandoDelete(String delete) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(delete, Statement.RETURN_GENERATED_KEYS);
            st.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.info(st.toString());
            throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
        } finally {
            DB.closeStatement(st);
        }
    }

}
