package org.jisho.textosJapones.controller.mangas;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.CheckBoxTreeTableCellCustom;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.database.mysql.ConexaoMysql;
import org.jisho.textosJapones.model.entities.comicinfo.BaseLista;
import org.jisho.textosJapones.model.entities.comicinfo.MAL;
import org.jisho.textosJapones.model.entities.comicinfo.MAL.Registro;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.processar.comicinfo.ProcessaComicInfo;

import com.jfoenix.controls.JFXButton;
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
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

public class MangasComicInfoController implements Initializable {

	final PseudoClass vinculado = PseudoClass.getPseudoClass("vinculado");

	@FXML
	private AnchorPane apRoot;

	@FXML
	private JFXComboBox<Language> cbLinguagem;

	@FXML
	private JFXTextField txtCaminho;

	@FXML
	private JFXButton btnCaminho;

	@FXML
	private JFXButton btnArquivo;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnProcessarMarcados;

	@FXML
	private JFXButton btnLimparLista;

	@FXML
	private TreeTableView<BaseLista> treeTabela;

	@FXML
	private TreeTableColumn<BaseLista, Boolean> treecMacado;

	@FXML
	private TreeTableColumn<BaseLista, String> treecManga;

	@FXML
	private TreeTableColumn<BaseLista, String> treecNome;

	@FXML
	private TreeTableColumn<BaseLista, String> treecMalID;

	@FXML
	private TreeTableColumn<BaseLista, String> treecProcessar;

	@FXML
	private TreeTableColumn<BaseLista, String> treecSite;

	@FXML
	private TreeTableColumn<BaseLista, ImageView> treecImagem;

	private ObservableList<MAL> REGISTROS = FXCollections.observableArrayList();
	private MangasController controller;

	public void setControllerPai(MangasController controller) {
		this.controller = controller;
	}

	public MangasController getControllerPai() {
		return controller;
	}

	@FXML
	private void onBtnProcessar() {
		if (btnProcessar.accessibleTextProperty().getValue().equals("PROCESSAR"))
			processar();
		else
			cancelar();
	}

	@FXML
	private void onBtnProcessarMarcados() {
		if (btnProcessarMarcados.accessibleTextProperty().getValue().equals("PROCESSAR"))
			processarLista(false);
		else
			PARAR = true;
	}

	@FXML
	private void onBtnCarregarCaminho() {
		txtCaminho.setText(selecionaPasta(txtCaminho.getText(), false));
	}

	@FXML
	private void onBtnCarregarArquivo() {
		txtCaminho.setText(selecionaPasta(txtCaminho.getText(), true));
	}

	@FXML
	private void onBtnLimparLista() {
		REGISTROS.clear();
		configuraTabela();
	}

	private void ativaCampos() {
		treeTabela.setDisable(false);
		btnProcessarMarcados.setDisable(false);
		btnProcessar.setDisable(false);
		btnLimparLista.setDisable(false);
	}

	private void bloqueiaCampos(Boolean isProcessar) {
		btnLimparLista.setDisable(true);
		treeTabela.setDisable(true);
		if (isProcessar)
			btnProcessarMarcados.setDisable(true);
		else
			btnProcessar.setDisable(true);
	}

	private String selecionaPasta(String local, Boolean isArquivo) {
		String pasta = "";
		File caminho = null;

		if (local != null && !local.isEmpty()) {
			caminho = new File(local);

			if (caminho.isFile()) {
				String file = caminho.getAbsolutePath();
				file = file.substring(0, file.indexOf(caminho.getName()));
				caminho = new File(file);
			}
		}

		if (isArquivo) {
			FileChooser fileChooser = new FileChooser();

			if (caminho != null)
				fileChooser.setInitialDirectory(caminho);

			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Arquivos", "*.cbr", "*.cbz",
					"*.rar", "*.zip");
			fileChooser.getExtensionFilters().add(extFilter);
			fileChooser.setTitle("Selecione o arquivo de destino");

			File file = fileChooser.showOpenDialog(null);
			pasta = file == null ? "" : file.getAbsolutePath();
		} else {
			DirectoryChooser fileChooser = new DirectoryChooser();
			fileChooser.setTitle("Selecione a pasta de destino");

			if (caminho != null)
				fileChooser.setInitialDirectory(caminho);

			File file = fileChooser.showDialog(null);
			pasta = file == null ? "" : file.getAbsolutePath();
		}

		return pasta;
	}

	private void cancelar() {
		ProcessaComicInfo.cancelar();
	}

	private void processar() {
		GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		progress.getTitulo().setText("ComicInfo");
		Task<Void> gerarJson = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					updateMessage("Processando itens....");

					Callback<Integer[], Boolean> callback = new Callback<Integer[], Boolean>() {
						@Override
						public Boolean call(Integer[] param) {
							Platform.runLater(() -> {
								updateMessage("Processando itens...." + param[0] + '/' + param[1]);
								updateProgress(param[0], param[1]);
								if (TaskbarProgressbar.isSupported())
									TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), param[0], param[1],
											Type.NORMAL);
							});
							return null;
						}
					};

					ProcessaComicInfo.processa(ConexaoMysql.getCaminhoWinrar(), cbLinguagem.getValue(),
							txtCaminho.getText(), callback);

				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void succeeded() {
				super.failed();
				Platform.runLater(() -> {
					ativaCampos();
					btnProcessar.setAccessibleText("PROCESSAR");
					btnProcessar.setText("Processar");
					progress.getBarraProgresso().progressProperty().unbind();
					progress.getLog().textProperty().unbind();
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
					MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
				});

			}

			@Override
			protected void failed() {
				super.failed();
				ativaCampos();
				btnProcessar.setAccessibleText("PROCESSAR");
				btnProcessar.setText("Processar");
				System.out.println("Erro na thread ComicInfo: " + super.getMessage());
			}
		};

		progress.getBarraProgresso().progressProperty().bind(gerarJson.progressProperty());
		progress.getLog().textProperty().bind(gerarJson.messageProperty());

		Thread t = new Thread(gerarJson);
		t.start();
		btnProcessar.setText("Cancelar");
		btnProcessar.setAccessibleText("PROCESSANDO");
		bloqueiaCampos(true);
	}

	private Integer I = 0;
	private Boolean PARAR = false;

	private void processarLista(Boolean isSelecionado) {
		GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		progress.getTitulo().setText("ComicInfo");
		Task<Void> gerarJson = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					updateMessage("Processando itens....");
					PARAR = false;
					I = 0;

					List<MAL> lista = isSelecionado
							? REGISTROS.parallelStream()
									.filter(it -> it.isSelecionado()
											|| it.getMyanimelist().parallelStream().anyMatch(re -> re.isSelecionado()))
									.toList()
							: REGISTROS.parallelStream().toList();

					for (MAL item : lista) {
						I++;

						Platform.runLater(() -> {
							updateMessage(
									"Processando itens...." + I + '/' + lista.size() + " - Manga: " + item.getNome());
							updateProgress(I, lista.size());
							if (TaskbarProgressbar.isSupported())
								TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, lista.size(),
										Type.NORMAL);
						});

						Optional<Registro> registro = item.getMyanimelist().stream().filter(it -> it.isMarcado())
								.findFirst();

						if (registro.isPresent()) {
							if (ProcessaComicInfo.processa(ConexaoMysql.getCaminhoWinrar(), cbLinguagem.getValue(),
									registro.get().getParent().getArquivo(), registro.get().getId()))
								REGISTROS.remove(item);

						}

						if (PARAR)
							break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void succeeded() {
				super.failed();
				Platform.runLater(() -> {
					ativaCampos();
					configuraTabela();
					btnProcessarMarcados.setAccessibleText("PROCESSAR");
					btnProcessarMarcados.setText("Processar Marcados");
					progress.getBarraProgresso().progressProperty().unbind();
					progress.getLog().textProperty().unbind();
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
					MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
				});

			}

			@Override
			protected void failed() {
				super.failed();
				ativaCampos();
				configuraTabela();
				btnProcessarMarcados.setAccessibleText("PROCESSAR");
				btnProcessarMarcados.setText("Processar Marcados");
				System.out.println("Erro na thread ComicInfo: " + super.getMessage());
			}
		};

		progress.getBarraProgresso().progressProperty().bind(gerarJson.progressProperty());
		progress.getLog().textProperty().bind(gerarJson.messageProperty());

		Thread t = new Thread(gerarJson);
		t.start();
		btnProcessarMarcados.setText("Cancelar");
		btnProcessarMarcados.setAccessibleText("PROCESSANDO");
		bloqueiaCampos(false);
	}

	private void openSiteMal(Long id) {
		try {
			Desktop.getDesktop().browse(new URI("https://myanimelist.net/manga/" + id));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public void addItem(MAL item) {
		REGISTROS.add(item);
		configuraTabela();
	}

	private TreeItem<BaseLista> getTreeData() {
		TreeItem<BaseLista> itmRoot = new TreeItem<BaseLista>(new BaseLista("...", "", null, false));
		for (MAL item : REGISTROS) {
			TreeItem<BaseLista> itmManga = new TreeItem<BaseLista>(item);

			// ---------------- Mal ---------------- //
			for (Registro registro : item.getMyanimelist()) {
				TreeItem<BaseLista> reg = new TreeItem<BaseLista>(registro);
				JFXButton processar = new JFXButton("Processar");
				processar.getStyleClass().add("background-White1");
				processar.setOnAction(event -> {
					String arquivo = registro.getParent().getArquivo();
					if (ProcessaComicInfo.processa(ConexaoMysql.getCaminhoWinrar(), cbLinguagem.getValue(), arquivo,
							registro.getId())) {
						REGISTROS.remove(item);
						itmRoot.getChildren().remove(itmManga);
						treeTabela.refresh();
					}
				});
				JFXButton site = new JFXButton("Site");
				site.getStyleClass().add("background-White1");
				site.setOnAction(event -> openSiteMal(registro.getId()));
				reg.getValue().setButton(processar, site);
				itmManga.getChildren().add(reg);
			}

			// ---------------- Adicionado na tabela ---------------- //
			itmRoot.getChildren().add(itmManga);
			itmRoot.setExpanded(true);
		}
		return itmRoot;
	}

	private TreeItem<BaseLista> DADOS;

	private void configuraTabela() {
		try {
			DADOS = getTreeData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		treeTabela.setRoot(DADOS);
	}

	private void onBtnTrocaId() {
		if (!REGISTROS.parallelStream()
				.anyMatch(it -> it.isSelecionado() || it.getMyanimelist().stream().anyMatch(re -> re.isSelecionado()))
				&& treeTabela.selectionModelProperty().getValue() != null)
			treeTabela.selectionModelProperty().getValue().getSelectedItem().getValue().setSelecionado(true);

		Callback<Registro, Boolean> callback = new Callback<Registro, Boolean>() {
			@Override
			public Boolean call(Registro param) {

				REGISTROS.parallelStream().filter(
						it -> it.isSelecionado() || it.getMyanimelist().stream().anyMatch(re -> re.isSelecionado()))
						.forEach(it -> {
							if (it.isSelecionado()) {
								it.getMyanimelist().parallelStream().forEach(re -> re.setMarcado(false));
								Registro reg = it.getMyanimelist().get(0);
								reg.setMarcado(true);
								reg.setId(param.getId());
								reg.setNome(param.getNome());
								reg.setImagem(param.getImagem());
							} else {
								it.getMyanimelist().forEach(re -> re.setMarcado(false));
								Optional<Registro> reg = it.getMyanimelist().parallelStream()
										.filter(re -> re.isSelecionado()).findFirst();
								if (reg.isPresent()) {
									reg.get().setMarcado(true);
									reg.get().setId(param.getId());
									reg.get().setNome(param.getNome());
									reg.get().setImagem(param.getImagem());
								} else {
									Registro aux = it.getMyanimelist().get(0);
									aux.setMarcado(true);
									aux.setId(param.getId());
									aux.setNome(param.getNome());
									aux.setImagem(param.getImagem());
								}
							}

							it.setSelecionado(false);
							it.getMyanimelist().parallelStream().forEach(re -> re.setSelecionado(false));
						});

				treeTabela.refresh();
				return null;
			}
		};

		MangasComicInfoMalId.abreTelaCorrecao(controller.getStackPane(), controller.getRoot(), callback);
	}

	private void editaColunas() {
		// ==== (CHECK-BOX) ===
		treecMacado.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<BaseLista, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<BaseLista, Boolean> param) {
						TreeItem<BaseLista> treeItem = param.getValue();
						if (treeItem.getValue() instanceof Registro) {
							Registro item = (Registro) treeItem.getValue();
							SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(item.isMarcado());

							booleanProp.addListener(new ChangeListener<Boolean>() {
								@Override
								public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
										Boolean newValue) {
									item.setMarcado(newValue);
									if (newValue) {
										MAL parent = item.getParent();

										for (Registro aux : parent.getMyanimelist()) {
											if (!aux.getId().equals(item.getId()))
												aux.setMarcado(false);
										}
									}

									treeTabela.refresh();
								}
							});

							return booleanProp;
						}
						return null;
					}
				});

		treecMacado
				.setCellFactory(new Callback<TreeTableColumn<BaseLista, Boolean>, TreeTableCell<BaseLista, Boolean>>() {
					@Override
					public TreeTableCell<BaseLista, Boolean> call(TreeTableColumn<BaseLista, Boolean> p) {
						CheckBoxTreeTableCellCustom<BaseLista, Boolean> cell = new CheckBoxTreeTableCellCustom<BaseLista, Boolean>();
						cell.setAlignment(Pos.CENTER);
						return cell;
					}
				});
	}

	private void limparSelecao() {
		anteriorSelecionado = null;
		REGISTROS.parallelStream()
				.filter(it -> it.isSelecionado() || it.getMyanimelist().stream().allMatch(re -> re.isSelecionado()))
				.forEach(it -> {
					it.setSelecionado(false);
					it.getMyanimelist().parallelStream().forEach(re -> re.setSelecionado(false));
				});
		treeTabela.refresh();
	}

	private void remover() {
		if (treeTabela.getSelectionModel().getSelectedItem() != null)
			if (AlertasPopup.ConfirmacaoModal("Aviso","Deseja remover o registro?")) {
				BaseLista parent = treeTabela.getSelectionModel().getSelectedItem()
						.getValue() instanceof MAL
								? treeTabela.getSelectionModel().getSelectedItem().getValue()
								: treeTabela.getSelectionModel().getSelectedItem().getParent().getValue();
				REGISTROS.remove(parent);
				treeTabela.refresh();
			}
	}

	private TreeItem<BaseLista> anteriorSelecionado = null;

	private void selecionaRegistros() {
		treeTabela.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent click) {
				if (click.getClickCount() > 1) {
					if (click.isControlDown()) {
						limparSelecao();
						return;
					} else if (click.isShiftDown()) {
						if (anteriorSelecionado != null
								&& anteriorSelecionado != treeTabela.getSelectionModel().getSelectedItem()) {
							BaseLista parentAnterio = anteriorSelecionado.getValue() instanceof MAL
									? anteriorSelecionado.getValue()
									: anteriorSelecionado.getParent().getValue();
							BaseLista parentAtual = treeTabela.getSelectionModel().getSelectedItem()
									.getValue() instanceof MAL
											? treeTabela.getSelectionModel().getSelectedItem().getValue()
											: treeTabela.getSelectionModel().getSelectedItem().getParent().getValue();

							int index = 0;
							int size = 0;
							if (REGISTROS.indexOf(parentAnterio) > REGISTROS.indexOf(parentAtual)) {
								index = REGISTROS.indexOf(parentAtual);
								size = REGISTROS.indexOf(parentAnterio) - 1;
							} else {
								index = REGISTROS.indexOf(parentAnterio) + 1;
								size = REGISTROS.indexOf(parentAtual);
							}

							if (index < 0)
								index = 0;

							if (size > REGISTROS.size())
								size = REGISTROS.size();

							for (int i = index; i <= size; i++)
								REGISTROS.get(i).setSelecionado(!REGISTROS.get(i).isSelecionado());
						}
					} else {
						BaseLista item = treeTabela.getSelectionModel().getSelectedItem().getValue();
						if (item != null)
							item.setSelecionado(!item.isSelecionado());
					}
					treeTabela.refresh();
					anteriorSelecionado = treeTabela.getSelectionModel().getSelectedItem();
				}
			}
		});

		PseudoClass selected = PseudoClass.getPseudoClass("selected");

		treeTabela.setRowFactory(tv -> {
			ContextMenu menu = new ContextMenu();

			MenuItem alterarId = new MenuItem("Alterar id");
			alterarId.setOnAction(e -> onBtnTrocaId());

			MenuItem processar = new MenuItem("Processar selecionado(s)");
			processar.setOnAction(e -> {
				if (btnProcessarMarcados.accessibleTextProperty().getValue().equals("PROCESSAR"))
					processarLista(true);
			});

			MenuItem limparSelecao = new MenuItem("Limpar seleção");
			limparSelecao.setOnAction(e -> limparSelecao());

			MenuItem remover = new MenuItem("Remover registro");
			remover.setOnAction(e -> remover());

			menu.getItems().add(alterarId);
			menu.getItems().add(processar);
			menu.getItems().add(limparSelecao);
			menu.getItems().add(remover);

			TreeTableRow<BaseLista> row = new TreeTableRow<>() {
				@Override
				public void updateItem(BaseLista item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null) {
						setStyle("");
						pseudoClassStateChanged(selected, false);
					} else {
						setContextMenu(menu);
						if (item.isSelecionado())
							pseudoClassStateChanged(selected, true);
						else
							pseudoClassStateChanged(selected, false);
					}
				}
			};

			return row;
		});

		/*
		 * MAL mal = new MAL("teste 1", ""); mal.addRegistro("reg1", 1L, true);
		 * mal.addRegistro("reg2", 2L, false); REGISTROS.add(mal);
		 * 
		 * mal = new MAL("teste 2", ""); mal.addRegistro("reg1", 1L, false);
		 * mal.addRegistro("reg2", 2L, true); REGISTROS.add(mal);
		 * 
		 * mal = new MAL("teste 3", ""); mal.addRegistro("reg1", 1L, false);
		 * mal.addRegistro("reg2", 2L, true); REGISTROS.add(mal);
		 * 
		 * mal = new MAL("teste 4", ""); mal.addRegistro("reg1", 1L, false);
		 * mal.addRegistro("reg2", 2L, true); REGISTROS.add(mal);
		 * 
		 * mal = new MAL("teste 5", ""); mal.addRegistro("reg1", 1L, false);
		 * mal.addRegistro("reg2", 2L, true); REGISTROS.add(mal);
		 * 
		 * mal = new MAL("teste 6", ""); mal.addRegistro("reg1", 1L, false);
		 * mal.addRegistro("reg2", 2L, true); mal.addRegistro("reg3", 3L, false);
		 * mal.addRegistro("reg4", 4L, false); REGISTROS.add(mal);
		 * 
		 * mal = new MAL("teste 7", ""); mal.addRegistro("reg1", 1L, false);
		 * mal.addRegistro("reg2", 2L, true); REGISTROS.add(mal);
		 * 
		 * mal = new MAL("teste 8", ""); mal.addRegistro("reg1", 1L, false);
		 * mal.addRegistro("reg2", 2L, true); mal.addRegistro("reg3", 3L, false);
		 * mal.addRegistro("reg4", 4L, false); addItem(mal);
		 */

	}

	private void linkaCelulas() {
		treecMacado.setCellValueFactory(new TreeItemPropertyValueFactory<BaseLista, Boolean>("marcado"));
		treecManga.setCellValueFactory(new TreeItemPropertyValueFactory<>("descricao"));
		treecNome.setCellValueFactory(new TreeItemPropertyValueFactory<>("nome"));
		treecMalID.setCellValueFactory(new TreeItemPropertyValueFactory<>("idVisual"));
		treecProcessar.setCellValueFactory(new TreeItemPropertyValueFactory<>("processar"));
		treecSite.setCellValueFactory(new TreeItemPropertyValueFactory<>("site"));
		treecImagem.setCellValueFactory(new TreeItemPropertyValueFactory<>("imagem"));
		treeTabela.setShowRoot(false);

		treecMalID.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
		treecMalID.setOnEditCommit(e -> {
			if (e.getNewValue() != null && !e.getNewValue().isEmpty()) {
				try {
					String number = e.getNewValue().replaceAll("/[^0-9]+/g", "");
					if (!number.isEmpty() && e.getTreeTableView().getTreeItem(e.getTreeTablePosition().getRow())
							.getValue() instanceof Registro) {
						if (!ProcessaComicInfo.getById(Long.valueOf(number), (Registro) e.getTreeTableView()
								.getTreeItem(e.getTreeTablePosition().getRow()).getValue()) && e.getOldValue() != null
								&& !e.getOldValue().isEmpty())
							e.getTreeTableView().getTreeItem(e.getTreeTablePosition().getRow()).getValue()
									.setId(Long.valueOf(e.getOldValue()));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					if (e.getOldValue() != null && !e.getOldValue().isEmpty())
						e.getTreeTableView().getTreeItem(e.getTreeTablePosition().getRow()).getValue()
								.setId(Long.valueOf(e.getOldValue()));
				}
			} else if (e.getOldValue() != null && !e.getOldValue().isEmpty())
				e.getTreeTableView().getTreeItem(e.getTreeTablePosition().getRow()).getValue()
						.setId(Long.valueOf(e.getOldValue()));
			treeTabela.requestFocus();
			treeTabela.refresh();
		});

		editaColunas();
		selecionaRegistros();
	}

	private Robot robot = new Robot();

	public void initialize(URL arg0, ResourceBundle arg1) {
		ProcessaComicInfo.setPai(this);
		cbLinguagem.getItems().addAll(Language.PORTUGUESE, Language.ENGLISH, Language.JAPANESE);
		cbLinguagem.getSelectionModel().selectFirst();

		cbLinguagem.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ESCAPE))
					cbLinguagem.getSelectionModel().clearSelection();
				else if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		linkaCelulas();
	}

	public static URL getFxmlLocate() {
		return MangasComicInfoController.class.getResource("/view/mangas/MangaComicInfo.fxml");
	}

}
