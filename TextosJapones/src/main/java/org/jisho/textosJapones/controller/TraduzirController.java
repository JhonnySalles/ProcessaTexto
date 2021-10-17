package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.processar.JapanDict;
import org.jisho.textosJapones.util.processar.Jisho;
import org.jisho.textosJapones.util.processar.ProcessarPalavra;
import org.jisho.textosJapones.util.processar.Tangorin;
import org.jisho.textosJapones.util.processar.TanoshiJapanese;
import org.jisho.textosJapones.util.scriptGoogle.ScriptGoogle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTextField;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;

public class TraduzirController implements Initializable {

	@FXML
	private AnchorPane apRoot;
	
	@FXML
	private JFXTextField txtQuantidadeRegistros;

	@FXML
	private JFXButton btnProcessarTudo;

	@FXML
	private JFXButton btnTraduzir;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private JFXButton btnAtualizar;

	@FXML
	private JFXButton btnJapaneseTanoshi;

	@FXML
	private JFXButton btnJapanDict;

	@FXML
	private JFXButton btnJisho;

	@FXML
	private JFXCheckBox ckbDesmembrar;

	@FXML
	private JFXCheckBox ckbMarcarTodos;

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

	private RevisarServices service = new RevisarServices();
	private LegendasController controller;
	private ProcessarPalavra processar = new ProcessarPalavra();

	public void setControllerPai(LegendasController controller) {
		this.controller = controller;
	}

	@FXML
	private void onBtnSalvar() {
		try {
			MenuPrincipalController.getController().getLblLog().setText("Salvando....");

			btnSalvar.setDisable(true);
			btnAtualizar.setDisable(true);
			tbVocabulario.setDisable(true);

			btnProcessarTudo.setDisable(true);
			btnTraduzir.setDisable(true);

			btnJapaneseTanoshi.setDisable(true);
			btnJapanDict.setDisable(true);
			btnJisho.setDisable(true);

			List<Revisar> update = tbVocabulario.getItems().stream()
					.filter(revisar -> revisar.getRevisado().isSelected()).collect(Collectors.toList());
			// List<Vocabulario> salvar = update.stream().map(revisar ->
			// Revisar.toVocabulario(revisar))
			// .collect(Collectors.toList());

			service.insertOrUpdate(update);

			// VocabularioServices service = new VocabularioServices();
			// service.insertOrUpdate(salvar);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal("Erro",	"Erro ao salvar as atualizações.");
		} finally {
			MenuPrincipalController.getController().getLblLog().setText("Salvamento concluido.");

			btnSalvar.setDisable(false);
			btnAtualizar.setDisable(false);
			tbVocabulario.setDisable(false);

			btnProcessarTudo.setDisable(false);
			btnTraduzir.setDisable(false);

			btnJapaneseTanoshi.setDisable(false);
			btnJapanDict.setDisable(false);
			btnJisho.setDisable(false);

			AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Salvo",
					"Salvo com sucesso.");
			onBtnAtualizar();
		}
	}

	@FXML
	private void onBtnAtualizar() {
		try {
			MenuPrincipalController.getController().getLblLog().setText("Atualizando.....");
			tbVocabulario.setItems(FXCollections.observableArrayList(service.selectTraduzir(Integer.valueOf(txtQuantidadeRegistros.getText()))));

			MenuPrincipalController.getController().getLblLog().setText("");
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal("Erro", "Erro ao pesquisar as revisões.");
		}
	}

	@FXML
	private void onBtnMarcarTodos() {
		tbVocabulario.getItems().forEach(e -> e.getRevisado().selectedProperty().set(ckbMarcarTodos.isSelected()));
		tbVocabulario.refresh();
	}

	private static Boolean desativar = false;
	private static Boolean desmembrar = false;

	private String getSignificado(String kanji) {
		if (kanji.trim().isEmpty())
			return "";

		String resultado = "";
		switch (MenuPrincipalController.getController().getSite()) {
		case TODOS:
			resultado = TanoshiJapanese.processa(kanji);
			
			if (resultado.isEmpty())
				resultado = JapanDict.processa(kanji);
			
			if (resultado.isEmpty())
				resultado = Jisho.processa(kanji);
			
			break;
		case JAPANESE_TANOSHI:
			resultado = TanoshiJapanese.processa(kanji);
			break;
		case JAPANDICT:
			resultado = JapanDict.processa(kanji);
			break;
		case JISHO:
			resultado = Jisho.processa(kanji);
			break;
		default:
		}

		return resultado;
	}

	private String processaPalavras(List<String> palavras, Modo modo) {
		String desmembrado = "";
		for (String palavra : palavras) {
			String resultado = getSignificado(palavra);

			if (!resultado.trim().isEmpty())
				desmembrado += palavra + " - " + resultado + "; ";
			else if (modo.equals(Modo.B)) {
				resultado = processaPalavras(processar.processarDesmembrar(palavra,
						MenuPrincipalController.getController().getDicionario(), Modo.A), Modo.A);
				if (!resultado.trim().isEmpty())
					desmembrado += resultado;
			}
		}

		return desmembrado;
	}

	private String getDesmembrado(String palavra) {
		String resultado = "";
		resultado = processaPalavras(
				processar.processarDesmembrar(palavra, MenuPrincipalController.getController().getDicionario(), Modo.B),
				Modo.B);

		if (resultado.isEmpty())
			resultado = processaPalavras(processar.processarDesmembrar(palavra,
					MenuPrincipalController.getController().getDicionario(), Modo.A), Modo.A);

		return resultado;
	}

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
		btnJapanDict.setDisable(true);
		btnJisho.setDisable(true);
		ckbDesmembrar.setDisable(true);

		btnProcessarTudo.setAccessibleText("PROCESSANDO");
		btnProcessarTudo.setText("Pausar");

		desativar = false;
		desmembrar = ckbDesmembrar.isSelected();
		
		GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
		progress.getTitulo().setText("Tradução");
		Task<Void> processarTudo = new Task<Void>() {
			List<Revisar> lista = null;
			Integer i = 0;

			@Override
			public Void call() throws IOException, InterruptedException {

				lista = new ArrayList<Revisar>(tbVocabulario.getItems());

				try {

					for (Revisar item : lista) {

						i++;
						updateMessage("Processando item " + i + " de " + lista.size());
						updateProgress(i, lista.size());

						Platform.runLater(() -> {
							if (TaskbarProgressbar.isSupported())
								TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, lista.size(),
										Type.NORMAL);
						});

						if (!item.getRevisado().isSelected()) {

							if (item.getIngles().isEmpty()) {
								item.setIngles(getSignificado(item.getVocabulario()));

								if (item.getIngles().isEmpty())
									item.setIngles(getSignificado(item.getFormaBasica()));

								if (desmembrar && item.getIngles().isEmpty())
									item.setIngles(getSignificado(getDesmembrado(item.getVocabulario())));
							}

							if (item.getTraducao().isEmpty() && !item.getIngles().isEmpty()) {
								try {
									item.setTraducao(Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(),
											Language.PORTUGUESE.getSigla(), item.getIngles(),
											MenuPrincipalController.getController().getContaGoogle())));
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							item.getRevisado().setSelected(true);
						}

						if (desativar)
							break;
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
						btnJapanDict.setDisable(false);
						btnJisho.setDisable(false);
						ckbDesmembrar.setDisable(false);

						progress.getLog().textProperty().unbind();
						progress.getBarraProgresso().progressProperty().unbind();
						MenuPrincipalController.getController().destroiBarraProgresso(progress, "");

						TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

						tbVocabulario.refresh();
					});
				}
				return null;
			}
		};

		Thread processa = new Thread(processarTudo);
		progress.getLog().textProperty().bind(processarTudo.messageProperty());
		progress.getBarraProgresso().progressProperty().bind(processarTudo.progressProperty());
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
			tbVocabulario.getSelectionModel().getSelectedItem().setTraducao(
					Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(),
							tbVocabulario.getSelectionModel().getSelectedItem().getIngles(),
							MenuPrincipalController.getController().getContaGoogle())));

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

		if (tbVocabulario.getSelectionModel().getSelectedItem().getIngles().isEmpty())
			tbVocabulario.getSelectionModel().getSelectedItem().setIngles(
					TanoshiJapanese.processa(tbVocabulario.getSelectionModel().getSelectedItem().getFormaBasica()));

		if (ckbDesmembrar.isSelected() && tbVocabulario.getSelectionModel().getSelectedItem().getIngles().isEmpty())
			tbVocabulario.getSelectionModel().getSelectedItem()
					.setIngles(getDesmembrado(tbVocabulario.getSelectionModel().getSelectedItem().getVocabulario()));

		tbVocabulario.refresh();
	}

	@FXML
	private void onBtnTangorin() {
		if (tbVocabulario.getItems().isEmpty() || tbVocabulario.getSelectionModel().getSelectedItem() == null)
			return;
		int index = tbVocabulario.getSelectionModel().getSelectedIndex();
		Tangorin.processa(tbVocabulario.getSelectionModel().getSelectedItem().getVocabulario(), (controller) -> {
			tbVocabulario.getItems().get(index).setIngles(controller);
			tbVocabulario.refresh();
		});
	}

	@FXML
	private void onBtnJapanDict() {
		if (tbVocabulario.getItems().isEmpty() || tbVocabulario.getSelectionModel().getSelectedItem() == null)
			return;

		tbVocabulario.getSelectionModel().getSelectedItem()
				.setIngles(JapanDict.processa(tbVocabulario.getSelectionModel().getSelectedItem().getVocabulario()));

		if (tbVocabulario.getSelectionModel().getSelectedItem().getIngles().isEmpty())
			tbVocabulario.getSelectionModel().getSelectedItem().setIngles(
					JapanDict.processa(tbVocabulario.getSelectionModel().getSelectedItem().getFormaBasica()));

		if (ckbDesmembrar.isSelected() && tbVocabulario.getSelectionModel().getSelectedItem().getIngles().isEmpty())
			tbVocabulario.getSelectionModel().getSelectedItem()
					.setIngles(getDesmembrado(tbVocabulario.getSelectionModel().getSelectedItem().getVocabulario()));

		tbVocabulario.refresh();
	}

	@FXML
	private void onBtnJisho() {
		if (tbVocabulario.getItems().isEmpty() || tbVocabulario.getSelectionModel().getSelectedItem() == null)
			return;

		tbVocabulario.getSelectionModel().getSelectedItem()
				.setIngles(Jisho.processa(tbVocabulario.getSelectionModel().getSelectedItem().getVocabulario()));

		if (tbVocabulario.getSelectionModel().getSelectedItem().getIngles().isEmpty())
			tbVocabulario.getSelectionModel().getSelectedItem()
					.setIngles(Jisho.processa(tbVocabulario.getSelectionModel().getSelectedItem().getFormaBasica()));

		if (ckbDesmembrar.isSelected() && tbVocabulario.getSelectionModel().getSelectedItem().getIngles().isEmpty())
			tbVocabulario.getSelectionModel().getSelectedItem()
					.setIngles(getDesmembrado(tbVocabulario.getSelectionModel().getSelectedItem().getVocabulario()));

		tbVocabulario.refresh();
	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	private void editaColunas() {
		tcVocabulario.setCellValueFactory(new PropertyValueFactory<>("vocabulario"));
		tcIngles.setCellValueFactory(new PropertyValueFactory<>("ingles"));
		tcTraducao.setCellValueFactory(new PropertyValueFactory<>("traducao"));

		tcVocabulario.setCellFactory(TextFieldTableCell.forTableColumn());

		tcIngles.setCellFactory(TextFieldTableCell.forTableColumn());
		tcIngles.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setIngles(e.getNewValue().trim());
			if (!e.getNewValue().trim().isEmpty()) {
				e.getTableView().getItems().get(e.getTablePosition().getRow()).getRevisado().setSelected(true);

				if (e.getTableView().getItems().get(e.getTablePosition().getRow()).getTraducao().isEmpty()) {
					try {
						e.getTableView().getItems().get(e.getTablePosition().getRow())
								.setTraducao(Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(),
										Language.PORTUGUESE.getSigla(), e.getNewValue().trim(),
										MenuPrincipalController.getController().getContaGoogle())));

						tbVocabulario.refresh();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			tbVocabulario.requestFocus();
		});

		tcTraducao.setCellFactory(TextFieldTableCell.forTableColumn());
		tcTraducao.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setTraducao(e.getNewValue().trim());
			if (!e.getNewValue().trim().isEmpty()) {
				e.getTableView().getItems().get(e.getTablePosition().getRow()).getRevisado().setSelected(true);
			}
			tbVocabulario.requestFocus();
		});

		tcRevisado.setCellValueFactory(new PropertyValueFactory<>("revisado"));
	}

	private void linkaCelulas() {
		editaColunas();
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		txtQuantidadeRegistros.textProperty().addListener((obs, oldValue, newValue) -> {
			if(newValue != null && !newValue.matches("\\d*"))
				txtQuantidadeRegistros.setText(oldValue);
			else if (newValue != null && newValue.isEmpty())
				txtQuantidadeRegistros.setText("0");
		});
		linkaCelulas();
		btnProcessarTudo.setAccessibleText("PROCESSAR");
	}

	public static URL getFxmlLocate() {
		return TraduzirController.class.getResource("/view/Traduzir.fxml");
	}

}
