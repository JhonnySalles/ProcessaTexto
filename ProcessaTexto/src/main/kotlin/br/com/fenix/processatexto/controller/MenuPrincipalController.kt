package br.com.fenix.processatexto.controller

import br.com.fenix.processatexto.components.animation.Animacao
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.mysql.Backup
import br.com.fenix.processatexto.model.enums.*
import br.com.fenix.processatexto.service.SincronizacaoServices
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXComboBox
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.event.Event
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.geometry.Insets
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Screen
import javafx.util.Duration
import org.controlsfx.control.PopOver
import org.controlsfx.control.PopOver.ArrowLocation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class MenuPrincipalController : Initializable {

    @FXML
    private lateinit var apGlobal: AnchorPane

    @FXML
    private lateinit var rootStackPane: StackPane

    @FXML
    protected lateinit var root: AnchorPane

    @FXML
    private lateinit var btnBanco: JFXButton

    @FXML
    private lateinit var imgConexaoBase: ImageView

    @FXML
    private lateinit var btnBackup: JFXButton

    @FXML
    private lateinit var imgBackup: ImageView

    @FXML
    private lateinit var btnCompartilhamento: JFXButton

    @FXML
    private lateinit var imgCompartilhamento: ImageView

    @FXML
    private lateinit var cbSite: JFXComboBox<Site>

    @FXML
    private lateinit var cbModo: JFXComboBox<Modo>

    @FXML
    private lateinit var cbDicionario: JFXComboBox<Dicionario>

    @FXML
    private lateinit var cbContaGoolge: JFXComboBox<Api>

    @FXML
    private lateinit var hbContainerLog: HBox

    @FXML
    private lateinit var lblLog: Label

    @FXML
    private lateinit var scpBarraProgress: ScrollPane

    @FXML
    private lateinit var vbBarraProgress: VBox

    @FXML
    private lateinit var tbAnki: Tab

    @FXML
    private lateinit var tbRevisar: Tab

    @FXML
    private lateinit var tbTraduzir: Tab

    @FXML
    private lateinit var tbLegendas: Tab

    @FXML
    private lateinit var tbNovels: Tab

    @FXML
    private lateinit var tbMangas: Tab

    @FXML
    private lateinit var revisarController: RevisarController

    private val progressBar: MutableMap<GrupoBarraProgressoController, Node> = mutableMapOf()
    private var pop: PopOver? = null

    private lateinit var tmlImagemBackup: Timeline
    private val animacao = Animacao()

    @FXML
    private fun onBtnVerificaConexao() {
        verificaConexao()
    }

    @FXML
    private fun onBtnCompartilhamentoDatabase() {
        compartilhaDataBase()
    }

    @FXML
    private fun onSelectRevisarChanged(event: Event) {
        SincronizacaoServices.processar = tbRevisar.isSelected
        revisarController.iniciaFirebase()
    }

    @FXML
    private fun onBtnConexaoOnMouseClicked(mouseClick: MouseEvent) {
        if (mouseClick.button === MouseButton.SECONDARY) {
            if (!pop!!.isShowing)
                mostrarConfiguracao()
        }
    }

    fun setImagemBancoErro(erro: String) {
        animacao.tmLineImageBanco.stop()
        imgConexaoBase.image = imgAnimaBancoErro
        Notificacoes.notificacao(Notificacao.ERRO, "Erro.", erro)
    }

    fun mostrarConfiguracao(): MenuPrincipalController {
        val screenBounds: Rectangle2D = Screen.getPrimary().visualBounds
        val scene: Scene = btnBanco.scene
        val windowCoord = Point2D(scene.window.x, scene.window.y)
        val sceneCoord = Point2D(scene.x, scene.y)
        val nodeCoord: Point2D = btnBanco.localToScene(0.0, 0.0)
        val cordenadaX = (windowCoord.x + sceneCoord.x + nodeCoord.x).roundToInt()
        if (cordenadaX < screenBounds.width / 2)
            pop!!.arrowLocationProperty().set(ArrowLocation.LEFT_TOP)
        else
            pop!!.arrowLocationProperty().set(ArrowLocation.RIGHT_TOP)
        pop!!.show(btnBanco, 30.0)
        return this
    }

    fun cancelaBackup() {
        animacao.tmLineImageBackup.stop()
        imgBackup.image = imgAnimaBackup
    }

    fun importaBackup() {
        animacao.animaImageBackup(imgBackup, imgAnimaImporta, imgAnimaImportaEspera)
        animacao.tmLineImageBackup.play()
        Backup.importarBackup(this)
    }

    fun importaConcluido(erro: Boolean) {
        animacao.tmLineImageBackup.stop()
        if (erro)
            imgBackup.image = imgAnimaImportaErro
        else {
            imgBackup.image = imgAnimaImportaConcluido
            verificaConexao()
        }
        tmlImagemBackup.play()
    }

    fun exportaBackup() {
        animacao.animaImageBackup(imgBackup, imgAnimaExporta, imgAnimaExportaEspera)
        animacao.tmLineImageBackup.play()
        Backup.exportarBackup(this)
    }

    fun exportaConcluido(erro: Boolean) {
        animacao.tmLineImageBackup.stop()
        if (erro) imgBackup.image = imgAnimaExportaErro else imgBackup.image = imgAnimaExportaConcluido
        tmlImagemBackup.play()
    }

    fun setAviso(aviso: String) = Notificacoes.notificacao(Notificacao.AVISO, "Aviso.", aviso)

    val popPup: PopOver?
        get() = pop
    val modo: Modo
        get() = cbModo.selectionModel.selectedItem
    val dicionario: Dicionario
        get() = cbDicionario.selectionModel.selectedItem
    var contaGoogle: Api
        get() = cbContaGoolge.selectionModel.selectedItem
        set(contaGoogle) {
            cbContaGoolge.selectionModel.select(contaGoogle)
        }
    val site: Site
        get() = cbSite.selectionModel.selectedItem

    fun getLblLog(): Label = lblLog

    fun setLblLog(text: String) {
        lblLog.text = text
    }

    private fun progressBarVisible(visible: Boolean) {
        scpBarraProgress.isVisible = visible
    }

    fun criaBarraProgresso(): GrupoBarraProgressoController? {
        try {
            progressBarVisible(true)
            lblLog.text = ""
            val loader = FXMLLoader()
            loader.location = GrupoBarraProgressoController.fxmlLocate
            val barra: Node = loader.load() // Necessário primeiro iniciar o loader para pegar o controller.
            val cnt: GrupoBarraProgressoController = loader.getController()
            vbBarraProgress.children.add(barra)
            progressBar[cnt] = barra
            return cnt
        } catch (e: IOException) {
            println("Erro ao criar barra de progresso.")
            LOGGER.error(e.message, e)
        }
        return null
    }

    fun destroiBarraProgresso(barra: GrupoBarraProgressoController?, texto: String?) {
        if (progressBar.containsKey(barra)) {
            val item: Node? = progressBar[barra]
            vbBarraProgress.children.remove(item)
            progressBar.remove(barra)
            progressBarVisible(progressBar.isNotEmpty())
        }
        lblLog.text = texto
    }

    fun verificaConexao() {
        animacao.tmLineImageBanco.play()

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        val verificaConexao: Task<String> = object : Task<String>() {
            @Override
            @Throws(Exception::class)
            override fun call(): String {
                TimeUnit.SECONDS.sleep(1)
                return JdbcFactory.testaConexao(Conexao.PROCESSA_TEXTO)
            }

            @Override
            override fun succeeded() {
                animacao.tmLineImageBanco.stop()
                val conectado: String = value
                if (conectado.isNotEmpty()) imgConexaoBase.image = imgAnimaBancoConectado else imgConexaoBase.image = imgAnimaBancoErro
            }
        }
        val t = Thread(verificaConexao)
        t.start()
    }

    private fun verificaBases() {
        tbLegendas.isDisable = JdbcFactory.getConfiguracao(Conexao.DECKSUBTITLE).isEmpty
        tbNovels.isDisable = JdbcFactory.getConfiguracao(Conexao.NOVEL_EXTRACTOR).isEmpty
        tbMangas.isDisable = JdbcFactory.getConfiguracao(Conexao.MANGA_EXTRACTOR).isEmpty
    }

    var sincronizacao: SincronizacaoServices = SincronizacaoServices(this)
    fun compartilhaDataBase() {
        val compartilhaDatabase: Task<Boolean> = object : Task<Boolean>() {
            @Override
            override fun call(): Boolean {
                sincronizacao.consultar()
                return sincronizacao.sincroniza()
            }

            @Override
            override fun succeeded() {
                if (!value) Notificacoes.notificacao(
                    Notificacao.ERRO,
                    "Compartilhamento de alterações da Database.",
                    "Não foi possível sincronizar os dados com a cloud."
                ) else Notificacoes.notificacao(Notificacao.SUCESSO, "Compartilhamento de alterações da Database.", "Sincronização de dados com a cloud concluída com sucesso.")
            }
        }
        val t = Thread(compartilhaDatabase)
        t.start()
    }

    fun animacaoSincronizacaoDatabase(isProcessando: Boolean, isErro: Boolean) {
        Platform.runLater {
            if (isProcessando) animacao.tmLineImageSincronizacao.play() else {
                animacao.tmLineImageSincronizacao.stop()
                if (isErro)
                    imgCompartilhamento.image = imgAnimaCompartilhaErro
                else
                    imgCompartilhamento.image = imgAnimaCompartilhaEnvio
            }
        }
    }

    private fun criaMenuBackup() {
        val menuBackup = ContextMenu()
        val miBackup = MenuItem("Backup")
        val imgExporta = ImageView(imgAnimaExporta)
        imgExporta.fitHeight = 20.0
        imgExporta.fitWidth = 20.0
        miBackup.graphic = imgExporta
        miBackup.setOnAction { exportaBackup() }
        val miRestaurar = MenuItem("Restaurar")
        val imgImporta = ImageView(imgAnimaImporta)
        imgImporta.fitHeight = 20.0
        imgImporta.fitWidth = 20.0
        miRestaurar.graphic = imgImporta
        miRestaurar.setOnAction { importaBackup() }
        menuBackup.items.addAll(miBackup, miRestaurar)
        btnBackup.setOnMouseClicked {
            val scene: Scene = btnBanco.scene
            val windowCoord = Point2D(scene.window.x, scene.window.y)
            val sceneCoord = Point2D(scene.x, scene.y)
            val nodeCoord: Point2D = btnBanco.localToScene(0.0, 0.0)
            val cordenadaX = (windowCoord.x + sceneCoord.x + nodeCoord.x).roundToInt()
            val cordenadaY = (windowCoord.y + sceneCoord.y + nodeCoord.y).roundToInt()
            menuBackup.show(btnBackup, cordenadaX + 95.0, cordenadaY + 30.0)
        }
        tmlImagemBackup = Timeline(KeyFrame(Duration.millis(5000.0), { cancelaBackup() }))
    }

    private fun criaConfiguracao(): MenuPrincipalController {
        pop = PopOver()
        val url: URL = MenuConfiguracaoController.fxmlLocate
        val loader = FXMLLoader()
        loader.location = url
        val vbox = VBox()
        vbox.padding = Insets(3.0) // 10px padding todos os lados
        try {
            vbox.children.add(loader.load())
            val cntConfiguracao: MenuConfiguracaoController = loader.getController()
            cntConfiguracao.setControllerPai(this)
            pop!!.title = "Configuração banco de dados"
            pop!!.contentNode = vbox
            pop!!.cornerRadius = 5.0
            pop!!.isHideOnEscape = true
            pop!!.isAutoFix = true
            pop!!.isAutoHide = true
            pop!!.setOnHidden { cntConfiguracao.salvar() }
            pop!!.setOnShowing { cntConfiguracao.carregar() }
            pop!!.root.stylesheets.add(MenuPrincipalController::class.java.getResource("/css/Dark_PopOver.css").toExternalForm())
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
        }
        return this
    }

    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        controller = this
        scpBarraProgress.managedProperty().bind(scpBarraProgress.visibleProperty())
        progressBarVisible(false)
        animacao.animaImageBanco(imgConexaoBase, imgAnimaBanco, imgAnimaBancoEspera)
        criaConfiguracao()
        criaMenuBackup()
        animacao.animaImageSincronizacao(imgCompartilhamento, imgAnimaCompartilha, imgAnimaCompartilhaEspera)

        cbSite.items.addAll(Site.values())
        cbSite.selectionModel.select(Site.TODOS)
        cbModo.items.addAll(Modo.values())
        cbModo.selectionModel.select(Modo.C)
        cbDicionario.items.addAll(Dicionario.values())
        cbDicionario.selectionModel.select(Dicionario.FULL)
        cbContaGoolge.items.addAll(Api.values())
        cbContaGoolge.selectionModel.selectFirst()

        /* Setando as variáveis para o alerta padrão. */
        AlertasPopup.rootStackPane = rootStackPane
        AlertasPopup.nodeBlur = root
        Notificacoes.rootAnchorPane = apGlobal

        sincronizacao.setObserver { observable ->
            if (!sincronizacao.isSincronizando)
                Platform.runLater {
                    if (!observable.list.isEmpty()) {
                        lblLog.text = "Pendente de envio " + observable.list.size + " registro(s)."
                        imgCompartilhamento.image = imgAnimaCompartilhaEspera
                    } else
                        imgCompartilhamento.image = imgAnimaCompartilha
                }
        }

        if (!sincronizacao.isConfigurado)
            imgCompartilhamento.image = imgAnimaCompartilhaErro
        else if (sincronizacao.listSize() > 0) {
            lblLog.text = "Pendente de envio " + sincronizacao.listSize() + " registro(s)."
            imgCompartilhamento.image = imgAnimaCompartilhaEspera
        } else imgCompartilhamento.image = imgAnimaCompartilha
        verificaConexao()
        verificaBases()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MenuPrincipalController::class.java)

        lateinit var controller: MenuPrincipalController
            private set

        val imgAnimaBanco: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoDataBase_48.png"))
        val imgAnimaBancoEspera: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoDataEspera_48.png"))
        val imgAnimaBancoErro: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoDataSemConexao_48.png"))
        val imgAnimaBancoConectado: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoDataConectado_48.png"))
        val imgAnimaBackup: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_48.png"))
        val imgAnimaExporta: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_Exportando_48.png"))
        val imgAnimaExportaEspera: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_Exportando_Espera_48.png"))
        val imgAnimaExportaErro: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_Exportando_Erro_48.png"))
        val imgAnimaExportaConcluido: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_Exportando_Concluido_48.png"))
        val imgAnimaImporta: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_Importando_48.png"))
        val imgAnimaImportaEspera: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_Importando_Espera_48.png"))
        val imgAnimaImportaErro: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_Importando_Erro_48.png"))
        val imgAnimaImportaConcluido: Image = Image(Animacao::class.java.getResourceAsStream("/images/export/icoBDBackup_Importando_Concluido_48.png"))
        val imgAnimaCompartilha: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoCompartilhamento_48.png"))
        val imgAnimaCompartilhaEspera: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoCompartilhamentoEspera_48.png"))
        val imgAnimaCompartilhaErro: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoCompartilhamentoErro_48.png"))
        val imgAnimaCompartilhaEnvio: Image = Image(Animacao::class.java.getResourceAsStream("/images/bd/icoCompartilhamentoEnvio_48.png"))

        val fxmlLocate: URL get() = MenuPrincipalController::class.java.getResource("/view/MenuPrincipal.fxml")
        val iconLocate: String get() = "/images/icoTextoJapones_128.png"
    }
}