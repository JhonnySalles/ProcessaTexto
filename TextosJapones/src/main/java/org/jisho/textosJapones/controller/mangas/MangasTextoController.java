package org.jisho.textosJapones.controller.mangas;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTexto;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.util.animation.Animacao;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class MangasTextoController implements Initializable {

	@FXML
	protected AnchorPane apRoot;

	@FXML
	private JFXButton btnClose;

	@FXML
	private JFXButton btnCarregarLegendas;

	@FXML
	private JFXButton btnOrderAutomatico;

	@FXML
	private JFXButton btnOrderPaginaDupla;

	@FXML
	private JFXButton btnOrderPaginaUnica;

	@FXML
	private JFXButton btnOrderSequencia;

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

	private MangasVincularController controller;

	public void setControllerPai(MangasVincularController controller) {
		this.controller = controller;
	}

	public MangasVincularController getControllerPai() {
		return controller;
	}

	@FXML
	private void onBtnClose() {
		new Animacao().fecharPane(controller.getControllerPai().getStackPane());
	}

	public void setDados(ObservableList<VinculoPagina> vinculado) {
		this.vinculado = vinculado;
		tvPaginasVinculadas.setItems(this.vinculado);
	}

	private void preparaCelulas() {
		// tvPaginasVinculadas.setSelectionModel(new
		// TableViewNoSelectionModel<VinculoPagina>(tvPaginasVinculadas));

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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		preparaCelulas();

	}

	public static URL getFxmlLocate() {
		return MangasTextoController.class.getResource("/view/mangas/MangaTexto.fxml");
	}
}
