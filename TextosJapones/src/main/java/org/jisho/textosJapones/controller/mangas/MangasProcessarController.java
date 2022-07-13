package org.jisho.textosJapones.controller.mangas;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.MangaServices;
import org.jisho.textosJapones.util.CheckBoxTreeTableCellCustom;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.processar.ProcessarMangas;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.util.Callback;

public class MangasProcessarController implements Initializable {

	@FXML
	protected AnchorPane apRoot;

	@FXML
	private JFXButton btnCarregar;

	@FXML
	private JFXButton btnProcessar;

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

	private MangasController controller;

	public void setControllerPai(MangasController controller) {
		this.controller = controller;
	}

	public MangasController getControllerPai() {
		return controller;
	}

	@FXML
	private void onBtnProcessar() {
		if (btnProcessar.getAccessibleText().equalsIgnoreCase("PROCESSANDO") && mangas != null) {
			mangas.setDesativar(true);
			return;
		}

		btnProcessar.setAccessibleText("PROCESSANDO");
		btnProcessar.setText("Pausar");
		btnCarregar.setDisable(true);

		if (mangas == null)
			mangas = new ProcessarMangas(this);

		treeBases.setDisable(true);
		MenuPrincipalController.getController().getLblLog().setText("Iniciando o processamento dos mangas...");
		mangas.processarTabelas(TABELAS);
	}

	@FXML
	private void onBtnCarregar() {
		carregar();
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

	public void habilitar() {
		treeBases.setDisable(false);
		MenuPrincipalController.getController().getLblLog().setText("");
		btnProcessar.setAccessibleText("PROCESSAR");
		btnProcessar.setText("Processar");
		btnCarregar.setDisable(false);
		TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
		controller.getBarraProgressoVolumes().setProgress(0);
		controller.getBarraProgressoCapitulos().setProgress(0);
		controller.getBarraProgressoPaginas().setProgress(0);
	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	public ProgressBar getBarraProgressoVolumes() {
		return controller.getBarraProgressoVolumes();
	}

	public ProgressBar getBarraProgressoCapitulos() {
		return controller.getBarraProgressoCapitulos();
	}

	public ProgressBar getBarraProgressoPaginas() {
		return controller.getBarraProgressoPaginas();
	}

	private String BASE_ORIGEM, BASE_DESTINO;

	private Integer I;
	private String error;

	private void transferir() {
		if (txtBaseOrigem.getText().trim().equalsIgnoreCase(txtBaseDestino.getText().trim())) {
			AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Aviso",
					"Favor informar outra base de destino.");
			return;
		}

		GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
		progress.getTitulo().setText("Mangas - Transferencia");
		progress.getLog().setText("Transferindo dados....");
		btnTransferir.setDisable(true);

		BASE_ORIGEM = txtBaseOrigem.getText().trim();
		BASE_DESTINO = txtBaseDestino.getText().trim();

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		Task<Void> transferir = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					error = "";
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
					error = e.getMessage();
				}
				return null;
			}

			@Override
			protected void succeeded() {
				Platform.runLater(() -> {
					progress.getBarraProgresso().progressProperty().unbind();
					progress.getLog().textProperty().unbind();
					MenuPrincipalController.getController().destroiBarraProgresso(progress, "");

					btnTransferir.setDisable(false);
					controller.getBarraProgressoVolumes().setProgress(0);
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

					if (!error.isEmpty())
						AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro", error);
					else
						AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Aviso",
								"Transferência concluida.");
				});

			}
		};
		progress.getBarraProgresso().progressProperty().bind(transferir.progressProperty());
		progress.getLog().textProperty().bind(transferir.messageProperty());
		Thread t = new Thread(transferir);
		t.start();
	}

	private Boolean PROCESSADOS;
	private String BASE;
	private String MANGA;
	private TreeItem<Manga> DADOS;

	private void carregar() {
		MenuPrincipalController.getController().getLblLog().setText("Carregando dados dos mangas...");
		btnCarregar.setDisable(true);
		btnProcessar.setDisable(true);
		treeBases.setDisable(true);

		PROCESSADOS = ckbProcessados.isSelected();
		BASE = txtBase.getText().trim();
		MANGA = txtManga.getText().trim();

		controller.getBarraProgressoCapitulos().setProgress(-1);
		controller.getBarraProgressoVolumes().setProgress(-1);

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<Void> carregaItens = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					service = new MangaServices();
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
					MenuPrincipalController.getController().getLblLog().setText("");
					ckbMarcarTodos.setSelected(true);
					btnCarregar.setDisable(false);
					btnProcessar.setDisable(false);
					treeBases.setDisable(false);
					controller.getBarraProgressoCapitulos().setProgress(0);
					controller.getBarraProgressoVolumes().setProgress(0);
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
					volume.setBase(tabela.getBase());
					itmManga = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(), "..."));
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
	}

	public static URL getFxmlLocate() {
		return MangasProcessarController.class.getResource("/view/mangas/MangaProcessar.fxml");
	}
}
