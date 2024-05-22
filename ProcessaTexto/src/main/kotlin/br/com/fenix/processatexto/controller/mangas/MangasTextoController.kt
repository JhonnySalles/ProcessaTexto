package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.components.TableViewNoSelectionModel
import br.com.fenix.processatexto.components.animation.Animacao
import br.com.fenix.processatexto.components.listener.VinculoTextoListener
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaPagina
import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import br.com.fenix.processatexto.model.enums.Language
import com.jfoenix.controls.*
import javafx.beans.InvalidationListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.robot.Robot
import javafx.scene.text.Text
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URL
import java.util.*
import java.util.stream.Collectors


class MangasTextoController : Initializable, VinculoTextoListener {

    @FXML
    private lateinit var apRoot: AnchorPane

    @FXML
    private lateinit var btnClose: JFXButton

    @FXML
    private lateinit var btnSalvar: JFXButton

    @FXML
    private lateinit var btnCarregarLegendas: JFXButton

    @FXML
    private lateinit var txtMangaOriginal: JFXTextField

    @FXML
    private lateinit var txtMangaVinculado: JFXTextField

    @FXML
    private lateinit var cbLinguagemOrigem: JFXComboBox<Language>

    @FXML
    private lateinit var cbLinguagemVinculado: JFXComboBox<Language>

    @FXML
    private lateinit var tvPaginasVinculadas: TableView<VinculoPagina>

    @FXML
    private lateinit var tcMangaOriginal: TableColumn<VinculoPagina, Image>

    @FXML
    private lateinit var tcTextoOriginal: TableColumn<VinculoPagina, MangaPagina>

    @FXML
    private lateinit var tcMangaVinculado: TableColumn<VinculoPagina, Image>

    @FXML
    private lateinit var tcTextoVinculado: TableColumn<VinculoPagina, MangaPagina>

    @FXML
    private lateinit var lvCapitulosOriginal: JFXListView<String>

    @FXML
    private lateinit var lvCapitulosVinculado: JFXListView<String>

    @FXML
    private lateinit var lvTextoNaoLocalizado: JFXListView<String>

    private lateinit var vinculado: ObservableList<VinculoPagina>

    private var autoCompleteMangaOriginal: JFXAutoCompletePopup<String>? = null
    private var autoCompleteMangaVinculado: JFXAutoCompletePopup<String>? = null
    private var naoLocalizado: MutableMap<String, MangaPagina> = mutableMapOf()

    private lateinit var controller: MangasVincularController
    var controllerPai: MangasVincularController
        get() = controller
        set(controller) {
            this.controller = controller
        }

    @Override
    override fun refresh() {
        tvPaginasVinculadas.refresh()
        tvPaginasVinculadas.requestLayout()
    }

    @FXML
    private fun onBtnClose() {
        controller.refreshListener = null
        controller.setAutoCompleteListener(false)
        setAutoCompleteListener(true, controller)
        txtMangaOriginal.textProperty().unbind()
        txtMangaVinculado.textProperty().unbind()
        cbLinguagemOrigem.selectionModelProperty().unbind()
        cbLinguagemVinculado.selectionModelProperty().unbind()
        Animacao().fecharPane(controller.controllerPai.stackPane)
    }

    private val listenerMangaOriginal: InvalidationListener = InvalidationListener {
        autoCompleteMangaOriginal!!.filter { string -> string.lowercase(Locale.getDefault()).contains(txtMangaOriginal.text.lowercase(Locale.getDefault())) }
        if (autoCompleteMangaOriginal!!.filteredSuggestions.isEmpty() || txtMangaOriginal.text.isEmpty())
            autoCompleteMangaOriginal!!.hide()
        else
            autoCompleteMangaOriginal!!.show(txtMangaOriginal)
    }
    private val listenerMangaVinculado: InvalidationListener = InvalidationListener {
        autoCompleteMangaVinculado!!.filter { string -> string.lowercase(Locale.getDefault()).contains(txtMangaVinculado.text.lowercase(Locale.getDefault())) }
        if (autoCompleteMangaVinculado!!.filteredSuggestions.isEmpty() || txtMangaVinculado.text.isEmpty())
            autoCompleteMangaVinculado!!.hide()
        else
            autoCompleteMangaVinculado!!.show(txtMangaVinculado)
    }
    private var listenerAutoCompleteMangaOriginal: ListChangeListener<String>? = null
    private var listenerAutoCompleteMangaVinculado: ListChangeListener<String>? = null
    private fun setAutoCompleteListener(isClear: Boolean, controller: MangasVincularController?) {
        if (isClear) {
            txtMangaOriginal.textProperty().removeListener(listenerMangaOriginal)
            txtMangaVinculado.textProperty().removeListener(listenerMangaVinculado)
            autoCompleteMangaOriginal!!.selectionHandler = null
            autoCompleteMangaVinculado!!.selectionHandler = null
            controller!!.getAutoCompleteMangaOriginal().suggestions.removeListener(listenerAutoCompleteMangaOriginal)
            controller.getAutoCompleteMangaVinculado().suggestions.removeListener(listenerAutoCompleteMangaVinculado)
            listenerAutoCompleteMangaOriginal = null
            listenerAutoCompleteMangaVinculado = null
        } else {
            autoCompleteMangaOriginal = JFXAutoCompletePopup<String>()
            autoCompleteMangaOriginal!!.suggestions.addAll(controller!!.getAutoCompleteMangaOriginal().suggestions)
            autoCompleteMangaOriginal!!.setSelectionHandler { event -> txtMangaOriginal.text = event.getObject() }
            autoCompleteMangaVinculado = JFXAutoCompletePopup<String>()
            autoCompleteMangaVinculado!!.suggestions.addAll(controller.getAutoCompleteMangaVinculado().suggestions)
            autoCompleteMangaVinculado!!.setSelectionHandler { event -> txtMangaVinculado.text = event.getObject() }

            listenerAutoCompleteMangaOriginal = ListChangeListener<String> { change ->
                while (change.next()) {
                    for (removed in change.removed)
                        autoCompleteMangaOriginal!!.suggestions.remove(removed)

                    for (added in change.addedSubList)
                        autoCompleteMangaOriginal!!.suggestions.add(added)
                }
            }
            listenerAutoCompleteMangaVinculado = ListChangeListener<String> { change ->
                while (change.next()) {
                    for (removed in change.removed)
                        autoCompleteMangaVinculado!!.suggestions.remove(removed)

                    for (added in change.addedSubList)
                        autoCompleteMangaVinculado!!.suggestions.add(added)
                }
            }
            controller.getAutoCompleteMangaOriginal().suggestions.addListener(listenerAutoCompleteMangaOriginal)
            controller.getAutoCompleteMangaVinculado().suggestions.addListener(listenerAutoCompleteMangaVinculado)
            txtMangaOriginal.textProperty().addListener(listenerMangaOriginal)
            txtMangaVinculado.textProperty().addListener(listenerMangaVinculado)
        }
    }

    private fun cloneData(controller: MangasVincularController) {
        btnSalvar.onActionProperty().set(controller.btnSalvar.onActionProperty().get())
        btnCarregarLegendas.onActionProperty().set(controller.btnCarregarLegendas.onActionProperty().get())
        txtMangaOriginal.text = controller.getTxtMangaOriginal().text
        txtMangaVinculado.text = controller.getTxtMangaVinculado().text
        txtMangaOriginal.textProperty().bindBidirectional(controller.getTxtMangaOriginal().textProperty())
        txtMangaVinculado.textProperty().bindBidirectional(controller.getTxtMangaVinculado().textProperty())
        setAutoCompleteListener(false, controller)

        controller.setAutoCompleteListener(true)
        cbLinguagemOrigem.items.addAll(controller.getCbLinguagemOrigem().items)
        cbLinguagemVinculado.items.addAll(controller.getCbLinguagemVinculado().items)
        cbLinguagemOrigem.selectionModel.select(controller.getCbLinguagemOrigem().selectionModel.selectedItem)
        cbLinguagemVinculado.selectionModel.select(controller.getCbLinguagemVinculado().selectionModel.selectedItem)
        cbLinguagemOrigem.selectionModelProperty().bindBidirectional(controller.getCbLinguagemOrigem().selectionModelProperty())
        cbLinguagemVinculado.selectionModelProperty().bindBidirectional(controller.getCbLinguagemVinculado().selectionModelProperty())

        lvCapitulosOriginal.items = controller.listCapitulosOriginal
        lvCapitulosVinculado.items = controller.listCapitulosVinculado
        naoLocalizado = HashMap<String, MangaPagina>()
        if (controller.getVinculo().volumeOriginal != null) {
            controller.getVinculo().volumeOriginal!!.capitulos
                .forEach { c ->
                    c.paginas.parallelStream()
                        .forEach { p ->
                            p.addOutrasInformacoes(
                                controller.getVinculo().nomeArquivoOriginal,
                                "Original", c.capitulo
                            )
                        }
                }
            naoLocalizado.putAll(controller.getVinculo().volumeOriginal!!.capitulos
                .flatMap { cap -> cap.paginas } // Transforma as sublistas de paginas em uma lista
                .filter { pag ->
                    !vinculado.parallelStream() // Filtra apenas as paginas que não estão vinculada
                        .anyMatch { vin -> (vin.mangaPaginaOriginal != null && vin.mangaPaginaOriginal!! == pag) }
                }.map { it.descricao to it }
            ) // Transforma em um map
        }
        if (controller.getVinculo().volumeVinculado != null) {
            controller.getVinculo().volumeVinculado!!.capitulos
                .forEach { c ->
                    c.paginas.parallelStream()
                        .forEach { p ->
                            p.addOutrasInformacoes(
                                controller.getVinculo().nomeArquivoVinculado,
                                "Vinculado", c.capitulo
                            )
                        }
                }
            naoLocalizado.putAll(controller.getVinculo().volumeVinculado!!.capitulos
                .flatMap { cap -> cap.paginas }  // Transforma as sublistas de paginas em uma lista
                .filter { pag ->
                    !vinculado.parallelStream() // Filtra apenas as paginas que não estão vinculada
                        .anyMatch { vin ->
                            (vin.mangaPaginaEsquerda != null && vin.mangaPaginaEsquerda!! == pag) || (vin.mangaPaginaDireita != null && vin.mangaPaginaDireita!! == pag)
                        }
                }.map { it.descricao to it }
            ) // Transforma em um map
        }
        lvTextoNaoLocalizado.items = FXCollections.observableArrayList(naoLocalizado.keys.sorted())
    }

    fun setDados(vinculado: ObservableList<VinculoPagina>, controller: MangasVincularController) {
        this.vinculado = vinculado
        tvPaginasVinculadas.items = this.vinculado
        cloneData(controller)
    }

    private fun preparaCelulas() {
        tvPaginasVinculadas.selectionModel = TableViewNoSelectionModel(tvPaginasVinculadas)
        tcMangaOriginal.cellValueFactory = PropertyValueFactory("imagemOriginal")
        tcMangaOriginal
            .setCellFactory {
                object : TableCell<VinculoPagina, Image>() {
                    @Override
                    override fun updateItem(item: Image?, empty: Boolean) {
                        text = null
                        if (empty || item == null) graphic = null else {
                            val mLLoader = FXMLLoader(MangasTextoCelulaController.fxmlLocate)
                            try {
                                mLLoader.load<MangasTextoCelulaController>()
                                val controller: MangasTextoCelulaController = mLLoader.getController()
                                var pagina = ""
                                if (tableRow.item != null) {
                                    val manga: MangaPagina? = tableRow.item.mangaPaginaOriginal
                                    if (manga != null)
                                        pagina = "Pag: " + manga.numero + " - " + manga.nomePagina
                                }
                                controller.setDados(item, pagina)
                                graphic = controller.root
                            } catch (e: IOException) {
                                LOGGER.error(e.message, e)
                                graphic = null
                            }
                        }
                    }
                }
            }
        tcMangaVinculado.cellValueFactory = PropertyValueFactory("imagemVinculadoEsquerda")
        tcMangaVinculado
            .setCellFactory {
                object : TableCell<VinculoPagina, Image>() {
                    @Override
                    override fun updateItem(item: Image?, empty: Boolean) {
                        text = null
                        graphic = if (empty || item == null)
                            null
                        else {
                            val mLLoader = FXMLLoader(MangasTextoCelulaDuplaController.fxmlLocate)

                            try {
                                mLLoader.load<MangasTextoCelulaDuplaController>()
                                val controller: MangasTextoCelulaDuplaController = mLLoader.getController()
                                controller.setDados(tableRow.item)
                                controller.hbRoot
                            } catch (e: IOException) {
                                LOGGER.error(e.message, e)
                                null
                            }
                        }
                    }
                }
            }
        tcTextoOriginal.cellValueFactory = PropertyValueFactory("mangaPaginaOriginal")
        tcTextoOriginal.setCellFactory {
            object : TableCell<VinculoPagina, MangaPagina>() {
                @Override
                override fun updateItem(item: MangaPagina?, empty: Boolean) {
                    text = null
                    if (empty) graphic = null else {
                        val text = Text()
                        text.fill = Paint.valueOf("white")
                        text.styleClass.add("texto-stilo")
                        val area = JFXTextArea()
                        area.styleClass.add("background-Blue3")
                        area.styleClass.add("texto-stilo")
                        VBox.setVgrow(area, Priority.ALWAYS)
                        val container = VBox()
                        container.children.addAll(text, area)
                        container.spacing = 5.0
                        VBox.setVgrow(container, Priority.ALWAYS)
                        graphic = container
                        if (item != null) {
                            area.text = item.textos.map { it.texto }.joinToString { "\n" }
                            text.text = "Pag: " + item.numero + " - " + item.nomePagina
                        }
                    }
                }
            }
        }
        tcTextoVinculado.cellValueFactory = PropertyValueFactory("mangaPaginaEsquerda")
        tcTextoVinculado.setCellFactory {
            object : TableCell<VinculoPagina, MangaPagina>() {
                @Override
                override fun updateItem(item: MangaPagina?, empty: Boolean) {
                    text = null
                    if (empty) graphic = null else {
                        val text = Text()
                        text.fill = Paint.valueOf("white")
                        text.styleClass.add("texto-stilo")
                        val area = JFXTextArea()
                        area.styleClass.add("background-Blue3")
                        area.styleClass.add("texto-stilo")
                        VBox.setVgrow(area, Priority.ALWAYS)
                        val container = VBox()
                        container.children.addAll(text, area)
                        container.spacing = 5.0
                        VBox.setVgrow(container, Priority.ALWAYS)
                        graphic = container
                        var textos = ""
                        var pagina = ""
                        if (item != null) {
                            textos = item.textos.stream().map { it.texto }.collect(Collectors.joining("\n"))
                            pagina = "Pag: " + item.numero + " - " + item.nomePagina
                        }
                        val linha: VinculoPagina? = tableRow.item
                        if (linha != null) {
                            val direita: MangaPagina? = linha.mangaPaginaDireita
                            if (direita != null) {
                                textos += direita.textos.map { it.texto }.joinToString { "\n" }
                                pagina += " | " + "Pag: " + item!!.numero + " - " + item.nomePagina
                            }
                        }
                        area.text = textos
                        text.text = pagina
                    }
                }
            }
        }
    }

    fun scroolTo(index: Int) = tvPaginasVinculadas.scrollTo(index)

    private fun selecionaCapitulo(capitulo: String?, isManga: Boolean) {
        if (capitulo == null || capitulo.isEmpty())
            return

        if (isManga && controller.getCapitulosOriginal().isEmpty() || !isManga && controller.getCapitulosVinculado().isEmpty())
            return

        if (isManga) {
            val numero = controller.getCapitulosOriginal()[capitulo]!!
            val pagina: Optional<VinculoPagina> = tvPaginasVinculadas.items.stream()
                .filter { pg -> pg.originalPagina.compareTo(numero) === 0 }.findFirst()
            if (pagina.isPresent)
                tvPaginasVinculadas.scrollTo(pagina.get())
        } else {
            val numero = controller.getCapitulosVinculado()[capitulo]!!
            val pagina: Optional<VinculoPagina> = tvPaginasVinculadas.items.stream()
                .filter { pg -> (pg.vinculadoEsquerdaPagina.compareTo(numero) === 0 || pg.vinculadoDireitaPagina.compareTo(numero) === 0) }
                .findFirst()
            if (pagina.isPresent)
                tvPaginasVinculadas.scrollTo(pagina.get())
        }
    }

    private val robot: Robot = Robot()

    @Override
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        preparaCelulas()
        txtMangaOriginal.focusTraversableProperty().addListener { _, oldValue, _ -> if (oldValue) txtMangaOriginal.unFocusColor = Color.web("#106ebe") }
        txtMangaOriginal.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        txtMangaVinculado.focusTraversableProperty().addListener { _, oldValue, _ -> if (oldValue) txtMangaVinculado.unFocusColor = Color.web("#106ebe") }
        txtMangaVinculado.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbLinguagemOrigem.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        cbLinguagemVinculado.setOnKeyPressed { ke -> if (ke.code.equals(KeyCode.ENTER)) robot.keyPress(KeyCode.TAB) }
        lvCapitulosOriginal.setOnMouseClicked { click -> if (click.clickCount > 1) selecionaCapitulo(lvCapitulosOriginal.selectionModel.selectedItem, true) }
        lvCapitulosVinculado.setOnMouseClicked { click -> if (click.clickCount > 1) selecionaCapitulo(lvCapitulosVinculado.selectionModel.selectedItem, false) }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(MangasTextoController::class.java)
        val fxmlLocate: URL get() = MangasTextoController::class.java.getResource("/view/mangas/MangaTexto.fxml") as URL
    }
}