package org.jisho.textosJapones.util.mysql;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jisho.textosJapones.Run;

public class ConexaoMysql {

	private static String server = "";
	private static String port = "";
	private static String dataBase = "";
	private static String user = "";
	private static String psswd = "";

	private static Properties loadProperties() {
		try (FileInputStream fs = new FileInputStream("db.properties")) {
			Properties props = new Properties();
			props.load(fs);
			return props;
		} catch (IOException e) {
			Run.getMainController().setImagemBancoErro("Erro ao carregar o properties");
			e.printStackTrace();
		}
		return null;
	}

	public static void getDadosConexao() {
		Properties props = loadProperties();
		server = props.getProperty("server");
		port = props.getProperty("port");
		dataBase = props.getProperty("dataBase");
		user = props.getProperty("user");
		psswd = props.getProperty("password");
	}

	public static String testaConexaoMySQL() {
		getDadosConexao();
		Connection connection = null;
		String conecta = "";
		try {

			String driverName = "com.mysql.cj.jdbc.Driver";
			Class.forName(driverName);

			String url = "jdbc:mysql://" + server + ":" + port + "/" + dataBase
					+ "?useTimezone=true&serverTimezone=UTC";
			connection = DriverManager.getConnection(url, user, psswd);

			if (connection != null) {
				conecta = dataBase;
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
