package org.jisho.textosJapones.controller.mangas;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.database.mysql.ConexaoMysql;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.processar.comicinfo.ProcessaComicInfo;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

	private MangasController controller;

	public void setControllerPai(MangasController controller) {
		this.controller = controller;
	}

	public MangasController getControllerPai() {
		return controller;
	}

	@FXML
	private void onBtnProcessar() {
		gerar();
	}

	@FXML
	private void onBtnCarregarCaminho() {
		txtCaminho.setText(selecionaPasta(txtCaminho.getText(), false));
	}
	
	@FXML
	private void onBtnCarregarArquivo() {
		txtCaminho.setText(selecionaPasta(txtCaminho.getText(), true));
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

			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Arquivos", "*.cbr", "*.cbz", "*.rar", "*.zip");
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


	private void gerar() {
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
					
					ProcessaComicInfo.processa(ConexaoMysql.getCaminhoWinrar(), 
							cbLinguagem.getValue(), 
							txtCaminho.getText(), 
							callback);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void succeeded() {
				super.failed();
				Platform.runLater(() -> {
					progress.getBarraProgresso().progressProperty().unbind();
					progress.getLog().textProperty().unbind();
					TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
					MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
				});

			}

			@Override
			protected void failed() {
				super.failed();
				System.out.println("Erro na thread ComicInfo: " + super.getMessage());
			}
		};

		progress.getBarraProgresso().progressProperty().bind(gerarJson.progressProperty());
		progress.getLog().textProperty().bind(gerarJson.messageProperty());

		Thread t = new Thread(gerarJson);
		t.start();
	}

	
	private Robot robot = new Robot();

	public void initialize(URL arg0, ResourceBundle arg1) {
		cbLinguagem.getItems().addAll(Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE,
				Language.PORTUGUESE_GOOGLE);
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
	}

	public static URL getFxmlLocate() {
		return MangasComicInfoController.class.getResource("/view/mangas/MangaComicInfo.fxml");
	}

}