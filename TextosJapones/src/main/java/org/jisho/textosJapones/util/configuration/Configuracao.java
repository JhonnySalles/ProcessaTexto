package org.jisho.textosJapones.util.configuration;

import org.jisho.textosJapones.components.notification.Alertas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class Configuracao {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuracao.class);

    private static Properties props = null;
    private static Properties secrets = null;

    public static void saveProperties(String winrar, String commictagger) {
        try (OutputStream os = new FileOutputStream("db.properties")) {
            if (props.containsKey("caminho_winrar"))
                props.replace("caminho_winrar", winrar);
            else
                props.put("caminho_winrar", winrar);

            if (props.containsKey("caminho_commictagger"))
                props.replace("caminho_commictagger", commictagger);
            else
                props.put("caminho_commictagger", commictagger);
            props.store(os, "");
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao salvar o properties", e.getMessage());
            
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void saveProperties(String server, String port, String user, String pws, String mysql, String base) {
        try (OutputStream os = new FileOutputStream("db.properties")) {
            if (props.containsKey("server"))
                props.replace("server", server);
            else
                props.put("server", server);

            if (props.containsKey("port"))
                props.replace("port", port);
            else
                props.put("port", port);

            if (props.containsKey("user"))
                props.replace("user", user);
            else
                props.put("user", user);

            if (props.containsKey("password"))
                props.replace("password", pws);
            else
                props.put("password", pws);

            if (props.containsKey("base"))
                props.replace("base", base);
            else
                props.put("base", base);

            if (props.containsKey("caminho_mysql"))
                props.replace("caminho_mysql", mysql);
            else
                props.put("caminho_mysql", mysql);

            props.store(os, "");
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao salvar o properties", e.getMessage());
            
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void createProperties() {
        props = new Properties();
        try (OutputStream os = new FileOutputStream("db.properties")) {
            props.clear();
            props.setProperty("server", "");
            props.setProperty("port", "");
            props.setProperty("user", "");
            props.setProperty("password", "");
            props.setProperty("base", "");
            props.setProperty("caminho_mysql", "");
            props.setProperty("caminho_winrar", "");
            props.setProperty("caminho_commictagger", "");
            props.store(os, "");
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao salvar o properties", e.getMessage());
            
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static Properties loadProperties() {
        File f = new File("db.properties");
        if (!f.exists())
            createProperties();

        if (props == null)
            props = new Properties();

        try (FileInputStream fs = new FileInputStream("db.properties")) {
            props.load(fs);
            return props;
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao carregar o properties", e.getMessage());
            
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static Properties getProperties() {
        if (props == null)
            loadProperties();
        return props;
    }

    public static String getCaminhoWinrar() {
        return getProperties().getProperty("caminho_winrar");
    }

    // -------------------------------------------------------------------------------------------------

    private static Properties loadSecrets() {
        File f = new File("secrets.properties");

        if (!f.exists()) {
            try (OutputStream os = new FileOutputStream("secrets.properties")) {
                Properties props = new Properties();

                props.setProperty("my_anime_list_client_id", "");
                props.store(os, "");
            } catch (IOException e) {
                Alertas.Tela_Alerta("Erro ao salvar o secrets", e.getMessage());
                
                LOGGER.error(e.getMessage(), e);
            }
        }

        try (FileInputStream fs = new FileInputStream("secrets.properties")) {
            Properties props = new Properties();
            props.load(fs);
            return props;
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao carregar o secrets", e.getMessage());
            
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static Properties getSecrets() {
        if (secrets == null)
            secrets = loadSecrets();
        return secrets;
    }

    public static String getMyAnimeListClient() {
        return getSecrets().getProperty("my_anime_list_client_id");
    }

}
