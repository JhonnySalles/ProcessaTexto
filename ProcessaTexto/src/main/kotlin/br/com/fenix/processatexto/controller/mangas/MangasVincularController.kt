package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.Run
import br.com.fenix.processatexto.components.ListViewNoSelectionModel
import br.com.fenix.processatexto.components.TableViewNoSelectionModel
import br.com.fenix.processatexto.components.animation.Animacao
import br.com.fenix.processatexto.components.listener.VinculoListener
import br.com.fenix.processatexto.components.listener.VinculoServiceListener
import br.com.fenix.processatexto.components.listener.VinculoTextoListener
import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.fileparse.Parse
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaPagina
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import br.com.fenix.processatexto.model.entities.processatexto.Atributos
import br.com.fenix.processatexto.model.entities.processatexto.Vinculo
import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.enums.Notificacao
import br.com.fenix.processatexto.model.enums.Pagina
import br.com.fenix.processatexto.model.enums.Tabela
import br.com.fenix.processatexto.service.VincularServices
import br.com.fenix.processatexto.util.ListaExecucoes
import br.com.fenix.processatexto.util.Utils
import com.jfoenix.controls.*
import com.nativejavafx.taskbar.TaskbarProgressbar
import com.nativejavafx.taskbar.TaskbarProgressbar.Type
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.concurrent.Task
import javafx.css.PseudoClass
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.input.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.robot.Robot
import javafx.stage.FileChooser
import br.com.fenix.processatexto.controller.mangas.MangasVincularCelulaSimplesController
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.sql.SQLException
import java.time.LocalDateTime
import java.util.*


class MangasVincularController : Initializable, VinculoListener, VinculoServiceListener {

    private val ON_DRAG_INICIADO: PseudoClass = PseudoClass.getPseudoClass("drag-iniciado")
    private val ON_DRAG_SELECIONADO: PseudoClass = PseudoClass.getPseudoClass("drag-selecionado")

    private val EXECUCOES = ListaExecucoes()

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var cbBase: JFXComboBox<String>

    @FXML
    private lateinit var txtMangaOriginal: JFXTextField

    @FXML
    private lateinit var txtMangaVinculado: JFXTextField

    @FXML
    private lateinit var txtArquivoOriginal: JFXTextField

    @FXML
    private lateinit var btnOriginal: JFXButton

    @FXML
    private lateinit var txtArquivoVinculado: JFXTextField

    @FXML
    private lateinit var btnVinculado: JFXButton

    @FXML
    private lateinit var btnLimpar: JFXButton

    @FXML
    private lateinit var btnDeletar: JFXButton

    @FXML
    lateinit var btnSalvar: JFXButton

    @FXML
    private lateinit var spnVolume: Spinner<Int>

    @FXML
    private lateinit var cbLinguagemOrigem: JFXComboBox<Language>

    @FXML
    private lateinit var cbLinguagemVinculado: JFXComboBox<Language>

    @FXML
    private lateinit var btnRecarregar: JFXButton

    @FXML
    lateinit var btnCarregarLegendas: JFXButton

    @FXML
    private lateinit var btnVisualizarLegendas: JFXButton

    @FXML
    private lateinit var btnOrderAutomatico: JFXButton

    @FXML
    private lateinit var btnOrderPaginaDupla: JFXButton

    @FXML
    private lateinit var btnOrderPaginaUnica: JFXButton

    @FXML
    private lateinit var btnOrderSequencia: JFXButton

    @FXML
    private lateinit var btnOrderPHash: JFXButton

    @FXML
    private lateinit var btnOrderHistogram: JFXButton

    @FXML
    private lateinit var btnOrderInteligente: JFXButton

    @FXML
    private lateinit var sldPrecisao: JFXSlider

    @FXML
    private lateinit var ckbPaginaDuplaCalculada: JFXCheckBox

    @FXML
    private lateinit var tvPaginasVinculadas: TableView<VinculoPagina>

    @FXML
    private lateinit var tcMangaOriginal: TableColumn<VinculoPagina, Int>

    @FXML
    private lateinit var tcMangaVinculado: TableColumn<VinculoPagina, Int>

    @FXML
    private lateinit var lvPaginasNaoVinculadas: ListView<VinculoPagina>

    @FXML
    private lateinit var apDragScroolUp: AnchorPane

    @FXML
    private lateinit var apDragScroolDown: AnchorPane

    @FXML
    private lateinit var lvCapitulosOriginal: ListView<String>

    @FXML
    private lateinit var lvCapitulosVinculado: ListView<String>

    private lateinit var autoCompleteMangaOriginal: JFXAutoCompletePopup<String>
    private lateinit var autoCompleteMangaVinculado: JFXAutoCompletePopup<String>
    private var automatico = false

    private var arquivoOriginal: File? = null
    private var arquivoVinculado: File? = null
    private var parseOriginal: Parse? = null
    private var parseVinculado: Parse? = null

    private val service: VincularServices = VincularServices()
    private var vinculo: Vinculo = Vinculo()

    private lateinit var vinculado: ObservableList<VinculoPagina>
    private lateinit var naoVinculado: ObservableList<VinculoPagina>
    private val capitulosOriginal: MutableMap<String, Int> = mutableMapOf()
    private val capitulosVinculado: MutableMap<String, Int> = mutableMapOf()

    var refreshListener: VinculoTextoListener? = null
    private lateinit var controller: MangasController
    var controllerPai: MangasController
        get() = controller
        set(controller) {
            this.controller = controller
        }

    fun getVinculo(): Vinculo = vinculo

    fun getTxtMangaOriginal(): JFXTextField = txtMangaOriginal

    fun getTxtMangaVinculado(): JFXTextField = txtMangaVinculado

    fun getCbLinguagemOrigem(): JFXComboBox<Language> = cbLinguagemOrigem

    fun getCbLinguagemVinculado(): JFXComboBox<Language> = cbLinguagemVinculado

    val listCapitulosOriginal: ObservableList<String> get() = lvCapitulosOriginal.items
    val listCapitulosVinculado: ObservableList<String> get() = lvCapitulosVinculado.items

    fun getCapitulosOriginal(): Map<String, Int> = capitulosOriginal

    fun getCapitulosVinculado(): Map<String, Int> = capitulosVinculado

    fun getAutoCompleteMangaOriginal(): JFXAutoCompletePopup<String> = autoCompleteMangaOriginal

    fun getAutoCompleteMangaVinculado(): JFXAutoCompletePopup<String> = autoCompleteMangaVinculado

    @FXML
    private fun onBtnCarregarLegendas() {
        EXECUCOES.addExecucao(object : ListaExecucoes.LambdaFunction {
            override fun call(abort: Boolean): Boolean {
                if (carregar()) {
                    vincularLegenda(true)
                    val texto = (""
                            + (if (vinculo.volumeOriginal != null) ("Original: " + vinculo.volumeOriginal!!.manga + " - V: " + vinculo.volumeOriginal!!.volume + " - L: "
                            + vinculo.volumeOriginal!!.lingua) + "|" else "")
                            + if (vinculo.volumeVinculado != null) (("Vinculado: " + vinculo.volumeVinculado!!.manga + " - V:"
                            + vinculo.volumeVinculado!!.volume) + " - L: "
                            + vinculo.volumeOriginal!!.lingua) else "")
                    Notificacoes.notificacao(Notificacao.SUCESSO, "Manga vinculado carregada com sucesso", texto)
                } else if (carregarLegendas()) {
                    vincularLegenda(false)
                    val texto = (""
                            + (if (vinculo.volumeOriginal != null) ("Original: " + vinculo.volumeOriginal!!.manga + " - V: " + vinculo.volumeOriginal!!.volume + " - L: "
                            + vinculo.volumeOriginal!!.lingua) + "|" else "")
                            + if (vinculo.volumeVinculado != null) (("Vinculado: " + vinculo.volumeVinculado!!.manga + " - V:"
                            + vinculo.volumeVinculado!!.volume) + " - L: " + vinculo.volumeOriginal!!.lingua) else "")
                    Notificacoes.notificacao(Notificacao.SUCESSO, "Legenda carregada com sucesso", texto)
                }
                return false
            }
        })
    }

    @FXML
    private fun onBtnVisualizarLegendas() = visualizarLegendas(0)

    @FXML
    private fun onBtnOriginal() {
        val pasta = if (arquivoOriginal != null)
            arquivoOriginal!!.path
        else if (arquivoVinculado != null)
            arquivoVinculado!!.path
        else null

        val arquivo = selecionaArquivo("Selecione o arquivo de origem", pasta)
        arquivoOriginal = arquivo
        if (!selecionarArquivo())
            carregarArquivo(arquivo, true)
        else
            carregaDados(arquivo, true)
    }

    @FXML
    private fun onBtnVinculado() {
        if (arquivoOriginal == null) {
            AlertasPopup.AvisoModal("Selecione o arquivo original", "Necessário informar o arquivo original primeiro.")
            return
        }
        val pasta = if (arquivoVinculado != null)
            arquivoVinculado!!.path
        else if (arquivoOriginal != null)
            arquivoOriginal!!.path
        else
            null

        val arquivo: File = selecionaArquivo("Selecione o arquivo vinculado", pasta)
        arquivoVinculado = arquivo
        if (!selecionarArquivo())
            carregarArquivo(arquivo, false)
        else
            carregaDados(arquivo, false)
    }

    @FXML
    private fun onBtnLimpar() {
        EXECUCOES.addExecucao(object : ListaExecucoes.LambdaFunction {
            override fun call(abort: Boolean): Boolean {
                limpar()
                return false
            }
        })
    }

    @FXML
    private fun onBtnDeletar() {
        EXECUCOES.addExecucao(object : ListaExecucoes.LambdaFunction {
            override fun call(abort: Boolean): Boolean {
                if (cbBase.selectionModel.selectedItem == null || vinculo == null)
                    return false

                try {
                    service.delete(cbBase.selectionModel.selectedItem, vinculo)
                    limpar()
                    Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Arquivo deletado com sucesso.")
                } catch (e: SQLException) {
                    LOGGER.error(e.message, e)
                    AlertasPopup.ErroModal("Erro ao deletar", e.message!!)
                }
                return false
            }
        })
    }

    @FXML
    private fun onBtnSalvar() {
        EXECUCOES.addExecucao(object : ListaExecucoes.LambdaFunction {
            override fun call(abort: Boolean): Boolean {
                if (valida())
                    salvar()
                return false
            }
        })
    }

    @FXML
    private fun onBtnRecarregar() {
        EXECUCOES.addExecucao(object : ListaExecucoes.LambdaFunction {
            override fun call(abort: Boolean): Boolean {
                if (recarregar()) {
                    vincularLegenda(true)
                    refreshTabelas(Tabela.ALL)
                    Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Recarregar.")
                } else
                    Notificacoes.notificacao(Notificacao.ALERTA, "Erro", "Não foi possivel recarregar.")

                return false
            }
        })
    }

    @FXML
    private fun onBtnOrderAutomatico() {
        service.autoReordenarPaginaDupla(false)
        refreshTabelas(Tabela.ALL)
        Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação automatica.")
    }

    @FXML
    private fun onBtnOrderPaginaDupla() {
        service.ordenarPaginaDupla(ckbPaginaDuplaCalculada.isSelected)
        refreshTabelas(Tabela.ALL)
        Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação página dupla.")
    }

    @FXML
    private fun onBtnOrderPaginaUnica() {
        service.ordenarPaginaSimples()
        refreshTabelas(Tabela.ALL)
        Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação página simples.")
    }

    @FXML
    private fun onBtnOrderSequencia() {
        service.reordenarPeloNumeroPagina()
        refreshTabelas(Tabela.ALL)
        Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação pela sequencia.")
    }

    @FXML
    private fun onBtnOrderPHash() {
        service.autoReordenarPHash(sldPrecisao.value)
        refreshTabelas(Tabela.ALL)
        Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação PHash.")
    }

    @FXML
    private fun onBtnOrderHistogram() {
        service.autoReordenarHistogram(sldPrecisao.value)
        refreshTabelas(Tabela.ALL)
        Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação Histogram.")
    }

    @FXML
    private fun onBtnOrderInteligente() {
        service.autoReordenarInteligente(sldPrecisao.value)
        refreshTabelas(Tabela.ALL)
        Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Ordenação Inteligente.")
    }

    private var lastTime: Long = System.currentTimeMillis()

    @FXML
    private fun onDragScroolUp(event: DragEvent) {
        if (System.currentTimeMillis() - lastTime > SLOW) {
            lastTime = System.currentTimeMillis()
            val index = Utils.getFirstVisibleIndex(tvPaginasVinculadas)
            if (index != null && index > 0)
                tvPaginasVinculadas.scrollTo(index - 1)
        }
    }

    @FXML
    private fun onDragScroolUpFast(event: DragEvent) {
        if (System.currentTimeMillis() - lastTime > FAST) {
            lastTime = System.currentTimeMillis()
            val index = Utils.getFirstVisibleIndex(tvPaginasVinculadas)
            if (index != null && index > 0)
                tvPaginasVinculadas.scrollTo(index - 1)
        }
    }

    @FXML
    private fun onDragScroolDown(event: DragEvent) {
        if (System.currentTimeMillis() - lastTime > SLOW) {
            lastTime = System.currentTimeMillis()
            val index = Utils.getFirstVisibleIndex(tvPaginasVinculadas)
            if (index != null && index - 1 < tvPaginasVinculadas.items.size)
                tvPaginasVinculadas.scrollTo(index + 1)
        }
    }

    @FXML
    private fun onDragScroolDownFast(event: DragEvent) {
        if (System.currentTimeMillis() - lastTime > FAST) {
            lastTime = System.currentTimeMillis()
            val index = Utils.getFirstVisibleIndex(tvPaginasVinculadas)
            if (index != null && index - 1 < tvPaginasVinculadas.items.size)
                tvPaginasVinculadas.scrollTo(index + 1)
        }
    }

    val root: AnchorPane
        get() = apRoot

    @Override
    override fun onDuploClique(root: Node, vinculo: VinculoPagina, origem: Pagina): Boolean {
        var item: VinculoPagina? = null
        if (origem === Pagina.VINCULADO_DIREITA)
            item = getVinculoOriginal(origem, vinculo)
        else if (origem === Pagina.VINCULADO_ESQUERDA)
            item = getVinculoOriginal(origem, vinculo)

        if (item != null)
            visualizarLegendas(vinculado.indexOf(item))
        else
            visualizarLegendas(0)

        return true
    }

    @Override
    override fun onDrop(origem: Pagina, vinculoOrigem: VinculoPagina, destino: Pagina, vinculoDestino: VinculoPagina) {
        val itemOrigem: VinculoPagina? = getVinculoOriginal(origem, vinculoOrigem)
        val itemDestino: VinculoPagina? = getVinculoOriginal(destino, vinculoDestino)

        if (origem === Pagina.VINCULADO_DIREITA || destino === Pagina.VINCULADO_DIREITA)
            service.onMovimentaDireita(origem, itemOrigem, destino, itemDestino)
        else if (origem === Pagina.VINCULADO_ESQUERDA)
            service.onMovimentaEsquerda(itemOrigem, itemDestino)
        else if (origem === Pagina.NAO_VINCULADO)
            service.fromNaoVinculado(itemOrigem, itemDestino, destino)

        refreshTabelas(Tabela.ALL)
    }

    @Override
    override fun onDragStart() {
        apDragScroolUp.isVisible = true
        apDragScroolDown.isVisible = true
        lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_INICIADO, true)
    }

    @Override
    override fun onDragEnd() {
        apDragScroolUp.isVisible = false
        apDragScroolDown.isVisible = false
        lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false)
        lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_INICIADO, false)
    }

    @get:Override
    override val vinculados: ObservableList<VinculoPagina>
        get() = vinculado

    @get:Override
    override val naoVinculados: ObservableList<VinculoPagina>
        get() = naoVinculado

    private fun refreshTabelas(tabela: Tabela) {
        when (tabela) {
            Tabela.VINCULADOS -> {
                tvPaginasVinculadas.refresh()
                tvPaginasVinculadas.requestLayout()
                if (refreshListener != null)
                    refreshListener!!.refresh()
            }
            Tabela.NAOVINCULADOS -> {
                lvPaginasNaoVinculadas.refresh()
                lvPaginasNaoVinculadas.requestLayout()
            }
            else -> {
                tvPaginasVinculadas.refresh()
                lvPaginasNaoVinculadas.refresh()
                tvPaginasVinculadas.requestLayout()
                lvPaginasNaoVinculadas.requestLayout()
                if (refreshListener != null)
                    refreshListener!!.refresh()
            }
        }
    }

    private fun desabilita() {
        btnOrderPaginaUnica.isDisable = true
        btnOrderPaginaDupla.isDisable = true
        btnOrderSequencia.isDisable = true
        btnOrderAutomatico.isDisable = true
        btnRecarregar.isDisable = true
        btnOrderPHash.isDisable = true
        btnOrderHistogram.isDisable = true
        btnOrderInteligente.isDisable = true
    }

    private fun habilita() {
        btnOrderPaginaUnica.isDisable = false
        btnOrderPaginaDupla.isDisable = false
        btnOrderSequencia.isDisable = false
        btnOrderAutomatico.isDisable = false
        btnRecarregar.isDisable = false
        btnOrderPHash.isDisable = false
        btnOrderHistogram.isDisable = false
        btnOrderInteligente.isDisable = false
    }

    private fun limpar() {
        EXECUCOES.abortProcess()
        cbLinguagemVinculado.selectionModel.select(Language.PORTUGUESE)
        cbLinguagemOrigem.selectionModel.select(Language.JAPANESE)
        txtArquivoOriginal.text = ""
        txtArquivoVinculado.text = ""
        ckbPaginaDuplaCalculada.selectedProperty().set(true)
        spnVolume.valueFactory.value = 1

        try {
            automatico = true
            txtMangaOriginal.text = ""
            txtMangaVinculado.text = ""
        } finally {
            automatico = false
        }
        setLista(mutableListOf(), mutableListOf())
        Utils.destroiParse(parseOriginal)
        Utils.destroiParse(parseVinculado)
        arquivoOriginal = null
        arquivoVinculado = null
        parseOriginal = null
        parseVinculado = null
    }

    private fun setLista(vinculado: List<VinculoPagina>, naoVinculado: List<VinculoPagina>) {
        this.vinculado = FXCollections.observableArrayList(vinculado)
        this.naoVinculado = FXCollections.observableArrayList(naoVinculado)
        tvPaginasVinculadas.items = this.vinculado
        lvPaginasNaoVinculadas.items = this.naoVinculado
        refreshTabelas(Tabela.ALL)
    }

    private fun visualizarLegendas(index: Int) {
        val loader = FXMLLoader(MangasTextoController.fxmlLocate)
        try {
            val newRoot: AnchorPane = loader.load()
            val controlador: MangasTextoController = (loader.getController() as MangasTextoController)
            controlador.setDados(vinculado, this)
            controlador.controllerPai = this
            controlador.scroolTo(index)
            refreshListener = controlador
            Animacao().abrirPane(controller.stackPane, newRoot)
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
        }
    }

    private fun validaLegendaOriginal(): Boolean {
        return cbBase.selectionModel.selectedItem != null && cbBase.selectionModel.selectedItem.isNotEmpty() &&
                txtMangaOriginal.text.isNotEmpty() && cbLinguagemOrigem.selectionModel.selectedItem != null
    }

    private fun validaLegendaVinculado(): Boolean {
        return cbBase.selectionModel.selectedItem != null && cbBase.selectionModel.selectedItem.isNotEmpty() &&
                txtMangaVinculado.text.isNotEmpty() && cbLinguagemVinculado.selectionModel.selectedItem != null
    }

    private fun carregarLegendas(): Boolean {
        if (!validaLegendaOriginal() && !validaLegendaVinculado()) {
            AlertasPopup.AvisoModal("Aviso", "Necessário selecionar a base e o manga.")
            return false
        }

        val volumeOriginal = service.selectVolume(
            cbBase.selectionModel.selectedItem,
            txtMangaOriginal.text, spnVolume.value,
            cbLinguagemOrigem.selectionModel.selectedItem
        ).orElse(null)

        val volumeVinculado = service.selectVolume(
            cbBase.selectionModel.selectedItem,
            txtMangaVinculado.text, spnVolume.value,
            cbLinguagemVinculado.selectionModel.selectedItem
        ).orElse(null)

        vinculo.volumeOriginal = volumeOriginal
        vinculo.volumeVinculado = volumeVinculado
        if (volumeOriginal == null && volumeVinculado == null)
            AlertasPopup.AvisoModal("Aviso", "Não encontrado nenhum item com as informações repassadas.")
        else if (volumeOriginal == null)
            AlertasPopup.AvisoModal("Aviso", "Manga original não encontrado.")
        else if (volumeVinculado == null)
            AlertasPopup.AvisoModal("Aviso", "Manga vinculado não encontrado.")

        return volumeOriginal != null || volumeVinculado != null
    }

    private var itensCapas: Int = 0
    private fun vincularLegenda(isCarregado: Boolean) {
        if (vinculado.isEmpty())
            return

        EXECUCOES.addExecucao(object : ListaExecucoes.LambdaFunction {
            override fun call(abort: Boolean): Boolean {
                desabilita()
                val progress = MenuPrincipalController.controller.criaBarraProgresso()
                progress!!.titulo.text = "Vinculando legendas"

                if (TaskbarProgressbar.isSupported())
                    TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())

                val vincular: Task<Void> = object : Task<Void>() {
                    var I: Long = 0
                    var Max: Long = 0
                    var error: String? = ""

                    @Throws(java.lang.Exception::class)
                    override fun call(): Void? {
                        try {
                            I = 0
                            Max = 1
                            error = ""
                            if (!isCarregado) {
                                updateMessage("Vinculando mangas....")
                                if (vinculo.volumeOriginal != null && vinculo.volumeVinculado != null)
                                    Max = vinculado.size.toLong() * 2 + naoVinculado.size
                                else if (vinculo.volumeOriginal != null)
                                    Max = vinculado.size.toLong()
                                else if (vinculo.volumeOriginal != null)
                                    Max = vinculado.size.toLong().plus(naoVinculado.size)

                                if (vinculo.volumeOriginal != null) {
                                    updateMessage("Vinculando mangas original....")
                                    val encontrados: MutableList<MangaPagina> = mutableListOf()
                                    val paginasOriginal: MutableList<MangaPagina> = mutableListOf()
                                    vinculo.volumeOriginal!!.capitulos.stream().forEach { cp ->
                                        cp.paginas.stream().forEach { pg -> pg.capitulo = cp.capitulo }
                                        paginasOriginal.addAll(cp.paginas)
                                    }
                                    itensCapas = java.lang.Long.valueOf(
                                        vinculado.stream()
                                            .filter { it.originalPathPagina.lowercase(Locale.getDefault()).contains("capa") }
                                            .count()
                                    ).toInt()
                                    vinculado.parallelStream().forEach { vi: VinculoPagina ->
                                        if (abort)
                                            return@forEach

                                        vi.mangaPaginaOriginal = service.findPagina(
                                            paginasOriginal,
                                            encontrados,
                                            vi.originalPathPagina,
                                            vi.originalNomePagina,
                                            vi.originalPagina - itensCapas,
                                            vi.originalHash
                                        )

                                        I += 1
                                        updateProgress(I.toLong(), Max.toLong())
                                        Platform.runLater {
                                            if (TaskbarProgressbar.isSupported())
                                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), Max.toLong(), Type.NORMAL)
                                        }
                                    }
                                }

                                if (abort)
                                    return null

                                if (vinculo.volumeVinculado != null) {
                                    updateMessage("Vinculando mangas vinculado....")
                                    val encontrados: MutableList<MangaPagina> = mutableListOf()
                                    val paginasOriginal: MutableList<MangaPagina> = mutableListOf()
                                    vinculo.volumeVinculado!!.capitulos.stream().forEach { cp ->
                                        cp.paginas.stream().forEach { pg -> pg.capitulo = cp.capitulo }
                                        paginasOriginal.addAll(cp.paginas)
                                    }
                                    itensCapas = java.lang.Long.valueOf(
                                        vinculado.stream()
                                            .filter {
                                                it.vinculadoEsquerdaPathPagina.lowercase(Locale.getDefault())
                                                    .contains("capa") || it.vinculadoDireitaPathPagina.lowercase(Locale.getDefault())
                                                    .contains("capa")
                                            }
                                            .count() + naoVinculado.stream().filter {
                                            it.vinculadoEsquerdaPathPagina.lowercase(Locale.getDefault()).contains("capa")
                                        }.count()
                                    ).toInt()

                                    vinculado.parallelStream().forEach { vi: VinculoPagina ->
                                        if (abort)
                                            return@forEach
                                        if (vi.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA)
                                            vi.mangaPaginaEsquerda = service.findPagina(
                                                paginasOriginal, encontrados,
                                                vi.vinculadoEsquerdaPathPagina,
                                                vi.vinculadoEsquerdaNomePagina,
                                                vi.vinculadoEsquerdaPagina - itensCapas,
                                                vi.vinculadoEsquerdaHash
                                            )

                                        if (vi.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA)
                                            vi.mangaPaginaDireita = service.findPagina(
                                                paginasOriginal, encontrados,
                                                vi.vinculadoDireitaPathPagina, vi.vinculadoDireitaNomePagina,
                                                vi.vinculadoDireitaPagina - itensCapas,
                                                vi.vinculadoDireitaHash
                                            )

                                        I += 1
                                        updateProgress(I.toLong(), Max.toLong())
                                        Platform.runLater {
                                            if (TaskbarProgressbar.isSupported())
                                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), Max.toLong(), Type.NORMAL)
                                        }
                                    }
                                    if (abort)
                                        return null

                                    naoVinculado.parallelStream().forEach { vi: VinculoPagina ->
                                        if (abort)
                                            return@forEach

                                        if (vi.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA)
                                            vi.mangaPaginaEsquerda = service.findPagina(
                                                paginasOriginal, encontrados,
                                                vi.vinculadoEsquerdaPathPagina,
                                                vi.vinculadoEsquerdaNomePagina,
                                                vi.vinculadoEsquerdaPagina - itensCapas,
                                                vi.vinculadoEsquerdaHash
                                            )

                                        I += 1
                                        updateProgress(I.toLong(), Max.toLong())
                                        Platform.runLater {
                                            if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showCustomProgress(
                                                Run.getPrimaryStage(), I.toLong(), Max.toLong(),
                                                Type.NORMAL
                                            )
                                        }
                                    }
                                    if (abort)
                                        return null
                                }
                                I += 1
                                updateProgress(I.toLong(), Max.toLong())
                                Platform.runLater {
                                    if (TaskbarProgressbar.isSupported())
                                        TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), Max.toLong(), Type.NORMAL)
                                }
                            } else {
                                val vinculo = vinculo
                                val vinculado = vinculado
                                val naoVinculado = naoVinculado
                                updateMessage("Carregando vinculo salvo....")
                                I = 0
                                Max = vinculo.vinculados.size.toLong().plus(vinculo.naoVinculados.size)
                                updateProgress(I, Max)
                                Platform.runLater {
                                    if (TaskbarProgressbar.isSupported())
                                        TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I.toLong(), Max.toLong(), Type.NORMAL)
                                }
                                for (pagina: VinculoPagina in vinculo.vinculados) {
                                    if (abort)
                                        return null

                                    I += 1
                                    pagina.addOriginal(vinculado[vinculo.vinculados.indexOf(pagina)])
                                    if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA) {
                                        val paginaCarregada = service.findPagina(vinculado, naoVinculado, pagina.vinculadoEsquerdaPagina)

                                        if (paginaCarregada == null)
                                            pagina.limparVinculadoEsquerda(false)
                                        else if (paginaCarregada.vinculadoEsquerdaPagina.compareTo(pagina.vinculadoEsquerdaPagina) === 0)
                                            pagina.addVinculoEsquerda(paginaCarregada)
                                        else
                                            pagina.addVinculoEsquerdaApartirDireita(paginaCarregada)
                                    }
                                    if (pagina.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA) {
                                        val paginaCarregada = service.findPagina(vinculado, naoVinculado, pagina.vinculadoDireitaPagina)
                                        if (paginaCarregada == null)
                                            pagina.limparVinculadoDireita()
                                        else if (paginaCarregada.vinculadoEsquerdaPagina.compareTo(pagina.vinculadoEsquerdaPagina) === 0)
                                            pagina.addVinculoDireitaApartirEsquerda(paginaCarregada)
                                        else
                                            pagina.addVinculoDireita(paginaCarregada)
                                    }
                                    updateProgress(I.toLong(), Max.toLong())
                                    Platform.runLater {
                                        if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showCustomProgress(
                                            Run.getPrimaryStage(), I.toLong(), Max.toLong(),
                                            Type.NORMAL
                                        )
                                    }
                                }
                                for (pagina: VinculoPagina in vinculo.naoVinculados) {
                                    if (abort)
                                        return null

                                    I += 1
                                    if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA) {
                                        val paginaCarregada = service.findPagina(
                                            vinculado, naoVinculado,
                                            pagina.vinculadoEsquerdaPagina
                                        )
                                        if (paginaCarregada == null) pagina.limparVinculadoEsquerda(false) else if (paginaCarregada.vinculadoEsquerdaPagina
                                                .compareTo(pagina.vinculadoEsquerdaPagina) === 0
                                        ) pagina.addVinculoEsquerda(paginaCarregada) else pagina.addVinculoEsquerdaApartirDireita(paginaCarregada)
                                    }
                                    updateProgress(I.toLong(), Max.toLong())
                                    Platform.runLater {
                                        if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showCustomProgress(
                                            Run.getPrimaryStage(), I.toLong(), Max.toLong(),
                                            Type.NORMAL
                                        )
                                    }
                                }
                                vinculo.naoVinculados.removeIf { it.vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA }
                                Platform.runLater { setLista(vinculo.vinculados, vinculo.naoVinculados) }
                            }
                        } catch (e: java.lang.Exception) {
                            LOGGER.error(e.message, e)
                            error = e.message
                        }
                        return null
                    }

                    override fun succeeded() {
                        Platform.runLater {
                            progress.barraProgresso.progressProperty().unbind()
                            progress.log.textProperty().unbind()
                            MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                            TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                            if (error!!.isNotEmpty())
                                AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", (error)!!)
                            habilita()
                            refreshTabelas(Tabela.VINCULADOS)
                            EXECUCOES.endProcess()
                        }
                    }

                    override fun failed() {
                        super.failed()
                        LOGGER.warn("Falha ao executar a thread de vincular a legenda: " + super.getMessage())
                        Platform.runLater {
                            progress.barraProgresso.progressProperty().unbind()
                            progress.log.textProperty().unbind()
                            MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                            TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                            MenuPrincipalController.controller.getLblLog().text = ""
                            habilita()
                            refreshTabelas(Tabela.VINCULADOS)
                            EXECUCOES.endProcess()
                        }
                    }
                }
                progress.log.textProperty().bind(vincular.messageProperty())
                progress.barraProgresso.progressProperty().bind(vincular.progressProperty())
                val t = Thread(vincular)
                t.start()
                return true
            }
        })
    }

    private fun selecionarArquivo(): Boolean {
        if (cbLinguagemOrigem.selectionModel.selectedItem == null && arquivoOriginal == null && cbLinguagemVinculado.selectionModel
                .selectedItem == null && arquivoVinculado == null
        )
            return false

        if (carregar()) {
            val vinculo: Vinculo = vinculo
            txtArquivoOriginal.text = vinculo.nomeArquivoOriginal
            txtArquivoVinculado.text = vinculo.nomeArquivoVinculado
            if (cbLinguagemOrigem.selectionModel.selectedItem == null)
                cbLinguagemOrigem.selectionModel.select(vinculo.linguagemOriginal)
            else if (vinculo.linguagemOriginal != null && cbLinguagemOrigem.selectionModel.selectedItem.compareTo(vinculo.linguagemOriginal) !== 0) {
                if (AlertasPopup.ConfirmacaoModal("Aviso", "A linguagem selecionada e o manga original são diferentes.\nDeseja recarregar?")) {
                    cbLinguagemOrigem.selectionModel.select(vinculo.linguagemOriginal)
                    return selecionarArquivo()
                }
            }

            if (cbLinguagemVinculado.selectionModel.selectedItem == null)
                cbLinguagemVinculado.selectionModel.select(vinculo.linguagemVinculado)
            else if (vinculo.linguagemVinculado != null && cbLinguagemVinculado.selectionModel.selectedItem.compareTo(vinculo.linguagemVinculado) !== 0) {
                if (AlertasPopup.ConfirmacaoModal("Aviso", "A linguagem selecionada e o manga vinculado são diferentes.\nDeseja recarregar?")) {
                    cbLinguagemVinculado.selectionModel.select(vinculo.linguagemVinculado)
                    return selecionarArquivo()
                }
            }

            try {
                automatico = true
                if (vinculo.volumeOriginal != null)
                    txtMangaOriginal.text = vinculo.volumeOriginal!!.manga

                if (vinculo.volumeVinculado != null)
                    txtMangaVinculado.text = vinculo.volumeVinculado!!.manga
            } finally {
                automatico = false
            }
            return true
        }
        return false
    }

    private fun getMangaVolume(isOriginal: Boolean): MangaVolume? {
        var manga: MangaVolume?
        if (isOriginal) {
            manga = vinculo.volumeOriginal
            if (manga == null && validaLegendaOriginal()) {
                manga = service.selectVolume(
                    cbBase.selectionModel.selectedItem, txtMangaOriginal.text,
                    spnVolume.value, cbLinguagemOrigem.selectionModel.selectedItem
                ).orElse(null)
                vinculo.volumeOriginal = manga
            }
        } else {
            manga = vinculo.volumeVinculado
            if (manga == null && validaLegendaVinculado()) {
                manga = service.selectVolume(
                    cbBase.selectionModel.selectedItem, txtMangaVinculado.text,
                    spnVolume.value, cbLinguagemVinculado.selectionModel.selectedItem
                ).orElse(null)
                vinculo.volumeVinculado = manga
            }
        }
        return manga
    }

    private fun carregaImagem(parse: Parse, pagina: Int): Pair<Image?, Atributos> {
        var image: Image? = null
        var imput: InputStream? = null
        var dupla: Boolean? = false
        var md5 = ""
        try {
            md5 = Utils.MD5(parse.getPagina(pagina))
            imput = parse.getPagina(pagina)
            image = Image(imput)
            dupla = image.width / image.height > 0.9
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }
        return Pair(image, Atributos(dupla!!, md5, ""))
    }

    private fun carregarArquivo(arquivo: File?, isManga: Boolean) {
        if (arquivo == null)
            return

        val parse: Parse = Utils.criaParse(arquivo)
        if (parse != null) {
            EXECUCOES.addExecucao(object : ListaExecucoes.LambdaFunction {
                override fun call(abort: Boolean): Boolean {
                    desabilita()
                    val progress = MenuPrincipalController.controller.criaBarraProgresso()
                    progress!!.titulo.text = "Vinculando legendas"
                    if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())
                    val carregar: Task<Void> = object : Task<Void>() {
                        var error = ""
                        var X: Long = 0
                        var SIZE: Long = 0

                        @Override
                        @Throws(Exception::class)
                        override fun call(): Void? {
                            try {
                                error = ""
                                updateMessage("Carregando manga....")
                                if (isManga) {
                                    Platform.runLater {
                                        txtArquivoOriginal.text = arquivo.name
                                        txtArquivoVinculado.text = ""
                                    }
                                    val volume: MangaVolume? = getMangaVolume(true)
                                    updateMessage("Carregando manga original....")
                                    val encontrados: MutableList<MangaPagina> = mutableListOf()
                                    val paginas: MutableList<MangaPagina> = mutableListOf()

                                    volume?.capitulos?.stream()?.forEach { paginas.addAll(it.paginas) }

                                    itensCapas = parse.getPastas().keys.stream().filter { k -> k.contains("capa") }.count().toInt()
                                    val list: MutableList<VinculoPagina> = mutableListOf()
                                    for (x in 0 until parse.getSize()) {
                                        val image = carregaImagem(parse, x)
                                        val detalhe: Atributos = image.second
                                        val path: String = parse.getPaginaPasta(x)
                                        list.add(
                                            VinculoPagina(
                                                Utils.getNome(path), Utils.getPasta(path), x,
                                                parse.getSize(), detalhe.dupla,
                                                service.findPagina(
                                                    paginas, encontrados, path, Utils.getPasta(path),
                                                    x - itensCapas, detalhe.md5
                                                ),
                                                image.first, detalhe.md5, "", floatArrayOf()
                                            )
                                        )
                                        X = x.toLong()
                                        SIZE = parse.getSize().toLong()
                                        updateProgress(X, SIZE)
                                        Platform.runLater {
                                            if (TaskbarProgressbar.isSupported())
                                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X, SIZE, Type.NORMAL)
                                        }
                                        if (abort)
                                            break
                                    }
                                    if (!abort) {
                                        Utils.destroiParse(parseOriginal)
                                        Utils.destroiParse(parseVinculado)
                                        parseOriginal = parse
                                        parseVinculado = null
                                        Utils.getCapitulos(parseOriginal!!, capitulosOriginal, lvCapitulosOriginal)
                                        Utils.getCapitulos(parseVinculado!!, capitulosVinculado, lvCapitulosVinculado)
                                        Platform.runLater { setLista(list, mutableListOf()) }
                                    } else limpar()
                                } else {
                                    Platform.runLater { txtArquivoVinculado.text = arquivo.name }
                                    updateMessage("Carregando manga vinculado....")
                                    val encontrados: MutableList<MangaPagina> = mutableListOf()
                                    val paginas: MutableList<MangaPagina> = mutableListOf()
                                    val volume: MangaVolume? = getMangaVolume(false)
                                    volume?.capitulos?.stream()?.forEach { paginas.addAll(it.paginas) }

                                    itensCapas = parse.getPastas().keys.stream().filter { k -> k.contains("capa") }.count().toInt()
                                    val vinculado: MutableList<VinculoPagina> = vinculado.toMutableList()
                                    val naoVinculado: MutableList<VinculoPagina> = mutableListOf()
                                    for (x in 0 until parse.getSize()) {
                                        val image = carregaImagem(parse, x)
                                        val detalhe: Atributos = image.second
                                        val path: String = parse.getPaginaPasta(x)
                                        if (x < vinculado.size) {
                                            val item: VinculoPagina = vinculado[x]
                                            item.limparVinculado()
                                            item.vinculadoEsquerdaPagina = x
                                            item.vinculadoEsquerdaNomePagina = Utils.getNome(path)
                                            item.vinculadoEsquerdaPathPagina = Utils.getPasta(path)
                                            item.vinculadoEsquerdaPaginas = parse.getSize()
                                            item.isVinculadoEsquerdaPaginaDupla = detalhe.dupla
                                            item.imagemVinculadoEsquerda = image.first
                                            item.vinculadoEsquerdaHash = detalhe.md5
                                            item.mangaPaginaEsquerda = service.findPagina(paginas, encontrados, path, Utils.getPasta(path), x - itensCapas, detalhe.md5)
                                        } else
                                            naoVinculado.add(
                                                VinculoPagina(
                                                    Utils.getNome(path), Utils.getPasta(path), x,
                                                    parse.getSize(), detalhe.dupla,
                                                    service.findPagina(paginas, encontrados, path, Utils.getPasta(path), x - itensCapas, detalhe.md5),
                                                    image.first, true, detalhe.md5, "", floatArrayOf()
                                                )
                                            )
                                        X = x.toLong()
                                        SIZE = parse.getSize().toLong()
                                        updateProgress(X, SIZE)
                                        Platform.runLater {
                                            if (TaskbarProgressbar.isSupported())
                                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X, SIZE, Type.NORMAL)
                                        }
                                        if (abort)
                                            break
                                    }

                                    if (!abort) {
                                        Utils.destroiParse(parseVinculado)
                                        parseVinculado = parse
                                        Platform.runLater {
                                            setLista(vinculado, naoVinculado)
                                            Utils.getCapitulos(
                                                parseVinculado!!, capitulosVinculado,
                                                lvCapitulosVinculado
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                LOGGER.error(e.message, e)
                                error = e.message!!
                            }
                            return null
                        }

                        @Override
                        override fun succeeded() {
                            Platform.runLater {
                                service.gerarAtributos(parse, isManga)
                                progress.barraProgresso.progressProperty().unbind()
                                progress.log.textProperty().unbind()
                                MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                                TaskbarProgressbar.stopProgress(Run.getPrimaryStage())

                                if (error.isNotEmpty())
                                    AlertasPopup.ErroModal(controller.stackPane, controller.root, mutableListOf(), "Erro", error)

                                MenuPrincipalController.controller.getLblLog().text = ""
                                habilita()
                                refreshTabelas(Tabela.VINCULADOS)
                                EXECUCOES.endProcess()
                            }
                        }

                        @Override
                        override fun failed() {
                            super.failed()
                            LOGGER.warn("Falha ao executar a thread de carregar arquivos: " + super.getMessage())
                            Platform.runLater {
                                progress.barraProgresso.progressProperty().unbind()
                                progress.log.textProperty().unbind()
                                MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                                TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                                MenuPrincipalController.controller.getLblLog().text = ""
                                habilita()
                                refreshTabelas(Tabela.VINCULADOS)
                                EXECUCOES.endProcess()
                            }
                        }
                    }
                    progress.log.textProperty().bind(carregar.messageProperty())
                    progress.barraProgresso.progressProperty().bind(carregar.progressProperty())
                    val t = Thread(carregar)
                    t.start()
                    return true
                }
            })
        }
    }

    private fun carregaDados(arquivo: File?, isManga: Boolean) {
        EXECUCOES.addExecucao(object : ListaExecucoes.LambdaFunction {
            override fun call(abort: Boolean): Boolean {
                desabilita()
                val progress = MenuPrincipalController.controller.criaBarraProgresso()
                progress!!.titulo.text = "Carregando dados."
                if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage())
                val carregar: Task<Void> = object : Task<Void>() {
                    var X: Long = 0
                    var SIZE: Long = 0

                    @Override
                    @Throws(Exception::class)
                    override fun call(): Void? {
                        updateMessage("Carregando dados....")
                        if (isManga) {
                            Utils.destroiParse(parseOriginal)
                            parseOriginal = Utils.criaParse(arquivo!!)
                            Utils.getCapitulos(parseOriginal!!, capitulosOriginal, lvCapitulosOriginal)
                        } else {
                            Utils.destroiParse(parseVinculado)
                            parseVinculado = Utils.criaParse(arquivo!!)
                            Utils.getCapitulos(parseVinculado!!, capitulosVinculado, lvCapitulosVinculado)
                        }
                        val original: MutableList<VinculoPagina> = vinculado
                        val vinculado: MutableList<VinculoPagina> = vinculo.vinculados
                        val naoVinculado: MutableList<VinculoPagina> = vinculo.naoVinculados
                        SIZE = vinculado.size.toLong().plus(naoVinculado.size)
                        for (pagina in vinculado) {
                            if (isManga) {
                                val image = carregaImagem(parseOriginal!!, pagina.originalPagina)
                                pagina.isOriginalPaginaDupla = image.second.dupla
                                pagina.imagemOriginal = image.first
                                pagina.originalHash = image.second.md5
                            } else {
                                pagina.addOriginalSemId(original[vinculado.indexOf(pagina)])
                                if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA) {
                                    val image = carregaImagem(parseVinculado!!, pagina.vinculadoEsquerdaPagina)
                                    pagina.isVinculadoEsquerdaPaginaDupla = image.second.dupla
                                    pagina.imagemVinculadoEsquerda = image.first
                                    pagina.vinculadoEsquerdaHash = image.second.md5
                                }
                                if (pagina.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA) {
                                    val image = carregaImagem(parseVinculado!!, pagina.vinculadoDireitaPagina)
                                    pagina.isVinculadoDireitaPaginaDupla = image.second.dupla
                                    pagina.imagemVinculadoDireita = image.first
                                    pagina.vinculadoDireitaHash = image.second.md5
                                }
                            }
                            X++
                            updateProgress(X, SIZE)
                            Platform.runLater { if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X, SIZE, Type.NORMAL) }
                        }
                        if (!isManga) {
                            for (pagina in naoVinculado) {
                                if (pagina.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA) {
                                    val image = carregaImagem(parseVinculado!!, pagina.vinculadoEsquerdaPagina)
                                    pagina.isVinculadoEsquerdaPaginaDupla = image.second.dupla
                                    pagina.imagemVinculadoEsquerda = image.first
                                    pagina.vinculadoEsquerdaHash = image.second.md5
                                }
                                X++
                                updateProgress(X, SIZE)
                                Platform.runLater { if (TaskbarProgressbar.isSupported()) TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), X, SIZE, Type.NORMAL) }
                            }
                        }
                        Platform.runLater { setLista(vinculado, naoVinculado) }
                        return null
                    }

                    @Override
                    override fun succeeded() {
                        Platform.runLater {
                            progress.barraProgresso.progressProperty().unbind()
                            progress.log.textProperty().unbind()
                            MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                            TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                            MenuPrincipalController.controller.getLblLog().text = ""
                            habilita()
                            refreshTabelas(Tabela.VINCULADOS)
                            EXECUCOES.endProcess()
                        }
                    }

                    @Override
                    override fun failed() {
                        super.failed()
                        LOGGER.warn("Falha ao executar a thread de carregamento de dados: " + super.getMessage())
                        Platform.runLater {
                            progress.barraProgresso.progressProperty().unbind()
                            progress.log.textProperty().unbind()
                            MenuPrincipalController.controller.destroiBarraProgresso(progress, "")
                            TaskbarProgressbar.stopProgress(Run.getPrimaryStage())
                            MenuPrincipalController.controller.getLblLog().text = ""
                            habilita()
                            refreshTabelas(Tabela.VINCULADOS)
                            EXECUCOES.endProcess()
                        }
                    }
                }
                progress.log.textProperty().bind(carregar.messageProperty())
                progress.barraProgresso.progressProperty().bind(carregar.progressProperty())
                val t = Thread(carregar)
                t.start()
                return true
            }
        })
    }

    private fun limpaVinculo(vinculo: Vinculo): Vinculo {
        vinculo.setId(null)
        vinculo.volumeVinculado = null
        vinculo.linguagemVinculado = Language.PORTUGUESE
        vinculo.nomeArquivoVinculado = ""
        vinculo.vinculados.stream().forEach {
            it.setId(null)
            it.limparVinculado()
        }
        vinculo.naoVinculados = mutableListOf()
        return vinculo
    }

    private fun valida(): Boolean {
        if (cbBase.selectionModel.selectedItem == null) {
            cbBase.unFocusColor = Color.RED
            AlertasPopup.AvisoModal("Alerta", "Necessário inforar uma base.")
            return false
        }
        if (txtMangaOriginal.text.isEmpty()) {
            txtMangaOriginal.unFocusColor = Color.RED
            AlertasPopup.AvisoModal("Alerta", "Necessário inforar um manga principal.")
            return false
        }
        if (txtMangaVinculado.text.isEmpty()) {
            txtMangaVinculado.unFocusColor = Color.RED
            AlertasPopup.AvisoModal("Alerta", "Necessário inforar um manga vinculado.")
            return false
        }
        return true
    }

    private fun recarregar(): Boolean {
        try {
            val vinculo = service.select(cbBase.selectionModel.selectedItem, vinculo.getId()!!)
            if (vinculo.isPresent) {
                this.vinculo = vinculo.get()
                return true
            }
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        }
        return false
    }

    private fun carregar(): Boolean {
        var carregado = false
        if (cbBase.selectionModel.selectedItem != null && cbBase.selectionModel.selectedItem.isNotEmpty()) {
            try {
                if (arquivoOriginal != null && arquivoVinculado != null) {
                    val arquivoOriginal = if (arquivoOriginal != null) arquivoOriginal!!.name else ""
                    val arquivoVinculado = if (arquivoVinculado != null) arquivoVinculado!!.name else ""
                    val vinculo = service.select(
                        cbBase.selectionModel.selectedItem, spnVolume.value,
                        txtMangaOriginal.text, cbLinguagemOrigem.selectionModel.selectedItem,
                        arquivoOriginal, txtMangaVinculado.text,
                        cbLinguagemVinculado.selectionModel.selectedItem, arquivoVinculado
                    )
                    if (vinculo.isPresent) {
                        carregado = true
                        this.vinculo = vinculo.get()
                    } else
                        this.vinculo = limpaVinculo(this.vinculo)
                } else if (arquivoOriginal != null) {
                    val arquivoOriginal = if (arquivoOriginal != null) arquivoOriginal!!.name else ""
                    val vinculo = service.select(
                        cbBase.selectionModel.selectedItem, spnVolume.value,
                        txtMangaOriginal.text, cbLinguagemOrigem.selectionModel.selectedItem,
                        arquivoOriginal, "", null, ""
                    )
                    if (vinculo.isPresent) {
                        carregado = true
                        this.vinculo = limpaVinculo(vinculo.get())
                    }
                }
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
        }
        return carregado
    }

    private fun salvar() {
        vinculo.ultimaAlteracao = LocalDateTime.now()
        vinculo.base = cbBase.selectionModel.selectedItem
        vinculo.volume = spnVolume.value
        vinculo.linguagemOriginal = cbLinguagemOrigem.selectionModel.selectedItem
        vinculo.linguagemVinculado = cbLinguagemVinculado.selectionModel.selectedItem
        vinculo.nomeArquivoOriginal = txtArquivoOriginal.text
        vinculo.nomeArquivoVinculado = txtArquivoVinculado.text
        vinculo.vinculados = vinculado
        vinculo.naoVinculados = naoVinculado

        try {
            service.salvar(cbBase.selectionModel.selectedItem, vinculo)
            Notificacoes.notificacao(Notificacao.AVISO, "Concluido", "Salvo com sucesso.")
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            AlertasPopup.ErroModal("Erro ao salvar", e.message!!)
        }
    }

    private fun selecionaArquivo(titulo: String, pasta: String?): File {
        val fileChooser = FileChooser()
        fileChooser.title = titulo
        if (pasta != null && pasta.isNotEmpty()) fileChooser.initialDirectory = File(Utils.getCaminho(pasta))
        fileChooser.extensionFilters.addAll(
            FileChooser.ExtensionFilter("ALL FILES", "*.*"),
            FileChooser.ExtensionFilter("ZIP", "*.zip"), FileChooser.ExtensionFilter("CBZ", "*.cbz"),
            FileChooser.ExtensionFilter("RAR", "*.rar"), FileChooser.ExtensionFilter("CBR", "*.cbr")
        )
        return fileChooser.showOpenDialog(null)
    }

    private val acMangaOriginal: InvalidationListener = InvalidationListener {
        if (cbBase.items.isEmpty()) cbBase.unFocusColor = Color.RED
        autoCompleteMangaOriginal
            .filter { string -> string.lowercase(Locale.getDefault()).contains(txtMangaOriginal.text.lowercase(Locale.getDefault())) }
        if (autoCompleteMangaOriginal.filteredSuggestions.isEmpty() || txtMangaOriginal.text.isEmpty()
            || automatico
        ) autoCompleteMangaOriginal.hide() else autoCompleteMangaOriginal.show(txtMangaOriginal)
    }
    private val acMangaVinculado: InvalidationListener = InvalidationListener {
        if (cbBase.items.isEmpty()) cbBase.unFocusColor = Color.RED
        autoCompleteMangaVinculado
            .filter { string -> string.lowercase(Locale.getDefault()).contains(txtMangaVinculado.text.lowercase(Locale.getDefault())) }
        if (autoCompleteMangaVinculado.filteredSuggestions.isEmpty() || txtMangaVinculado.text.isEmpty()
            || automatico
        ) autoCompleteMangaVinculado.hide() else autoCompleteMangaVinculado.show(txtMangaVinculado)
    }

    fun setAutoCompleteListener(isClear: Boolean) {
        if (isClear) {
            txtMangaOriginal.textProperty().removeListener(acMangaOriginal)
            txtMangaVinculado.textProperty().removeListener(acMangaVinculado)
            autoCompleteMangaOriginal.setSelectionHandler(null)
            autoCompleteMangaVinculado.setSelectionHandler(null)
        } else {
            txtMangaOriginal.textProperty().addListener(acMangaOriginal)
            txtMangaVinculado.textProperty().addListener(acMangaVinculado)
            autoCompleteMangaOriginal.setSelectionHandler { event -> txtMangaOriginal.text = event.getObject() }
            autoCompleteMangaVinculado.setSelectionHandler { event -> txtMangaVinculado.text = event.getObject() }
        }
    }

    private fun selecionaBase(base: String?) {
        autoCompleteMangaOriginal.suggestions.clear()
        autoCompleteMangaVinculado.suggestions.clear()
        if (base == null || base.isEmpty())
            return

        try {
            service.createTabelas(base)
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            println("Erro ao consultar as sugestões de mangas.")
        }
        selectionaManga(autoCompleteMangaOriginal, cbLinguagemOrigem, txtMangaOriginal)
        selectionaManga(autoCompleteMangaVinculado, cbLinguagemVinculado, txtMangaVinculado)
    }

    private fun selectionaManga(autoComplete: JFXAutoCompletePopup<String>, linguagem: JFXComboBox<Language>, manga: JFXTextField) {
        try {
            autoComplete.suggestions.clear()
            val mangas: List<String> = service.getMangas(cbBase.selectionModel.selectedItem ?: "", linguagem.selectionModel.selectedItem)
            autoComplete.suggestions.addAll(mangas)
            manga.text = ""
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
            println("Erro ao consultar as sugestões de mangas.")
        }
    }

    private fun onClose() {
        Utils.destroiParse(parseOriginal)
        Utils.destroiParse(parseVinculado)
    }

    private fun selecionaCapitulo(capitulo: String?, isManga: Boolean) {
        if (capitulo == null || capitulo.isEmpty()) return
        if (isManga && capitulosOriginal.isEmpty() || !isManga && capitulosVinculado.isEmpty()) return
        if (isManga) {
            val numero: Int = capitulosOriginal[capitulo]!!
            val pagina: Optional<VinculoPagina> = tvPaginasVinculadas.items.stream()
                .filter { pg -> pg.originalPagina.compareTo(numero) === 0 }.findFirst()
            if (pagina.isPresent) tvPaginasVinculadas.scrollTo(pagina.get())
        } else {
            val numero: Int = capitulosVinculado[capitulo]!!
            var pagina: Optional<VinculoPagina?> = tvPaginasVinculadas.items.stream()
                .filter { pg ->
                    (pg.vinculadoEsquerdaPagina.compareTo(numero) === 0
                            || pg.vinculadoDireitaPagina.compareTo(numero) === 0)
                }
                .findFirst()
            if (pagina.isPresent) tvPaginasVinculadas.scrollTo(pagina.get()) else {
                pagina = lvPaginasNaoVinculadas.items.stream()
                    .filter { pg -> pg.vinculadoEsquerdaPagina.compareTo(numero) === 0 }.findFirst()
                if (pagina.isPresent) lvPaginasNaoVinculadas.scrollTo(pagina.get())
            }
        }
    }

    // Necessário pois a imagem não é serializada
    private fun getVinculoOriginal(origem: Pagina, copia: VinculoPagina): VinculoPagina? {
        var original: VinculoPagina? = null
        when (origem) {
            Pagina.VINCULADO_DIREITA, Pagina.VINCULADO_ESQUERDA -> original = vinculado[vinculado.indexOf(copia)]
            Pagina.NAO_VINCULADO -> original = naoVinculado[naoVinculado.indexOf(copia)]
            else -> {}
        }
        return original
    }

    private fun preparaOnDrop() {
        lvPaginasNaoVinculadas.setOnDragOver { event ->
            if (event.gestureSource !== lvPaginasNaoVinculadas && event.dragboard.hasContent(Utils.VINCULO_ITEM_FORMAT))
                event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE)
            event.consume()
        }
        lvPaginasNaoVinculadas.setOnDragEntered { event ->
            lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_SELECIONADO, true)
            lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_INICIADO, false)
            event.consume()
        }
        lvPaginasNaoVinculadas.setOnDragExited { event ->
            lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false)
            lvPaginasNaoVinculadas.pseudoClassStateChanged(ON_DRAG_INICIADO, true)
            event.consume()
        }
        lvPaginasNaoVinculadas.setOnDragDropped { event ->
            val db: Dragboard = event.dragboard
            var success = false
            if (db.hasContent(Utils.VINCULO_ITEM_FORMAT)) {
                val vinculo = db.getContent(Utils.VINCULO_ITEM_FORMAT) as VinculoPagina
                getVinculoOriginal(vinculo.onDragOrigem!!, vinculo)?.let { service.addNaoVInculado(it, vinculo.onDragOrigem!!) }
                refreshTabelas(Tabela.ALL)
                success = true
            }
            event.isDropCompleted = success
            event.consume()
        }
        lvPaginasNaoVinculadas.onDragDone = EventHandler { onDragEnd() }
    }

    private fun preparaCelulas() {
        tvPaginasVinculadas.selectionModel = TableViewNoSelectionModel(tvPaginasVinculadas)
        lvPaginasNaoVinculadas.selectionModel = ListViewNoSelectionModel<VinculoPagina>()
        tcMangaOriginal.cellValueFactory = PropertyValueFactory("originalPagina")
        tcMangaOriginal.setCellFactory {
            object : TableCell<VinculoPagina, Int>() {
                @Override
                override fun updateItem(item: Int?, empty: Boolean) {
                    text = null
                    graphic = if (empty || item == null) null else {
                        val mLLoader = FXMLLoader(MangasVincularCelulaSimplesController.fxmlLocate)
                        try {
                            mLLoader.load<MangasVincularCelulaSimplesController>()
                            val controller = mLLoader.getController() as MangasVincularCelulaSimplesController
                            controller.setDados(tableRow.item)
                            controller.root
                        } catch (e: IOException) {
                            LOGGER.error(e.message, e)
                            null
                        }
                    }
                }
            }
        }

        tcMangaVinculado.setCellValueFactory(PropertyValueFactory("vinculadoEsquerdaPagina"))
        tcMangaVinculado.setCellFactory {
            object : TableCell<VinculoPagina, Int>() {
                @Override
                override fun updateItem(item: Int?, empty: Boolean) {
                    text = null
                    graphic = if (empty || item == null) null else {
                        val mLLoader = FXMLLoader(MangasVincularCelulaDuplaController.fxmlLocate)
                        try {
                            mLLoader.load<MangasVincularCelulaDuplaController>()
                            val controller = mLLoader.getController() as MangasVincularCelulaDuplaController
                            controller.setDados(tableRow.item)
                            controller.setListener(this@MangasVincularController)
                            HBox.setHgrow(controller.root, Priority.ALWAYS)
                            controller.root
                        } catch (e: IOException) {
                            LOGGER.error(e.message, e)
                            null
                        }
                    }
                }
            }
        }
        tvPaginasVinculadas.widthProperty().addListener { _, _, _ ->
            val header: Pane = tvPaginasVinculadas.lookup("TableHeaderRow") as Pane
            if (header.isVisible) {
                header.maxHeight = 0.0
                header.minHeight = 0.0
                header.prefHeight = 0.0
                header.isVisible = false
            }
        }
        lvPaginasNaoVinculadas.setCellFactory {
            object : ListCell<VinculoPagina>() {
                @Override
                override fun updateItem(item: VinculoPagina?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = null
                    graphic = if (empty || item == null) null else {
                        val mLLoader = FXMLLoader(MangasVincularCelulaPequenaController.fxmlLocate)
                        try {
                            mLLoader.load()
                        } catch (e: IOException) {
                            LOGGER.error(e.message, e)
                        }
                        val controller = mLLoader.getController() as MangasVincularCelulaPequenaController
                        controller.setDados(item)
                        controller.setListener(this@MangasVincularController)
                        controller.root
                    }
                }
            }
        }
        lvCapitulosOriginal.setOnMouseClicked { click -> if (click.clickCount > 1) selecionaCapitulo(lvCapitulosOriginal.selectionModel.selectedItem, true) }
        lvCapitulosVinculado.setOnMouseClicked { click -> if (click.clickCount > 1) selecionaCapitulo(lvCapitulosVinculado.selectionModel.selectedItem, false) }
    }

    private val robot: Robot = Robot()
    override fun initialize(arg0: URL?, arg1: ResourceBundle?) {
        Run.getPrimaryStage().setOnCloseRequest { onClose() }
        service.setListener(this)
        try {
            cbBase.items.setAll(service.tabelas)
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
        }
        cbBase.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            if (newValue.isNotEmpty()) {
                if (cbBase.items.isEmpty()) cbBase.setUnFocusColor(Color.RED) else {
                    cbBase.unFocusColor = Color.web("#106ebe")
                    if (cbBase.items.contains(newValue)) selecionaBase(newValue)
                }
            }
        }
        val autoCompletePopup: JFXAutoCompletePopup<String> = JFXAutoCompletePopup()
        autoCompletePopup.suggestions.addAll(cbBase.items)
        autoCompletePopup.setSelectionHandler { event -> cbBase.setValue(event.getObject()) }
        cbBase.editor.textProperty().addListener { _, _, _ ->
            autoCompletePopup.filter { item -> item.lowercase(Locale.getDefault()).contains(cbBase.editor.text.lowercase(Locale.getDefault())) }
            if (autoCompletePopup.filteredSuggestions.isEmpty() || cbBase.showingProperty().get() || cbBase.editor.text.isEmpty())
                autoCompletePopup.hide()
            else
                autoCompletePopup.show(cbBase.editor)
        }
        cbBase.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtMangaOriginal.focusTraversableProperty().addListener { _, oldValue, _ -> if (oldValue) txtMangaOriginal.unFocusColor = Color.web("#106ebe") }
        txtMangaOriginal.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        autoCompleteMangaOriginal = JFXAutoCompletePopup<String>()
        txtMangaVinculado.focusTraversableProperty().addListener { _, oldValue, _ -> if (oldValue) txtMangaVinculado.unFocusColor = Color.web("#106ebe") }
        txtMangaVinculado.onKeyPressed = EventHandler { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        autoCompleteMangaVinculado = JFXAutoCompletePopup<String>()
        setAutoCompleteListener(false)
        spnVolume.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbLinguagemOrigem.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbLinguagemOrigem.selectionModel.selectedItemProperty().addListener { _, _, _ -> selectionaManga(autoCompleteMangaOriginal, cbLinguagemOrigem, txtMangaOriginal) }
        cbLinguagemVinculado.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbLinguagemVinculado.selectionModel.selectedItemProperty().addListener { _, _, _ -> selectionaManga(autoCompleteMangaVinculado, cbLinguagemVinculado, txtMangaVinculado) }
        txtArquivoOriginal.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtArquivoVinculado.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        preparaCelulas()
        preparaOnDrop()
        spnVolume.setValueFactory(SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0))
        cbLinguagemVinculado.items.addAll(
            Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE,
            Language.PORTUGUESE_GOOGLE
        )
        cbLinguagemOrigem.items.addAll(
            Language.ENGLISH, Language.JAPANESE, Language.PORTUGUESE,
            Language.PORTUGUESE_GOOGLE
        )
        limpar()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MangasVincularController::class.java)
        private const val FAST: Int = 200
        private const val SLOW: Int = 500
        val fxmlLocate: URL get() = MangasVincularController::class.java.getResource("/view/mangas/MangaVincular.fxml") as URL
    }
}