package br.com.fenix.processatexto.controller.legendas

import com.jfoenix.controls.JFXTextArea
import com.jfoenix.controls.JFXTextField
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.robot.Robot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.ResourceBundle


class LegendasMarcasController : Initializable {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var txtAreaOriginal: JFXTextArea

    @FXML
    private lateinit var txtAreaProcessado: JFXTextArea

    @FXML
    private lateinit var txtPipe: JFXTextField

    private lateinit var controller: LegendasController
    var controllerPai: LegendasController
        get() = controller
        set(controller) {
            this.controller = controller
        }

    @FXML
    private fun onBtnLimpar() {
        txtAreaOriginal.text = ""
        txtAreaProcessado.text = ""
    }

    private fun getMarca(linha: String?): String {
        if (linha == null || linha.isEmpty())
            return ""

        val itens: List<String> = if (txtPipe.text == null || txtPipe.text.isEmpty())
            linha.split("\t")
        else
            linha.split(txtPipe.text)

        var tempo: String = itens[0].trim()
        tempo = tempo.substring(tempo.lastIndexOf(" ")).trim()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val time: LocalTime = LocalTime.parse(tempo, formatter)
        val nextTime: LocalTime = time.plusSeconds(3)
        var nome = if (itens.size > 1)
            itens[1].trim()
        else
            "Posição " + time.format(DateTimeFormatter.ofPattern("HH-mm-ss"))
        nome = nome.replace("\n".toRegex(), " ")
        return (time.format(DateTimeFormatter.ofPattern("HH:mm:ss.S")) + " " + nextTime.format(DateTimeFormatter.ofPattern("HH:mm:ss.S"))) + " " + nome + "\n"
    }

    @FXML
    private fun onBtnGerar() {
        if (txtAreaOriginal.text == null || txtAreaOriginal.text.isEmpty())
            return

        try {
            var marcas = ""
            if (txtAreaOriginal.text.contains("\n")) {
                for (linha in txtAreaOriginal.text.split("\n"))
                    marcas += getMarca(linha)
            } else
                marcas += getMarca(txtAreaOriginal.text)
            txtAreaProcessado.text = "start_region_table $marcas end_region_table".trimIndent()
        } catch (ex: Exception) {
            LOGGER.error("Erro ao processar marcas.", ex)
        }
    }

    private val robot: Robot = Robot()
    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        txtAreaOriginal.focusedProperty().addListener { _, oldValue, _ -> if (oldValue) onBtnGerar() }
        txtPipe.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(LegendasMarcasController::class.java)
        val fxmlLocate: URL get() = LegendasMarcasController::class.java.getResource("/view/legendas/LegendasMarcas.fxml") as URL
    }
}