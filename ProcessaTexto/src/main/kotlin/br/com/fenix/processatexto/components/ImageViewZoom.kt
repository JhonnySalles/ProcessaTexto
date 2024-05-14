package br.com.fenix.processatexto.components

import com.jfoenix.controls.JFXSlider
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Point2D
import javafx.geometry.Rectangle2D
import javafx.scene.image.Image
import javafx.scene.image.ImageView

object ImageViewZoom {
    private const val MAX_ZOOM = 5
    fun limpa(imageView: ImageView, slider: JFXSlider) {
        if (imageView == null || slider == null) return
        imageView.setOnMousePressed(null)
        imageView.setOnMouseDragged(null)
        imageView.setOnMouseClicked(null)
        slider.valueProperty().addListener { e -> }
    }

    fun configura(image: Image, imageView: ImageView, slider: JFXSlider) {
        val width: Double = image.getWidth()
        val height: Double = image.getHeight()
        imageView.setPreserveRatio(true)
        reset(imageView, width, height)
        val mouseDown: ObjectProperty<Point2D> = SimpleObjectProperty()
        imageView.setOnMousePressed { e ->
            val mousePress: Point2D = imageViewToImage(imageView, Point2D(e.getX(), e.getY()))
            mouseDown.set(mousePress)
        }
        imageView.setOnMouseDragged { e ->
            val dragPoint: Point2D = imageViewToImage(imageView, Point2D(e.getX(), e.getY()))
            shift(imageView, dragPoint.subtract(mouseDown.get()))
            mouseDown.set(imageViewToImage(imageView, Point2D(e.getX(), e.getY())))
        }
        imageView.setOnMouseClicked { e ->
            if (e.getClickCount() === 2) {
                slider.valueProperty().set(1.0)
                reset(imageView, width, height)
            }
        }
        slider.setMin(1.0)
        slider.setMax(5.0)
        slider.setBlockIncrement(0.1)
        slider.setValue(1.0)
        slider.valueProperty().addListener { e ->
            val zoom: Double = slider.getValue()
            val viewport: Rectangle2D = imageView.getViewport()
            if (zoom != 1.0) {
                val mouse: Point2D = imageViewToImage(
                    imageView,
                    Point2D(image.getWidth() / MAX_ZOOM, image.getHeight() / MAX_ZOOM)
                )
                var newWidth: Double = image.getWidth()
                var newHeight: Double = image.getHeight()
                val imageViewRatio: Double = imageView.getFitWidth() / imageView.getFitHeight()
                val viewportRatio = newWidth / newHeight
                if (viewportRatio < imageViewRatio) {
                    newHeight = newHeight / zoom
                    newWidth = newHeight / imageViewRatio
                    if (newWidth > image.getWidth()) {
                        newWidth = image.getWidth()
                    }
                } else {
                    newWidth = newWidth / zoom
                    newHeight = newWidth / imageViewRatio
                    if (newHeight > image.getHeight()) {
                        newHeight = image.getHeight()
                    }
                }
                var newMinX = 0.0
                if (newWidth < image.getWidth()) {
                    newMinX = clamp(mouse.getX() - (mouse.getX() - viewport.getMinX()) / zoom, 0.0, width - newWidth)
                }
                var newMinY = 0.0
                if (newHeight < image.getHeight()) {
                    newMinY = clamp(mouse.getY() - (mouse.getY() - viewport.getMinY()) / zoom, 0.0, height - newHeight)
                }
                imageView.setViewport(Rectangle2D(newMinX, newMinY, newWidth, newHeight))
            } else reset(imageView, width, height)
        }
    }

    private fun reset(imageView: ImageView, width: Double, height: Double) {
        imageView.setViewport(Rectangle2D(0.0, 0.0, width, height))
    }

    private fun shift(imageView: ImageView, delta: Point2D) {
        val viewport: Rectangle2D = imageView.getViewport()
        val width: Double = imageView.getImage().getWidth()
        val height: Double = imageView.getImage().getHeight()
        val maxX: Double = width - viewport.getWidth()
        val maxY: Double = height - viewport.getHeight()
        val minX = clamp(viewport.getMinX() - delta.getX(), 0.0, maxX)
        val minY = clamp(viewport.getMinY() - delta.getY(), 0.0, maxY)
        imageView.setViewport(Rectangle2D(minX, minY, viewport.getWidth(), viewport.getHeight()))
    }

    private fun clamp(value: Double, min: Double, max: Double): Double {
        if (value < min) return min
        return if (value > max) max else value
    }

    private fun imageViewToImage(imageView: ImageView, imageViewCoordinates: Point2D): Point2D {
        val xProportion: Double = imageViewCoordinates.getX() / imageView.getBoundsInLocal().getWidth()
        val yProportion: Double = imageViewCoordinates.getY() / imageView.getBoundsInLocal().getHeight()
        val viewport: Rectangle2D = imageView.getViewport()
        return Point2D(
            viewport.getMinX() + xProportion * viewport.getWidth(),
            viewport.getMinY() + yProportion * viewport.getHeight()
        )
    }
}