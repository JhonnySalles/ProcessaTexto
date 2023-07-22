package org.jisho.textosJapones.util.configuration;

import org.jisho.textosJapones.components.notification.Alertas;

import java.io.*;
import java.util.Properties;

public class Configuracao {

	public static void createProperties(String server, String port, String user, String pws, 
			String mysql, String winrar, String baseManga, String baseJapones, String baseIngles) {
		try (OutputStream os = new FileOutputStream("db.properties")) {
			Properties props = new Properties();

			props.setProperty("server", server);
			props.setProperty("port", port);
			props.setProperty("user", user);
			props.setProperty("password", pws);
			props.setProperty("caminho_mysql", mysql);
			props.setProperty("caminho_winrar", winrar);
			props.setProperty("base_manga", baseManga);
			props.setProperty("base_japones", baseJapones);
			props.setProperty("base_ingles", baseIngles);
			props.store(os, "");
		} catch (IOException e) {
			Alertas.Tela_Alerta("Erro ao salvar o properties", e.getMessage());
			e.printStackTrace();
		}
	}

	public static Properties loadProperties() {
		File f = new File("db.properties");
		if (!f.exists())
			createProperties("", "", "", "", "", "", "", "", "");

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
	
	
	public static Properties loadSecrets() {
		File f = new File("secrets.properties");
		
		if (!f.exists()) {
			try (OutputStream os = new FileOutputStream("db.properties")) {
				Properties props = new Properties();

				props.setProperty("my_anime_list_client_id", "");
				props.store(os, "");
			} catch (IOException e) {
				Alertas.Tela_Alerta("Erro ao salvar o secrets", e.getMessage());
				e.printStackTrace();
			}
		}

		try (FileInputStream fs = new FileInputStream("secrets.properties")) {
			Properties props = new Properties();
			props.load(fs);
			return props;
		} catch (IOException e) {
			Alertas.Tela_Alerta("Erro ao carregar o secrets", e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}
