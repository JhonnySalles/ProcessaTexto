package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.notification.Notificacoes;
import org.jisho.textosJapones.util.processar.ProcessarLegendas;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class LegendasController implements Initializable {

	@FXML
	private AnchorPane apGlobal;

	@FXML
	private StackPane rootStackPane;

	@FXML
	protected AnchorPane root;

	@FXML
	private Label lblLog;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnPesquisar;

	@FXML
	private JFXComboBox<Modo> cbModo;

	@FXML
	private JFXComboBox<Dicionario> cbDicionario;

	@FXML
	private JFXTextArea txtAreaPesquisa;

	@FXML
	private TableView<String> tbFrases;

	@FXML
	private TableColumn<String, String> tcFrase;

	@FXML
	private AnchorPane revisar;

	@FXML
	private TraduzirController traduzirController;

	@FXML
	private ProcessarController processarController;

	@FXML
	private RevisarController revisarController;

	@FXML
	private ProgressBar barraProgresso;

	private RevisarServices service = new RevisarServices();
	private ProcessarLegendas legendas;
	private ObservableList<String> frases;

	@FXML
	private void onBtnProcessar() {
		onBtnPesquisar();

		if (!frases.isEmpty()) {
			if (legendas == null)
				legendas = new ProcessarLegendas(this);

			lblLog.setText("Iniciando o processamento..");
			legendas.processarLegendas(frases);
		} else
			AlertasPopup.AvisoModal(rootStackPane, root, null, "Aviso", "A lista se encontra vazia.");
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
			AlertasPopup.ErroModal(rootStackPane, root, null, "Erro", "Erro ao pesquisar as frases.");
		}
	}

	public Modo getModo() {
		return cbModo.getSelectionModel().getSelectedItem();
	}

	public Dicionario getDicionario() {
		return cbDicionario.getSelectionModel().getSelectedItem();
	}

	public ProgressBar getBarraProgresso() {
		return barraProgresso;
	}

	public Label getLog() {
		return lblLog;
	}

	public AnchorPane getRoot() {
		return root;
	}

	public StackPane getStackPane() {
		return rootStackPane;
	}

	private void editaColunas() {
		tcFrase.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
	}

	private void linkaCelulas() {
		editaColunas();
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		linkaCelulas();

		traduzirController.setControllerPai(this);
		processarController.setControllerPai(this);

		cbModo.getItems().addAll(Modo.values());
		cbModo.getSelectionModel().select(Modo.C);

		cbDicionario.getItems().addAll(Dicionario.values());
		cbDicionario.getSelectionModel().select(Dicionario.FULL);
		
		revisarController.setAnime(true);
		revisarController.setManga(false);

		/* Setando as variáveis para o alerta padrão. */
		AlertasPopup.setRootStackPane(rootStackPane);
		AlertasPopup.setNodeBlur(root);
		Notificacoes.setRootStackPane(apGlobal);
	}

	public static URL getFxmlLocate() {
		return LegendasController.class.getResource("/view/Legendas.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
