package org.jisho.textosJapones.components.notification;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.events.JFXDialogEvent;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertasPopup {

	public final static ImageView ALERTA = new ImageView(
			new Image(AlertasPopup.class.getResourceAsStream("/images/alert/icoAlerta_48.png")));
	public final static ImageView AVISO = new ImageView(
			new Image(AlertasPopup.class.getResourceAsStream("/images/alert/icoAviso_48.png")));
	public final static ImageView ERRO = new ImageView(
			new Image(AlertasPopup.class.getResourceAsStream("/images/alert/icoErro_48.png")));
	public final static ImageView CONFIRMA = new ImageView(
			new Image(AlertasPopup.class.getResourceAsStream("/images/alert/icoConfirma_48.png")));

	public final static String CSS = AlertasPopup.class.getResource("/css/Dark_Alerts.css").toExternalForm();
	public final static String CSS_THEME = AlertasPopup.class.getResource("/css/Dark_Theme.css").toExternalForm();

	private static StackPane ROOT_STACK_PANE;
	private static Node NODE_BLUR;

	public static StackPane getRootStackPane() {
		return ROOT_STACK_PANE;
	}

	public static void setRootStackPane(StackPane rootStackPane) {
		AlertasPopup.ROOT_STACK_PANE = rootStackPane;
	}

	public static Node getNodeBlur() {
		return NODE_BLUR;
	}

	public static void setNodeBlur(Node nodeBlur) {
		AlertasPopup.NODE_BLUR = nodeBlur;
	}

	/**
	 * <p>
	 * Função para apresentar mensagem de aviso, onde irá mostrar uma caixa no topo
	 * e esmaecer o fundo.
	 * </p>
	 * 
	 * @param Primeiro  parâmetro deve-se passar a referência para o stack pane.
	 * @param Conforme  a cascata, obter o primeiro conteudo interno para que seja
	 *                  esmaecido.
	 * @param Parametro opcional, pode-se passar varios botões em uma lista, caso
	 *                  não informe por padrão irá adicionar um botão ok.
	 * @param Campo     <b>String</b> que irá conter a mensagem a ser exibida.
	 * 
	 */
	public static void AvisoModal(StackPane rootStackPane, Node nodeBlur, List<JFXButton> botoes, String titulo,
			String texto) {
		dialogModern(rootStackPane, nodeBlur, botoes, titulo, texto, AVISO);
	}

	/**
	 * <p>
	 * Função padrão de aviso que apenas recebe os textos, irá obter os pane global
	 * do dashboard.
	 * </p>
	 * 
	 */
	public static void AvisoModal(String titulo, String texto) {
		dialogModern(ROOT_STACK_PANE, NODE_BLUR, null, titulo, texto, AVISO);
	}

	/**
	 * <p>
	 * Função para apresentar mensagem de alerta, onde irá mostrar uma caixa no topo
	 * e esmaecer o fundo.
	 * </p>
	 * 
	 * @param Primeiro  parâmetro deve-se passar a referência para o stack pane.
	 * @param Conforme  a cascata, obter o primeiro conteudo interno para que seja
	 *                  esmaecido.
	 * @param Parametro opcional, pode-se passar varios botões em uma lista, caso
	 *                  não informe por padrão irá adicionar um botão ok.
	 * @param Campo     <b>String</b> que irá conter a mensagem a ser exibida.
	 * 
	 */
	public static void AlertaModal(StackPane rootStackPane, Node nodeBlur, List<JFXButton> botoes, String titulo,
			String texto) {
		dialogModern(rootStackPane, nodeBlur, botoes, titulo, texto, ALERTA);
	}

	/**
	 * <p>
	 * Função padrão de alerta que apenas recebe os textos, irá obter os pane global
	 * do dashboard.
	 * </p>
	 * 
	 */
	public static void AlertaModal(String titulo, String texto) {
		dialogModern(ROOT_STACK_PANE, NODE_BLUR, null, titulo, texto, ALERTA);
	}

	/**
	 * <p>
	 * Função para apresentar mensagem de erro, onde irá mostrar uma caixa no topo e
	 * esmaecer o fundo.
	 * </p>
	 * 
	 * @param Primeiro  parâmetro deve-se passar a referência para o stack pane.
	 * @param Conforme  a cascata, obter o primeiro conteudo interno para que seja
	 *                  esmaecido.
	 * @param Parametro opcional, pode-se passar varios botões em uma lista, caso
	 *                  não informe por padrão irá adicionar um botão ok.
	 * @param Campo     <b>String</b> que irá conter a mensagem a ser exibida.
	 * 
	 */
	public static void ErroModal(StackPane rootStackPane, Node nodeBlur, List<JFXButton> botoes, String titulo,
			String texto) {
		dialogModern(rootStackPane, nodeBlur, botoes, titulo, texto, ERRO);
	}

	/**
	 * <p>
	 * Função padrão de alerta que apenas recebe os textos, irá obter os pane global
	 * do dashboard.
	 * </p>
	 * 
	 */
	public static void ErroModal(String titulo, String texto) {
		dialogModern(ROOT_STACK_PANE, NODE_BLUR, null, titulo, texto, ERRO);
	}

	/**
	 * <p>
	 * Função para apresentar mensagem com confirmação, onde irá mostrar uma caixa
	 * no topo e esmaecer o fundo.
	 * </p>
	 * 
	 * @param Primeiro  parâmetro deve-se passar a referência para o stack pane.
	 * @param Conforme  a cascata, obter o primeiro conteudo interno para que seja
	 *                  esmaecido.
	 * @param Parametro opcional, pode-se passar varios botões em uma lista, caso
	 *                  não informe por padrão irá adicionar um botão ok.
	 * @param Campo     <b>String</b> que irá conter a mensagem a ser exibida.
	 * @return Resulta o valor referente ao botão cancelar ou confirmar.
	 * 
	 */
	public static boolean ConfirmacaoModal(StackPane rootStackPane, Node nodeBlur, String titulo, String texto) {
		return alertModern(rootStackPane, nodeBlur, titulo, texto, CONFIRMA);
	}

	/**
	 * <p>
	 * Função padrão para apresentar mensagem de confirmação que apenas recebe os
	 * textos, irá obter os pane global do dashboard.
	 * </p>
	 * 
	 */
	public static boolean ConfirmacaoModal(String titulo, String texto) {
		return alertModern(ROOT_STACK_PANE, NODE_BLUR, titulo, texto, CONFIRMA);
	}

	static Boolean RESULTADO = false;

	private static boolean alertModern(StackPane rootStackPane, Node nodeBlur, String titulo, String texto,
			ImageView imagem) {
		RESULTADO = false;
		BoxBlur blur = new BoxBlur(3, 3, 3);

		JFXAlert<String> alert = new JFXAlert<>((Stage) rootStackPane.getScene().getWindow());
		alert.initModality(Modality.APPLICATION_MODAL);
		alert.setOverlayClose(false);

		JFXDialogLayout layout = new JFXDialogLayout();
		Label title = new Label(titulo);
		title.getStyleClass().add("texto-stilo-fundo-azul");
		layout.setHeading(title);
		JFXTextArea content = new JFXTextArea(texto);
		content.setEditable(false);
		content.setPrefHeight(100);
		content.getStyleClass().add("texto-stilo-fundo-azul");

		layout.setBody(new HBox(CONFIRMA, content));
		layout.getStylesheets().add(CSS);
		layout.getStylesheets().add(CSS_THEME);

		JFXButton confirmButton = new JFXButton("Confirmar");
		confirmButton.setDefaultButton(true);
		confirmButton.setOnAction(ConfirmarEvent -> {
			RESULTADO = true;
			alert.hideWithAnimation();
		});
		confirmButton.getStyleClass().add("btnConfirma");

		JFXButton cancelButton = new JFXButton("Cancelar");
		cancelButton.setCancelButton(true);
		cancelButton.setOnAction(CancelarEvent -> {
			RESULTADO = false;
			alert.hideWithAnimation();
		});
		cancelButton.getStyleClass().add("btnCancela");

		layout.setActions(cancelButton, confirmButton);
		alert.setContent(layout);

		alert.onCloseRequestProperty().set(event1 -> nodeBlur.setEffect(null));
		nodeBlur.setEffect(blur);

		// Devido a um erro no componente, não funciona o retorno padrão, será feito
		// pela variável resultado.
		alert.setResultConverter(buttonType -> {
			return null;
		});
		Optional<String> result = alert.showAndWait();
		if (result.isPresent()) {
			alert.setResult(null);
		}

		return RESULTADO;
	}

	private static void dialogModern(StackPane rootStackPane, Node nodeBlur, List<JFXButton> botoes, String titulo,
			String texto, ImageView imagem) {
		BoxBlur blur = new BoxBlur(3, 3, 3);

		if (botoes == null)
			botoes = new ArrayList<JFXButton>();

		if (botoes.isEmpty())
			botoes.add(new JFXButton("Ok"));

		JFXDialogLayout dialogLayout = new JFXDialogLayout();
		JFXDialog dialog = new JFXDialog(rootStackPane, dialogLayout, JFXDialog.DialogTransition.CENTER);

		dialog.getStylesheets().add(CSS);

		botoes.forEach(controlButton -> {
			controlButton.getStyleClass().add("btnAlerta");
			controlButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
				dialog.close();
			});
		});

		dialogLayout.setHeading(new Label(titulo));

		dialogLayout.setBody(new HBox(imagem, new Label(texto)));
		dialogLayout.setActions(botoes);
		dialog.setOnDialogClosed((JFXDialogEvent event1) -> {
			nodeBlur.setEffect(null);
		});
		nodeBlur.setEffect(blur);
		dialog.show();
	}

	// Ver
	public static final String CAMINHO_ICONE = "/org/jisho/textosJapones/resources/images/bd/icoDataBase_48.png";

	public static void showTrayMessage(String title, String message) {
		try {
			SystemTray tray = SystemTray.getSystemTray();
			BufferedImage image = ImageIO.read(AlertasPopup.class.getResource(CAMINHO_ICONE));
			TrayIcon trayIcon = new TrayIcon(image, "Teste");
			trayIcon.setImageAutoSize(true);
			trayIcon.setToolTip("Teste");
			tray.add(trayIcon);
			trayIcon.displayMessage(title, message, MessageType.INFO);
			tray.remove(trayIcon);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}
}
