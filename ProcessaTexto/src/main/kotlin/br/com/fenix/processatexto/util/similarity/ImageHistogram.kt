package br.com.fenix.processatexto.util.similarity

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO
import kotlin.math.sqrt


class ImageHistogram {

    private val redBins: Int
    private val greenBins: Int
    private val blueBins = 4

    companion object {
        const val SIMILAR = 1.0 // Images close to 1 are very similar
        const val NOT_SIMILAR = 0.0 // Images close to 0 are not similar

        // Expected threshold for the image to be similar, where above it will be equal
        const val LIMIAR = 0.7
    }

    init {
        greenBins = blueBins
        redBins = greenBins
    }

    @Throws(IOException::class)
    fun generate(`is`: InputStream?): FloatArray {
        return filter(ImageIO.read(`is`))
    }

    private fun filter(src: BufferedImage): FloatArray {
        val width: Int = src.getWidth()
        val height: Int = src.getHeight()
        val inPixels = IntArray(width * height)
        val histogramData = FloatArray(redBins * greenBins * blueBins)
        getRGB(src, 0, 0, width, height, inPixels)
        var index = 0
        var redIdx = 0
        var greenIdx = 0
        var blueIdx = 0
        var singleIndex = 0
        var total = 0f
        for (row in 0 until height) {
            var tr = 0
            var tg = 0
            var tb = 0
            for (col in 0 until width) {
                index = row * width + col
                tr = inPixels[index] shr 16 and 0xff
                tg = inPixels[index] shr 8 and 0xff
                tb = inPixels[index] and 0xff
                redIdx = getBinIndex(redBins, tr, 255).toInt()
                greenIdx = getBinIndex(greenBins, tg, 255).toInt()
                blueIdx = getBinIndex(blueBins, tb, 255).toInt()
                singleIndex = redIdx + greenIdx * redBins + blueIdx * redBins * greenBins
                histogramData[singleIndex] += 1f
                total += 1f
            }
        }
        for (i in histogramData.indices) {
            histogramData[i] = histogramData[i] / total
        }
        return histogramData
    }

    private fun getBinIndex(binCount: Int, color: Int, colorMaxValue: Int): Float {
        var binIndex = color.toFloat() / colorMaxValue.toFloat() * binCount.toFloat()
        if (binIndex >= binCount)
            binIndex = (binCount - 1).toFloat()
        return binIndex
    }

    private fun getRGB(image: BufferedImage, x: Int, y: Int, width: Int, height: Int, pixels: IntArray): IntArray {
        val type: Int = image.getType()
        return if (type == BufferedImage.TYPE_INT_ARGB || type == BufferedImage.TYPE_INT_RGB)
            image.raster.getDataElements(x, y, width, height, pixels) as IntArray
        else
            image.getRGB(x, y, width, height, pixels, 0, width)
    }

    @Throws(IOException::class)
    fun match(srcFile: File?, canFile: File?): Double {
        val sourceData = filter(ImageIO.read(srcFile))
        val candidateData = filter(ImageIO.read(canFile))
        return calcSimilarity(sourceData, candidateData)
    }

    @Throws(IOException::class)
    fun match(srcUrl: URL?, canUrl: URL?): Double {
        val sourceData = filter(ImageIO.read(srcUrl))
        val candidateData = filter(ImageIO.read(canUrl))
        return calcSimilarity(sourceData, candidateData)
    }

    private fun calcSimilarity(sourceData: FloatArray, candidateData: FloatArray): Double {
        val mixedData = DoubleArray(sourceData.size)
        for (i in sourceData.indices) {
            mixedData[i] = sqrt((sourceData[i] * candidateData[i]).toDouble())
        }

        // The values of Bhattacharyya Coefficient ranges from 0 to 1,
        var similarity = 0.0
        for (i in mixedData.indices) {
            similarity += mixedData[i]
        }

        // The degree of similarity
        return similarity
    }

    fun match(sourceData: FloatArray?, candidateData: FloatArray?): Boolean {
        return if (sourceData == null || candidateData == null) false else calcSimilarity(sourceData, candidateData) >= LIMIAR
    }

    fun match(sourceData: FloatArray?, candidateData: FloatArray?, limiar: Double): Boolean {
        return if (sourceData == null || candidateData == null) false else calcSimilarity(sourceData, candidateData) >= limiar
    }

    fun matchLimiar(sourceData: FloatArray?, candidateData: FloatArray?, limiar: Double): Double {
        return if (sourceData == null || candidateData == null) 0.0 else calcSimilarity(sourceData, candidateData)
    }

}