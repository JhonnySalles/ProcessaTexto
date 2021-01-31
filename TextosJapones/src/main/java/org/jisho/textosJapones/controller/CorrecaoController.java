package org.jisho.textosJapones.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.notification.Notificacoes;

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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.text.Font;

public class CorrecaoController implements Initializable {

	final private static String STYLE_SHEET = AlertasPopup.class
			.getResource("/org/jisho/textosJapones/resources/css/Dark_Theme.css").toExternalForm();

	@FXML
	public JFXTextField txtVocabulario;

	@FXML
	public JFXTextField txtTraducao;

	public static JFXButton btnVoltar;
	public static JFXButton btnCancelar;
	public static JFXButton btnConfirmar;

	private static JFXDialog dialog;

	private VocabularioServices vocabServ;
	private Vocabulario vocabulario;
	private Robot robot = new Robot();

	private void onBtnCancelar() {
		limpar();
	}

	private void onBtnConfirmar() {
		salvar();
	}

	private CorrecaoController servico() {
		vocabServ = new VocabularioServices();
		return this;
	}

	private CorrecaoController procurar() {
		if (!txtVocabulario.getText().trim().isEmpty()) {
			if (vocabServ == null)
				servico();

			try {
				if (vocabServ.existe(txtVocabulario.getText())) {
					vocabulario = vocabServ.select(txtVocabulario.getText().trim());
					carregar();
				} else {
					txtVocabulario.setUnFocusColor(Color.RED);
					Notificacoes.notificacao(Notificacao.ERRO, "Vocabulário informado não encontrado.",
							txtVocabulario.getText());
				}

			} catch (ExcessaoBd e) {
				e.printStackTrace();
				Notificacoes.notificacao(Notificacao.ERRO, "Erro ao carregar vocabulário.", txtVocabulario.getText());
				txtVocabulario.setUnFocusColor(Color.RED);
			}
		}
		return this;
	}

	private CorrecaoController salvar() {
		if (!txtTraducao.getText().trim().isEmpty()) {
			if (vocabServ == null)
				servico();

			try {
				atualiza();
				vocabServ.insertOrUpdate(vocabulario);
				Notificacoes.notificacao(Notificacao.SUCESSO, "Vocabulário salvo com sucesso.",
						vocabulario.getTraducao());
				limpar();
				txtVocabulario.requestFocus();
			} catch (ExcessaoBd e) {
				e.printStackTrace();
				Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar tradução.", txtTraducao.getText());
			}
		} else
			txtVocabulario.setUnFocusColor(Color.RED);

		return this;
	}

	private CorrecaoController carregar() {
		txtVocabulario.setText(vocabulario.getVocabulario());
		txtTraducao.setText(vocabulario.getTraducao());
		return this;
	}

	private CorrecaoController atualiza() {
		vocabulario.setTraducao(txtTraducao.getText().trim());
		return this;
	}

	private CorrecaoController limpar() {
		txtVocabulario.setText("");
		txtTraducao.setText("");
		vocabulario = null;
		return this;
	}

	public static void abreTelaCorrecao(StackPane rootStackPane, Node nodeBlur) {
		try {
			BoxBlur blur = new BoxBlur(3, 3, 3);
			JFXDialogLayout dialogLayout = new JFXDialogLayout();
			dialog = new JFXDialog(rootStackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getFxmlLocate());
			Parent newAnchorPane = loader.load();
			CorrecaoController cnt = loader.getController();

			Label titulo = new Label("Tela de correção");
			titulo.setFont(Font.font(20));
			titulo.setTextFill(Color.web("#ffffff", 0.8));

			List<JFXButton> botoes = new ArrayList<JFXButton>();

			btnConfirmar = new JFXButton("Confirmar");
			btnConfirmar.setOnAction(AE -> cnt.onBtnConfirmar());
			btnConfirmar.getStyleClass().add("background-Green2");
			botoes.add(btnConfirmar);

			btnCancelar = new JFXButton("Cancelar");
			btnCancelar.setOnAction(AC -> cnt.onBtnCancelar());
			btnCancelar.getStyleClass().add("background-Red2");
			botoes.add(btnCancelar);

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

			e.printStackTrace();
		}
	}

	private void configuraListenert() {
		txtVocabulario.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal) {
				txtVocabulario.setUnFocusColor(Color.web("#106ebe"));
				procurar();
			}
		});

		txtTraducao.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (oldVal)
				txtTraducao.setUnFocusColor(Color.web("#106ebe"));
		});

		txtVocabulario.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER))
					robot.keyPress(KeyCode.TAB);
			}
		});

		txtTraducao.setOnKeyPressed(new EventHandler<KeyEvent>() {
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
		servico();
	}

	public static URL getFxmlLocate() {
		return CorrecaoController.class.getResource("/org/jisho/textosJapones/view/Correcao.fxml");
	}
}
