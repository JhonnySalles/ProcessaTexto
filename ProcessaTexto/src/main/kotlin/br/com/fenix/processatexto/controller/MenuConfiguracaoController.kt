package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.util.configuration.Configuracao
import br.com.fenix.processatexto.util.constraints.Validadores
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXPasswordField
import com.jfoenix.controls.JFXTextField
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.stage.DirectoryChooser
import java.io.File
import java.net.URL
import java.util.*


class MenuConfiguracaoController : Initializable {

    @FXML
    lateinit var txtUsuario: JFXTextField

    @FXML
    lateinit var pswSenha: JFXPasswordField

    @FXML
    lateinit var txtServer: JFXTextField

    @FXML
    lateinit var txtPorta: JFXTextField

    @FXML
    lateinit var txtDataBase: JFXTextField

    @FXML
    lateinit var txtCaminhoMysql: JFXTextField

    @FXML
    lateinit var txtCaminhoWinrar: JFXTextField

    @FXML
    lateinit var txtCaminhoCommicTagger: JFXTextField

    @FXML
    lateinit var btnCaminhoMysql: JFXButton

    @FXML
    lateinit var btnCaminhoWinrar: JFXButton

    @FXML
    lateinit var btnCaminhoCommicTagger: JFXButton

    private lateinit var controller: MenuPrincipalController

    @FXML
    private fun onBtnCarregarCaminhoMysql() {
        controller.popPup!!.isDetached = true
        val caminho = selecionaPasta("Selecione a pasta do mysql", txtCaminhoMysql.text)
        txtCaminhoMysql.text = caminho
    }

    @FXML
    private fun onBtnCarregarCaminhoWinrar() {
        controller.popPup!!.isDetached = true
        txtCaminhoWinrar.text = selecionaPasta("Selecione a pasta do winrar", txtCaminhoWinrar.text)
    }

    @FXML
    private fun onBtnCarregarCaminhoCommicTagger() {
        controller.popPup!!.isDetached = true
        txtCaminhoCommicTagger.text = selecionaPasta("Selecione a pasta do commictagger", txtCaminhoCommicTagger.text)
    }

    fun salvar() {
        Configuracao.server = txtServer.text
        Configuracao.port = txtPorta.text
        Configuracao.database = txtDataBase.text
        Configuracao.user = txtUsuario.text
        Configuracao.password = pswSenha.text
        Configuracao.caminhoMysql = txtCaminhoMysql.text
        Configuracao.caminhoWinrar = txtCaminhoWinrar.text
        Configuracao.caminhoCommicTagger = txtCaminhoCommicTagger.text
        Configuracao.saveProperties()
    }

    fun carregar() {
        txtServer.text = Configuracao.server
        txtPorta.text = Configuracao.port
        txtDataBase.text = Configuracao.database
        txtUsuario.text = Configuracao.user
        pswSenha.text = Configuracao.password
        txtCaminhoMysql.text = Configuracao.caminhoMysql
        txtCaminhoWinrar.text = Configuracao.caminhoWinrar
        txtCaminhoCommicTagger.text = Configuracao.caminhoCommicTagger
    }

    private fun selecionaPasta(titulo: String, pasta: String?): String {
        val fileChooser = DirectoryChooser()
        fileChooser.title = titulo

        if (pasta != null && pasta.isNotEmpty())
            fileChooser.initialDirectory = File(pasta)

        val caminho: File = fileChooser.showDialog(null)

        return if (caminho == null) "" else caminho.absolutePath
    }

    fun setControllerPai(cnt: MenuPrincipalController) {
        controller = cnt
    }

    @Override
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        Validadores.setTextFieldNotEmpty(txtServer)
        Validadores.setTextFieldNotEmpty(txtPorta)
        Validadores.setTextFieldNotEmpty(txtDataBase)
        Validadores.setTextFieldNotEmpty(txtUsuario)
        Validadores.setTextFieldNotEmpty(pswSenha)
    }

    companion object {
        val fxmlLocate: URL get() = MenuConfiguracaoController::class.java.getResource("/view/MenuConfiguracao.fxml") as URL
    }
}