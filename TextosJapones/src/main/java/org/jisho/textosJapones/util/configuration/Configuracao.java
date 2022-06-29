package org.jisho.textosJapones.util.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import org.jisho.textosJapones.util.notification.Alertas;

public class Configuracao {

	public static void createProperties(String server, String port, String base, String user, String pws, String mysql,
			String baseManga, String winrar) {
		try (OutputStream os = new FileOutputStream("db.properties")) {
			Properties props = new Properties();

			props.setProperty("server", server);
			props.setProperty("port", port);
			props.setProperty("dataBase", base);
			props.setProperty("user", user);
			props.setProperty("password", pws);
			props.setProperty("caminho_mysql", mysql);
			props.setProperty("caminho_winrar", winrar);
			props.setProperty("dataBase_manga", baseManga);
			props.store(os, "");
		} catch (IOException e) {
			Alertas.Tela_Alerta("Erro ao salvar o properties", e.getMessage());
			e.printStackTrace();
		}
	}

	public static Properties loadProperties() {
		File f = new File("db.properties");
		if (!f.exists())
			createProperties("", "", "", "", "", "", "", "");

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

}
