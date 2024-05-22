package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.components.listener.VinculoListener
import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import br.com.fenix.processatexto.model.enums.Pagina
import br.com.fenix.processatexto.util.Utils
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import java.net.URL
import java.util.*


class MangasVincularCelulaPequenaController : Initializable {

    @FXML
    lateinit var root: AnchorPane

    @FXML
    lateinit var imagem: ImageView

    @FXML
    lateinit var numero: Text

    @FXML
    lateinit var containerNome: VBox

    @FXML
    lateinit var nomePagina: Text

    private lateinit var listener: VinculoListener
    fun setDados(vinculo: VinculoPagina) {
        imagem.image = vinculo.imagemVinculadoEsquerda
        numero.text = vinculo.vinculadoEsquerdaPagina.toString()
        nomePagina.text = vinculo.vinculadoEsquerdaNomePagina
        root.setOnDragDetected {
            listener.onDragStart()
            val db = root.startDragAndDrop(TransferMode.COPY, TransferMode.MOVE, TransferMode.LINK)
            db.setDragView(Utils.criaSnapshot(root), it.x, it.y)
            vinculo.onDragOrigem = Pagina.NAO_VINCULADO
            val content = ClipboardContent()
            content[Utils.VINCULO_ITEM_FORMAT] = vinculo
            content[Utils.NUMERO_PAGINA_ITEM_FORMAT] = vinculo.vinculadoEsquerdaPagina.toString()
            content.putString(vinculo.vinculadoEsquerdaNomePagina)
            db.setContent(content)
            it.consume()
        }
    }

    fun setListener(listener: VinculoListener) {
        this.listener = listener
    }

    @Override
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        imagem.fitWidthProperty().bind(root.widthProperty())
        imagem.fitHeightProperty().bind(root.heightProperty())
        imagem.isPreserveRatio = true
        imagem.fitWidthProperty().bind(containerNome.widthProperty())
    }

    companion object {
        val fxmlLocate: URL get() = MangasVincularCelulaPequenaController::class.java.getResource("/view/mangas/MangaVincularCelulaPequena.fxml") as URL
    }
}