package org.jisho.textosJapones.controller.mangas;

import com.jfoenix.controls.*;
import com.nativejavafx.taskbar.TaskbarProgressbar;
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
import javafx.scene.control.*;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.util.Callback;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.CheckBoxTreeTableCellCustom;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.message.Mensagens;
import org.jisho.textosJapones.model.services.MangaServices;
import org.jisho.textosJapones.util.converter.FloatConverter;
import org.jisho.textosJapones.util.converter.IntegerConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MangasAjustarController implements Initializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MangasAjustarController.class);

	@FXML
	private AnchorPane apRoot;
	
	@FXML
	private JFXComboBox<String> cbBase;

	@FXML
	private JFXTextField txtManga;

	@FXML
	private JFXComboBox<Language> cbLinguagem;

	@FXML
	private Spinner<Integer> spnVolume;

	@FXML
	private Spinner<Double> spnCapitulo;

	@FXML
	private JFXButton btnCarregar;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private JFXCheckBox ckbReprocessarDemais;

	@FXML
	private JFXCheckBox ckbInverterOrdemTexto;

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
	private TreeTableColumn<Manga, Language> treecLinguagem;

	@FXML
	private TreeTableColumn<Manga, Integer> treecVolumeOrigem;

	@FXML
	private TreeTableColumn<Manga, Float> treecCapituloOrigem;

	@FXML
	private TreeTableColumn<Manga, Integer> treecVolumeDestino;

	@FXML
	private TreeTableColumn<Manga, Float> treecCapituloDestino;

	@FXML
	private TreeTableColumn<Manga, Boolean> treecExtra;

	@FXML
	private TreeTableColumn<Manga, String> treecPagina;

	@FXML
	private TreeTableColumn<Manga, String> treecTexto;

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
	private void onBtnCarregar() {
		carregar();
	}

	@FXML
	private void onBtnSalvar() {
		if (TABELAS != null && !TABELAS.isEmpty())
			salvar();
		else
			AlertasPopup.AvisoModal("Aviso", "Nenhum item para salvar.");
	}

	@FXML
	private void onBtnMarcarTodos() {
		marcarTodosFilhos(treeBases.getRoot(), ckbMarcarTodos.isSelected());
		treeBases.refresh();
	}

	private String BASE;
	private String MANGA;
	private Integer VOLUME;
	private Float CAPITULO;
	private Language LINGUAGEM;
	private TreeItem<Manga> DADOS;

	private void carregar() {
		MenuPrincipalController.getController().getLblLog().setText("Carregando informações...");

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		btnCarregar.setDisable(true);
		btnSalvar.setDisable(true);
		treeBases.setDisable(true);
		BASE = cbBase.getValue() != null ? cbBase.getValue().trim() : "";
		MANGA = txtManga.getText().trim();
		VOLUME = spnVolume.getValue();
		CAPITULO = spnCapitulo.getValue().floatValue();
		LINGUAGEM = cbLinguagem.getSelectionModel().getSelectedItem();

		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<Void> carregaItens = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try {
					service = new MangaServices();
					TABELAS = FXCollections.observableArrayList(service.selectTabelasJson(BASE, MANGA, VOLUME, CAPITULO,
							LINGUAGEM, ckbInverterOrdemTexto.isSelected()));
					DADOS = getTreeData();
				} catch (ExcessaoBd e) {
					
					LOGGER.error(e.getMessage(), e);
					throw new Exception(Mensagens.BD_ERRO_SELECT);
				}
				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				Platform.runLater(() -> {
					treeBases.setRoot(DADOS);

					MenuPrincipalController.getController().getLblLog().setText("");
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

					ckbMarcarTodos.setSelected(true);
					btnCarregar.setDisable(false);
					btnSalvar.setDisable(false);
					treeBases.setDisable(false);
				});

			}

			@Override
			protected void failed() {
				super.failed();
				LOGGER.warn("Erro na thread de carregamento de itens: " + super.getMessage());
				System.out.print("Erro na thread de carregamento de itens: " + super.getMessage());
			}
		};
		Thread t = new Thread(carregaItens);
		t.start();
	}

	private void salvar() {
		MenuPrincipalController.getController().getLblLog().setText("Salvando as informações corrigidas...");

		if (TaskbarProgressbar.isSupported())
			TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

		btnCarregar.setDisable(true);
		btnSalvar.setDisable(true);
		treeBases.setDisable(true);

		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<Void> carregaItens = new Task<Void>() {

			@Override
			protected Void call() {
				try {
					for (MangaTabela tabela : TABELAS) {
						for (MangaVolume volume : tabela.getVolumes()) {
							if (volume.isAlterado())
								volume.setVolume(volume.getVolumeDestino());

							for (MangaCapitulo capitulo : volume.getCapitulos()) {
								if (capitulo.isAlterado() || volume.isAlterado())
									capitulo.setVolume(volume.getVolume());

								if (capitulo.isAlterado())
									capitulo.setCapitulo(capitulo.getCapituloDestino());
							}
						}
					}

					service.salvarAjustes(TABELAS);
					TABELAS.clear();
					DADOS.getChildren().clear();
				} catch (ExcessaoBd e) {
					
					LOGGER.error(e.getMessage(), e);
				}
				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				Platform.runLater(() -> {
					treeBases.setRoot(DADOS);

					AlertasPopup.AvisoModal("Aviso", "Alterações salva com sucesso.");
					MenuPrincipalController.getController().getLblLog().setText("");
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

					ckbMarcarTodos.setSelected(true);
					btnCarregar.setDisable(false);
					btnSalvar.setDisable(false);
					treeBases.setDisable(false);
				});

			}

			@Override
			protected void failed() {
				super.failed();
				LOGGER.warn("Falha ao executar a thread de salvamento de as informações corrigidas: " + super.getMessage());
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
			TreeItem<Manga> itmLingua = null;
			String volumeAnterior = "";
			Language linguagemAnterior = null;

			for (MangaVolume volume : tabela.getVolumes()) {
				// Implementa um nivel por tipo
				if (!volume.getManga().equalsIgnoreCase(volumeAnterior) || itmManga == null) {
					volumeAnterior = volume.getManga();
					itmManga = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(), "..."));
					itmTabela.getChildren().add(itmManga);
					itmTabela.setExpanded(true);
					
					itmLingua = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(),
							volume.getLingua().getSigla().toUpperCase()));
					linguagemAnterior = volume.getLingua();
					itmManga.getChildren().add(itmLingua);
				}

				if (linguagemAnterior == null || volume.getLingua().compareTo(linguagemAnterior) != 0) {
					itmLingua = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(),
							volume.getLingua().getSigla().toUpperCase()));
					linguagemAnterior = volume.getLingua();
					itmManga.getChildren().add(itmLingua);
				}

				volume.addOutrasInformacoes(tabela.getBase(), volume.getManga(), volume.getVolume(), null,
						volume.getLingua());

				TreeItem<Manga> itmVolume = new TreeItem<Manga>(volume);

				for (MangaCapitulo capitulo : volume.getCapitulos()) {
					capitulo.addOutrasInformacoes(tabela.getBase(), capitulo.getManga(), capitulo.getVolume(),
							capitulo.getCapitulo(), linguagemAnterior);
					capitulo.setNomePagina("...");

					TreeItem<Manga> itmCapitulo = new TreeItem<Manga>(capitulo);
					for (MangaPagina pagina : capitulo.getPaginas()) {
						pagina.addOutrasInformacoes(tabela.getBase(), capitulo.getManga(), capitulo.getVolume(),
								capitulo.getCapitulo(), linguagemAnterior, pagina.getNumero(), pagina.getNomePagina(),
								"...");

						TreeItem<Manga> itmPagina = new TreeItem<Manga>(pagina);
						for (MangaTexto texto : pagina.getTextos()) {
							texto.addOutrasInformacoes(tabela.getBase(), capitulo.getManga(), capitulo.getVolume(),
									capitulo.getCapitulo(), linguagemAnterior, pagina.getNumero(),
									pagina.getNomePagina(), texto.getTexto());
							itmPagina.getChildren().add(new TreeItem<Manga>(texto));
						}
						itmCapitulo.getChildren().add(itmPagina);
					}
					itmVolume.getChildren().add(itmCapitulo);
				}
				itmLingua.getChildren().add(itmVolume);
				itmLingua.setExpanded(true);
			}
			itmTabela.setExpanded(true);
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

	private void setCapitulosChildreen(TreeItem<Manga> treeItem, Float capitulo) {
		if ((treeItem.getValue() instanceof MangaCapitulo) && ((MangaCapitulo) treeItem.getValue()).isExtra())
			return;
		
		if (treeItem.getValue().isProcessar()) {
			treeItem.getValue().setAlterado(true);
			treeItem.getValue().setCapituloDestino(capitulo);
			treeItem.getChildren().forEach(treeItemNivel2 -> setCapitulosChildreen(treeItemNivel2, capitulo));
		}
	}

	private void setVolumesChildreen(TreeItem<Manga> treeItem, Integer volume) {
		if ((treeItem.getValue() instanceof MangaCapitulo) && ((MangaCapitulo) treeItem.getValue()).isExtra())
			return;
		
		if (treeItem.getValue().isProcessar()) {
			treeItem.getValue().setAlterado(true);
			treeItem.getValue().setVolumeDestino(volume);
			treeItem.getChildren().forEach(treeItemNivel2 -> setVolumesChildreen(treeItemNivel2, volume));
		}
	}

	private Integer i;

	private void deletaCapitulo(TreeItem<Manga> treeItem) {
		if (treeItem != null && treeItem.getValue() != null) {
			if (treeItem.getValue() instanceof MangaCapitulo || treeItem.getValue() instanceof MangaPagina
					|| treeItem.getValue() instanceof MangaTexto) {
				String descricao = "";

				if (treeItem.getValue() instanceof MangaCapitulo)
					descricao = "Deseja remover o capítulo selecionado?" + '\n' + treeItem.getValue().getCapitulo();
				else if (treeItem.getValue() instanceof MangaPagina)
					descricao = "Deseja remover a página selecionada?" + '\n' + treeItem.getValue().getNomePagina();
				else
					descricao = "Deseja remover o texto selecionado?" + '\n' + '"' + treeItem.getValue().getTexto()
							+ '"';

				if (AlertasPopup.ConfirmacaoModal("Apagar", descricao)) {
					if (treeItem.getValue() instanceof MangaCapitulo) {
						MangaVolume volume = (MangaVolume) treeItem.getParent().getValue();
						volume.getCapitulos().remove(treeItem.getValue());
						volume.setAlterado(true);
						volume.setItemExcluido(true);
					} else if (treeItem.getValue() instanceof MangaPagina) {
						MangaCapitulo capitulo = (MangaCapitulo) treeItem.getParent().getValue();
						capitulo.getPaginas().remove(treeItem.getValue());
						i = 1;
						capitulo.getPaginas().forEach(t -> t.setNumero(i++));
						capitulo.setAlterado(true);
						capitulo.setItemExcluido(true);
					} else {
						MangaPagina pagina = (MangaPagina) treeItem.getParent().getValue();
						pagina.getTextos().remove(treeItem.getValue());
						i = 1;
						pagina.getTextos().forEach(t -> t.setSequencia(i++));
						pagina.setItemExcluido(true);
						
						MangaCapitulo capitulo = (MangaCapitulo) treeItem.getParent().getParent().getValue();
						capitulo.setAlterado(true);
						capitulo.setItemExcluido(true);
					}

					treeItem.getParent().getChildren().remove(treeItem);
					treeBases.refresh();
				}
			}
		}
	}

	private void ajustaCapitulosDoVolume(TreeItem<Manga> treeItem, Integer volume) {
		if (treeItem.getValue() instanceof MangaVolume)
			setVolumesChildreen(treeItem, volume);
		else if (treeItem.getValue() instanceof MangaCapitulo) {
			TreeItem<Manga> treeLanguage = treeItem.getParent().getParent();
			MangaCapitulo capitulo = (MangaCapitulo) treeItem.getValue();
			MangaVolume origem = (MangaVolume) treeItem.getParent().getValue();
			origem.getCapitulos().remove(capitulo);
			int position = volume.compareTo(capitulo.getVolumeDestino());
			capitulo.setVolumeDestino(volume);
			capitulo.setAlterado(true);

			TreeItem<Manga> treeNext = null;
			if (position > 0)
				treeNext = treeItem.nextSibling();
			else if (position < 0)
				treeNext = treeItem.previousSibling();
			else
				return;

			MangaVolume destino = null;
			for (MangaTabela tabela : TABELAS)
				if (tabela.getVolumes().contains(origem)) {
					destino = tabela.getVolumes().stream()
							.filter(it -> it.getVolume().compareTo(volume) == 0
									&& it.getManga().equalsIgnoreCase(origem.getManga())
									&& it.getLingua().compareTo(origem.getLingua()) == 0)
							.findFirst().orElse(new MangaVolume(null, origem.getManga(), volume, origem.getLingua(),
									origem.getArquivo(), new ArrayList<MangaCapitulo>()));

					destino.addCapitulos(capitulo);
					if (destino.getId() == null)
						tabela.getVolumes().add(destino);
					break;
				}

			if (destino.getId() != null) {
				for (TreeItem<Manga> language : treeLanguage.getChildren()) {
					if (language.getValue().getVolumeDestino().equals(volume)) {
						treeItem.getParent().getChildren().remove(treeItem);
						if (position < 0)
							language.getChildren().add(treeItem);
						else
							language.getChildren().add(0, treeItem);
						treeItem.getValue().setVolumeDestino(volume);
						break;
					}
				}
			} else {
				treeItem.getParent().getChildren().remove(treeItem);
				destino.addOutrasInformacoes(capitulo.getBase(), capitulo.getManga(), volume, null,
						capitulo.getLingua());

				TreeItem<Manga> itmVolume = new TreeItem<Manga>(destino);
				itmVolume.getChildren().add(treeItem);
				treeLanguage.getChildren().add(itmVolume);
				destino.setId(null);
			}

			setVolumesChildreen(treeItem, volume);
			if (ckbReprocessarDemais.isSelected() && treeNext != null) {
				if (treeNext.getValue() instanceof MangaCapitulo) {
					MangaCapitulo next = (MangaCapitulo) treeNext.getValue();

					if (origem.getVolumeDestino().compareTo(next.getVolumeDestino()) != 0)
						return;

					if ((position > 0 && next.getCapituloDestino().compareTo(capitulo.getCapituloDestino()) > 0)
							|| (position < 0 && next.getCapituloDestino().compareTo(capitulo.getCapituloDestino()) < 0))
						ajustaCapitulosDoVolume(treeNext, volume);
				}
			}
		}
	}

	private void ajustaCapitulosAnteriores(Manga original, TreeItem<Manga> treeItem, Integer quantidade) {
		if (treeItem == null || !treeItem.getValue().equals(original)
				|| treeItem.getValue().getVolume().compareTo(original.getVolume()) != 0)
			return;

		setCapitulosChildreen(treeItem, treeItem.getValue().getCapituloDestino() - quantidade);
		ajustaCapitulosAnteriores(original, treeItem.previousSibling(), quantidade);
	}

	private void ajustaCapitulosPosteriores(Manga original, TreeItem<Manga> treeItem, Integer quantidade) {
		if (treeItem == null || treeItem.getValue().getVolumeDestino().compareTo(original.getVolumeDestino()) != 0)
			return;

		setCapitulosChildreen(treeItem, treeItem.getValue().getCapituloDestino() + quantidade);
		ajustaCapitulosPosteriores(original, treeItem.nextSibling(), quantidade);
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

		treecExtra.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<Manga, Boolean>, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<Manga, Boolean> param) {
						TreeItem<Manga> treeItem = param.getValue();
						if (treeItem.getValue() instanceof MangaCapitulo) {
							MangaCapitulo item = (MangaCapitulo) treeItem.getValue();
							SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(item.isExtra());

							booleanProp.addListener(new ChangeListener<Boolean>() {
								@Override
								public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
										Boolean newValue) {
									item.setExtra(newValue);
									item.setAlterado(true);
								}
							});

							return booleanProp;
						} else
							return null;
					}

				});

		treecExtra.setCellFactory(new Callback<TreeTableColumn<Manga, Boolean>, TreeTableCell<Manga, Boolean>>() {
			@Override
			public TreeTableCell<Manga, Boolean> call(TreeTableColumn<Manga, Boolean> p) {
				CheckBoxTreeTableCellCustom<Manga, Boolean> checkbox = new CheckBoxTreeTableCellCustom<Manga, Boolean>() {
					@Override
					public void updateItem(Boolean item, boolean empty) {
						TreeItem<Manga> treeItem = p.getTreeTableView().getTreeItem(getIndex());
						if (treeItem != null && treeItem.getValue() != null
								&& treeItem.getValue() instanceof MangaCapitulo)
							super.updateItem(item, empty);
						else {
							setText(null);
							setGraphic(null);
						}
					}
				};
				checkbox.setAlignment(Pos.CENTER);
				return checkbox;
			}

		});

		treecVolumeDestino.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(new IntegerConverter()));
		treecVolumeDestino.setOnEditCommit(new EventHandler<CellEditEvent<Manga, Integer>>() {
			@Override
			public void handle(CellEditEvent<Manga, Integer> event) {
				TreeItem<Manga> row = treeBases.getTreeItem(event.getTreeTablePosition().getRow());
				if (event.getNewValue() == null || row == null)
					return;

				if ((row.getValue() instanceof MangaCapitulo) || (row.getValue() instanceof MangaVolume)) {
					int value = event.getOldValue().compareTo(event.getNewValue());
					if (ckbReprocessarDemais.isSelected()) {
						if (value != 0)
							ajustaCapitulosDoVolume(row, event.getNewValue());
					} else {
						row.getValue().setVolumeDestino(event.getNewValue());
						row.getValue().setAlterado(true);
					}
				}
				treeBases.refresh();
			}

		});

		treecCapituloDestino.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn(new FloatConverter()));
		treecCapituloDestino.setOnEditCommit(new EventHandler<CellEditEvent<Manga, Float>>() {
			@Override
			public void handle(CellEditEvent<Manga, Float> event) {
				TreeItem<Manga> row = treeBases.getTreeItem(event.getTreeTablePosition().getRow());
				if (event.getNewValue() == null || row == null)
					return;

				if (row.getValue() instanceof MangaCapitulo) {
					Integer diferenca = Float.valueOf(event.getNewValue() - event.getOldValue()).intValue();

					if (ckbReprocessarDemais.isSelected()) {
						if (((MangaCapitulo) row.getValue()).isExtra()) {
							row.getValue().setAlterado(true);
							row.getValue().setCapituloDestino(event.getNewValue());
						} else {
							setCapitulosChildreen(row, event.getNewValue());
							ajustaCapitulosAnteriores(row.getValue(), row.previousSibling(), diferenca * -1);
							ajustaCapitulosPosteriores(row.getValue(), row.nextSibling(), diferenca);
						}
					}
				}
				treeBases.refresh();
			}

		});

	}

	private void linkaCelulas() {
		treecMacado.setCellValueFactory(new TreeItemPropertyValueFactory<Manga, Boolean>("processar"));
		treecBase.setCellValueFactory(new TreeItemPropertyValueFactory<>("base"));
		treecManga.setCellValueFactory(new TreeItemPropertyValueFactory<>("manga"));
		treecLinguagem.setCellValueFactory(new TreeItemPropertyValueFactory<>("linguagem"));
		treecVolumeOrigem.setCellValueFactory(new TreeItemPropertyValueFactory<>("volume"));
		treecCapituloOrigem.setCellValueFactory(new TreeItemPropertyValueFactory<>("capitulo"));
		treecVolumeDestino.setCellValueFactory(new TreeItemPropertyValueFactory<>("volumeDestino"));
		treecCapituloDestino.setCellValueFactory(new TreeItemPropertyValueFactory<>("capituloDestino"));
		treecExtra.setCellValueFactory(new TreeItemPropertyValueFactory<Manga, Boolean>("isExtra"));
		treecPagina.setCellValueFactory(new TreeItemPropertyValueFactory<>("nomePagina"));
		treecTexto.setCellValueFactory(new TreeItemPropertyValueFactory<>("texto"));
		treeBases.setShowRoot(false);
		treeBases.setEditable(true);

		editaColunas();
	}

	private final Robot robot = new Robot();

	public void initialize(URL arg0, ResourceBundle arg1) {
		
		try {
			cbBase.getItems().setAll(service.getTabelas());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
		JFXAutoCompletePopup<String> autoCompletePopup = new JFXAutoCompletePopup<>();
		autoCompletePopup.getSuggestions().addAll(cbBase.getItems());

		autoCompletePopup.setSelectionHandler(event -> {
			cbBase.setValue(event.getObject());
		});

		cbBase.getEditor().textProperty().addListener(observable -> {
			autoCompletePopup.filter(item -> item.toLowerCase().contains(cbBase.getEditor().getText().toLowerCase()));
			if (autoCompletePopup.getFilteredSuggestions().isEmpty() || cbBase.showingProperty().get()
					|| cbBase.getEditor().getText().isEmpty())
				autoCompletePopup.hide();
			else
				autoCompletePopup.show(cbBase.getEditor());
		});

		cbBase.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});


		cbLinguagem.getItems().addAll(Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE,
				Language.PORTUGUESE_GOOGLE);

		cbLinguagem.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ESCAPE))
					cbLinguagem.getSelectionModel().clearSelection();
				else if (ke.getCode().equals(KeyCode.ENTER))
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

		spnVolume.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		spnCapitulo.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		treeBases.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.DELETE))
					deletaCapitulo(treeBases.getSelectionModel().getSelectedItem());
			}
		});

		spnVolume.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
		spnCapitulo.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 99999, 0, 1));

		linkaCelulas();
	}

	public static URL getFxmlLocate() {
		return MangasAjustarController.class.getResource("/view/mangas/MangaAjustar.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
