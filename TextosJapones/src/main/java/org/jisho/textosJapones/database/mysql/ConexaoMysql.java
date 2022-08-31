package org.jisho.textosJapones.database.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jisho.textosJapones.util.configuration.Configuracao;

public class ConexaoMysql {

	private static String SERVER = "";
	private static String PORT = "";
	private static String USER = "";
	private static String PASSWORD = "";
	private static String CAMINHO_MYSQL = "";
	private static String CAMINHO_WINRAR = "";
	private static String DATA_BASE_MANGA = "";
	private static String DATA_BASE_JAPONES = "";
	private static String DATA_BASE_INGLES = "";


	public static void setDadosConexao(String server, String port, String user, String psswd,
			String mysql, String winrar, String dataBaseManga, String dataBaseJapones, String dataBaseIngles) {
		SERVER = server;
		PORT = port;
		USER = user;
		PASSWORD = psswd;
		CAMINHO_MYSQL = mysql;
		CAMINHO_WINRAR = winrar;
		DATA_BASE_MANGA = dataBaseManga;
		DATA_BASE_JAPONES = dataBaseJapones;
		DATA_BASE_INGLES = dataBaseIngles;
		saveDadosConexao();
	}

	private static void saveDadosConexao() {
		Configuracao.createProperties(SERVER, PORT, USER, PASSWORD, CAMINHO_MYSQL, CAMINHO_WINRAR, 
				DATA_BASE_MANGA, DATA_BASE_JAPONES, DATA_BASE_INGLES );
	}

	public static String getServer() {
		return SERVER;
	}

	public static String getPort() {
		return PORT;
	}

	public static String getDataBaseManga() {
		return DATA_BASE_MANGA;
	}
	
	public static String getDataBaseJapones() {
		return DATA_BASE_JAPONES;
	}

	
	public static String getDataBaseIngles() {
		return DATA_BASE_INGLES;
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
		DATA_BASE_MANGA = props.getProperty("base_manga");
		DATA_BASE_JAPONES = props.getProperty("base_japones");
		DATA_BASE_INGLES = props.getProperty("base_ingles");
	}

	public static String testaConexaoMySQL() {
		getDadosConexao();
		Connection connection = null;
		String conecta = "";
		try {

			String driverName = "com.mysql.cj.jdbc.Driver";
			Class.forName(driverName);

			String url = "jdbc:mysql://" + SERVER + ":" + PORT + "/" + DATA_BASE_JAPONES
					+ "?useTimezone=true&serverTimezone=UTC";
			connection = DriverManager.getConnection(url, USER, PASSWORD);

			if (connection != null)
				conecta = DATA_BASE_JAPONES;
			

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
