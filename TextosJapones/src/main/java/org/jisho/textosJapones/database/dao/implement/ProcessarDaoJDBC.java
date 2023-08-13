package org.jisho.textosJapones.database.dao.implement;

import org.jisho.textosJapones.database.dao.ProcessarDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProcessarDaoJDBC implements ProcessarDao {

    private Connection conn;

    final private String INSERT_FILA = "INSERT INTO fila_sql (id, select_sql, update_sql, delete_sql, vocabulario, isExporta, isLimpeza) VALUES (?, ?, ?, ?, ?, ?, ?);";
    final private String UPDATE_FILA = "UPDATE fila_sql SET select_sql = ?, update_sql = ?, delete_sql = ?, vocabulario = ?, isExporta = ?, isLimpeza = ? WHERE id = ?";
    final private String SELECT_FILA = "SELECT id, sequencial, select_sql, update_sql, delete_sql, vocabulario, isExporta, isLimpeza FROM fila_sql";

    public ProcessarDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void update(String update, Processar obj) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(update, Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getVocabulario());
            st.setString(2, obj.getId());

            int rowsAffected = st.executeUpdate();

            if (rowsAffected < 1) {
                System.out.println(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
            }
        } catch (SQLException e) {
            System.out.println(st.toString());
            e.printStackTrace();
            throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public List<Processar> select(String select) throws ExcessaoBd {
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
            e.printStackTrace();
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public void insert(FilaSQL fila) throws ExcessaoBd {
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
                System.out.println(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
            }
        } catch (SQLException e) {
            System.out.println(st.toString());
            e.printStackTrace();
            throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
        } finally {
            DB.closeStatement(st);
        }

    }

    @Override
    public void update(FilaSQL fila) throws ExcessaoBd {
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
                System.out.println(st.toString());
                throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
            }
        } catch (SQLException e) {
            System.out.println(st.toString());
            e.printStackTrace();
            throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
        } finally {
            DB.closeStatement(st);
        }

    }

    @Override
    public List<FilaSQL> select() throws ExcessaoBd {
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
            e.printStackTrace();
            throw new ExcessaoBd(Mensagens.BD_ERRO_SELECT);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
    }

    @Override
    public void delete(String delete) throws ExcessaoBd {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(delete, Statement.RETURN_GENERATED_KEYS);
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println(st.toString());
            e.printStackTrace();
            throw new ExcessaoBd(Mensagens.BD_ERRO_UPDATE);
        } finally {
            DB.closeStatement(st);
        }
    }

}
