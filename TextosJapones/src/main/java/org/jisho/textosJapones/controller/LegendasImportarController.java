package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.processar.ProcessarLegendas;

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

public class LegendasImportarController implements Initializable {

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

	private RevisarServices service = new RevisarServices();
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

		if (!frases.isEmpty()) {
			if (legendas == null)
				legendas = new ProcessarLegendas(this);

			// lblLog.setText("Iniciando o processamento..");
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
			e.printStackTrace();
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
		return LegendasImportarController.class.getResource("/view/LegendasImportar.fxml");
	}

}
