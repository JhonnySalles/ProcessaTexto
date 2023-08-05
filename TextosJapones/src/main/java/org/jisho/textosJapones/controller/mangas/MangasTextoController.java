package org.jisho.textosJapones.controller.mangas;

import com.jfoenix.controls.*;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.robot.Robot;
import javafx.scene.text.Text;
import javafx.util.Callback;
import org.jisho.textosJapones.components.TableViewNoSelectionModel;
import org.jisho.textosJapones.components.animation.Animacao;
import org.jisho.textosJapones.components.listener.VinculoTextoListener;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaPagina;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaTexto;
import org.jisho.textosJapones.model.enums.Language;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MangasTextoController implements Initializable, VinculoTextoListener {

	@FXML
	protected AnchorPane apRoot;

	@FXML
	private JFXButton btnClose;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private JFXButton btnCarregarLegendas;

	@FXML
	private JFXTextField txtMangaOriginal;

	@FXML
	private JFXTextField txtMangaVinculado;

	@FXML
	private JFXComboBox<Language> cbLinguagemOrigem;

	@FXML
	private JFXComboBox<Language> cbLinguagemVinculado;

	@FXML
	private TableView<VinculoPagina> tvPaginasVinculadas;

	@FXML
	private TableColumn<VinculoPagina, Image> tcMangaOriginal;

	@FXML
	private TableColumn<VinculoPagina, MangaPagina> tcTextoOriginal;

	@FXML
	private TableColumn<VinculoPagina, Image> tcMangaVinculado;

	@FXML
	private TableColumn<VinculoPagina, MangaPagina> tcTextoVinculado;

	private ObservableList<VinculoPagina> vinculado;

	@FXML
	private JFXListView<String> lvCapitulosOriginal;

	@FXML
	private JFXListView<String> lvCapitulosVinculado;

	@FXML
	private JFXListView<String> lvTextoNaoLocalizado;

	private JFXAutoCompletePopup<String> autoCompleteMangaOriginal;
	private JFXAutoCompletePopup<String> autoCompleteMangaVinculado;

	private Map<String, MangaPagina> naoLocalizado;

	private MangasVincularController controller;

	public void setControllerPai(MangasVincularController controller) {
		this.controller = controller;
	}

	public MangasVincularController getControllerPai() {
		return controller;
	}

	@Override
	public void refresh() {
		tvPaginasVinculadas.refresh();
		tvPaginasVinculadas.requestLayout();
	}

	@FXML
	private void onBtnClose() {
		controller.refreshListener = null;
		controller.setAutoCompleteListener(false);
		setAutoCompleteListener(true, controller);

		txtMangaOriginal.textProperty().unbind();
		txtMangaVinculado.textProperty().unbind();
		cbLinguagemOrigem.selectionModelProperty().unbind();
		cbLinguagemVinculado.selectionModelProperty().unbind();

		new Animacao().fecharPane(controller.getControllerPai().getStackPane());
	}

	private InvalidationListener listenerMangaOriginal = observable -> {
		autoCompleteMangaOriginal
				.filter(string -> string.toLowerCase().contains(txtMangaOriginal.getText().toLowerCase()));
		if (autoCompleteMangaOriginal.getFilteredSuggestions().isEmpty() || txtMangaOriginal.getText().isEmpty())
			autoCompleteMangaOriginal.hide();
		else
			autoCompleteMangaOriginal.show(txtMangaOriginal);
	};

	private InvalidationListener listenerMangaVinculado = observable -> {
		autoCompleteMangaVinculado
				.filter(string -> string.toLowerCase().contains(txtMangaVinculado.getText().toLowerCase()));
		if (autoCompleteMangaVinculado.getFilteredSuggestions().isEmpty() || txtMangaVinculado.getText().isEmpty())
			autoCompleteMangaVinculado.hide();
		else
			autoCompleteMangaVinculado.show(txtMangaVinculado);
	};

	private ListChangeListener<String> listenerAutoCompleteMangaOriginal;
	private ListChangeListener<String> listenerAutoCompleteMangaVinculado;

	private void setAutoCompleteListener(Boolean isClear, MangasVincularController controller) {
		if (isClear) {
			txtMangaOriginal.textProperty().removeListener(listenerMangaOriginal);
			txtMangaVinculado.textProperty().removeListener(listenerMangaVinculado);

			autoCompleteMangaOriginal.setSelectionHandler(null);
			autoCompleteMangaVinculado.setSelectionHandler(null);

			controller.getAutoCompleteMangaOriginal().getSuggestions()
					.removeListener(listenerAutoCompleteMangaOriginal);
			controller.getAutoCompleteMangaVinculado().getSuggestions()
					.removeListener(listenerAutoCompleteMangaVinculado);

			listenerAutoCompleteMangaOriginal = null;
			listenerAutoCompleteMangaVinculado = null;
		} else {
			autoCompleteMangaOriginal = new JFXAutoCompletePopup<String>();
			autoCompleteMangaOriginal.getSuggestions()
					.addAll(controller.getAutoCompleteMangaOriginal().getSuggestions());

			autoCompleteMangaOriginal.setSelectionHandler(event -> {
				txtMangaOriginal.setText(event.getObject());
			});

			autoCompleteMangaVinculado = new JFXAutoCompletePopup<String>();
			autoCompleteMangaVinculado.getSuggestions()
					.addAll(controller.getAutoCompleteMangaVinculado().getSuggestions());

			autoCompleteMangaVinculado.setSelectionHandler(event -> {
				txtMangaVinculado.setText(event.getObject());
			});

			listenerAutoCompleteMangaOriginal = new ListChangeListener<String>() {
				@Override
				public void onChanged(Change<? extends String> change) {
					while (change.next()) {
						for (String removed : change.getRemoved())
							autoCompleteMangaOriginal.getSuggestions().remove(removed);

						for (String added : change.getAddedSubList())
							autoCompleteMangaOriginal.getSuggestions().add(added);
					}
				}
			};

			listenerAutoCompleteMangaVinculado = new ListChangeListener<String>() {
				@Override
				public void onChanged(Change<? extends String> change) {
					while (change.next()) {
						for (String removed : change.getRemoved())
							autoCompleteMangaVinculado.getSuggestions().remove(removed);

						for (String added : change.getAddedSubList())
							autoCompleteMangaVinculado.getSuggestions().add(added);
					}
				}
			};

			controller.getAutoCompleteMangaOriginal().getSuggestions().addListener(listenerAutoCompleteMangaOriginal);
			controller.getAutoCompleteMangaVinculado().getSuggestions().addListener(listenerAutoCompleteMangaVinculado);

			txtMangaOriginal.textProperty().addListener(listenerMangaOriginal);
			txtMangaVinculado.textProperty().addListener(listenerMangaVinculado);
		}
	}

	private void cloneData(MangasVincularController controller) {
		btnSalvar.onActionProperty().set(controller.getBtnSalvar().onActionProperty().get());
		btnCarregarLegendas.onActionProperty().set(controller.getBtnCarregarLegendas().onActionProperty().get());

		txtMangaOriginal.setText(controller.getTxtMangaOriginal().getText());
		txtMangaVinculado.setText(controller.getTxtMangaVinculado().getText());

		txtMangaOriginal.textProperty().bindBidirectional(controller.getTxtMangaOriginal().textProperty());
		txtMangaVinculado.textProperty().bindBidirectional(controller.getTxtMangaVinculado().textProperty());

		setAutoCompleteListener(false, controller);

		controller.setAutoCompleteListener(true);

		cbLinguagemOrigem.getItems().addAll(controller.getCbLinguagemOrigem().getItems());
		cbLinguagemVinculado.getItems().addAll(controller.getCbLinguagemVinculado().getItems());

		cbLinguagemOrigem.getSelectionModel()
				.select(controller.getCbLinguagemOrigem().getSelectionModel().getSelectedItem());
		cbLinguagemVinculado.getSelectionModel()
				.select(controller.getCbLinguagemVinculado().getSelectionModel().getSelectedItem());

		cbLinguagemOrigem.selectionModelProperty()
				.bindBidirectional(controller.getCbLinguagemOrigem().selectionModelProperty());
		cbLinguagemVinculado.selectionModelProperty()
				.bindBidirectional(controller.getCbLinguagemVinculado().selectionModelProperty());

		lvCapitulosOriginal.setItems(controller.getListCapitulosOriginal());
		lvCapitulosVinculado.setItems(controller.getListCapitulosVinculado());

		naoLocalizado = new HashMap<String, MangaPagina>();
		if (controller.getVinculo().getVolumeOriginal() != null) {
			controller.getVinculo().getVolumeOriginal().getCapitulos().parallelStream()
					.forEach(c -> c.getPaginas().parallelStream()
							.forEach(p -> p.addOutrasInformacoes(controller.getVinculo().getNomeArquivoOriginal(),
									"Original", c.getCapitulo())));

			naoLocalizado.putAll(controller.getVinculo().getVolumeOriginal().getCapitulos().parallelStream()
					.flatMap(cap -> cap.getPaginas().stream()) // Transforma as sublistas de paginas em uma lista
					.filter(pag -> !vinculado.parallelStream() // Filtra apenas as paginas que não estão vinculada
							.anyMatch(vin -> vin.getMangaPaginaOriginal() != null
									&& vin.getMangaPaginaOriginal().equals(pag)))
					.collect(Collectors.toMap(MangaPagina::getDescricao, p -> p))); // Transforma em um map
		}

		if (controller.getVinculo().getVolumeVinculado() != null) {
			controller.getVinculo().getVolumeVinculado().getCapitulos().parallelStream()
					.forEach(c -> c.getPaginas().parallelStream()
							.forEach(p -> p.addOutrasInformacoes(controller.getVinculo().getNomeArquivoVinculado(),
									"Vinculado", c.getCapitulo())));

			naoLocalizado.putAll(controller.getVinculo().getVolumeVinculado().getCapitulos().parallelStream()
					.flatMap(cap -> cap.getPaginas().stream()) // Transforma as sublistas de paginas em uma lista
					.filter(pag -> !vinculado.parallelStream() // Filtra apenas as paginas que não estão vinculada
							.anyMatch(vin -> (vin.getMangaPaginaEsquerda() != null
									&& vin.getMangaPaginaEsquerda().equals(pag))
									|| (vin.getMangaPaginaDireita() != null
											&& vin.getMangaPaginaDireita().equals(pag))))
					.collect(Collectors.toMap(MangaPagina::getDescricao, p -> p))); // Transforma em um map
		}

		lvTextoNaoLocalizado.setItems(FXCollections.observableArrayList(
				naoLocalizado.keySet().parallelStream().sorted((a, b) -> a.compareToIgnoreCase(b))
						.collect(Collectors.toList())));
	}

	public void setDados(ObservableList<VinculoPagina> vinculado, MangasVincularController controller) {
		this.vinculado = vinculado;
		tvPaginasVinculadas.setItems(this.vinculado);

		cloneData(controller);
	}

	private void preparaCelulas() {
		tvPaginasVinculadas.setSelectionModel(new TableViewNoSelectionModel<VinculoPagina>(tvPaginasVinculadas));

		tcMangaOriginal.setCellValueFactory(new PropertyValueFactory<>("imagemOriginal"));
		tcMangaOriginal
				.setCellFactory(new Callback<TableColumn<VinculoPagina, Image>, TableCell<VinculoPagina, Image>>() {
					@Override
					public TableCell<VinculoPagina, Image> call(TableColumn<VinculoPagina, Image> param) {
						TableCell<VinculoPagina, Image> cell = new TableCell<VinculoPagina, Image>() {
							@Override
							public void updateItem(Image item, boolean empty) {
								setText(null);
								if (empty || item == null)
									setGraphic(null);
								else {
									FXMLLoader mLLoader = new FXMLLoader(MangasTextoCelulaController.getFxmlLocate());

									try {
										mLLoader.load();
										MangasTextoCelulaController controller = mLLoader.getController();
										String pagina = "";

										if (getTableRow().getItem() != null) {
											MangaPagina manga = getTableRow().getItem().getMangaPaginaOriginal();

											if (manga != null)
												pagina = "Pag: " + manga.getNumero() + " - " + manga.getNomePagina();
										}

										controller.setDados(item, pagina);
										setGraphic(controller.root);
									} catch (IOException e) {
										e.printStackTrace();
										setGraphic(null);
									}
								}
							}
						};
						return cell;
					}
				});

		tcMangaVinculado.setCellValueFactory(new PropertyValueFactory<>("imagemVinculadoEsquerda"));
		tcMangaVinculado
				.setCellFactory(new Callback<TableColumn<VinculoPagina, Image>, TableCell<VinculoPagina, Image>>() {
					@Override
					public TableCell<VinculoPagina, Image> call(TableColumn<VinculoPagina, Image> param) {
						TableCell<VinculoPagina, Image> cell = new TableCell<VinculoPagina, Image>() {
							@Override
							public void updateItem(Image item, boolean empty) {
								setText(null);
								if (empty || item == null)
									setGraphic(null);
								else {
									FXMLLoader mLLoader = new FXMLLoader(
											MangasTextoCelulaDuplaController.getFxmlLocate());

									try {
										mLLoader.load();
										MangasTextoCelulaDuplaController controller = mLLoader.getController();
										controller.setDados(getTableRow().getItem());
										setGraphic(controller.hbRoot);
									} catch (IOException e) {
										e.printStackTrace();
										setGraphic(null);
									}
								}
							}
						};
						return cell;
					}
				});

		tcTextoOriginal.setCellValueFactory(new PropertyValueFactory<>("mangaPaginaOriginal"));
		tcTextoOriginal.setCellFactory(
				new Callback<TableColumn<VinculoPagina, MangaPagina>, TableCell<VinculoPagina, MangaPagina>>() {
					@Override
					public TableCell<VinculoPagina, MangaPagina> call(TableColumn<VinculoPagina, MangaPagina> param) {
						TableCell<VinculoPagina, MangaPagina> cell = new TableCell<VinculoPagina, MangaPagina>() {
							@Override
							public void updateItem(MangaPagina item, boolean empty) {
								setText(null);

								if (empty)
									setGraphic(null);
								else {
									Text text = new Text();
									text.setFill(Paint.valueOf("white"));
									text.getStyleClass().add("texto-stilo");

									JFXTextArea area = new JFXTextArea();
									area.getStyleClass().add("background-Blue3");
									area.getStyleClass().add("texto-stilo");
									VBox.setVgrow(area, Priority.ALWAYS);

									VBox container = new VBox();
									container.getChildren().addAll(text, area);
									container.setSpacing(5);
									VBox.setVgrow(container, Priority.ALWAYS);
									setGraphic(container);

									if (item != null) {
										area.setText(item.getTextos().stream().map(MangaTexto::getTexto)
												.collect(Collectors.joining("\n")));

										text.setText("Pag: " + item.getNumero() + " - " + item.getNomePagina());
									}
								}
							}
						};
						return cell;
					}
				});

		tcTextoVinculado.setCellValueFactory(new PropertyValueFactory<>("mangaPaginaEsquerda"));
		tcTextoVinculado.setCellFactory(
				new Callback<TableColumn<VinculoPagina, MangaPagina>, TableCell<VinculoPagina, MangaPagina>>() {
					@Override
					public TableCell<VinculoPagina, MangaPagina> call(TableColumn<VinculoPagina, MangaPagina> param) {
						TableCell<VinculoPagina, MangaPagina> cell = new TableCell<VinculoPagina, MangaPagina>() {
							@Override
							public void updateItem(MangaPagina item, boolean empty) {
								setText(null);

								if (empty)
									setGraphic(null);
								else {
									Text text = new Text();
									text.setFill(Paint.valueOf("white"));
									text.getStyleClass().add("texto-stilo");

									JFXTextArea area = new JFXTextArea();
									area.getStyleClass().add("background-Blue3");
									area.getStyleClass().add("texto-stilo");
									VBox.setVgrow(area, Priority.ALWAYS);

									VBox container = new VBox();
									container.getChildren().addAll(text, area);
									container.setSpacing(5);
									VBox.setVgrow(container, Priority.ALWAYS);
									setGraphic(container);

									String textos = "";
									String pagina = "";

									if (item != null) {
										textos = item.getTextos().stream().map(MangaTexto::getTexto)
												.collect(Collectors.joining("\n"));
										pagina = "Pag: " + item.getNumero() + " - " + item.getNomePagina();
									}

									VinculoPagina linha = getTableRow().getItem();

									if (linha != null) {
										MangaPagina direita = linha.getMangaPaginaDireita();

										if (direita != null) {
											textos += direita.getTextos().stream().map(MangaTexto::getTexto)
													.collect(Collectors.joining("\n"));

											pagina += " | " + "Pag: " + item.getNumero() + " - " + item.getNomePagina();
										}
									}

									area.setText(textos);
									text.setText(pagina);

								}
							}
						};
						return cell;
					}
				});

	}

	public void scroolTo(Integer index) {
		if (index != null)
			tvPaginasVinculadas.scrollTo(index);
	}

	private void selecionaCapitulo(String capitulo, Boolean isManga) {
		if (capitulo == null || capitulo.isEmpty())
			return;

		if (isManga && controller.getCapitulosOriginal().isEmpty()
				|| !isManga && controller.getCapitulosVinculado().isEmpty())
			return;

		if (isManga) {
			Integer numero = controller.getCapitulosOriginal().get(capitulo);
			Optional<VinculoPagina> pagina = tvPaginasVinculadas.getItems().stream()
					.filter(pg -> pg.getOriginalPagina().compareTo(numero) == 0).findFirst();
			if (pagina.isPresent())
				tvPaginasVinculadas.scrollTo(pagina.get());
		} else {
			Integer numero = controller.getCapitulosVinculado().get(capitulo);
			Optional<VinculoPagina> pagina = tvPaginasVinculadas.getItems().stream()
					.filter(pg -> pg.getVinculadoEsquerdaPagina().compareTo(numero) == 0
							|| pg.getVinculadoDireitaPagina().compareTo(numero) == 0)
					.findFirst();
			if (pagina.isPresent())
				tvPaginasVinculadas.scrollTo(pagina.get());
		}
	}

	private Robot robot = new Robot();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		preparaCelulas();

		txtMangaOriginal.focusTraversableProperty().addListener((options, oldValue, newValue) -> {
			if (oldValue)
				txtMangaOriginal.setUnFocusColor(Color.web("#106ebe"));
		});

		txtMangaOriginal.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		txtMangaVinculado.focusTraversableProperty().addListener((options, oldValue, newValue) -> {
			if (oldValue)
				txtMangaVinculado.setUnFocusColor(Color.web("#106ebe"));
		});

		txtMangaVinculado.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		cbLinguagemOrigem.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		cbLinguagemVinculado.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		lvCapitulosOriginal.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent click) {
				if (click.getClickCount() > 1)
					selecionaCapitulo(lvCapitulosOriginal.getSelectionModel().getSelectedItem(), true);
			}
		});

		lvCapitulosVinculado.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent click) {
				if (click.getClickCount() > 1)
					selecionaCapitulo(lvCapitulosVinculado.getSelectionModel().getSelectedItem(), false);
			}
		});

	}

	public static URL getFxmlLocate() {
		return MangasTextoController.class.getResource("/view/mangas/MangaTexto.fxml");
	}
}
