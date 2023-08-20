package org.jisho.textosJapones.controller.legendas;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarJaponesServices;
import org.jisho.textosJapones.processar.ProcessarLegendas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class LegendasImportarController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegendasImportarController.class);

    @FXML
    private AnchorPane apRoot;

    @FXML
    private JFXButton btnProcessar;

    @FXML
    private JFXButton btnPesquisar;

    @FXML
    private JFXTextArea txtAreaPesquisa;

    @FXML
    private TableView<String> tbFrases;

    @FXML
    private TableColumn<String, String> tcFrase;

    private final RevisarJaponesServices service = new RevisarJaponesServices();
    private ProcessarLegendas legendas;
    private ObservableList<String> frases;

    private LegendasController controller;

    public void setControllerPai(LegendasController controller) {
        this.controller = controller;
    }

    public LegendasController getControllerPai() {
        return controller;
    }

    @FXML
    private void onBtnProcessar() {
        onBtnPesquisar();

        if (frases != null && !frases.isEmpty()) {
            if (legendas == null)
                legendas = new ProcessarLegendas(this);

            MenuPrincipalController.getController().getLblLog().setText("Iniciando o processamento das legendas...");
            legendas.processarLegendas(frases);
        } else
            AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Aviso",
                    "A lista se encontra vazia.");
    }

    @FXML
    private void onBtnPesquisar() {
        try {
            if (!txtAreaPesquisa.getText().isEmpty()) {
                frases = FXCollections.observableArrayList(service.selectFrases(txtAreaPesquisa.getText()));
                tbFrases.setItems(frases);
            }
        } catch (ExcessaoBd e) {
            
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
                    "Erro ao pesquisar as frases.");
        }
    }

    private void editaColunas() {
        tcFrase.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
    }

    private void linkaCelulas() {
        editaColunas();
    }

    public void initialize(URL arg0, ResourceBundle arg1) {
        linkaCelulas();

    }

    public static URL getFxmlLocate() {
        return LegendasImportarController.class.getResource("/view/legendas/LegendasImportar.fxml");
    }

}
