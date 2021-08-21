package org.jisho.textosJapones.controller;

import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Site;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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
	private ProgressBar barraProgresso;

	@FXML
	private TraduzirController traduzirController;

	@FXML
	private RevisarController revisarController;

	@FXML
	private JFXButton btnProcessar;

	@FXML
	private JFXButton btnGerarJson;

	@FXML
	private JFXTextField txtBase;

	@FXML
	private TreeTableView<MangaTabela> treeBases;

	@FXML
	private TreeTableColumn<MangaTabela, Boolean> treecMacado;

	@FXML
	private TreeTableColumn<MangaTabela, String> treecBase;

	@FXML
	private TreeTableColumn<MangaTabela, String> treecManga;

	@FXML
	private TreeTableColumn<MangaTabela, Integer> treecVolume;

	@FXML
	private TreeTableColumn<MangaTabela, Float> treecCapitulo;

	@FXML
	private void onBtnProcessar() {

	}

	@FXML
	private void onBtnGerarJson() {

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

	public AnchorPane getRoot() {
		return root;
	}

	public StackPane getStackPane() {
		return rootStackPane;
	}

	public ProgressBar getBarraProgresso() {
		return barraProgresso;
	}

	public Label getLog() {
		return lblLog;
	}

	private void editaColunas() {

		// ==== (CHECK-BOX) ===
		treecMacado.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<MangaTabela, Boolean>, //
				ObservableValue<Boolean>>() {

			@Override
			public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<MangaTabela, Boolean> param) {
				TreeItem<MangaTabela> treeItem = param.getValue();
				MangaTabela esta = treeItem.getValue();
				SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(esta.isProcessar());

				booleanProp.addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						esta.setProcessar(newValue);
					}
				});
				return booleanProp;
			}
		});

		treecMacado.setCellFactory(
				new Callback<TreeTableColumn<MangaTabela, Boolean>, TreeTableCell<MangaTabela, Boolean>>() {
					@Override
					public TreeTableCell<MangaTabela, Boolean> call(TreeTableColumn<MangaTabela, Boolean> p) {
						CheckBoxTreeTableCell<MangaTabela, Boolean> cell = new CheckBoxTreeTableCell<MangaTabela, Boolean>();
						cell.setAlignment(Pos.CENTER);
						cell.getStyleClass().add("hide-non-leaf"); // Insere o tipo para ser invisivel
						return cell;
					}
				});

	}

	private void linkaCelulas() {
		treecBase.setCellValueFactory(new TreeItemPropertyValueFactory<>("vocabulario"));
		treecManga.setCellValueFactory(new TreeItemPropertyValueFactory<>("leitura"));
		treecVolume.setCellValueFactory(new TreeItemPropertyValueFactory<>("tabela"));
		treecCapitulo.setCellValueFactory(new TreeItemPropertyValueFactory<>("tipo"));
		treecMacado.setCellValueFactory(new TreeItemPropertyValueFactory<MangaTabela, Boolean>("gerar"));

		editaColunas();

	}

	public void initialize(URL arg0, ResourceBundle arg1) {
		cbContaGoolge.getItems().addAll(Api.values());
		cbContaGoolge.getSelectionModel().selectFirst();

		cbSite.getItems().addAll(Site.values());
		cbSite.getSelectionModel().selectFirst();

		cbModo.getItems().addAll(Modo.values());
		cbModo.getSelectionModel().select(Modo.C);

		cbDicionario.getItems().addAll(Dicionario.values());
		cbDicionario.getSelectionModel().select(Dicionario.FULL);

		linkaCelulas();
		
		revisarController.setAnime(false);
		revisarController.setManga(true);
	}

	public static URL getFxmlLocate() {
		return MangasController.class.getResource("/view/Manga.fxml");
	}
	
	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
