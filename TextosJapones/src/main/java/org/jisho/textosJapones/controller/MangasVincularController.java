package org.jisho.textosJapones.controller;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.services.MangaServices;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.stage.FileChooser;

public class MangasVincularController implements Initializable {

	@FXML
	protected AnchorPane apRoot;

	@FXML
	private JFXTextField txtBase;

	@FXML
	private JFXTextField txtManga;

	@FXML
	private JFXTextField txtArquivoOriginal;

	@FXML
	private JFXButton btnOriginal;

	@FXML
	private JFXTextField txtArquivoVinculado;

	@FXML
	private JFXButton btnVinculado;

	@FXML
	private JFXButton btnDeletar;

	@FXML
	private JFXButton btnSalvar;

	@FXML
	private Spinner<Integer> spnVolume;

	@FXML
	private JFXComboBox<Language> cbLinguagemOrigem;

	@FXML
	private JFXComboBox<Language> cbLinguagemVinculado;

	@FXML
	private JFXButton btnRecarregar;

	@FXML
	private JFXButton btnOrderAutomatico;

	@FXML
	private JFXButton btnOrderPaginaDupla;

	@FXML
	private JFXButton btnOrderPaginaUnica;

	@FXML
	private JFXButton btnOrderSequencia;

	@FXML
	private JFXCheckBox ckbPaginaDuplaCalculada;

	@FXML
	private ListView<VinculoPagina> lvPaginasVinculadas;

	private File arquivoOriginal;
	private File arquivoVinculado;

	private MangaServices service = new MangaServices();
	private Vinculo vinculo = new Vinculo();
	private ObservableList<VinculoPagina> paginas;

	private MangasController controller;

	public void setControllerPai(MangasController controller) {
		this.controller = controller;
	}

	public MangasController getControllerPai() {
		return controller;
	}

	@FXML
	private void onBtnOriginal() {
		String pasta = arquivoOriginal != null ? arquivoOriginal.getPath()
				: (arquivoVinculado != null ? arquivoVinculado.getPath() : null);
		arquivoOriginal = selectFile("Selecione o arquivo de origem", pasta);
	}

	@FXML
	private void onBtnVinculado() {
		String pasta = arquivoVinculado != null ? arquivoVinculado.getPath()
				: (arquivoOriginal != null ? arquivoOriginal.getPath() : null);
		arquivoVinculado = selectFile("Selecione o arquivo de origem", pasta);
	}

	@FXML
	private void onBtnDeletar() {

	}

	@FXML
	private void onBtnSalvar() {

	}

	@FXML
	private void onBtnRecarregar() {

	}

	@FXML
	private void onBtnOrderAutomatico() {

	}

	@FXML
	private void onBtnOrderPaginaDupla() {

	}

	@FXML
	private void onBtnOrderPaginaUnica() {

	}

	@FXML
	private void onBtnOrderSequencia() {

	}

	public AnchorPane getRoot() {
		return apRoot;
	}

	private File selectFile(String titulo, String pasta) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(titulo);

		if (pasta != null && !pasta.isEmpty())
			fileChooser.setInitialDirectory(new File(pasta));

		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("ALL FILES", "*.*"),
				new FileChooser.ExtensionFilter("ZIP", "*.zip"), new FileChooser.ExtensionFilter("CBZ", "*.cbz"),
				new FileChooser.ExtensionFilter("RAR", "*.rar"), new FileChooser.ExtensionFilter("CBR", "*.cbr"));

		return fileChooser.showOpenDialog(null);
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

		spnVolume.setOnKeyPressed(new EventHandler<KeyEvent>() {
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

		txtArquivoOriginal.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		txtArquivoVinculado.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

	}

	public static URL getFxmlLocate() {
		return MangasVincularController.class.getResource("/view/MangaVincular.fxml");
	}

	public static String getIconLocate() {
		return "/images/icoTextoJapones_128.png";
	}

}
