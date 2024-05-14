package br.com.fenix.processatexto.components.notification

import com.jfoenix.controls.*
import com.jfoenix.controls.events.JFXDialogEvent
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.effect.BoxBlur
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.stage.Modality
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.TrayIcon.MessageType
import java.awt.image.BufferedImage
import java.util.*
import javax.imageio.ImageIO

object AlertasPopup {
    val ALERTA: ImageView = ImageView(Image(AlertasPopup::class.java.getResourceAsStream("/images/alert/icoAlerta_48.png")))
    val AVISO: ImageView = ImageView(Image(AlertasPopup::class.java.getResourceAsStream("/images/alert/icoAviso_48.png")))
    val ERRO: ImageView = ImageView(Image(AlertasPopup::class.java.getResourceAsStream("/images/alert/icoErro_48.png")))
    val CONFIRMA: ImageView = ImageView(Image(AlertasPopup::class.java.getResourceAsStream("/images/alert/icoConfirma_48.png")))

    val CSS: String = AlertasPopup::class.java.getResource("/css/Dark_Alerts.css").toExternalForm()
    val CSS_THEME: String = AlertasPopup::class.java.getResource("/css/Dark_Theme.css").toExternalForm()
    private lateinit var ROOT_STACK_PANE: StackPane
    private lateinit var NODE_BLUR: Node

    var rootStackPane: StackPane
        get() = ROOT_STACK_PANE
        set(rootStackPane) {
            ROOT_STACK_PANE = rootStackPane
        }
    var nodeBlur: Node
        get() = NODE_BLUR
        set(nodeBlur) {
            NODE_BLUR = nodeBlur
        }

    /**
     *
     *
     * Função para apresentar mensagem de aviso, onde irá mostrar uma caixa no topo
     * e esmaecer o fundo.
     *
     *
     * @param Primeiro  parâmetro deve-se passar a referência para o stack pane.
     * @param Conforme  a cascata, obter o primeiro conteudo interno para que seja
     * esmaecido.
     * @param Parametro opcional, pode-se passar varios botões em uma lista, caso
     * não informe por padrão irá adicionar um botão ok.
     * @param Campo     **String** que irá conter a mensagem a ser exibida.
     */
    fun AvisoModal(rootStackPane: StackPane, nodeBlur: Node, botoes: MutableList<JFXButton>, titulo: String, texto: String) =
        dialogModern(rootStackPane, nodeBlur, botoes, titulo, texto, AVISO)

    /**
     *
     *
     * Função padrão de aviso que apenas recebe os textos, irá obter os pane global
     * do dashboard.
     *
     *
     */
    fun AvisoModal(titulo: String, texto: String) = dialogModern(ROOT_STACK_PANE, NODE_BLUR, mutableListOf(), titulo, texto, AVISO)

    /**
     *
     *
     * Função para apresentar mensagem de alerta, onde irá mostrar uma caixa no topo
     * e esmaecer o fundo.
     *
     *
     * @param Primeiro  parâmetro deve-se passar a referência para o stack pane.
     * @param Conforme  a cascata, obter o primeiro conteudo interno para que seja
     * esmaecido.
     * @param Parametro opcional, pode-se passar varios botões em uma lista, caso
     * não informe por padrão irá adicionar um botão ok.
     * @param Campo     **String** que irá conter a mensagem a ser exibida.
     */
    fun AlertaModal(rootStackPane: StackPane, nodeBlur: Node, botoes: MutableList<JFXButton>, titulo: String, texto: String) = dialogModern(rootStackPane, nodeBlur, botoes, titulo, texto, ALERTA)

    /**
     *
     *
     * Função padrão de alerta que apenas recebe os textos, irá obter os pane global
     * do dashboard.
     *
     *
     */
    fun AlertaModal(titulo: String, texto: String) = dialogModern(ROOT_STACK_PANE, NODE_BLUR, mutableListOf(), titulo, texto, ALERTA)

    /**
     *
     *
     * Função para apresentar mensagem de erro, onde irá mostrar uma caixa no topo e
     * esmaecer o fundo.
     *
     *
     * @param Primeiro  parâmetro deve-se passar a referência para o stack pane.
     * @param Conforme  a cascata, obter o primeiro conteudo interno para que seja
     * esmaecido.
     * @param Parametro opcional, pode-se passar varios botões em uma lista, caso
     * não informe por padrão irá adicionar um botão ok.
     * @param Campo     **String** que irá conter a mensagem a ser exibida.
     */
    fun ErroModal(rootStackPane: StackPane, nodeBlur: Node, botoes: MutableList<JFXButton>, titulo: String, texto: String) {
        dialogModern(rootStackPane, nodeBlur, botoes, titulo, texto, ERRO)
    }

    /**
     *
     *
     * Função padrão de alerta que apenas recebe os textos, irá obter os pane global
     * do dashboard.
     *
     *
     */
    fun ErroModal(titulo: String, texto: String) = dialogModern(ROOT_STACK_PANE, NODE_BLUR, mutableListOf(), titulo, texto, ERRO)

    /**
     *
     *
     * Função para apresentar mensagem com confirmação, onde irá mostrar uma caixa
     * no topo e esmaecer o fundo.
     *
     *
     * @param Primeiro  parâmetro deve-se passar a referência para o stack pane.
     * @param Conforme  a cascata, obter o primeiro conteudo interno para que seja
     * esmaecido.
     * @param Parametro opcional, pode-se passar varios botões em uma lista, caso
     * não informe por padrão irá adicionar um botão ok.
     * @param Campo     **String** que irá conter a mensagem a ser exibida.
     * @return Resulta o valor referente ao botão cancelar ou confirmar.
     */
    fun ConfirmacaoModal(rootStackPane: StackPane, nodeBlur: Node, titulo: String, texto: String): Boolean = alertModern(rootStackPane, nodeBlur, titulo, texto, CONFIRMA)

    /**
     *
     *
     * Função padrão para apresentar mensagem de confirmação que apenas recebe os
     * textos, irá obter os pane global do dashboard.
     *
     *
     */
    fun ConfirmacaoModal(titulo: String, texto: String): Boolean {
        return alertModern(ROOT_STACK_PANE, NODE_BLUR, titulo, texto, CONFIRMA)
    }

    var RESULTADO = false
    private fun alertModern(rootStackPane: StackPane, nodeBlur: Node, titulo: String, texto: String, imagem: ImageView): Boolean {
        RESULTADO = false
        val blur = BoxBlur(3.0, 3.0, 3)
        val alert: JFXAlert<String> = JFXAlert(rootStackPane.getScene().getWindow())
        alert.initModality(Modality.APPLICATION_MODAL)
        alert.setOverlayClose(false)
        val layout = JFXDialogLayout()
        val title = Label(titulo)
        title.getStyleClass().add("texto-stilo-fundo-azul")
        layout.setHeading(title)
        val content = JFXTextArea(texto)
        content.setEditable(false)
        content.setPrefHeight(100.0)
        content.getStyleClass().add("texto-stilo-fundo-azul")
        layout.setBody(HBox(CONFIRMA, content))
        layout.getStylesheets().add(CSS)
        layout.getStylesheets().add(CSS_THEME)
        val confirmButton = JFXButton("Confirmar")
        confirmButton.setDefaultButton(true)
        confirmButton.setOnAction { ConfirmarEvent ->
            RESULTADO = true
            alert.hideWithAnimation()
        }
        confirmButton.getStyleClass().add("btnConfirma")
        val cancelButton = JFXButton("Cancelar")
        cancelButton.setCancelButton(true)
        cancelButton.setOnAction { CancelarEvent ->
            RESULTADO = false
            alert.hideWithAnimation()
        }
        cancelButton.getStyleClass().add("btnCancela")
        layout.setActions(cancelButton, confirmButton)
        alert.setContent(layout)
        alert.onCloseRequestProperty().set { event1 -> nodeBlur.setEffect(null) }
        nodeBlur.setEffect(blur)

        // Devido a um erro no componente, não funciona o retorno padrão, será feito
        // pela variável resultado.
        alert.setResultConverter { buttonType -> null }
        val result: Optional<String> = alert.showAndWait()
        if (result.isPresent()) {
            alert.setResult(null)
        }
        return RESULTADO
    }

    private fun dialogModern(rootStackPane: StackPane, nodeBlur: Node, botoes: MutableList<JFXButton>, titulo: String, texto: String, imagem: ImageView) {
        val blur = BoxBlur(3.0, 3.0, 3)

        if (botoes.isEmpty())
            botoes.add(JFXButton("Ok"))

        val dialogLayout = JFXDialogLayout()
        val dialog = JFXDialog(rootStackPane, dialogLayout, JFXDialog.DialogTransition.CENTER)
        dialog.getStylesheets().add(CSS)
        botoes.forEach { controlButton ->
            controlButton.getStyleClass().add("btnAlerta")
            controlButton.addEventHandler(MouseEvent.MOUSE_CLICKED) { mouseEvent: MouseEvent -> dialog.close() }
        }
        dialogLayout.setHeading(Label(titulo))
        dialogLayout.setBody(HBox(imagem, Label(texto)))
        dialogLayout.setActions(botoes)
        dialog.setOnDialogClosed { event1: JFXDialogEvent -> nodeBlur.setEffect(null) }
        nodeBlur.setEffect(blur)
        dialog.show()
    }

    // Ver
    const val CAMINHO_ICONE = "/org/jisho/textosJapones/resources/images/bd/icoDataBase_48.png"
    fun showTrayMessage(title: String, message: String) {
        try {
            val tray: SystemTray = SystemTray.getSystemTray()
            val image: BufferedImage = ImageIO.read(AlertasPopup::class.java.getResource(CAMINHO_ICONE))
            val trayIcon = TrayIcon(image, "Teste")
            trayIcon.setImageAutoSize(true)
            trayIcon.setToolTip("Teste")
            tray.add(trayIcon)
            trayIcon.displayMessage(title, message, MessageType.INFO)
            tray.remove(trayIcon)
        } catch (exp: Exception) {
            exp.printStackTrace()
        }
    }
}