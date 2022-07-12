package org.jisho.textosJapones.controller;

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
										controller.setDados(item);
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

		tcMangaVinculado.setCellValueFactory(new PropertyValueFactory<>("imagemVinculadoDireita"));
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
									JFXTextArea text = new JFXTextArea();
									text.getStyleClass().add("background-Blue3");
									text.getStyleClass().add("texto-stilo");
									setGraphic(text);

									if (item != null)
										text.setText(item.getTextos().stream().map(MangaTexto::getTexto)
												.collect(Collectors.joining("\n")));
								}
							}
						};
						return cell;
					}
				});

		tcTextoVinculado.setCellValueFactory(new PropertyValueFactory<>("mangaPaginaDireita"));
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
									JFXTextArea text = new JFXTextArea();
									text.getStyleClass().add("background-Blue3");
									text.getStyleClass().add("texto-stilo");
									setGraphic(text);

									String textos = "";

									if (item != null)
										textos = item.getTextos().stream().map(MangaTexto::getTexto)
												.collect(Collectors.joining("\n"));

									VinculoPagina linha = getTableRow().getItem();

									if (linha != null) {
										MangaPagina direita = linha.getMangaPaginaDireita();

										if (direita != null)
											textos += direita.getTextos().stream().map(MangaTexto::getTexto)
													.collect(Collectors.joining("\n"));
									}

									text.setText(textos);

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
		return MangasTextoController.class.getResource("/view/MangaTexto.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
