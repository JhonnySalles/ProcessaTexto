package org.jisho.textosJapones.util.mysql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jisho.textosJapones.util.notification.Alertas;

public class ConexaoMysql {

	private static String SERVER = "";
	private static String PORT = "";
	private static String DATA_BASE = "";
	private static String USER = "";
	private static String PASSWORD = "";
	private static String CAMINHO_MYSQL = "";

	private static Properties loadProperties() {
		File f = new File("db.properties");
		if (!f.exists())
			createProperties();

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

	private static void createProperties() {
		try (OutputStream os = new FileOutputStream("db.properties")) {
			Properties props = new Properties();

			props.setProperty("server", SERVER);
			props.setProperty("port", PORT);
			props.setProperty("dataBase", DATA_BASE);
			props.setProperty("user", USER);
			props.setProperty("password", PASSWORD);
			props.setProperty("caminho_mysql", CAMINHO_MYSQL);
			props.store(os, "");
		} catch (IOException e) {
			Alertas.Tela_Alerta("Erro ao salvar o properties", e.getMessage());
			e.printStackTrace();
		}
	}

	public static void setDadosConexao(String server, String port, String dataBase, String user, String psswd,
			String mysql) {
		SERVER = server;
		PORT = port;
		DATA_BASE = dataBase;
		USER = user;
		PASSWORD = psswd;
		CAMINHO_MYSQL = mysql;
		createProperties();
	}

	public static String getServer() {
		return SERVER;
	}

	public static String getPort() {
		return PORT;
	}

	public static String getDataBase() {
		return DATA_BASE;
	}

	public static String getUser() {
		return USER;
	}

	public static String getPassword() {
		return PASSWORD;
	}

	public static String getCaminhoMysql() {
		return CAMINHO_MYSQL;
	}

	public static void getDadosConexao() {
		Properties props = loadProperties();
		SERVER = props.getProperty("server");
		PORT = props.getProperty("port");
		DATA_BASE = props.getProperty("dataBase");
		USER = props.getProperty("user");
		PASSWORD = props.getProperty("password");
		CAMINHO_MYSQL = props.getProperty("caminho_mysql");
	}

	public static String testaConexaoMySQL() {
		getDadosConexao();
		Connection connection = null;
		String conecta = "";
		try {

			String driverName = "com.mysql.cj.jdbc.Driver";
			Class.forName(driverName);

			String url = "jdbc:mysql://" + SERVER + ":" + PORT + "/" + DATA_BASE
					+ "?useTimezone=true&serverTimezone=UTC";
			connection = DriverManager.getConnection(url, USER, PASSWORD);

			if (connection != null) {
				conecta = DATA_BASE;
			}

			connection.close();

		} catch (ClassNotFoundException e) { // Driver n�o encontrado
			System.out.println("O driver de conex�o expecificado nao foi encontrado.");
			e.printStackTrace();

		} catch (SQLException e) {
			System.out.println("Nao foi possivel conectar ao Banco de Dados.");
			e.printStackTrace();

		}
		return conecta;
	}

}
