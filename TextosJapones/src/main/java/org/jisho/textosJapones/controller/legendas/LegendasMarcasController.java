package org.jisho.textosJapones.controller.legendas;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LegendasMarcasController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegendasMarcasController.class);

    @FXML
    private AnchorPane apRoot;

    @FXML
    private JFXTextArea txtAreaOriginal;

    @FXML
    private JFXTextArea txtAreaProcessado;

    @FXML
    private JFXTextField txtPipe;

    private LegendasController controller;

    public void setControllerPai(LegendasController controller) {
        this.controller = controller;
    }

    public LegendasController getControllerPai() {
        return controller;
    }

    @FXML
    private void onBtnLimpar() {
        txtAreaOriginal.setText("");
        txtAreaProcessado.setText("");
    }

    private String getMarca(String linha) {
        if (linha == null || linha.isEmpty())
            return "";

        String[] itens;
        if (txtPipe.getText() == null || txtPipe.getText().isEmpty())
            itens = linha.split("\t");
        else
            itens = linha.split(txtPipe.getText());

        String tempo = itens[0].trim();
        tempo = tempo.substring(tempo.lastIndexOf(" ")).trim();
        String nome = itens[1].trim();
        nome = nome.replaceAll("\n", " ");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime time = LocalTime.parse(tempo, formatter);
        LocalTime nextTime = time.plusSeconds(3);

        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss.S")) + " " + nextTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.S")) + " " + nome + "\n";
    }

    @FXML
    private void onBtnGerar() {
        if (txtAreaOriginal.getText() == null || txtAreaOriginal.getText().isEmpty())
            return;

        try {
            String marcas = "";

            if (txtAreaOriginal.getText().contains("\n")) {
                for (String linha : txtAreaOriginal.getText().split("\n"))
                    marcas += getMarca(linha);
            } else
                marcas += getMarca(txtAreaOriginal.getText());

            txtAreaProcessado.setText("start_region_table\n" + marcas + "end_region_table");
        } catch (Exception ex) {
            LOGGER.error("Erro ao processar marcas.", ex);
        }
    }

    private final Robot robot = new Robot();

    public void initialize(URL arg0, ResourceBundle arg1) {
        txtAreaOriginal.focusedProperty().addListener((options, oldValue, newValue) -> {
            if (oldValue)
                onBtnGerar();
        });

        txtPipe.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });
    }

    public static URL getFxmlLocate() {
        return LegendasMarcasController.class.getResource("/view/legendas/LegendasMarcas.fxml");
    }

}
