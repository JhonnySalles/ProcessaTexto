package org.jisho.textosJapones.controller.novels;

import com.jfoenix.controls.*;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.NovelServices;
import org.jisho.textosJapones.processar.ProcessarNovels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
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
    private ProgressBar barraProgressoArquivos;

    @FXML
    private ProgressBar barraProgressoCapitulos;

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
        if (btnProcessar.getAccessibleText().equalsIgnoreCase("PROCESSANDO")) {
            //mangas.setDesativar(true);
            return;
        }

        btnProcessar.setAccessibleText("PROCESSANDO");
        btnProcessar.setText("Pausar");
    }

    public void habilitar() {
        MenuPrincipalController.getController().getLblLog().setText("");
        btnProcessar.setAccessibleText("PROCESSAR");
        btnProcessar.setText("Processar");
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
        getBarraProgressoArquivos().setProgress(0);
        getBarraProgressoCapitulos().setProgress(0);
    }

    @FXML
    private void onBtnCarregarCaminho() {
        txtCaminho.setText(selecionaPasta(txtCaminho.getText(), false));
    }

    @FXML
    private void onBtnCarregarArquivo() {
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

    public AnchorPane getRoot() {
        return apRoot;
    }

    public ProgressBar getBarraProgressoArquivos() {
        return barraProgressoArquivos;
    }

    public ProgressBar getBarraProgressoCapitulos() {
        return barraProgressoCapitulos;
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

        txtNovel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });
    }

    public static URL getFxmlLocate() {
        return NovelsProcessarController.class.getResource("/view/novels/NovelProcessar.fxml");
    }
}
