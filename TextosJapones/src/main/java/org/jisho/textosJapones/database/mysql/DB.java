package org.jisho.textosJapones.database.mysql;

import org.jisho.textosJapones.components.notification.Alertas;
import org.jisho.textosJapones.model.entities.DadosConexao;
import org.jisho.textosJapones.model.enums.Conexao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class DB {

    private static final Logger LOGGER = LoggerFactory.getLogger(DB.class);

    private static final Set<DadosConexao> dados = new HashSet<>();
    private static final Map<Conexao, Connection> connections = new HashMap<>();
    private static Connection conn = null;

    private static Connection createConnection(Properties props, String url) {
        try {
            return DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
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
            dados.add(new DadosConexao(0L, Conexao.TEXTOJAPONES, "jdbc:mysql://" + props.getProperty("server") + ":" + props.getProperty("port"), props.getProperty("base"), props.getProperty("user"), props.getProperty("password")));
            conn = createConnection(props, url);
        }
        return conn;
    }

    public static Connection getConnection(Conexao conexao) {
        switch (conexao) {
            case TEXTOJAPONES -> {
                return conn;
            }
            case FIREBASE -> {
                return null;
            }
            default -> {
                if (connections.containsKey(conexao))
                    return connections.get(conexao);
                else {
                    Connection created = createConnection(conexao);
                    if (created != null)
                        connections.put(conexao, created);
                    return created;
                }
            }
        }
    }

    private static DadosConexao findConnection(Conexao conexao) {
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            st = conn.prepareStatement("SELECT id, tipo, url, username, password, base, driver FROM conexoes WHERE tipo = ?");
            st.setString(1, conexao.toString());
            rs = st.executeQuery();

            if (rs.next())
                return new DadosConexao(rs.getLong("id"), conexao, rs.getString("url"), rs.getString("base"), rs.getString("username"), rs.getString("password"));
        } catch (SQLException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            DB.closeStatement(st);
            DB.closeResultSet(rs);
        }
        return null;
    }

    private static Connection createConnection(Conexao conexao) {
        DadosConexao dados = findConnection(conexao);

        if (dados == null)
            return null;

        Properties props = loadProperties();
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("useUnicode", "true");
        String url = dados.getUrl() + "/" + dados.getBase() + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        DB.dados.add(dados);
        return createConnection(props, url);
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
                LOGGER.error(e.getMessage(), e);
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
            
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static void closeStatement(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
