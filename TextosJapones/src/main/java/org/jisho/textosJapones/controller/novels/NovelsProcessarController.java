package org.jisho.textosJapones.controller.novels;

import com.jfoenix.controls.*;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.notification.Alertas;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.NovelServices;
import org.jisho.textosJapones.processar.ProcessarMangas;
import org.jisho.textosJapones.processar.ProcessarNovels;
import org.jisho.textosJapones.util.configuration.Configuracao;
import org.jisho.textosJapones.util.constraints.Validadores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class NovelsProcessarController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NovelsProcessarController.class);

    @FXML
    protected AnchorPane apRoot;

    @FXML
    private JFXButton btnProcessar;

    @FXML
    private JFXComboBox<String> cbBase;

    @FXML
    private JFXTextField txtNovel;

    @FXML
    private JFXCheckBox ckbFavorito;

    @FXML
    private JFXComboBox<Language> cbLinguagem;

    @FXML
    private JFXTextField txtCaminho;

    @FXML
    private JFXButton btnCaminho;

    @FXML
    private JFXButton btnArquivo;

    @FXML
    private JFXTextArea txtLog;

    @FXML
    private ProgressBar barraProgressoArquivos;

    @FXML
    private ProgressBar barraProgressoTextos;

    private ProcessarNovels novels;

    private NovelServices service = new NovelServices();

    private NovelsController controller;

    public void setControllerPai(NovelsController controller) {
        this.controller = controller;
    }

    public NovelsController getControllerPai() {
        return controller;
    }

    @FXML
    private void onBtnProcessar() {
        if (btnProcessar.getAccessibleText().equalsIgnoreCase("PROCESSANDO") && novels != null) {
            novels.setDesativar(true);
            return;
        }

        if (!valida())
            return;

        desabilitar();
        saveConfig();

        if (novels == null)
            novels = new ProcessarNovels(this);

        MenuPrincipalController.getController().getLblLog().setText("Iniciando o processamento das novels...");

        novels.processarArquivos(new File(txtCaminho.getText()), cbBase.getEditor().getText(), cbLinguagem.getSelectionModel().getSelectedItem(), ckbFavorito.isSelected());
    }

    public boolean valida() {
        if (txtCaminho.getText().isEmpty()) {
            txtCaminho.setUnFocusColor(Color.RED);
            return false;
        }

        File caminho = new File(txtCaminho.getText());
        if (!caminho.exists()) {
            txtCaminho.setUnFocusColor(Color.RED);
            return false;
        }

        return true;
    }

    public void desabilitar() {
        btnProcessar.setAccessibleText("PROCESSANDO");
        btnProcessar.setText("Pausar");
    }

    public void habilitar() {
        MenuPrincipalController.getController().getLblLog().setText("");
        btnProcessar.setAccessibleText("PROCESSAR");
        btnProcessar.setText("Processar");
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
        getBarraProgressoArquivos().setProgress(0);
        getBarraProgressoTextos().setProgress(0);
    }

    @FXML
    private void onBtnCarregarCaminho() {
        txtCaminho.setUnFocusColor(Color.web("#106ebe"));
        txtCaminho.setText(selecionaPasta(txtCaminho.getText(), false));
    }

    @FXML
    private void onBtnCarregarArquivo() {
        txtCaminho.setUnFocusColor(Color.web("#106ebe"));
        txtCaminho.setText(selecionaPasta(txtCaminho.getText(), true));
    }

    private String selecionaPasta(String local, Boolean isArquivo) {
        String pasta = "";
        File caminho = null;

        if (local != null && !local.isEmpty()) {
            caminho = new File(local);

            if (caminho.isFile()) {
                String file = caminho.getAbsolutePath();
                file = file.substring(0, file.indexOf(caminho.getName()));
                caminho = new File(file);
            }
        }

        if (isArquivo) {
            FileChooser fileChooser = new FileChooser();

            if (caminho != null)
                fileChooser.setInitialDirectory(caminho);

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Arquivos", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setTitle("Selecione o arquivo de destino");

            File file = fileChooser.showOpenDialog(null);
            pasta = file == null ? "" : file.getAbsolutePath();
        } else {
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setTitle("Selecione a pasta de destino");

            if (caminho != null)
                fileChooser.setInitialDirectory(caminho);

            File file = fileChooser.showDialog(null);
            pasta = file == null ? "" : file.getAbsolutePath();
        }

        return pasta;
    }

    private static String CONFIG = "processa.config";
    public void saveConfig() {
        if (txtCaminho.getText() == null || txtCaminho.getText().trim().isEmpty())
            return;

        File caminho = new File(txtCaminho.getText());
        if (!caminho.exists())
            return;

        File pasta = null;

        if (caminho.isFile())
            pasta = new File((caminho.getParentFile() + "\\" + CONFIG).replaceAll("\\\\\\\\", "\\"));
        else
            pasta = new File((caminho.getPath() + "\\" + CONFIG).replaceAll("\\\\\\\\", "\\"));


        Properties props = new Properties();
        try (OutputStream os = new FileOutputStream(pasta)) {
            props.clear();
            props.setProperty("base", cbBase.getEditor().getText());
            props.setProperty("linguagem", cbLinguagem.getSelectionModel().getSelectedItem().toString());
            props.setProperty("favorito", ckbFavorito.isSelected() ? "sim" : "nao");
            props.setProperty("novel", txtNovel.getText());
            props.store(os, "");
        } catch (IOException e) {
            Alertas.Tela_Alerta("Erro ao salvar o properties de configuração", e.getMessage());
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void loadConfig() {
        if (txtCaminho.getText() == null || txtCaminho.getText().trim().isEmpty() || !new File(txtCaminho.getText()).exists())
            return;

        File config = new File((txtCaminho.getText() + "\\" + CONFIG).replaceAll("\\\\\\\\", "\\"));
        if (config.exists()) {
            Properties props = new Properties();
            try (FileInputStream fs = new FileInputStream(config)) {
                props.load(fs);
                cbBase.getEditor().setText(props.getProperty("base"));
                cbLinguagem.getSelectionModel().select(Language.valueOf(props.getProperty("linguagem")));
                ckbFavorito.setSelected(props.getProperty("favorito").equalsIgnoreCase("sim"));
                txtNovel.setText(props.getProperty("novel"));
                cbBase.requestFocus();
            } catch (IOException e) {
                Alertas.Tela_Alerta("Erro ao carregar o properties de configuração", e.getMessage());
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public void addLog(String text) {
        txtLog.appendText(text + "\n");
    }

    public AnchorPane getRoot() {
        return apRoot;
    }

    public ProgressBar getBarraProgressoArquivos() {
        return barraProgressoArquivos;
    }

    public ProgressBar getBarraProgressoTextos() {
        return barraProgressoTextos;
    }

    private final Robot robot = new Robot();

    public void initialize(URL arg0, ResourceBundle arg1) {
        cbLinguagem.getItems().addAll(Language.JAPANESE, Language.ENGLISH);
        cbLinguagem.getSelectionModel().selectFirst();

        try {
            cbBase.getItems().setAll(service.getTabelas());
        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal("Erro ao carregar as tabelas", e.getMessage());
        }

        JFXAutoCompletePopup<String> autoCompletePopup = new JFXAutoCompletePopup<>();
        autoCompletePopup.getSuggestions().addAll(cbBase.getItems());

        autoCompletePopup.setSelectionHandler(event -> {
            cbBase.setValue(event.getObject());
        });

        cbBase.getEditor().textProperty().addListener(observable -> {
            autoCompletePopup.filter(item -> item.toLowerCase().contains(cbBase.getEditor().getText().toLowerCase()));
            if (autoCompletePopup.getFilteredSuggestions().isEmpty() || cbBase.showingProperty().get()
                    || cbBase.getEditor().getText().isEmpty())
                autoCompletePopup.hide();
            else
                autoCompletePopup.show(cbBase.getEditor());
        });

        cbBase.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        cbBase.focusedProperty().addListener((options, oldValue, newValue) -> {
            if (oldValue) {
                String base = cbBase.getEditor().getText();
                if (base != null) {
                    if (base.contains(" "))
                        cbBase.getEditor().setText(base.replaceAll(" ", "_").toLowerCase());
                    else
                        cbBase.getEditor().setText(base.toLowerCase());
                }
            }
        });

        txtNovel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        txtCaminho.focusedProperty().addListener((options, oldValue, newValue) -> {
            if (oldValue)
                loadConfig();
        });

        Validadores.setTextFieldNotEmpty(txtCaminho);
    }

    public static URL getFxmlLocate() {
        return NovelsProcessarController.class.getResource("/view/novels/NovelProcessar.fxml");
    }
}
