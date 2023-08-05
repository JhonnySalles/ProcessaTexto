package org.jisho.textosJapones.database.mysql;

import org.jisho.textosJapones.util.configuration.Configuracao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoMysql {

	private static String SERVER = "";
	private static String PORT = "";
	private static String USER = "";
	private static String PASSWORD = "";
	private static String CAMINHO_MYSQL = "";
	private static String CAMINHO_WINRAR = "";
	private static String DATA_BASE = "";


	public static void setDadosConexao(String server, String port, String user, String psswd,
			String mysql, String winrar, String dataBase) {
		SERVER = server;
		PORT = port;
		USER = user;
		PASSWORD = psswd;
		CAMINHO_MYSQL = mysql;
		CAMINHO_WINRAR = winrar;
		DATA_BASE = dataBase;
		saveDadosConexao();
	}

	private static void saveDadosConexao() {
		Configuracao.createProperties(SERVER, PORT, USER, PASSWORD, CAMINHO_MYSQL, CAMINHO_WINRAR, DATA_BASE );
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
	
	public static String getCaminhoWinrar() {
		return CAMINHO_WINRAR;
	}

	public static void getDadosConexao() {
		Properties props = Configuracao.loadProperties();
		SERVER = props.getProperty("server");
		PORT = props.getProperty("port");
		USER = props.getProperty("user");
		PASSWORD = props.getProperty("password");
		CAMINHO_MYSQL = props.getProperty("caminho_mysql");
		CAMINHO_WINRAR = props.getProperty("caminho_winrar");
		DATA_BASE = props.getProperty("base");
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

			if (connection != null)
				conecta = DATA_BASE;
			

			connection.close();

		} catch (ClassNotFoundException e) { // Driver n�o encontrado
			System.out.println("O driver de conexão expecificado nao foi encontrado.");
			e.printStackTrace();

		} catch (SQLException e) {
			System.out.println("Nao foi possivel conectar ao Banco de Dados.");
			e.printStackTrace();

		}
		return conecta;
	}

}
