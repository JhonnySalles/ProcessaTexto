package br.com.fenix.processatexto.components

import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.image.PixelWriter
import javafx.scene.image.WritableImage
import java.io.Serializable

class SerializableImage : Serializable {

    companion object {
        private const val serialVersionUID = -3056416441879535423L
    }

    var width = 0.0
        private set
    var height = 0.0
        private set
    lateinit var pixels: Array<IntArray>
        private set

    var image: Image
        get() {
            val image = WritableImage(width.toInt(), height.toInt())
            val w: PixelWriter = image.pixelWriter
            for (i in 0 until width.toInt())
                for (j in 0 until height.toInt())
                    w.setArgb(i, j, pixels[i][j])
            return image
        }
        set(image) {
            width = image.width
            height = image.height
            pixels = Array(width.toInt()) { IntArray(height.toInt()) }
            val r: PixelReader = image.pixelReader
            for (i in 0 until width.toInt())
                for (j in 0 until height.toInt())
                    pixels[i][j] = r.getArgb(i, j)
        }

    fun equals(si: SerializableImage): Boolean {
        if (width != si.width)
            return false
        if (height != si.height)
            return false

        for (i in 0 until width.toInt())
            for (j in 0 until height.toInt())
                if (pixels[i][j] != si.pixels[i][j])
                    return false

        return true
    }

}