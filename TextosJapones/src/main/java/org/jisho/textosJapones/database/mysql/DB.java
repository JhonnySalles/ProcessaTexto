package org.jisho.textosJapones.database.mysql;

import org.jisho.textosJapones.components.notification.Alertas;
import org.jisho.textosJapones.model.entities.DadosConexao;
import org.jisho.textosJapones.model.enums.Conexao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DB {

    private static Set<DadosConexao> dados = new HashSet<>();
    private static Map<Conexao, Connection> connections = new HashMap<>();
    private static Connection conn = null;

    private static Connection createConnection(Properties props, String url) {
        try {
            return DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getLocalConnection() {
        if (conn == null) {
            Properties props = loadProperties();
            props.setProperty("characterEncoding", "UTF-8");
            props.setProperty("useUnicode", "true");
            String url = "jdbc:mysql://" + props.getProperty("server") + ":" + props.getProperty("port") + "/"
                    + props.getProperty("base")
                    + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            dados.add(new DadosConexao(0L, Conexao.TEXTOJAPONES, "jdbc:mysql://" + props.getProperty("server") + ":" + props.getProperty("port"), props.getProperty("base")));
            conn = createConnection(props, url);
        }
        return conn;
    }

    public static Connection getConnection(Conexao conexao) {
        if (conexao == Conexao.TEXTOJAPONES)
            return conn;
        else {
            if (connections.containsKey(conexao))
                return connections.get(conexao);
            else {
                Connection created = findConnection(conexao);
                if (created != null)
                    connections.put(conexao, created);
                return created;
            }
        }
    }

    private static Connection findConnection(Conexao conexao) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT id, tipo, url, base, driver FROM conexoes WHERE tipo = ?");
            st.setString(1, conexao.toString());
            rs = st.executeQuery();

            if (rs.next()) {
                Properties props = loadProperties();
                props.setProperty("characterEncoding", "UTF-8");
                props.setProperty("useUnicode", "true");
                String url = rs.getString("url") + "/" + rs.getString("base")
                        + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
                dados.add(new DadosConexao(rs.getLong("id"), conexao, rs.getString("url"), rs.getString("base")));
                return createConnection(props, url);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return null;
    }

    public static DadosConexao getDados(Conexao conexao) {
        Optional<DadosConexao> aux = dados.stream().filter(it -> it.getTipo().equals(conexao)).findFirst();
        return aux.orElse(null);
    }

    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static Properties loadProperties() {
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            Properties props = new Properties();
            props.load(fs);
            return props;
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao carregar o properties", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void closeStatement(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
