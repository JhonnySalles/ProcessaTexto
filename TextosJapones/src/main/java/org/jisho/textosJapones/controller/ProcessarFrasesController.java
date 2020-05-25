package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.animation.Animacao;
import org.jisho.textosJapones.util.mysql.Backup;
import org.jisho.textosJapones.util.mysql.ConexaoMysql;
import org.jisho.textosJapones.util.notification.Alertas;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
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
import javafx.stage.Screen;
import javafx.util.Duration;

public class ProcessarFrasesController implements Initializable {

	final static Image imgAnimaBanco = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/bd/icoDataBase_48.png"));
	final static Image imgAnimaBancoEspera = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/bd/icoDataEspera_48.png"));
	final static Image imgAnimaBancoErro = new Image(Animacao.class
			.getResourceAsStream("/org/jisho/textosJapones/resources/images/bd/icoDataSemConexao_48.png"));
	final static Image imgAnimaBancoConectado = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/bd/icoDataConectado_48.png"));

	final static Image imgAnimaBackup = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/export/icoBDBackup_48.png"));

	final static Image imgAnimaExporta = new Image(Animacao.class
			.getResourceAsStream("/org/jisho/textosJapones/resources/images/export/icoBDBackup_Exportando_48.png"));
	final static Image imgAnimaExportaEspera = new Image(Animacao.class.getResourceAsStream(
			"/org/jisho/textosJapones/resources/images/export/icoBDBackup_Exportando_Espera_48.png"));
	final static Image imgAnimaExportaErro = new Image(Animacao.class.getResourceAsStream(
			"/org/jisho/textosJapones/resources/images/export/icoBDBackup_Exportando_Erro_48.png"));
	final static Image imgAnimaExportaConcluido = new Image(Animacao.class.getResourceAsStream(
			"/org/jisho/textosJapones/resources/images/export/icoBDBackup_Exportando_Concluido_48.png"));

	final static Image imgAnimaImporta = new Image(Animacao.class
			.getResourceAsStream("/org/jisho/textosJapones/resources/images/export/icoBDBackup_Importando_48.png"));
	final static Image imgAnimaImportaEspera = new Image(Animacao.class.getResourceAsStream(
			"/org/jisho/textosJapones/resources/images/export/icoBDBackup_Importando_Espera_48.png"));
	final static Image imgAnimaImportaErro = new Image(Animacao.class.getResourceAsStream(
			"/org/jisho/textosJapones/resources/images/export/icoBDBackup_Importando_Erro_48.png"));
	final static Image imgAnimaImportaConcluido = new Image(Animacao.class.getResourceAsStream(
			"/org/jisho/textosJapones/resources/images/export/icoBDBackup_Importando_Concluido_48.png"));

	@FXML
	private AnchorPane apGlobal;

	@FXML
	private StackPane rootStackPane;

	@FXML
	protected AnchorPane root;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private JFXButton btnBanco;

	@FXML
	private ImageView imgConexaoBase;

	@FXML
	private JFXButton btnBackup;

	@FXML
	private ImageView imgBackup;

	@FXML
	private JFXComboBox<Modo> cbModo;

	@FXML
	private JFXComboBox<Dicionario> cbDicionario;

	@FXML
	private JFXTextField txtVocabulario;

	@FXML
	private JFXTextArea txtAreaOrigem;

	@FXML
	private JFXTextArea txtAreaProcessar;

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
	private ObservableList<Vocabulario> obsLVocabulario;
	private VocabularioServices vocabServ;

	private Animacao animacao = new Animacao();
	private Set<String> excluido;
	private List<Vocabulario> vocabNovo = new ArrayList<>();
	private Vocabulario vocabulario;
	private PopOver pop;
	private Timeline tmlImagemBackup;
	private Robot robot;

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

	public ProcessarFrasesController mostrarConfiguracao() {
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
		else
			imgBackup.setImage(imgAnimaImportaConcluido);

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
			Notificacoes.notificacao(Notificacao.ERRO, "Erro", "Erro pesquisar a palavra. " + palavra);
			txtVocabulario.setUnFocusColor(Color.RED);
		}
	}

	public Set<String> getExcluido() {
		return excluido;
	}

	public String getTextoOrigem() {
		return txtAreaOrigem.getText();
	}

	public void setTextoDestino(String texto) {
		txtAreaProcessar.setText(texto);
	}

	public void setAviso(String aviso) {
		Notificacoes.notificacao(Notificacao.AVISO, "", aviso);
	}

	public void setVocabulario(List<Vocabulario> lista) {
		vocabNovo = lista;

		if (vocabNovo.isEmpty())
			vocabNovo.add(new Vocabulario());

		obsLVocabulario = FXCollections.observableArrayList(vocabNovo);
		tbVocabulario.setItems(obsLVocabulario);
	}

	public Modo getModo() {
		return cbModo.getSelectionModel().getSelectedItem();
	}

	public Dicionario getDicionario() {
		return cbDicionario.getSelectionModel().getSelectedItem();
	}

	public PopOver getPopPup() {
		return pop;
	}

	private void salvaVocabulario() {
		if (txtVocabulario.isEditable())
			if (!txtVocabulario.getText().isEmpty()) {
				vocabulario.setTraducao(txtVocabulario.getText());
				try {
					vocabServ.insertOrUpdate(vocabulario);
					Notificacoes.notificacao(Notificacao.SUCESSO, "",
							"Salvamento vocabulário concluído. " + txtVocabulario.getText());
					txtVocabulario.setUnFocusColor(Color.LIME);
					txtVocabulario.setEditable(false);
				} catch (ExcessaoBd e) {
					e.printStackTrace();
					Notificacoes.notificacao(Notificacao.ERRO, "Erro",
							"Erro ao salvar vocabulario. " + txtVocabulario.getText());
					txtVocabulario.setUnFocusColor(Color.RED);
					txtVocabulario.setEditable(true);
				}

			} else if (!txtAreaOrigem.getText().isEmpty()) {
				txtVocabulario.setUnFocusColor(Color.RED);
				txtVocabulario.setEditable(true);
			}
	}

	private void salvaExclusao() {
		if (!txtExclusoes.getText().isEmpty()) {
			try {
				excluido = vocabServ.insertExclusao(txtExclusoes.getText()).selectExclusao();
				lblExclusoes.setText(excluido.toString());
				Notificacoes.notificacao(Notificacao.SUCESSO, "",
						"Salvamento exclusão concluído. " + txtExclusoes.getText());
				txtExclusoes.setUnFocusColor(Color.LIME);
				txtExclusoes.setText("");
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				Notificacoes.notificacao(Notificacao.ERRO, "Erro",
						"Erro ao salvar vocabulário de exclusão. " + txtExclusoes.getText());
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
			tokenizer.processaTexto(this);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			Notificacoes.notificacao(Notificacao.ERRO, "", "Erro ao pesquisar vocabulário excluído.");
		}
	}

	private void salvarTexto() {
		if (vocabNovo.size() > 0) {
			try {
				vocabServ.insert(vocabNovo);
				vocabNovo.clear();
				vocabNovo.add(new Vocabulario());
				obsLVocabulario = FXCollections.observableArrayList(vocabNovo);
				tbVocabulario.setItems(obsLVocabulario);
				Notificacoes.notificacao(Notificacao.SUCESSO, "", "Salvamento concluído.");
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				Notificacoes.notificacao(Notificacao.ERRO, "Erro", "Erro ao salvar os novos vocabulários.");
			}
		} else
			Notificacoes.notificacao(Notificacao.AVISO, "", "Lista vazia.");
	}

	public void verificaConexao() {
		animacao.tmLineImageBanco.play();

		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<String> verificaConexao = new Task<String>() {

			@Override
			protected String call() throws Exception {
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
		t.setDaemon(true);
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

	private ProcessarFrasesController criaConfiguracao() {
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
			pop.getRoot().getStylesheets().add(ProcessarFrasesController.class
					.getResource("/org/jisho/textosJapones/resources/css/Dark_PopOver.css").toExternalForm());

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
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setVocabulario(e.getNewValue());
			tbVocabulario.requestFocus();
		});

		tcTraducao.setCellFactory(TextFieldTableCell.forTableColumn());
		tcTraducao.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setTraducao(e.getNewValue());
			tbVocabulario.requestFocus();
		});
	}

	private void linkaCelulas() {
		editaColunas();
		adicionaUltimaLinha();

		vocabNovo.add(new Vocabulario());
		obsLVocabulario = FXCollections.observableArrayList(vocabNovo);
		tbVocabulario.setItems(obsLVocabulario);
	}

	private void configuraListenert() {
		txtAreaOrigem.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				processaTexto();
			}
		});

		txtVocabulario.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				txtVocabulario.setUnFocusColor(Color.web("#106ebe"));
				salvaVocabulario();
			}
		});

		txtExclusoes.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				txtExclusoes.setUnFocusColor(Color.web("#106ebe"));
			}
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
		robot = new Robot();

		cbModo.getItems().addAll(Modo.values());
		cbModo.getSelectionModel().select(Modo.C);

		cbDicionario.getItems().addAll(Dicionario.values());
		cbDicionario.getSelectionModel().select(Dicionario.FULL);

		/* Setando as variáveis para o alerta padrão. */
		Alertas.setRootStackPane(rootStackPane);
		Alertas.setNodeBlur(root);
		Notificacoes.setRootStackPane(apGlobal);

		verificaConexao();
	}

	public void setImagemBancoErro(String erro) {
		animacao.tmLineImageBanco.stop();
		imgConexaoBase.setImage(imgAnimaBancoErro);
		Notificacoes.notificacao(Notificacao.ERRO, "Erro", erro);
	}

	public static URL getFxmlLocate() {
		return ProcessarFrasesController.class.getResource("/org/jisho/textosJapones/view/ProcessarFrases.fxml");
	}

	public static String getIconLocate() {
		return "/org/jisho/textosJapones/resources/images/icoTranslate_128.png";
	}

}
