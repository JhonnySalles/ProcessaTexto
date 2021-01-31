package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.notification.Notificacoes;
import org.jisho.textosJapones.util.processar.ProcessarLegendas;
import org.jisho.textosJapones.util.processar.TanoshiJapanese;
import org.jisho.textosJapones.util.scriptGoogle.ScriptGoogle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
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
	private JFXComboBox<Api> cbContaGoolge;

	@FXML
	private JFXButton btnProcessarTudo;

	@FXML
	private JFXButton btnTraduzir;

	@FXML
	private JFXButton btnJapaneseTanoshi;

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

	@FXML
	private void onBtnSalvar() {
		try {
			lblLog.setText("Iniciando salvamento.");

			btnSalvar.setDisable(true);
			btnAtualizar.setDisable(true);
			tbVocabulario.setDisable(true);

			btnProcessarTudo.setDisable(true);
			btnTraduzir.setDisable(true);
			btnJapaneseTanoshi.setDisable(true);

			List<Revisar> update = tbVocabulario.getItems().stream()
					.filter(revisar -> revisar.getRevisado().isSelected()).collect(Collectors.toList());
			//List<Vocabulario> salvar = update.stream().map(revisar -> Revisar.toVocabulario(revisar))
				//	.collect(Collectors.toList());

			service.insertOrUpdate(update);

			//VocabularioServices service = new VocabularioServices();
			//service.insertOrUpdate(salvar);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal(rootStackPane, root, null, "Erro", "Erro ao salvar as atualizações.");
		} finally {
			lblLog.setText("Salvamento concluido.");

			btnSalvar.setDisable(false);
			btnAtualizar.setDisable(false);
			tbVocabulario.setDisable(false);

			btnProcessarTudo.setDisable(false);
			btnTraduzir.setDisable(false);
			btnJapaneseTanoshi.setDisable(false);

			AlertasPopup.AvisoModal(rootStackPane, root, null, "Salvo", "Salvo com sucesso.");
			onBtnAtualizar();
		}
	}

	@FXML
	private void onBtnAtualizar() {
		try {
			lblLog.setText("Atualizando....");
			tbVocabulario.setItems(FXCollections.observableArrayList(service.selectRevisar()));

			lblLog.setText("");
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal(rootStackPane, root, null, "Erro", "Erro ao pesquisar as revisões.");
		}
	}

	private static Boolean desativar = false;

	@FXML
	private void onBtnProcessarTudo() {
		if (btnProcessarTudo.getAccessibleText().equalsIgnoreCase("PROCESSANDO")) {
			desativar = true;
			return;
		}

		if (tbVocabulario.getItems().isEmpty())
			return;

		btnSalvar.setDisable(true);
		btnAtualizar.setDisable(true);
		tbVocabulario.setDisable(true);
		btnTraduzir.setDisable(true);
		btnJapaneseTanoshi.setDisable(true);
		
		btnProcessarTudo.setAccessibleText("PROCESSANDO");
		btnProcessarTudo.setText("Pausar");
		
		desativar = false;

		Task<Void> processarTudo = new Task<Void>() {
			List<Revisar> lista = null;

			@Override
			public Void call() throws IOException, InterruptedException {

				lista = new ArrayList<Revisar>(tbVocabulario.getItems());

				try {

					int i = 0;
					for (Revisar item : lista) {

						i++;
						updateMessage("Processando item " + i + " de " + lista.size());
						updateProgress(i, lista.size());

						if (!item.getRevisado().isSelected()) {

							if (item.getIngles().isEmpty())
								item.setIngles(TanoshiJapanese.processa(item.getVocabulario()));

							if (item.getTraducao().isEmpty()) {
								try {
									item.setTraducao(ScriptGoogle.translate(Language.ENGLISH.getSigla(),
											Language.PORTUGUESE.getSigla(), item.getIngles(),
											cbContaGoolge.getValue()));
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
							item.getRevisado().setSelected(true);

							// TimeUnit.MILLISECONDS.sleep(400);

							if (desativar)
								return null;
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Platform.runLater(() -> tbVocabulario.setItems(FXCollections.observableArrayList(lista)));

					Platform.runLater(() -> {
						btnProcessarTudo.setAccessibleText("PROCESSAR");
						btnProcessarTudo.setText("Processar tudo");
						
						btnSalvar.setDisable(false);
						btnAtualizar.setDisable(false);
						tbVocabulario.setDisable(false);
						btnTraduzir.setDisable(false);
						btnJapaneseTanoshi.setDisable(false);
						
						lblLog.textProperty().unbind();
						barraProgresso.progressProperty().unbind();
						
						tbVocabulario.refresh();
					});
				}
				return null;
			}
		};

		Thread processa = new Thread(processarTudo);
		lblLog.textProperty().bind(processarTudo.messageProperty());
		barraProgresso.progressProperty().bind(processarTudo.progressProperty());
		processa.start();
	}

	@FXML
	private void onBtnTraduzir() {
		if (tbVocabulario.getItems().isEmpty())
			return;

		if (tbVocabulario.getSelectionModel().getSelectedItem() == null
				|| tbVocabulario.getSelectionModel().getSelectedItem().getIngles().isEmpty())
			return;

		try {
			tbVocabulario.getSelectionModel().getSelectedItem()
					.setTraducao(ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(),
							tbVocabulario.getSelectionModel().getSelectedItem().getIngles(), cbContaGoolge.getValue()));

			tbVocabulario.refresh();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void onBtnJapaneseTanoshi() {
		if (tbVocabulario.getItems().isEmpty() || tbVocabulario.getSelectionModel().getSelectedItem() == null)
			return;

		tbVocabulario.getSelectionModel().getSelectedItem().setIngles(
				TanoshiJapanese.processa(tbVocabulario.getSelectionModel().getSelectedItem().getVocabulario()));

		tbVocabulario.refresh();
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

	public Label getLabel() {
		return lblLog;
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

		cbContaGoolge.getItems().addAll(Api.values());
		cbContaGoolge.getSelectionModel().selectFirst();

		/* Setando as variáveis para o alerta padrão. */
		AlertasPopup.setRootStackPane(rootStackPane);
		AlertasPopup.setNodeBlur(root);
		Notificacoes.setRootStackPane(apGlobal);
		
		btnProcessarTudo.setAccessibleText("PROCESSAR");
	}

	public static URL getFxmlLocate() {
		return LegendasController.class.getResource("/org/jisho/textosJapones/view/Legendas.fxml");
	}

	public static String getIconLocate() {
		return "/org/jisho/textosJapones/resources/images/icoTextoJapones_128.png";
	}

}
