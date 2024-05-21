package br.com.fenix.processatexto.controller.mangas

import br.com.fenix.processatexto.components.ImageViewZoom
import com.jfoenix.controls.JFXSlider
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.scene.text.Text
import java.net.URL
import java.util.*


class MangasTextoCelulaController : Initializable {
    
    @FXML
    lateinit var root: AnchorPane

    @FXML
    lateinit var imagem: ImageView

    @FXML
    lateinit var slider: JFXSlider

    @FXML
    lateinit var pagina: Text

    fun setDados(imagemOriginal: Image, namePagina: String) {
        imagem.image = imagemOriginal
        pagina.text = namePagina
        ImageViewZoom.configura(imagemOriginal, imagem, slider)
    }

    @Override
    override fun initialize(location: URL, resources: ResourceBundle) {
        imagem.fitWidthProperty().bind(root.widthProperty())
        imagem.fitHeightProperty().bind(root.heightProperty())
        imagem.isPreserveRatio = true
    }

    companion object {
        val fxmlLocate: URL get() = MangasTextoCelulaController::class.java.getResource("/view/mangas/MangaTextoCelula.fxml") as URL
    }
}