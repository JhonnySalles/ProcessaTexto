package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.util.components.NoSelectionModel;

import com.jfoenix.controls.JFXButton;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

public class MangasTextoController implements Initializable {
	
	@FXML
	protected AnchorPane apRoot;

	@FXML
	private JFXButton btnRecarregar;

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
	private ListView<VinculoPagina> lvPaginasVinculadas;

	private ObservableList<VinculoPagina> vinculado;

	public void setDados(ObservableList<VinculoPagina> vinculado) {
		this.vinculado = vinculado;
		lvPaginasVinculadas.setItems(this.vinculado);
	}

	private void preparaCelulas() {
		lvPaginasVinculadas.setSelectionModel(new NoSelectionModel<VinculoPagina>());

		lvPaginasVinculadas.setCellFactory(new Callback<ListView<VinculoPagina>, ListCell<VinculoPagina>>() {
			@Override
			public ListCell<VinculoPagina> call(ListView<VinculoPagina> studentListView) {
				ListCell<VinculoPagina> cell = new ListCell<VinculoPagina>() {
					@Override
					public void updateItem(VinculoPagina item, boolean empty) {
						super.updateItem(item, empty);

						setText(null);

						if (empty || item == null)
							setGraphic(null);
						else {
							FXMLLoader mLLoader = new FXMLLoader(MangasTextoCelulaController.getFxmlLocate());

							try {
								mLLoader.load();
							} catch (IOException e) {
								e.printStackTrace();
							}

							MangasTextoCelulaController controller = mLLoader.getController();
							controller.setDados(item);

							setGraphic(controller.hbRoot);
						}
					}
				};

				return cell;
			}
		});
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
