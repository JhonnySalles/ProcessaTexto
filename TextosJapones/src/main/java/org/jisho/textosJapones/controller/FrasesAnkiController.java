package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.enums.Tipo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.kanjiStatics.ImportaEstatistica;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.notification.Notificacoes;
import org.jisho.textosJapones.util.tokenizers.SudachiTokenizer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class FrasesAnkiController implements Initializable {

	@FXML
	private AnchorPane apRoot;

	@FXML
	private StackPane stackPane;

	@FXML
	protected AnchorPane apConteinerRoot;

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
	private JFXComboBox<Tipo> cbTipo;

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

	private VocabularioServices vocabServ = new VocabularioServices();
	private RevisarServices revisaServ = new RevisarServices();
	private Vocabulario vocabulario;
	private Set<String> excluido;

	private Robot robot = new Robot();

	@FXML
	private void onBtnSalvar() {
		salvarTexto();
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
		CorrecaoController.abreTelaCorrecao(stackPane, apConteinerRoot);
	}

	@FXML
	private void onBtnProcessar() {
		if (btnProcessar.getAccessibleText().equalsIgnoreCase("PROCESSAR"))
			processaTexto();
		else
			SudachiTokenizer.DESATIVAR = true;
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
		cbTipo.setDisable(true);
		btnCorrecao.setDisable(true);
		btnEstatistica.setDisable(true);
		btnImportar.setDisable(true);
		btnSalvar.setDisable(true);
		tbVocabulario.setDisable(true);

		btnProcessar.setAccessibleText("PAUSAR");
		btnProcessar.setText("Pausar");
	}

	public void habilitaBotoes() {
		cbTipo.setDisable(false);
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

	public Tipo getTipo() {
		return cbTipo.getSelectionModel().getSelectedItem();
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
							item.getFormaBasica() + " - " + item.getTraducao()));
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

			if (!e.getNewValue().trim().isEmpty())
				frase = Util.normalize(e.getNewValue().trim());

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
		linkaCelulas();
		configuraListenert();

		cbTipo.getItems().addAll(Tipo.values());
		cbTipo.getSelectionModel().select(Tipo.TEXTO);

		/* Setando as variáveis para o alerta padrão. */
		AlertasPopup.setRootStackPane(stackPane);
		AlertasPopup.setNodeBlur(apConteinerRoot);
		Notificacoes.setRootAnchorPane(apRoot);

		btnProcessar.setAccessibleText("PROCESSAR");
		btnProcessar.setText("Processar");
		try {
			atualizaExclusao();
		} catch (ExcessaoBd e) {
			e.printStackTrace();
		}
	}

	public static URL getFxmlLocate() {
		return FrasesAnkiController.class.getResource("/view/FrasesAnki.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
