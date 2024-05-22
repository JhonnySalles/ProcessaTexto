package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.components.ImageViewZoom
import br.com.fenix.processatexto.model.entities.processatexto.VinculoPagina
import com.jfoenix.controls.JFXSlider
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.text.Text
import java.net.URL
import java.util.*


class MangasTextoCelulaDuplaController : Initializable {

    @FXML
    lateinit var hbRoot: HBox

    @FXML
    lateinit var esquerdaRoot: AnchorPane

    @FXML
    lateinit var esquerdaImagem: ImageView

    @FXML
    lateinit var esquerdaSlider: JFXSlider

    @FXML
    lateinit var esquerdaPagina: Text

    @FXML
    lateinit var direitaRoot: AnchorPane

    @FXML
    lateinit var direitaImagem: ImageView

    @FXML
    lateinit var direitaSlider: JFXSlider

    @FXML
    lateinit var direitaPagina: Text

    fun setDados(vinculo: VinculoPagina) {
        if (vinculo == null) {
            esquerdaImagem.image = null
            direitaImagem.image = null
            direitaRoot.isVisible = false
            esquerdaPagina.text = ""
            direitaPagina.text = ""
        } else {
            esquerdaImagem.image = vinculo.imagemVinculadoEsquerda
            direitaImagem.image = vinculo.imagemVinculadoDireita
            direitaRoot.isVisible = vinculo.vinculadoDireitaPagina !== VinculoPagina.PAGINA_VAZIA
            if (vinculo.mangaPaginaEsquerda != null)
                esquerdaPagina.text = "Pag: " + vinculo.mangaPaginaEsquerda!!.numero + " - " + vinculo.mangaPaginaEsquerda!!.nomePagina
            else
                esquerdaPagina.text = ""

            if (vinculo.mangaPaginaDireita != null)
                direitaPagina.text = "Pag: " + vinculo.mangaPaginaDireita!!.numero + " - " + vinculo.mangaPaginaDireita!!.nomePagina
            else
                direitaPagina.text = ""
            if (vinculo.imagemVinculadoEsquerda != null)
                ImageViewZoom.configura(vinculo.imagemVinculadoEsquerda!!, esquerdaImagem, esquerdaSlider)
            else
                ImageViewZoom.limpa(esquerdaImagem, esquerdaSlider)

            if (vinculo.imagemVinculadoDireita != null)
                ImageViewZoom.configura(vinculo.imagemVinculadoDireita!!, direitaImagem, direitaSlider)
            else
                ImageViewZoom.limpa(direitaImagem, direitaSlider)
        }
    }

    @Override
    override fun initialize(location: URL?, resources: ResourceBundle?) {
        esquerdaImagem.fitWidthProperty().bind(esquerdaRoot.widthProperty())
        esquerdaImagem.fitHeightProperty().bind(esquerdaRoot.heightProperty())
        esquerdaImagem.isPreserveRatio = true
        direitaImagem.fitWidthProperty().bind(direitaRoot.widthProperty())
        direitaImagem.fitHeightProperty().bind(direitaRoot.heightProperty())
        direitaImagem.isPreserveRatio = true
        direitaRoot.managedProperty().bind(direitaRoot.visibleProperty())
    }

    companion object {
        val fxmlLocate: URL get() = MangasTextoCelulaDuplaController::class.java.getResource("/view/mangas/MangaTextoCelulaDupla.fxml") as URL
    }
}