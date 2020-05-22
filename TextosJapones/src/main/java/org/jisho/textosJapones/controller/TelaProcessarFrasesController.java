package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.animation.Animacao;
import org.jisho.textosJapones.util.enuns.Modo;
import org.jisho.textosJapones.util.exception.ExcessaoBd;
import org.jisho.textosJapones.util.mysql.ConexaoMysql;
import org.jisho.textosJapones.util.process.SudachiTokenizer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class TelaProcessarFrasesController implements Initializable {

	final static Image imgAnimaBanco = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/icoDataBase_48.png"));
	final static Image imgAnimaBancoConec = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/icoDataEspera_48.png"));
	final static Image imgAnimaBancoErro = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/icoDataSemConexao_48.png"));
	final static Image imgAnimaBancoConectado = new Image(
			Animacao.class.getResourceAsStream("/org/jisho/textosJapones/resources/images/icoDataConectado_48.png"));

	@FXML
	private AnchorPane background;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private JFXButton btnBanco;

	@FXML
	private JFXComboBox<Modo> cbModo;

	@FXML
	private ImageView imgConexaoBase;

	@FXML
	private JFXTextField txtVocabulario;

	@FXML
	private JFXTextArea txtAreaOrigem;

	@FXML
	private JFXTextArea txtAreaProcessar;

	@FXML
	private JFXTextField txtExclusoes;

	@FXML
	private Label lblAviso;

	private VocabularioServices vocabServ;

	@FXML
	private TableView<Vocabulario> tbVocabulario;

	@FXML
	private TableColumn<Vocabulario, String> tcVocabulario;

	@FXML
	private TableColumn<Vocabulario, String> tcTraducao;
	private ObservableList<Vocabulario> obsLVocabulario;

	final private Animacao animacao = new Animacao();
	private Set<String> excluido;
	private List<Vocabulario> vocabNovo = new ArrayList<>();
	private Vocabulario vocabulario;

	@FXML
	private void onBtnSalvar() {
		lblAviso.setText("Salvando....");
		salvarTexto();
	}

	@FXML
	private void onBtnProcessar() {
		processaTexto();
	}

	@FXML
	private void onBtnVerificaConexao() {
		verificaConexao();
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
			lblAviso.setText("Erro pesquisar a palavra. " + palavra);
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
		lblAviso.setText(aviso);
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

	private void salvaVocabulario() {
		if (txtVocabulario.isEditable())
			if (!txtVocabulario.getText().isEmpty()) {
				vocabulario.setTraducao(txtVocabulario.getText());
				try {
					vocabServ.insertOrUpdate(vocabulario);
					lblAviso.setText("Salvamento vocabulário concluído. " + txtVocabulario.getText());
					txtVocabulario.setUnFocusColor(Color.LIME);
				} catch (ExcessaoBd e) {
					e.printStackTrace();
					lblAviso.setText("Erro ao salvar vocabulario. " + txtVocabulario.getText());
					txtVocabulario.setUnFocusColor(Color.RED);
				}

			} else
				if (!txtAreaOrigem.getText().isEmpty())
					txtVocabulario.setUnFocusColor(Color.RED);
	}

	private void salvaExclusao() {

		if (!txtExclusoes.getText().isEmpty()) {
			try {
				excluido = vocabServ.insertExclusao(txtExclusoes.getText()).selectExclusao();
				lblAviso.setText("Salvamento exclusão concluído. " + txtExclusoes.getText());
				txtExclusoes.setUnFocusColor(Color.LIME);
				txtExclusoes.setText("");
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				lblAviso.setText("Erro ao salvar vocabulário de exclusão. " + txtExclusoes.getText());
				txtExclusoes.setUnFocusColor(Color.RED);
			}
		}
	}

	private void processaTexto() {
		try {
			if (excluido == null)
				excluido = vocabServ.selectExclusao();
			
			SudachiTokenizer tokenizer = new SudachiTokenizer();
			tokenizer.processaTexto(this);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			lblAviso.setText("Erro ao pesquisar vocabulário excluído.");
		}
	}

	private void salvarTexto() {
		if (vocabNovo.size() > 0) {
			try {
				vocabServ.insert(vocabNovo);
				vocabNovo.clear();
				vocabNovo.add(new Vocabulario());
				lblAviso.setText("Salvamento concluído.");
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				lblAviso.setText("Erro ao salvar os novos vocabulários.");
			}
		} else
			lblAviso.setText("Lista vazia.");
	}

	private void verificaConexao() {
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

				if (!conectado.isEmpty()) {
					imgConexaoBase.setImage(imgAnimaBancoConectado);
					lblAviso.setText("Conectado.");

				} else {
					imgConexaoBase.setImage(imgAnimaBancoErro);
					lblAviso.setText("Erro ao tentar conectar ao banco.");
				}
				vocabServ = new VocabularioServices();
				
				try {
					excluido = vocabServ.selectExclusao();
				} catch (ExcessaoBd e) {
					e.printStackTrace();
				}
			}
		};

		Thread t = new Thread(verificaConexao);
		t.setDaemon(true);
		t.start();
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

	public void initialize(URL arg0, ResourceBundle arg1) {
		animacao.animaImageBanco(imgConexaoBase, imgAnimaBanco, imgAnimaBancoConec);
		linkaCelulas();

		cbModo.getItems().addAll(Modo.values());
		cbModo.getSelectionModel().select(Modo.C);

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
				}
			}
		});

		txtExclusoes.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					salvaExclusao();
				}
			}
		});

		verificaConexao();
	}

	public void setMessagemErro(String erro) {

	}

	public void setImagemBancoErro(String erro) {
		animacao.tmLineImageBanco.stop();
		imgConexaoBase.setImage(imgAnimaBancoErro);
		lblAviso.setText(erro);
	}

	public static URL getFxmlLocate() {
		return TelaProcessarFrasesController.class
				.getResource("/org/jisho/textosJapones/view/TelaProcessarFrases.fxml");
	}

	public static String getIconLocate() {
		return "/org/jisho/textosJapones/resources/images/icoTranslate_128.png";
	}

}
