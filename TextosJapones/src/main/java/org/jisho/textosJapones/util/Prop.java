package org.jisho.textosJapones.util;

import org.jisho.textosJapones.components.notification.Alertas;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Prop {

    public static java.util.Properties loadProperties() {
        try (FileInputStream fs = new FileInputStream("db.properties")) {
            java.util.Properties props = new java.util.Properties();
            props.load(fs);
            return props;
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao carregar o properties", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void save(java.util.Properties propertie) {
        try (OutputStream os = new FileOutputStream("db.properties")) {
            propertie.store(os, "");
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao salvar o properties", e.getMessage());
            e.printStackTrace();
        }
    }
}
