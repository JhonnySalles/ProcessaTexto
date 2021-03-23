package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.enums.Tipo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.animation.Animacao;
import org.jisho.textosJapones.util.kanjiStatics.ImportaEstatistica;
import org.jisho.textosJapones.util.mysql.Backup;
import org.jisho.textosJapones.util.mysql.ConexaoMysql;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.notification.Notificacoes;
import org.jisho.textosJapones.util.tokenizers.SudachiTokenizer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class FrasesController implements Initializable {

	final static Image imgAnimaBanco = new Image(Animacao.class.getResourceAsStream("/images/bd/icoDataBase_48.png"));
	final static Image imgAnimaBancoEspera = new Image(
			Animacao.class.getResourceAsStream("/images/bd/icoDataEspera_48.png"));
	final static Image imgAnimaBancoErro = new Image(
			Animacao.class.getResourceAsStream("/images/bd/icoDataSemConexao_48.png"));
	final static Image imgAnimaBancoConectado = new Image(
			Animacao.class.getResourceAsStream("/images/bd/icoDataConectado_48.png"));

	final static Image imgAnimaBackup = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_48.png"));

	final static Image imgAnimaExporta = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Exportando_48.png"));
	final static Image imgAnimaExportaEspera = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Exportando_Espera_48.png"));
	final static Image imgAnimaExportaErro = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Exportando_Erro_48.png"));
	final static Image imgAnimaExportaConcluido = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Exportando_Concluido_48.png"));

	final static Image imgAnimaImporta = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Importando_48.png"));
	final static Image imgAnimaImportaEspera = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Importando_Espera_48.png"));
	final static Image imgAnimaImportaErro = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Importando_Erro_48.png"));
	final static Image imgAnimaImportaConcluido = new Image(
			Animacao.class.getResourceAsStream("/images/export/icoBDBackup_Importando_Concluido_48.png"));

	@FXML
	private AnchorPane apGlobal;

	@FXML
	private StackPane rootStackPane;

	@FXML
	protected AnchorPane root;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnEstatistica;

	@FXML
	private JFXButton btnCorrecao;

	@FXML
	private JFXButton btnImportar;

	@FXML
	private JFXButton btnBanco;

	@FXML
	private ImageView imgConexaoBase;

	@FXML
	private JFXButton btnBackup;

	@FXML
	private ImageView imgBackup;

	@FXML
	private JFXComboBox<Tipo> cbTipo;

	@FXML
	private JFXComboBox<Modo> cbModo;

	@FXML
	private JFXComboBox<Dicionario> cbDicionario;

	@FXML
	private JFXComboBox<Api> cbContaGoolge;

	@FXML
	private JFXTextField txtVocabulario;

	@FXML
	private JFXTextArea txtAreaOrigem;

	@FXML
	private JFXTextArea txtAreaDestino;

	@FXML
	private JFXTextField txtExclusoes;

	@FXML
	private Label lblExclusoes;

	@FXML
	private TableView<Vocabulario> tbVocabulario;

	@FXML
	private TableColumn<Vocabulario, String> tcVocabulario;

	@FXML
	private TableColumn<Vocabulario, String> tcTraducao;

	@FXML
	private ProgressBar barraProgresso;

	private VocabularioServices vocabServ = new VocabularioServices();
	private RevisarServices revisaServ = new RevisarServices();
	private Vocabulario vocabulario;
	private Set<String> excluido;

	private PopOver pop;
	private Timeline tmlImagemBackup;
	private Robot robot = new Robot();
	private Animacao animacao = new Animacao();

	@FXML
	private void onBtnSalvar() {
		salvarTexto();
	}

	@FXML
	private void onBtnVerificaConexao() {
		verificaConexao();
	}

	@FXML
	private void onBtnConexaoOnMouseClicked(MouseEvent mouseClick) {
		if (mouseClick.getButton() == MouseButton.SECONDARY) {
			if (!pop.isShowing())
				mostrarConfiguracao();
		}
	}

	@FXML
	private void onBtnImportar() {
		ImportaEstatistica.importa();
	}

	@FXML
	private void onBtnEstatistica() {

		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(EstatisticaController.getFxmlLocate());
			AnchorPane newAnchorPane = loader.load();

			Scene mainScene = new Scene(newAnchorPane); // Carrega a scena
			mainScene.setFill(Color.BLACK);

			Stage stage = new Stage();
			stage.setScene(mainScene); // Seta a cena principal
			stage.setTitle("Gerar estatisticas");
			stage.initStyle(StageStyle.DECORATED);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.getIcons().add(new Image(getClass().getResourceAsStream(EstatisticaController.getIconLocate())));
			stage.show(); // Mostra a tela.

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Erro ao abrir a tela de estatistica.");
		}

	}

	@FXML
	private void onBtnCorrecao() {
		CorrecaoController.abreTelaCorrecao(rootStackPane, root);
	}

	@FXML
	private void onBtnProcessar() {
		if (btnProcessar.getAccessibleText().equalsIgnoreCase("PROCESSAR"))
			processaTexto();
		else
			SudachiTokenizer.DESATIVAR = true;
	}

	public void setImagemBancoErro(String erro) {
		animacao.tmLineImageBanco.stop();
		imgConexaoBase.setImage(imgAnimaBancoErro);
		Notificacoes.notificacao(Notificacao.ERRO, "Erro.", erro);
	}

	public FrasesController mostrarConfiguracao() {
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		Scene scene = btnBanco.getScene();
		Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
		Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
		Point2D nodeCoord = btnBanco.localToScene(0.0, 0.0);
		double cordenadaX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());

		if (cordenadaX < screenBounds.getWidth() / 2)
			pop.arrowLocationProperty().set(ArrowLocation.LEFT_TOP);
		else
			pop.arrowLocationProperty().set(ArrowLocation.RIGHT_TOP);

		pop.show(btnBanco, 30);
		return this;
	}

	public void cancelaBackup() {
		animacao.tmLineImageBackup.stop();
		imgBackup.setImage(imgAnimaBackup);
	}

	public void importaBackup() {
		animacao.animaImageBackup(imgBackup, imgAnimaImporta, imgAnimaImportaEspera);
		animacao.tmLineImageBackup.play();
		Backup.importarBackup(this);
	}

	public void importaConcluido(boolean erro) {
		animacao.tmLineImageBackup.stop();
		if (erro)
			imgBackup.setImage(imgAnimaImportaErro);
		else {
			imgBackup.setImage(imgAnimaImportaConcluido);
			verificaConexao();
		}

		tmlImagemBackup.play();
	}

	public void exportaBackup() {
		animacao.animaImageBackup(imgBackup, imgAnimaExporta, imgAnimaExportaEspera);
		animacao.tmLineImageBackup.play();
		Backup.exportarBackup(this);
	}

	public void exportaConcluido(boolean erro) {
		animacao.tmLineImageBackup.stop();
		if (erro)
			imgBackup.setImage(imgAnimaExportaErro);
		else
			imgBackup.setImage(imgAnimaExportaConcluido);

		tmlImagemBackup.play();
	}

	public void setPalavra(String palavra) {
		try {
			this.vocabulario = vocabServ.select(palavra);
			if (vocabulario.getTraducao().isEmpty()) {
				if (!txtAreaOrigem.getText().isEmpty())
					txtVocabulario.setUnFocusColor(Color.RED);
				else
					txtVocabulario.setUnFocusColor(Color.web("#106ebe"));

				txtVocabulario.setText("");
				txtVocabulario.setEditable(true);
			} else {
				txtVocabulario.setText(vocabulario.getTraducao());
				txtVocabulario.setEditable(false);
				txtVocabulario.setUnFocusColor(Color.web("#106ebe"));
			}
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			Notificacoes.notificacao(Notificacao.ERRO, "Erro pesquisar a palavra.", palavra);
			txtVocabulario.setUnFocusColor(Color.RED);
		}
	}

	public void desabilitaBotoes() {
		cbContaGoolge.setDisable(true);
		cbTipo.setDisable(true);
		cbDicionario.setDisable(true);
		cbModo.setDisable(true);
		btnCorrecao.setDisable(true);
		btnEstatistica.setDisable(true);
		btnImportar.setDisable(true);
		btnSalvar.setDisable(true);
		tbVocabulario.setDisable(true);

		btnProcessar.setAccessibleText("PAUSAR");
		btnProcessar.setText("Pausar");
	}

	public void habilitaBotoes() {
		cbContaGoolge.setDisable(false);
		cbTipo.setDisable(false);
		cbDicionario.setDisable(false);
		cbModo.setDisable(false);
		btnCorrecao.setDisable(false);
		btnEstatistica.setDisable(false);
		btnImportar.setDisable(false);
		btnSalvar.setDisable(false);
		tbVocabulario.setDisable(false);

		btnProcessar.setAccessibleText("PROCESSAR");
		btnProcessar.setText("Processar");
	}

	public void limpaVocabulario() {
		vocabulario = null;
		txtVocabulario.setText("");
		txtVocabulario.setEditable(false);
		txtVocabulario.setUnFocusColor(Color.web("#106ebe"));
	}

	public Set<String> getExcluido() {
		return excluido;
	}

	public String getTextoOrigem() {
		return txtAreaOrigem.getText();
	}

	public void setTextoDestino(String texto) {
		txtAreaDestino.setText(texto);
	}

	public void setAviso(String aviso) {
		Notificacoes.notificacao(Notificacao.AVISO, "Aviso.", aviso);
	}

	public void setVocabulario(List<Vocabulario> lista) {
		tbVocabulario.getItems().clear();
		tbVocabulario.getItems().addAll(lista);

		if (tbVocabulario.getItems().isEmpty())
			tbVocabulario.getItems().add(new Vocabulario());

		tbVocabulario.refresh();
	}

	public Modo getModo() {
		return cbModo.getSelectionModel().getSelectedItem();
	}

	public Tipo getTipo() {
		return cbTipo.getSelectionModel().getSelectedItem();
	}

	public Dicionario getDicionario() {
		return cbDicionario.getSelectionModel().getSelectedItem();
	}

	public Api getContaGoogle() {
		return cbContaGoolge.getSelectionModel().getSelectedItem();
	}

	public ProgressBar getBarraProgresso() {
		return barraProgresso;
	}

	public PopOver getPopPup() {
		return pop;
	}

	private void salvaVocabulario() {
		if (txtVocabulario.isEditable())
			if (!txtVocabulario.getText().trim().isEmpty()) {
				vocabulario.setTraducao(txtVocabulario.getText().trim());
				try {
					vocabServ.insertOrUpdate(vocabulario);
					Notificacoes.notificacao(Notificacao.SUCESSO, "Salvamento vocabulário concluído.",
							txtVocabulario.getText());
					txtVocabulario.setUnFocusColor(Color.LIME);
					txtVocabulario.setEditable(false);
				} catch (ExcessaoBd e) {
					e.printStackTrace();
					Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar vocabulario.", txtVocabulario.getText());
					txtVocabulario.setUnFocusColor(Color.RED);
					txtVocabulario.setEditable(true);
				}

			} else if (!txtAreaOrigem.getText().isEmpty()) {
				txtVocabulario.setUnFocusColor(Color.RED);
				txtVocabulario.setEditable(true);
			}
	}

	private void salvaExclusao() {
		if (!txtExclusoes.getText().trim().isEmpty()) {
			try {
				if (!vocabServ.existeExclusao(txtExclusoes.getText().trim())) {
					excluido = vocabServ.insertExclusao(txtExclusoes.getText()).selectExclusao();
					lblExclusoes.setText(excluido.toString());
					Notificacoes.notificacao(Notificacao.SUCESSO, "Salvamento exclusão concluído.",
							txtExclusoes.getText());
				} else
					Notificacoes.notificacao(Notificacao.ALERTA, "Palavra já existe na exclusão.",
							txtExclusoes.getText());

				txtExclusoes.setUnFocusColor(Color.LIME);
				txtExclusoes.setText("");
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar vocabulário de exclusão.",
						txtExclusoes.getText());
				txtExclusoes.setUnFocusColor(Color.RED);
			}
		}
	}

	private void atualizaExclusao() throws ExcessaoBd {
		excluido = vocabServ.selectExclusao();
		lblExclusoes.setText(excluido.toString());
	}

	private void processaTexto() {
		try {
			if (excluido == null)
				atualizaExclusao();

			SudachiTokenizer tokenizer = new SudachiTokenizer();
			tokenizer.processa(this);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			Notificacoes.notificacao(Notificacao.ERRO, "Erro.", "Erro ao pesquisar vocabulário excluído.");
		}
	}

	private void salvarTexto() {
		if (tbVocabulario.getItems().size() > 0) {
			try {

				List<Vocabulario> salvar = tbVocabulario.getItems().stream().filter(e -> !e.getTraducao().isEmpty())
						.collect(Collectors.toList());

				vocabServ.insert(salvar);

				String itensSalvo = "";
				for (Vocabulario item : salvar) {
					txtAreaDestino.setText(txtAreaDestino.getText().replaceAll(item.getFormaBasica() + " \\*\\*",
							item.getFormaBasica() + " - " + item.getTraducao() + "."));
					itensSalvo += item.toString();

					revisaServ.delete(item.getVocabulario());
				}

				if (salvar.size() != tbVocabulario.getItems().size())
					tbVocabulario.getItems().removeIf(item -> salvar.contains(item));
				else {
					tbVocabulario.getItems().clear();
					tbVocabulario.getItems().add(new Vocabulario());
				}

				if (itensSalvo.isEmpty())
					Notificacoes.notificacao(Notificacao.AVISO, "Nenhum item encontrado.",
							"Nenhum item com tradução encontrada.");
				else
					Notificacoes.notificacao(Notificacao.SUCESSO, "Salvamento texto concluído.",
							itensSalvo.substring(0, itensSalvo.lastIndexOf(", ")) + ".");
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				Notificacoes.notificacao(Notificacao.ERRO, "Erro.", "Erro ao salvar os novos vocabulários.");
			}
		} else
			Notificacoes.notificacao(Notificacao.AVISO, "Aviso.", "Lista vazia.");
	}

	public void verificaConexao() {
		animacao.tmLineImageBanco.play();

		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<String> verificaConexao = new Task<String>() {

			@Override
			protected String call() throws Exception {
				TimeUnit.SECONDS.sleep(1);
				return ConexaoMysql.testaConexaoMySQL();
			}

			@Override
			protected void succeeded() {
				animacao.tmLineImageBanco.stop();
				String conectado = getValue();

				if (!conectado.isEmpty())
					imgConexaoBase.setImage(imgAnimaBancoConectado);

				else
					imgConexaoBase.setImage(imgAnimaBancoErro);

				vocabServ = new VocabularioServices();

				try {
					atualizaExclusao();
				} catch (ExcessaoBd e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(verificaConexao);
		t.start();
	}

	private void criaMenuBackup() {
		ContextMenu menuBackup = new ContextMenu();

		MenuItem miBackup = new MenuItem("Backup");
		ImageView imgExporta = new ImageView(imgAnimaExporta);
		imgExporta.setFitHeight(20);
		imgExporta.setFitWidth(20);
		miBackup.setGraphic(imgExporta);
		miBackup.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				exportaBackup();
			}
		});

		MenuItem miRestaurar = new MenuItem("Restaurar");
		ImageView imgImporta = new ImageView(imgAnimaImporta);
		imgImporta.setFitHeight(20);
		imgImporta.setFitWidth(20);
		miRestaurar.setGraphic(imgImporta);
		miRestaurar.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				importaBackup();
			}
		});

		menuBackup.getItems().addAll(miBackup, miRestaurar);

		btnBackup.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				Scene scene = btnBanco.getScene();
				Point2D windowCoord = new Point2D(scene.getWindow().getX(), scene.getWindow().getY());
				Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
				Point2D nodeCoord = btnBanco.localToScene(0.0, 0.0);
				double cordenadaX = Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX());
				double cordenadaY = Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY());
				menuBackup.show(btnBackup, cordenadaX + 95, cordenadaY + 30);
			}
		});

		tmlImagemBackup = new Timeline(new KeyFrame(Duration.millis(5000), ae -> cancelaBackup()));
	}

	private FrasesController criaConfiguracao() {
		pop = new PopOver();
		URL url = ConfiguracaoController.getFxmlLocate();
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(url);
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(3));// 10px padding todos os lados
		try {
			vbox.getChildren().add(loader.load());

			ConfiguracaoController cntConfiguracao = loader.getController();
			cntConfiguracao.setControllerPai(this);
			pop.setTitle("Configuração banco de dados");
			pop.setContentNode(vbox);
			pop.setCornerRadius(5);
			pop.setHideOnEscape(true);
			pop.setAutoFix(true);
			pop.setAutoHide(true);
			pop.setOnHidden(e -> cntConfiguracao.salvar());
			pop.setOnShowing(e -> cntConfiguracao.carregar());
			pop.getRoot().getStylesheets()
					.add(FrasesController.class.getResource("/css/Dark_PopOver.css").toExternalForm());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	private void adicionaUltimaLinha() {
		tbVocabulario.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {

				if (event.getCode() == KeyCode.DOWN) {
					@SuppressWarnings("unchecked")
					TablePosition<Vocabulario, ?> pos = tbVocabulario.getFocusModel().getFocusedCell();

					if (pos.getRow() == -1) {
						tbVocabulario.getSelectionModel().select(0);
					}
					// add new row when we are at the last row
					else if (pos.getRow() == tbVocabulario.getItems().size() - 1) {
						addRow();
					}
				}
			}
		});
	}

	public void addRow() {
		@SuppressWarnings("unchecked")
		TablePosition<Vocabulario, ?> pos = tbVocabulario.getFocusModel().getFocusedCell();
		tbVocabulario.getSelectionModel().clearSelection();
		Vocabulario data = new Vocabulario();
		tbVocabulario.getItems().add(data);
		tbVocabulario.getSelectionModel().select(tbVocabulario.getItems().size() - 1, pos.getTableColumn());
		tbVocabulario.scrollTo(data);
	}

	private void editaColunas() {
		tcVocabulario.setCellValueFactory(new PropertyValueFactory<>("vocabulario"));
		tcTraducao.setCellValueFactory(new PropertyValueFactory<>("traducao"));

		tcVocabulario.setCellFactory(TextFieldTableCell.forTableColumn());
		tcVocabulario.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setVocabulario(e.getNewValue().trim());
			tbVocabulario.requestFocus();
		});

		tcTraducao.setCellFactory(TextFieldTableCell.forTableColumn());
		tcTraducao.setOnEditCommit(e -> {
			String frase = "";

			if (!e.getNewValue().trim().isEmpty()) {
				frase = e.getNewValue().trim();
				frase = frase.substring(0, 1).toUpperCase() + frase.substring(1) + ".";
				if (frase.contains(".."))
					frase = frase.replaceAll("\\.{2,}", ".");
			}

			e.getTableView().getItems().get(e.getTablePosition().getRow()).setTraducao(frase);
			tbVocabulario.refresh();
			tbVocabulario.requestFocus();
		});

		tbVocabulario.setRowFactory(tv -> {
			TableRow<Vocabulario> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (row.isEmpty()))
					addRow();
			});
			return row;
		});
	}

	private void linkaCelulas() {
		editaColunas();
		adicionaUltimaLinha();

		List<Vocabulario> vocabulario = new ArrayList<>();
		vocabulario.add(new Vocabulario());
		ObservableList<Vocabulario> observable = FXCollections.observableArrayList(vocabulario);
		tbVocabulario.setItems(observable);
	}

	private void configuraListenert() {
		txtAreaOrigem.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal && !cbTipo.getSelectionModel().getSelectedItem().equals(Tipo.VOCABULARIO))
				processaTexto();

		});

		txtVocabulario.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				txtVocabulario.setUnFocusColor(Color.web("#106ebe"));
				salvaVocabulario();
			}
		});

		txtExclusoes.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal)
				txtExclusoes.setUnFocusColor(Color.web("#106ebe"));
		});

		txtVocabulario.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					salvaVocabulario();
					robot.keyPress(KeyCode.TAB);
				}
			}
		});

		txtExclusoes.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					salvaExclusao();
					robot.keyPress(KeyCode.TAB);
				}
			}
		});
	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		animacao.animaImageBanco(imgConexaoBase, imgAnimaBanco, imgAnimaBancoEspera);
		linkaCelulas();
		configuraListenert();
		criaConfiguracao();
		criaMenuBackup();

		cbTipo.getItems().addAll(Tipo.values());
		cbTipo.getSelectionModel().select(Tipo.TEXTO);

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

		btnProcessar.setAccessibleText("PROCESSAR");
		btnProcessar.setText("Processar");

		verificaConexao();
	}

	public static URL getFxmlLocate() {
		return FrasesController.class.getResource("/view/Frases.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
