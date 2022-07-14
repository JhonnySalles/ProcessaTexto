package org.jisho.textosJapones.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jisho.textosJapones.util.configuration.Configuracao;

public class ConexaoMysql {

	private static String SERVER = "";
	private static String PORT = "";
	private static String DATA_BASE = "";
	private static String USER = "";
	private static String PASSWORD = "";
	private static String CAMINHO_MYSQL = "";
	private static String DATA_BASE_MANGA = "";
	private static String CAMINHO_WINRAR = "";

	public static void setDadosConexao(String server, String port, String dataBase, String user, String psswd,
			String mysql, String dataBaseManga, String winrar) {
		SERVER = server;
		PORT = port;
		DATA_BASE = dataBase;
		USER = user;
		PASSWORD = psswd;
		CAMINHO_MYSQL = mysql;
		DATA_BASE_MANGA = dataBaseManga;
		CAMINHO_WINRAR = winrar;
		saveDadosConexao();
	}

	private static void saveDadosConexao() {
		Configuracao.createProperties(SERVER, PORT, DATA_BASE, USER, PASSWORD, CAMINHO_MYSQL, DATA_BASE_MANGA, CAMINHO_WINRAR);
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

	public static String getDataBaseManga() {
		return DATA_BASE_MANGA;
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
	
	public static String getCaminhoWinrar() {
		return CAMINHO_WINRAR;
	}

	public static void getDadosConexao() {
		Properties props = Configuracao.loadProperties();
		SERVER = props.getProperty("server");
		PORT = props.getProperty("port");
		DATA_BASE = props.getProperty("dataBase");
		USER = props.getProperty("user");
		PASSWORD = props.getProperty("password");
		CAMINHO_MYSQL = props.getProperty("caminho_mysql");
		DATA_BASE_MANGA = props.getProperty("dataBase_manga");
		CAMINHO_WINRAR = props.getProperty("caminho_winrar");
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
