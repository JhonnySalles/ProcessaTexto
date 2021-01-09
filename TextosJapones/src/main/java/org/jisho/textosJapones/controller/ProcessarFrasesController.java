package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.notification.Alertas;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class ProcessarFrasesController implements Initializable {

	@FXML
	private AnchorPane apGlobal;

	@FXML
	private StackPane rootStackPane;

	@FXML
	protected AnchorPane root;

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
	private JFXButton btnSalvar;

	@FXML
	private JFXButton btnAtualizar;

	@FXML
	private TableView<Revisar> tbVocabulario;

	@FXML
	private TableColumn<Revisar, String> tcVocabulario;

	@FXML
	private TableColumn<Revisar, String> tcIngles;

	@FXML
	private TableColumn<Revisar, String> tcTraducao;

	@FXML
	private TableColumn<Revisar, CheckBox> tcRevisado;

	@FXML
	private ProgressBar barraProgresso;

	private ObservableList<Revisar> revisar;
	private RevisarServices service = new RevisarServices();
	private ProcessarLegendas legendas;
	private ObservableList<String> frases;

	@FXML
	private void onBtnProcessar() {
		onBtnPesquisar();

		if (!frases.isEmpty()) {
			if (legendas == null)
				legendas = new ProcessarLegendas(this);

			legendas.processarLegendas(frases);
		} else
			Alertas.AvisoModal(rootStackPane, root, null, "Aviso", "A lista se encontra vazia.");
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
			Alertas.ErroModal(rootStackPane, root, null, "Erro", "Erro ao pesquisar as frases.");
		}
	}

	@FXML
	private void onBtnSalvar() {
		try {
			btnSalvar.setDisable(true);
			btnAtualizar.setDisable(true);
			tbVocabulario.setDisable(true);

			List<Revisar> update = revisar.stream().filter(revisar -> revisar.getRevisado().isSelected())
					.collect(Collectors.toList());
			List<Vocabulario> salvar = update.stream().map(revisar -> Revisar.toVocabulario(revisar))
					.collect(Collectors.toList());

			service.insertOrUpdate(update);

			VocabularioServices service = new VocabularioServices();
			service.insertOrUpdate(salvar);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			Alertas.ErroModal(rootStackPane, root, null, "Erro", "Erro ao salvar as atualizações.");
		} finally {
			btnSalvar.setDisable(false);
			btnAtualizar.setDisable(false);
			tbVocabulario.setDisable(false);
			Alertas.AvisoModal(rootStackPane, root, null, "Salvo", "Salvo com sucesso.");
			onBtnAtualizar();
		}
	}

	@FXML
	private void onBtnAtualizar() {
		try {
			revisar = FXCollections.observableArrayList(service.selectRevisar());
			tbVocabulario.setItems(revisar);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			Alertas.ErroModal(rootStackPane, root, null, "Erro", "Erro ao pesquisar as revisões.");
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

	public AnchorPane getRoot() {
		return root;
	}

	public StackPane getStackPane() {
		return rootStackPane;
	}

	private void editaColunas() {
		tcVocabulario.setCellValueFactory(new PropertyValueFactory<>("vocabulario"));
		tcIngles.setCellValueFactory(new PropertyValueFactory<>("ingles"));
		tcTraducao.setCellValueFactory(new PropertyValueFactory<>("traducao"));

		tcVocabulario.setCellFactory(TextFieldTableCell.forTableColumn());

		tcIngles.setCellFactory(TextFieldTableCell.forTableColumn());
		tcIngles.setOnEditCommit(e -> {
			if (!e.getNewValue().trim().isEmpty()) {
				e.getTableView().getItems().get(e.getTablePosition().getRow()).setIngles(e.getNewValue().trim());
				e.getTableView().getItems().get(e.getTablePosition().getRow()).getRevisado().setSelected(true);
				tbVocabulario.requestFocus();
			}
		});
		
		tcTraducao.setCellFactory(TextFieldTableCell.forTableColumn());
		tcTraducao.setOnEditCommit(e -> {
			if (!e.getNewValue().trim().isEmpty()) {
				e.getTableView().getItems().get(e.getTablePosition().getRow()).setTraducao(e.getNewValue().trim());
				e.getTableView().getItems().get(e.getTablePosition().getRow()).getRevisado().setSelected(true);
				tbVocabulario.requestFocus();
			}
		});

		tcRevisado.setCellValueFactory(new PropertyValueFactory<>("revisado"));

		tcFrase.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
	}

	private void linkaCelulas() {
		editaColunas();
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		linkaCelulas();

		cbModo.getItems().addAll(Modo.values());
		cbModo.getSelectionModel().select(Modo.C);

		cbDicionario.getItems().addAll(Dicionario.values());
		cbDicionario.getSelectionModel().select(Dicionario.FULL);

		/* Setando as variáveis para o alerta padrão. */
		Alertas.setRootStackPane(rootStackPane);
		Alertas.setNodeBlur(root);
		Notificacoes.setRootStackPane(apGlobal);
	}

	public static URL getFxmlLocate() {
		return ProcessarFrasesController.class.getResource("/org/jisho/textosJapones/view/ProcessarFrases.fxml");
	}

	public static String getIconLocate() {
		return "/org/jisho/textosJapones/resources/images/icoTextoJapones_128.png";
	}

}
