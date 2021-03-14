package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.ProcessarServices;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.processar.ProcessarLegendas;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;

public class ProcessarController implements Initializable {

	@FXML
	private AnchorPane apRoot;

	@FXML
	private JFXButton btnExclusao;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private JFXButton btnAtualizar;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnProcessarTudo;

	@FXML
	private JFXTextArea txtAreaSelect;

	@FXML
	private JFXTextArea txtAreaUpdate;

	@FXML
	private JFXTextArea txtAreaVocabulario;

	@FXML
	private TableView<Processar> tbLista;

	@FXML
	private TableColumn<Processar, String> tcId;

	@FXML
	private TableColumn<Processar, String> tcOriginal;

	@FXML
	private TableColumn<Processar, String> tcVocabulario;

	private ProcessarServices service = new ProcessarServices();
	private LegendasController controller;
	private ProcessarLegendas processar = new ProcessarLegendas(null);

	public void setControllerPai(LegendasController controller) {
		this.controller = controller;
	}

	@FXML
	private void onBtnSalvar() {
		if (txtAreaUpdate.getText().trim().isEmpty()
				|| txtAreaUpdate.getText().equalsIgnoreCase("UPDATE tabela SET campo3 = ? WHERE id = ?")) {
			AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta",
					"Necessário informar um update para prosseguir com o salvamento.");
			return;
		}

		try {
			controller.getLog().setText("Iniciando salvamento.");

			btnSalvar.setDisable(true);
			btnAtualizar.setDisable(true);
			btnProcessar.setDisable(true);
			btnProcessarTudo.setDisable(true);
			btnExclusao.setDisable(true);

			List<Processar> update = tbLista.getItems().stream()
					.filter(revisar -> !revisar.getVocabulario().trim().isEmpty()).collect(Collectors.toList());
			service.update(txtAreaUpdate.getText(), update);

			AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Salvo",
					"Salvo com sucesso.");
			onBtnAtualizar();

		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
					"Erro ao salvar as atualizações.");
		} finally {
			controller.getLog().setText("Salvamento concluido.");

			btnSalvar.setDisable(false);
			btnAtualizar.setDisable(false);
			btnProcessar.setDisable(false);
			btnProcessarTudo.setDisable(false);
			btnExclusao.setDisable(false);
		}
	}

	@FXML
	private void onBtnAtualizar() {
		if (txtAreaSelect.getText().trim().isEmpty())
			return;

		try {
			controller.getLog().setText("Atualizando....");
			tbLista.setItems(FXCollections.observableArrayList(service.select(txtAreaSelect.getText())));
			controller.getLog().setText("");
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
					"Erro ao realizar a pesquisa.");
		}
	}

	private String getVocabulario(Dicionario dicionario, Modo modo, String palavra) {
		return processar.processarVocabulario(dicionario, modo, palavra);
	}

	@FXML
	private void onBtnProcessar() {
		if (tbLista.getItems().isEmpty() || tbLista.getSelectionModel().getSelectedItem() == null)
			return;

		if (tbLista.getSelectionModel().getSelectedItem().getVocabulario().isEmpty()) {
			processar.vocabulario.clear();

			tbLista.getSelectionModel().getSelectedItem().setVocabulario(getVocabulario(controller.getDicionario(),
					controller.getModo(), tbLista.getSelectionModel().getSelectedItem().getOriginal()));

			txtAreaVocabulario.setText(processar.vocabulario.stream().collect(Collectors.joining("\n")));
		}
		tbLista.refresh();
	}

	private static Boolean desativar = false;

	@FXML
	private void onBtnProcessarTudo() {
		if (btnProcessarTudo.getAccessibleText().equalsIgnoreCase("PROCESSANDO")) {
			desativar = true;
			return;
		}

		if (tbLista.getItems().isEmpty())
			return;

		btnSalvar.setDisable(true);
		btnAtualizar.setDisable(true);
		btnProcessar.setDisable(true);
		btnExclusao.setDisable(true);
		tbLista.setDisable(true);

		btnProcessarTudo.setAccessibleText("PROCESSANDO");
		btnProcessarTudo.setText("Pausar");

		txtAreaVocabulario.setText("");

		desativar = false;

		processar.vocabulario.clear();

		Task<Void> processarTudo = new Task<Void>() {
			List<Processar> lista = null;
			Dicionario dicionario = controller.getDicionario();
			Modo modo = controller.getModo();
			Integer i = 0;

			@Override
			public Void call() throws IOException, InterruptedException {
				lista = new ArrayList<Processar>(tbLista.getItems());

				try {
					for (Processar item : lista) {

						i++;
						updateMessage("Processando item " + i + " de " + lista.size());
						updateProgress(i, lista.size());

						Platform.runLater(() -> {
							if (TaskbarProgressbar.isSupported())
								TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, lista.size(),
										Type.NORMAL);
						});

						if (item.getVocabulario().isEmpty())
							item.setVocabulario(getVocabulario(dicionario, modo, item.getOriginal()));

						if (desativar)
							break;
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Platform.runLater(() -> tbLista.setItems(FXCollections.observableArrayList(lista)));

					Platform.runLater(() -> {
						btnProcessarTudo.setAccessibleText("PROCESSAR");
						btnProcessarTudo.setText("Processar tudo");

						btnSalvar.setDisable(false);
						btnAtualizar.setDisable(false);
						btnProcessar.setDisable(false);
						btnExclusao.setDisable(false);
						tbLista.setDisable(false);

						controller.getLog().textProperty().unbind();
						controller.getBarraProgresso().progressProperty().unbind();

						TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
						txtAreaVocabulario.setText(processar.vocabulario.stream().collect(Collectors.joining("\n")));

						tbLista.refresh();
					});
				}
				return null;
			}
		};

		Thread processa = new Thread(processarTudo);
		controller.getLog().textProperty().bind(processarTudo.messageProperty());
		controller.getBarraProgresso().progressProperty().bind(processarTudo.progressProperty());
		processa.start();
	}

	@FXML
	private void onBtnEclusao() {
		if (txtAreaVocabulario.getText().isEmpty())
			return;

		try {
			service.exclusao(new ArrayList<String>(Arrays.asList(txtAreaVocabulario.getText().split("\n"))));
			txtAreaVocabulario.setText("");
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
					"Erro ao salvar a exclusao.");
		}
	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	private void editaColunas() {
		tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tcOriginal.setCellValueFactory(new PropertyValueFactory<>("original"));
		tcVocabulario.setCellValueFactory(new PropertyValueFactory<>("vocabulario"));

		tcOriginal.setCellFactory(TextFieldTableCell.forTableColumn());

		tcVocabulario.setCellFactory(TextFieldTableCell.forTableColumn());
		tcVocabulario.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setVocabulario(e.getNewValue().trim());
			tbLista.refresh();
			tbLista.requestFocus();
		});

	}

	private void linkaCelulas() {
		editaColunas();
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		linkaCelulas();
		btnProcessarTudo.setAccessibleText("PROCESSAR");
	}

	public static URL getFxmlLocate() {
		return ProcessarController.class.getResource("/view/Processar.fxml");
	}

}
