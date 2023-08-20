package org.jisho.textosJapones.controller.mangas;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.events.JFXDialogEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;
import javafx.util.Callback;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.model.entities.comicinfo.MAL;
import org.jisho.textosJapones.model.entities.comicinfo.MAL.Registro;
import org.jisho.textosJapones.processar.comicinfo.ProcessaComicInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MangasComicInfoMalId implements Initializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MangasComicInfoMalId.class);

	final private static String STYLE_SHEET = AlertasPopup.class.getResource("/css/Dark_Theme.css").toExternalForm();

	@FXML
	public JFXTextField txtId;

	@FXML
	public JFXTextField txtNome;

	@FXML
	public ImageView imagem;
	
	private final MAL mal = new MAL("", "");
	private final Robot robot = new Robot();
	private Registro consulta = null;

	private static JFXButton btnConfirmar;
	private static JFXButton btnVoltar;
	private static JFXDialog dialog;
	
	private Boolean validaCampos() {
		Boolean valida = !txtId.getText().replaceAll("/[^0-9]+/g", "").trim().isEmpty();
		if (!valida)
			txtId.setUnFocusColor(Color.RED);
		return valida;
	}
	
	public String getId() {
		return validaCampos() ? txtId.getText() : "";
	}
	
	public Registro getObjeto() {
		return consulta;
	}
	
	public static void abreTelaCorrecao(StackPane rootStackPane, Node nodeBlur, Callback<Registro, Boolean> callback) {
		try {
			BoxBlur blur = new BoxBlur(3, 3, 3);
			JFXDialogLayout dialogLayout = new JFXDialogLayout();
			dialog = new JFXDialog(rootStackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getFxmlLocate());
			Parent newAnchorPane = loader.load();
			MangasComicInfoMalId cnt = loader.getController();

			Label titulo = new Label("Troca de Id");
			titulo.setFont(Font.font(20));
			titulo.setTextFill(Color.web("#ffffff", 0.8));

			List<JFXButton> botoes = new ArrayList<JFXButton>();

			btnConfirmar = new JFXButton("Confirmar");
			btnConfirmar.setOnAction(AE -> {
				String numero = cnt.getId().replaceAll("/[^0-9]+/g", "");
				if (!numero.isEmpty()) {
					callback.call(cnt.getObjeto());
					dialog.close();
				}
			});
			btnConfirmar.getStyleClass().add("background-Green2");
			botoes.add(btnConfirmar);
			
			btnVoltar = new JFXButton("Voltar");
			btnVoltar.setOnAction(AV -> dialog.close());
			btnVoltar.getStyleClass().add("background-White1");
			botoes.add(btnVoltar);

			dialogLayout.setHeading(titulo);
			dialogLayout.setBody(newAnchorPane);
			dialogLayout.setActions(botoes);

			dialog.getStylesheets().add(STYLE_SHEET);
			dialog.setPadding(new Insets(0, 0, 0, 0));
			dialog.setOnDialogClosed((JFXDialogEvent event1) -> {
				nodeBlur.setEffect(null);
			});

			nodeBlur.setEffect(blur);
			dialog.show();
		} catch (IOException e) {
			
			LOGGER.error(e.getMessage(), e);
		}
	}

	private void configuraListenert() {
		txtId.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				if (validaCampos()) {
					try {
						consulta = null;
						txtNome.setText("");
						imagem.setImage(null);
						
						txtId.setUnFocusColor(Color.web("#106ebe"));
						
						Long numero = Long.valueOf(getId().replaceAll("/[^0-9]+/g", ""));
						mal.getMyanimelist().clear();
						consulta = mal.addRegistro("", numero, false);
						ProcessaComicInfo.getById(numero, consulta);
						txtNome.setText(consulta.getNome());
						imagem.setImage(consulta.getImagem().getImage());
					} catch (Exception e) {
						
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		});
		
		txtId.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		txtNome.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		configuraListenert();
	}

	public static URL getFxmlLocate() {
		return MangasComicInfoMalId.class.getResource("/view/Mangas/MangaComicInfoMalId.fxml");
	}
}
