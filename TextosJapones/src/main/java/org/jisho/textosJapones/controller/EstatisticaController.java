package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.Estatistica;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.EstatisticaServices;
import org.jisho.textosJapones.util.animation.Animacao;
import org.jisho.textosJapones.util.mysql.ConexaoMysql;
import org.jisho.textosJapones.util.notification.AlertasPopup;

import com.google.common.collect.Sets;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.util.Callback;

public class EstatisticaController implements Initializable {

	final static Image imgAnimaBanco = new Image(Animacao.class.getResourceAsStream("/images/bd/icoDataBase_48.png"));
	final static Image imgAnimaBancoEspera = new Image(
			Animacao.class.getResourceAsStream("/images/bd/icoDataEspera_48.png"));
	final static Image imgAnimaBancoErro = new Image(
			Animacao.class.getResourceAsStream("/images/bd/icoDataSemConexao_48.png"));
	final static Image imgAnimaBancoConectado = new Image(
			Animacao.class.getResourceAsStream("/images/bd/icoDataConectado_48.png"));

	final private static PseudoClass FOLHA = PseudoClass.getPseudoClass("leaf");

	@FXML
	private AnchorPane apGlobal;

	@FXML
	private StackPane rootStackPane;

	@FXML
	protected AnchorPane root;

	@FXML
	private JFXButton btnBanco;

	@FXML
	private ImageView imgConexaoBase;

	@FXML
	private JFXTextField txtVocabulario;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnGerarTabelas;

	@FXML
	private JFXTextField txtPesquisa;

	@FXML
	private TreeTableView<Estatistica> treePalavras;

	@FXML
	private TreeTableColumn<Estatistica, String> treecTipo;

	@FXML
	private TreeTableColumn<Estatistica, String> treecLeitura;

	@FXML
	private TreeTableColumn<Estatistica, String> treecQuantidade;

	@FXML
	private TreeTableColumn<Estatistica, String> treecPercentual;

	@FXML
	private TreeTableColumn<Estatistica, String> treecMedia;

	@FXML
	private TreeTableColumn<Estatistica, Boolean> treecGerar;

	@FXML
	private TableView<Tabela> tbVocabulario;

	@FXML
	private TableColumn<Tabela, String> tcVocabulario;

	@FXML
	private TableColumn<Tabela, String> tcLeitura;

	@FXML
	private TableColumn<Tabela, String> tcTabela;

	private List<List<Estatistica>> listaPalavra = new ArrayList<List<Estatistica>>();
	private List<Tabela> combinacoes = new ArrayList<>();
	private ObservableList<Tabela> obsLCombinacoes;
	private EstatisticaServices estatisticaServ;

	private Animacao animacao = new Animacao();
	private Robot robot = new Robot();

	final PseudoClass pesquisa = PseudoClass.getPseudoClass("pesquisa");

	@FXML
	private void onBtnVerificaConexao() {
		verificaConexao();
	}

	@FXML
	private void onBtnProcessar() {
		if (!txtVocabulario.getText().isEmpty()) {
			processarVocabulario();
		}
	}

	@FXML
	private void onBtnGerarTabela() {
		geraProcessaLista();
	}

	private void pesquisaVocabulario() {
		if (!txtPesquisa.getText().isEmpty()) {

			try {
				combinacoes.addAll(estatisticaServ.pesquisa(txtPesquisa.getText()));
				obsLCombinacoes = FXCollections.observableArrayList(combinacoes);
				tbVocabulario.setItems(obsLCombinacoes);
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				AlertasPopup.ErroModal(rootStackPane, root, null, "Não foi possível realizar a pesquisa.",
						e.getMessage());
			}

		}
	}

	// UNICODE RANGE : DESCRIPTION
	//
	// 3000-303F : punctuation
	// 3040-309F : hiragana
	// 30A0-30FF : katakana
	// FF00-FFEF : Full-width roman + half-width katakana
	// 4E00-9FAF : Common and uncommon kanji
	//
	// Non-Japanese punctuation/formatting characters commonly used in Japanese text
	// 2605-2606 : Stars
	// 2190-2195 : Arrows
	// u203B : Weird asterisk thing

	final private String pattern = ".*[\u4E00-\u9FAF].*";

	private void processarVocabulario() {
		listaPalavra.clear();
		String hiragana = "";
		String letra;
		try {
			TreeItem<Estatistica> root = new TreeItem<>(new Estatistica("Kanjis"));
			for (int i = 0; i < txtVocabulario.getText().length(); i++) {

				letra = txtVocabulario.getText().substring(i, i + 1);

				if (!letra.matches(pattern)) {
					hiragana += letra;
				} else {
					// Antes de iniciar o processo, verificar se tem hiraganas adicionados
					if (!hiragana.isEmpty()) {
						adicionaHiragana(root, hiragana);
						hiragana = "";
					}

					Estatistica titulo = new Estatistica(letra);

					TreeItem<Estatistica> kanji = new TreeItem<>(titulo);

					List<Estatistica> estatistica = estatisticaServ.select(letra);

					if (estatistica.size() <= 0) {
						Estatistica novo = new Estatistica();
						novo.setKanji(letra);
						novo.setLeitura(letra);
						novo.setGerar(true);
						estatistica.add(novo);
						kanji.getChildren().add(new TreeItem<>(novo));
					} else {
						for (Estatistica ls : estatistica)
							kanji.getChildren().add(new TreeItem<>(ls));
					}

					root.getChildren().add(kanji);
					listaPalavra.add(estatistica);
				}

			}
			// Caso não possua mais kanjis será necessário adicionar os hiragana
			if (!hiragana.isEmpty())
				adicionaHiragana(root, hiragana);

			treePalavras.setRoot(root);
			treePalavras.setShowRoot(false);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			AlertasPopup.ErroModal(rootStackPane, root, null, "Erro ao processar vocabulario", e.getMessage());
		}
	}

	private void adicionaHiragana(TreeItem<Estatistica> root, String hiragana) {
		Estatistica novo = new Estatistica();
		novo.setKanji(hiragana);
		novo.setLeitura(hiragana);
		novo.setGerar(true);

		Estatistica titulo = new Estatistica(hiragana);

		TreeItem<Estatistica> tree_hiragana = new TreeItem<>(titulo);
		tree_hiragana.getChildren().add(new TreeItem<>(novo));

		List<Estatistica> estatistica = new ArrayList<>();
		estatistica.add(novo);

		root.getChildren().add(tree_hiragana);
		listaPalavra.add(estatistica);
	}

	public void geraProcessaLista() {
		combinacoes.clear();

		List<Set<Estatistica>> selecao = new ArrayList<Set<Estatistica>>();
		for (List<Estatistica> ls : listaPalavra) {
			Set<Estatistica> listaPalavra = ls.stream().filter(c -> c.isGerar()).collect(Collectors.toSet());
			selecao.add(listaPalavra);
		}

		Set<List<Estatistica>> result = Sets.cartesianProduct(selecao);

		if (result.size() <= 0)
			AlertasPopup.AlertaModal(rootStackPane, root, null, "Lista vazia.",
					"Necessário selecionar ao menos uma leitura de cada kanji.");
		else {
			for (List<Estatistica> ls : result)
				combinacoes.add(new Tabela(ls));

			obsLCombinacoes = FXCollections.observableArrayList(combinacoes);
			tbVocabulario.setItems(obsLCombinacoes);
		}
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
			}
		};

		Thread t = new Thread(verificaConexao);
		t.setDaemon(true);
		t.start();
	}

	private void configuraListenert() {

		txtVocabulario.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					onBtnProcessar();
					robot.keyPress(KeyCode.TAB);
				}
			}
		});

		txtPesquisa.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					pesquisaVocabulario();
				}
			}
		});
	}

	private void editaColunas() {

		tcLeitura.setCellFactory(TextFieldTableCell.forTableColumn());
		tcLeitura.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setLeitura(e.getNewValue());
			tbVocabulario.requestFocus();
		});

		tcTabela.setCellFactory(TextFieldTableCell.forTableColumn());
		tcTabela.setOnEditCommit(e -> {
			e.getTableView().getItems().get(e.getTablePosition().getRow()).setTabela(e.getNewValue());
			tbVocabulario.requestFocus();
		});

		tbVocabulario.setRowFactory(tv -> {
			TableRow<Tabela> row = new TableRow<>() {
				@Override
				public void updateItem(Tabela item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null) {
						setStyle("");
						pseudoClassStateChanged(pesquisa, false);
					} else {
						if (item.isPesquisa())
							pseudoClassStateChanged(pesquisa, true);
						else
							pseudoClassStateChanged(pesquisa, false);
					}
				}
			};
			return row;
		});

		// ==== Gerar (CHECK-BOX) ===
		treecGerar.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Estatistica, Boolean>, //
				ObservableValue<Boolean>>() {

			@Override
			public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<Estatistica, Boolean> param) {
				TreeItem<Estatistica> treeItem = param.getValue();
				Estatistica esta = treeItem.getValue();
				SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(esta.isGerar());

				booleanProp.addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						esta.setGerar(newValue);
					}
				});
				return booleanProp;
			}
		});

		treecGerar.setCellFactory(
				new Callback<TreeTableColumn<Estatistica, Boolean>, TreeTableCell<Estatistica, Boolean>>() {
					@Override
					public TreeTableCell<Estatistica, Boolean> call(TreeTableColumn<Estatistica, Boolean> p) {
						CheckBoxTreeTableCell<Estatistica, Boolean> cell = new CheckBoxTreeTableCell<Estatistica, Boolean>();
						cell.setAlignment(Pos.CENTER);
						cell.getStyleClass().add("hide-non-leaf"); // Insere o tipo para ser invisivel
						return cell;
					}
				});

		// A função irá deixar invisíveis todos os checkbox que não sejam folha
		treePalavras.setRowFactory(view -> new TreeTableRow<Estatistica>() {
			{
				ChangeListener<Boolean> listener = (observable, oldValue, newValue) -> {
					pseudoClassStateChanged(FOLHA, newValue);
				};
				treeItemProperty().addListener((observable, oldItem, newItem) -> {
					if (oldItem != null) {
						oldItem.leafProperty().removeListener(listener);
					}
					if (newItem != null) {
						newItem.leafProperty().addListener(listener);
						listener.changed(null, null, newItem.isLeaf());
					} else {
						listener.changed(null, null, Boolean.FALSE);
					}
				});
			}

		});
	}

	private void linkaCelulas() {

		tcVocabulario.setCellValueFactory(new PropertyValueFactory<>("vocabulario"));
		tcLeitura.setCellValueFactory(new PropertyValueFactory<>("leitura"));
		tcTabela.setCellValueFactory(new PropertyValueFactory<>("tabela"));

		treecTipo.setCellValueFactory(new TreeItemPropertyValueFactory<>("tipo"));
		treecLeitura.setCellValueFactory(new TreeItemPropertyValueFactory<>("leitura"));
		treecQuantidade.setCellValueFactory(new TreeItemPropertyValueFactory<>("quantidade"));
		treecPercentual.setCellValueFactory(new TreeItemPropertyValueFactory<>("percentual"));
		treecMedia.setCellValueFactory(new TreeItemPropertyValueFactory<>("media"));
		treecGerar.setCellValueFactory(new TreeItemPropertyValueFactory<Estatistica, Boolean>("gerar"));

		editaColunas();

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		animacao.animaImageBanco(imgConexaoBase, imgAnimaBanco, imgAnimaBancoEspera);
		linkaCelulas();
		configuraListenert();
		verificaConexao();

		estatisticaServ = new EstatisticaServices();

	}

	// Classe interna para utilização somente aqui.
	public static class Tabela {
		private String vocabulario;
		private String leitura;
		private String tabela;
		private List<Estatistica> estatistica = new ArrayList<>();
		private boolean pesquisa = false;

		public String getVocabulario() {
			return vocabulario;
		}

		public void setVocabulario(String vocabulario) {
			this.vocabulario = vocabulario;
		}

		public String getLeitura() {
			return leitura;
		}

		public void setLeitura(String leitura) {
			this.leitura = leitura;
		}

		public String getTabela() {
			return tabela;
		}

		public void setTabela(String tabela) {
			this.tabela = tabela;
		}

		public List<Estatistica> getEstatistica() {
			return estatistica;
		}

		public void setPesquisa(boolean pesquisa) {
			this.pesquisa = pesquisa;
		}

		public boolean isPesquisa() {
			return pesquisa;
		}

		public void setEstatistica(List<Estatistica> estatistica) {
			this.estatistica.clear();
			this.estatistica.addAll(estatistica);

			vocabulario = "";
			leitura = "";

			String reading = "", chars = "", type = "", perc = "";
			String classe = "";

			for (Estatistica es : estatistica) {
				vocabulario += es.getKanji();
				leitura += es.getLeitura();

				if (es.getTipo().equalsIgnoreCase("ON"))
					classe = " class=\"o" + es.getCorSequencial() + "\">";
				else if (es.getTipo().equalsIgnoreCase("KUN"))
					classe = " class=\"k" + es.getCorSequencial() + "\">";
				else
					classe = ">";

				if (es.getLeitura().equalsIgnoreCase(es.getKanji())) {
					reading += "<td></td>";
					chars += "<td>" + es.getKanji() + "</td>";
					type += "<td></td>";
					perc += "<td></td>";
				} else {
					reading += "<td" + classe + es.getLeitura() + "</td>";
					chars += "<td>" + es.getKanji() + "</td>";
					type += "<td>" + (es.getTipo().equalsIgnoreCase("Irreg.") ? "Irr" : es.getTipo()) + "</td>";
					perc += "<td>" + es.getPercentual() + "%</td>";
				}
			}

			tabela = "<table class=\"detailed-word\"><tbody>" + "<tr class=\"reading\">" + reading + "</tr>"
					+ "<tr class=\"chars\">" + chars + "</tr>" + "<tr class=\"type\">" + type + "</tr>"
					+ "<tr class=\"perc\">" + perc + "</tr>" + "</tbody></table>";

		}

		public void addEstatistica(Estatistica estatistica) {
			this.estatistica.add(estatistica);
		}

		public Tabela() {
			this.vocabulario = "";
			this.leitura = "";
			this.tabela = "";
		}

		public Tabela(List<Estatistica> estatistica) {
			setEstatistica(estatistica);
		}

		public Tabela(String vocabulario, String leitura, String tabela, boolean pesquisa) {
			this.vocabulario = vocabulario;
			this.leitura = leitura;
			this.tabela = tabela;
			this.pesquisa = pesquisa;
		}

	}

	public static URL getFxmlLocate() {
		return EstatisticaController.class.getResource("/view/Estatistica.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
