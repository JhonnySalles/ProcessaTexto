package org.jisho.textosJapones.database.mysql;

import org.jisho.textosJapones.components.notification.Alertas;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DB {

	private static Connection conn = null;

	public static Connection getConnection() {
		if (conn == null) {
			try {
				Properties props = loadProperties();
				props.setProperty("characterEncoding", "UTF-8");
				props.setProperty("useUnicode", "true");
				String url = "jdbc:mysql://" + props.getProperty("server") + ":" + props.getProperty("port") + "/"
						+ props.getProperty("base_japones")
						+ "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
				conn = DriverManager.getConnection(url, props);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return conn;
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
