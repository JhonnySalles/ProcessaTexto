package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import javafx.css.PseudoClass
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import java.net.URL
import java.util.ResourceBundle


class MangasVincularCelulaSimplesController : Initializable {
    val ON_DRAG_SELECIONADO: PseudoClass = PseudoClass.getPseudoClass("drag-selecionado")

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
    
    fun setDados(vinculo: VinculoPagina?) {
        if (vinculo != null) {
            imagem.image = vinculo.imagemOriginal
            numero.text = vinculo.originalPagina.toString()
            nomePagina.text = vinculo.originalNomePagina
        } else {
            imagem.image = null
            numero.text = ""
            nomePagina.text = ""
        }
    }

    @Override
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        imagem.fitWidthProperty().bind(root.widthProperty())
        imagem.fitHeightProperty().bind(root.heightProperty())
        imagem.isPreserveRatio = true
        imagem.fitWidthProperty().bind(root.widthProperty())
    }

    companion object {
        val fxmlLocate: URL get() = MangasVincularCelulaSimplesController::class.java.getResource("/view/mangas/MangaVincularCelulaSimples.fxml") as URL
    }
}