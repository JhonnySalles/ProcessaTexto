package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.components.listener.VinculoListener
import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import br.com.fenix.processatexto.model.enums.Pagina
import br.com.fenix.processatexto.util.Utils
import javafx.css.PseudoClass
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import java.net.URL
import java.util.*


class MangasVincularCelulaDuplaController : Initializable {

    val ON_DRAG_SELECIONADO: PseudoClass = PseudoClass.getPseudoClass("drag-selecionado")

    @FXML
    lateinit var root: HBox

    @FXML
    lateinit var esquerdaContainer: AnchorPane

    @FXML
    lateinit var esquerdaImagem: ImageView

    @FXML
    lateinit var esquerdaNumero: Text

    @FXML
    lateinit var esquerdaNome: VBox

    @FXML
    lateinit var esquerdaNomePagina: Text

    @FXML
    lateinit var direitaContainer: AnchorPane

    @FXML
    lateinit var direitaImagem: ImageView

    @FXML
    lateinit var direitaNumero: Text

    @FXML
    lateinit var direitaNome: VBox

    @FXML
    lateinit var direitaNomePagina: Text

    private var listener: VinculoListener? = null
    fun setDados(vinculo: VinculoPagina) {
        root.setOnDragEntered {
            if (vinculo.vinculadoEsquerdaPagina !== VinculoPagina.PAGINA_VAZIA) {
                direitaContainer.isVisible = true
                direitaContainer.prefWidth = esquerdaContainer.width
            }
        }
        root.setOnDragExited { if (!vinculo.isImagemDupla && vinculo.vinculadoDireitaPagina === VinculoPagina.PAGINA_VAZIA) direitaContainer.isVisible = false }
        root.setOnDragDone { if (!vinculo.isImagemDupla && vinculo.vinculadoDireitaPagina === VinculoPagina.PAGINA_VAZIA) direitaContainer.isVisible = false }
        if (vinculo != null) {
            configuraDrop(esquerdaContainer, vinculo, Pagina.VINCULADO_ESQUERDA)
            if (vinculo.vinculadoEsquerdaPagina === VinculoPagina.PAGINA_VAZIA) {
                esquerdaContainer.onDragDetected = null
                esquerdaImagem.image = vinculo.imagemVinculadoEsquerda
                esquerdaNumero.text = ""
                esquerdaNomePagina.text = ""
                esquerdaNome.isVisible = false
            } else {
                esquerdaContainer.setOnDragDetected { event ->
                    listener!!.onDragStart()
                    val db: Dragboard = esquerdaContainer.startDragAndDrop(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK)
                    db.setDragView(Utils.criaSnapshot(esquerdaContainer), event.x, event.y)
                    vinculo.onDragOrigem = Pagina.VINCULADO_ESQUERDA
                    val content = ClipboardContent()
                    content[Utils.VINCULO_ITEM_FORMAT] = vinculo
                    content[Utils.NUMERO_PAGINA_ITEM_FORMAT] = vinculo.vinculadoEsquerdaPagina.toString()
                    content.putString(vinculo.vinculadoEsquerdaNomePagina)
                    db.setContent(content)
                    event.consume()
                }
                esquerdaContainer.setOnDragDone {
                    listener!!.onDragEnd()
                    direitaContainer.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false)
                }
                esquerdaImagem.image = vinculo.imagemVinculadoEsquerda
                esquerdaNumero.text = vinculo.vinculadoEsquerdaPagina.toString()
                esquerdaNomePagina.text = vinculo.vinculadoEsquerdaNomePagina
                esquerdaNome.isVisible = true
            }
            configuraDrop(direitaContainer, vinculo, Pagina.VINCULADO_DIREITA)
            if (vinculo.vinculadoDireitaPagina === VinculoPagina.PAGINA_VAZIA) {
                direitaContainer.onDragDetected = null
                direitaContainer.isVisible = false
                direitaImagem.image = vinculo.imagemVinculadoDireita
                direitaNumero.text = ""
                direitaNomePagina.text = ""
            } else {
                direitaContainer.setOnDragDetected { event ->
                    listener!!.onDragStart()
                    val db: Dragboard = esquerdaContainer.startDragAndDrop(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK)
                    vinculo.onDragOrigem = Pagina.VINCULADO_DIREITA
                    val content = ClipboardContent()
                    content[Utils.VINCULO_ITEM_FORMAT] = vinculo
                    content[Utils.NUMERO_PAGINA_ITEM_FORMAT] = vinculo.vinculadoEsquerdaPagina.toString()
                    content.putString(vinculo.vinculadoEsquerdaNomePagina)
                    db.setContent(content)
                    event.consume()
                }
                direitaContainer.setOnDragDone {
                    listener!!.onDragEnd()
                    direitaContainer.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false)
                }
                direitaContainer.isVisible = vinculo.imagemVinculadoDireita != null
                direitaImagem.image = vinculo.imagemVinculadoDireita
                direitaNumero.text = vinculo.vinculadoDireitaPagina.toString()
                direitaNomePagina.text = vinculo.vinculadoDireitaNomePagina
            }
        } else {
            configuraDrop(esquerdaContainer, vinculo, Pagina.VINCULADO_DIREITA)
            esquerdaImagem.image = null
            esquerdaNumero.text = ""
            esquerdaNomePagina.text = ""
            esquerdaNome.isVisible = false
            direitaContainer.isVisible = false
            direitaImagem.image = null
            direitaNumero.text = ""
            direitaNomePagina.text = ""
            limpaDrop(direitaContainer)
        }
    }

    private fun limpaDrop(pane: AnchorPane) {
        pane.onDragEntered = null
        pane.onDragExited = null
    }

    private fun configuraDrop(pane: AnchorPane, destino: VinculoPagina?, tipo: Pagina) {
        pane.setOnDragOver { event ->
            if (event.gestureSource !== pane && event.dragboard.hasContent(Utils.VINCULO_ITEM_FORMAT))
                event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE)
            event.consume()
        }
        pane.setOnDragEntered { event ->
            if (event.gestureSource !== pane)
                pane.pseudoClassStateChanged(ON_DRAG_SELECIONADO, true)
            event.consume()
        }
        pane.setOnDragExited { event ->
            pane.pseudoClassStateChanged(ON_DRAG_SELECIONADO, false)
            event.consume()
        }
        pane.setOnDragDropped { event ->
            val db: Dragboard = event.dragboard
            var success = false
            if (db.hasContent(Utils.VINCULO_ITEM_FORMAT)) {
                val vinculo: VinculoPagina = db.getContent(Utils.VINCULO_ITEM_FORMAT) as VinculoPagina
                listener!!.onDrop(vinculo.onDragOrigem!!, vinculo, tipo, destino!!)
                success = true
            }
            event.isDropCompleted = success
            event.consume()
        }
    }

    fun setListener(listener: VinculoListener?) {
        this.listener = listener
    }

    @Override
    override fun initialize(location: URL, resources: ResourceBundle) {
        esquerdaImagem.fitWidthProperty().bind(esquerdaContainer.widthProperty())
        esquerdaImagem.fitHeightProperty().bind(esquerdaContainer.heightProperty())
        esquerdaImagem.isPreserveRatio = true
        esquerdaImagem.fitWidthProperty().bind(esquerdaNome.widthProperty())
        direitaImagem.fitWidthProperty().bind(direitaContainer.widthProperty())
        direitaImagem.fitHeightProperty().bind(direitaContainer.heightProperty())
        direitaImagem.isPreserveRatio = true
        direitaImagem.fitWidthProperty().bind(direitaNome.widthProperty())
        direitaContainer.managedProperty().bind(direitaContainer.visibleProperty())
    }

    companion object {
        val fxmlLocate: URL get() = MangasVincularCelulaDuplaController::class.java.getResource("/view/mangas/MangaVincularCelulaDupla.fxml") as URL
    }
}