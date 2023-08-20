package org.jisho.textosJapones.util;

import org.jisho.textosJapones.components.notification.Alertas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Prop {

    private static final Logger LOGGER = LoggerFactory.getLogger(Prop.class);

    public static java.util.Properties loadProperties() {
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            java.util.Properties props = new java.util.Properties();
            props.load(fs);
            return props;
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao carregar o properties", e.getMessage());
            
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    public static void save(java.util.Properties propertie) {
        try (OutputStream os = new FileOutputStream("db.properties")) {
            propertie.store(os, "");
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao salvar o properties", e.getMessage());
            
            LOGGER.error(e.getMessage(), e);
        }
    }
}
