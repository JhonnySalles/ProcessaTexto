package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Site;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.MangaServices;
import org.jisho.textosJapones.util.CheckBoxTreeTableCellCustom;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.processar.ProcessarMangas;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.util.Callback;

public class MangasController implements Initializable {

	@FXML
	private AnchorPane apGlobal;

	@FXML
	private StackPane rootStackPane;

	@FXML
	protected AnchorPane root;

	@FXML
	private JFXComboBox<Api> cbContaGoolge;

	@FXML
	private JFXComboBox<Site> cbSite;

	@FXML
	private JFXComboBox<Modo> cbModo;

	@FXML
	private JFXComboBox<Dicionario> cbDicionario;

	@FXML
	private Label lblLog;

	@FXML
	private Label lblLogConsultas;

	@FXML
	private ProgressBar barraProgressoGeral;

	@FXML
	private ProgressBar barraProgressoVolumes;

	@FXML
	private ProgressBar barraProgressoCapitulos;

	@FXML
	private ProgressBar barraProgressoPaginas;

	@FXML
	private TraduzirController traduzirController;

	@FXML
	private RevisarController revisarController;

	@FXML
	private JFXButton btnCarregar;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnGerarJson;

	@FXML
	private JFXTextField txtBase;

	@FXML
	private JFXTextField txtManga;

	@FXML
	private JFXCheckBox ckbProcessados;

	@FXML
	private JFXCheckBox ckbMarcarTodos;

	@FXML
	private TreeTableView<Manga> treeBases;

	@FXML
	private TreeTableColumn<Manga, Boolean> treecMacado;

	@FXML
	private TreeTableColumn<Manga, String> treecBase;

	@FXML
	private TreeTableColumn<Manga, String> treecManga;

	@FXML
	private TreeTableColumn<Manga, Integer> treecVolume;

	@FXML
	private TreeTableColumn<Manga, Float> treecCapitulo;

	@FXML
	private TreeTableColumn<Manga, Integer> treecPagina;

	@FXML
	private TreeTableColumn<Manga, String> treecNomePagina;

	@FXML
	private JFXButton btnTransferir;

	@FXML
	private JFXTextField txtBaseOrigem;

	@FXML
	private JFXTextField txtBaseDestino;
	
	@FXML
	private JFXCheckBox ckbCriarBase;

	private ProcessarMangas mangas;
	private MangaServices service = new MangaServices();
	private ObservableList<MangaTabela> TABELAS;

	@FXML
	private void onBtnProcessar() {
		if (btnProcessar.getAccessibleText().equalsIgnoreCase("PROCESSANDO") && mangas != null) {
			mangas.setDesativar(true);
			return;
		}

		btnProcessar.setAccessibleText("PROCESSANDO");
		btnProcessar.setText("Pausar");

		if (mangas == null)
			mangas = new ProcessarMangas(this);

		treeBases.setDisable(true);
		lblLog.setText("Iniciando o processamento..");
		mangas.processarTabelas(TABELAS);
	}

	@FXML
	private void onBtnCarregar() {
		carregar();
	}

	@FXML
	private void onBtnGerarJson() {

	}

	@FXML
	private void onBtnMarcarTodos() {
		marcarTodosFilhos(treeBases.getRoot(), ckbMarcarTodos.isSelected());
		treeBases.refresh();
	}

	@FXML
	private void onBtnTransferir() {
		transferir();
	}

	public Api getContaGoogle() {
		return cbContaGoolge.getSelectionModel().getSelectedItem();
	}

	public Site getSiteTraducao() {
		return cbSite.getSelectionModel().getSelectedItem();
	}

	public Modo getModo() {
		return cbModo.getSelectionModel().getSelectedItem();
	}

	public Dicionario getDicionario() {
		return cbDicionario.getSelectionModel().getSelectedItem();
	}

	public void habilitar() {
		treeBases.setDisable(false);
		lblLog.setText("");
		lblLogConsultas.setText("");
		btnProcessar.setAccessibleText("PROCESSAR");
		btnProcessar.setText("Processar");
		TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
		barraProgressoGeral.setProgress(0);
		barraProgressoVolumes.setProgress(0);
		barraProgressoCapitulos.setProgress(0);
		barraProgressoPaginas.setProgress(0);
	}

	public AnchorPane getRoot() {
		return root;
	}

	public StackPane getStackPane() {
		return rootStackPane;
	}

	public ProgressBar getBarraProgressoGeral() {
		return barraProgressoGeral;
	}

	public ProgressBar getBarraProgressoVolumes() {
		return barraProgressoVolumes;
	}

	public ProgressBar getBarraProgressoCapitulos() {
		return barraProgressoCapitulos;
	}

	public ProgressBar getBarraProgressoPaginas() {
		return barraProgressoPaginas;
	}

	public Label getLog() {
		return lblLog;
	}

	public Label getLogConsultas() {
		return lblLogConsultas;
	}

	private String BASE_ORIGEM, BASE_DESTINO;

	private Integer I;

	private void transferir() {
		if (txtBaseOrigem.getText().trim().equalsIgnoreCase(txtBaseDestino.getText().trim())) {
			AlertasPopup.AvisoModal(rootStackPane, root, null, "Aviso", "Favor informar outra base de destino.");
			return;
		}
		
		lblLog.setText("Transferindo dados....");
		btnTransferir.setDisable(true);
		
		BASE_ORIGEM = txtBaseOrigem.getText().trim();
		BASE_DESTINO = txtBaseDestino.getText().trim();

		barraProgressoGeral.setProgress(-1);

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		Task<Void> transferir = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					updateMessage("Carregando dados....");
					List<MangaVolume> lista = service.selectDadosTransferir(BASE_ORIGEM);
					
					if (ckbCriarBase.isSelected()) {
						updateMessage("Criando a base....");
						service.createDataBase(BASE_DESTINO);
					}

					updateMessage("Transferindo dados....");
					I = 0;
					for (MangaVolume volume : lista) {
						updateMessage("Transferindo dados.... " + volume.getManga());
						I++;
						service.insertDadosTransferir(BASE_DESTINO, volume);
						updateProgress(I, lista.size());

						Platform.runLater(() -> {
							if (TaskbarProgressbar.isSupported())
								TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, lista.size(),
										Type.NORMAL);
						});
					}

				} catch (ExcessaoBd e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void succeeded() {
				Platform.runLater(() -> {
					barraProgressoGeral.progressProperty().unbind();
					lblLog.textProperty().unbind();
					lblLog.setText("");
					btnTransferir.setDisable(false);
					barraProgressoGeral.setProgress(0);
					barraProgressoVolumes.setProgress(0);
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
					AlertasPopup.AvisoModal(rootStackPane, root, null, "Aviso", "Transferência concluida.");
				});

			}
		};
		barraProgressoGeral.progressProperty().bind(transferir.progressProperty());
		lblLog.textProperty().bind(transferir.messageProperty());
		Thread t = new Thread(transferir);
		t.start();
	}

	private Boolean PROCESSADOS;
	private String BASE;
	private String MANGA;
	private TreeItem<Manga> DADOS;

	private void carregar() {
		lblLog.setText("Carregando....");
		btnCarregar.setDisable(true);
		btnProcessar.setDisable(true);
		btnGerarJson.setDisable(true);
		treeBases.setDisable(true);

		PROCESSADOS = ckbProcessados.isSelected();
		BASE = txtBase.getText().trim();
		MANGA = txtManga.getText().trim();

		barraProgressoGeral.setProgress(-1);
		barraProgressoVolumes.setProgress(-1);

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<Void> carregaItens = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					TABELAS = FXCollections.observableArrayList(service.selectTabelas(!PROCESSADOS, BASE, MANGA));
					DADOS = getTreeData();
				} catch (ExcessaoBd e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void succeeded() {
				Platform.runLater(() -> {
					treeBases.setRoot(DADOS);
					lblLog.setText("");
					ckbMarcarTodos.setSelected(true);
					btnCarregar.setDisable(false);
					btnProcessar.setDisable(false);
					btnGerarJson.setDisable(false);
					treeBases.setDisable(false);
					barraProgressoGeral.setProgress(0);
					barraProgressoVolumes.setProgress(0);
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
				});

			}
		};
		Thread t = new Thread(carregaItens);
		t.start();
	}

	private TreeItem<Manga> getTreeData() {
		TreeItem<Manga> itmRoot = new TreeItem<Manga>(new Manga("...", ""));
		for (MangaTabela tabela : TABELAS) {
			tabela.setManga("...");
			TreeItem<Manga> itmTabela = new TreeItem<Manga>(tabela);
			TreeItem<Manga> itmManga = null;
			String volumeAnterior = "";
			for (MangaVolume volume : tabela.getVolumes()) {
				// Implementa um nivel por tipo
				if (!volume.getManga().equalsIgnoreCase(volumeAnterior) || itmManga == null) {
					volumeAnterior = volume.getManga();
					itmManga = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga()));
					itmTabela.getChildren().add(itmManga);
					itmTabela.setExpanded(true);
				}

				volume.setBase(tabela.getBase());
				TreeItem<Manga> itmVolume = new TreeItem<Manga>(volume);

				for (MangaCapitulo capitulo : volume.getCapitulos()) {
					capitulo.setBase(tabela.getBase());
					capitulo.setNomePagina("...");
					TreeItem<Manga> itmCapitulo = new TreeItem<Manga>(capitulo);
					for (MangaPagina pagina : capitulo.getPaginas()) {
						pagina.addOutrasInformacoes(tabela.getBase(), volume.getManga(), volume.getVolume(),
								capitulo.getCapitulo(), volume.getLingua());
						itmCapitulo.getChildren().add(new TreeItem<Manga>(pagina));
					}
					itmVolume.getChildren().add(itmCapitulo);
				}

				itmManga.getChildren().add(itmVolume);
			}
			itmRoot.getChildren().add(itmTabela);
			itmRoot.setExpanded(true);
		}
		return itmRoot;
	}

	private void marcarTodosFilhos(TreeItem<Manga> treeItem, Boolean newValue) {
		treeItem.getValue().setProcessar(newValue);
		treeItem.getChildren().forEach(treeItemNivel2 -> marcarTodosFilhos(treeItemNivel2, newValue));
	}

	private void ativaTodosPai(TreeItem<Manga> treeItem, Boolean newValue) {
		if (treeItem.getParent() != null) {
			treeItem.getParent().getValue().setProcessar(newValue);
			ativaTodosPai(treeItem.getParent(), newValue);
		}
	}

	private void editaColunas() {

		// ==== (CHECK-BOX) ===
		treecMacado.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<Manga, Boolean>, ObservableValue<Boolean>>() {

					@Override
					public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<Manga, Boolean> param) {
						TreeItem<Manga> treeItem = param.getValue();
						Manga item = treeItem.getValue();
						SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(item.isProcessar());

						booleanProp.addListener(new ChangeListener<Boolean>() {
							@Override
							public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
									Boolean newValue) {
								item.setProcessar(newValue);
								marcarTodosFilhos(treeItem, newValue);
								if (newValue) // Somente ativa caso seja true, pois ao menos um nó precisa estar ativo
									ativaTodosPai(treeItem, newValue);

								treeBases.refresh();
							}
						});

						return booleanProp;
					}
				});

		treecMacado.setCellFactory(new Callback<TreeTableColumn<Manga, Boolean>, TreeTableCell<Manga, Boolean>>() {
			@Override
			public TreeTableCell<Manga, Boolean> call(TreeTableColumn<Manga, Boolean> p) {
				CheckBoxTreeTableCellCustom<Manga, Boolean> cell = new CheckBoxTreeTableCellCustom<Manga, Boolean>();
				cell.setAlignment(Pos.CENTER);
				return cell;
			}
		});

	}

	private void linkaCelulas() {
		treecMacado.setCellValueFactory(new TreeItemPropertyValueFactory<Manga, Boolean>("processar"));
		treecBase.setCellValueFactory(new TreeItemPropertyValueFactory<>("base"));
		treecManga.setCellValueFactory(new TreeItemPropertyValueFactory<>("manga"));
		treecVolume.setCellValueFactory(new TreeItemPropertyValueFactory<>("volume"));
		treecCapitulo.setCellValueFactory(new TreeItemPropertyValueFactory<>("capitulo"));
		treecPagina.setCellValueFactory(new TreeItemPropertyValueFactory<>("pagina"));
		treecNomePagina.setCellValueFactory(new TreeItemPropertyValueFactory<>("nomePagina"));
		treeBases.setShowRoot(false);

		editaColunas();

	}

	private Robot robot = new Robot();

	public void initialize(URL arg0, ResourceBundle arg1) {
		cbContaGoolge.getItems().addAll(Api.values());
		cbContaGoolge.getSelectionModel().selectFirst();

		cbSite.getItems().addAll(Site.values());
		cbSite.getSelectionModel().selectFirst();

		cbModo.getItems().addAll(Modo.values());
		cbModo.getSelectionModel().select(Modo.C);

		cbDicionario.getItems().addAll(Dicionario.values());
		cbDicionario.getSelectionModel().select(Dicionario.FULL);

		txtBase.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		txtManga.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		linkaCelulas();

		revisarController.setAnime(false);
		revisarController.setManga(true);
		revisarController.pesquisar();
	}

	public static URL getFxmlLocate() {
		return MangasController.class.getResource("/view/Manga.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
