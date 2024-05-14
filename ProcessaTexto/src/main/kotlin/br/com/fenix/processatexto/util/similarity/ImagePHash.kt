package br.com.fenix.processatexto.util.similarity

import java.awt.Graphics2D
import java.awt.color.ColorSpace
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO


class ImagePHash {
    
    private var size = 32
    private var smallerSize = 8
    private var c: DoubleArray = DoubleArray(size)

    companion object {
        const val EQUAL = 5 // Images up to 5 apart is an equal image
        const val SIMILAR = 10 // Images up to 10 apart are similar
        private fun getBlue(img: BufferedImage, x: Int, y: Int): Int {
            return img.getRGB(x, y) and 0xff
        }
    }

    constructor() {
        initCoefficients()
    }

    constructor(size: Int, smallerSize: Int) {
        this.size = size
        this.smallerSize = smallerSize
        initCoefficients()
    }

    private fun initCoefficients() {
        c = DoubleArray(size)
        for (i in 1 until size) {
            c[i] = 1.0
        }
        c[0] = 1 / Math.sqrt(2.0)
    }

    private fun distance(imgStr: String, canStr: String): Int {
        var counter = 0
        var k = 0
        while (k < imgStr.length && k < canStr.length) {
            if (imgStr[k] !== canStr[k]) {
                counter++
            }
            k++
        }
        return counter
    }

    // Returns a 'binary string' (like. 001010111011100010) which is easy to do
    // a hamming distance on.
    @Throws(Exception::class)
    fun getHash(`is`: InputStream?): String {
        var img: BufferedImage = ImageIO.read(`is`)

        /*
		 * 1. Reduce size. Like Average Hash, pHash starts with a small image. However,
		 * the image is larger than 8x8; 32x32 is a good size. This is really done to
		 * simplify the DCT computation and not because it is needed to reduce the high
		 * frequencies.
		 */img = resize(img, size, size)

        /*
		 * 2. Reduce color. The image is reduced to a grayscale just to further simplify
		 * the number of computations.
		 */img = grayscale(img)
        val vals = Array(size) { DoubleArray(size) }
        for (x in 0 until img.width) {
            for (y in 0 until img.height) {
                vals[x][y] = getBlue(img, x, y).toDouble()
            }
        }

        /*
		 * 3. Compute the DCT. The DCT separates the image into a collection of
		 * frequencies and scalars. While JPEG uses an 8x8 DCT, this algorithm uses a
		 * 32x32 DCT.
		 */
        val dctVals = applyDCT(vals)

        /*
		 * 4. Reduce the DCT. This is the magic step. While the DCT is 32x32, just keep
		 * the top-left 8x8. Those represent the lowest frequencies in the picture.
		 */
        /*
		 * 5. Compute the average value. Like the Average Hash, compute the mean DCT
		 * value (using only the 8x8 DCT low-frequency values and excluding the first
		 * term since the DC coefficient can be significantly different from the other
		 * values and will throw off the average).
		 */
        var total = 0.0
        for (x in 0 until smallerSize) {
            for (y in 0 until smallerSize) {
                total += dctVals[x][y]
            }
        }
        total -= dctVals[0][0]
        val avg = total / (smallerSize * smallerSize - 1).toDouble()

        /*
		 * 6. Further reduce the DCT. This is the magic step. Set the 64 hash bits to 0
		 * or 1 depending on whether each of the 64 DCT values is above or below the
		 * average value. The result doesn't tell us the actual low frequencies; it just
		 * tells us the very-rough relative scale of the frequencies to the mean. The
		 * result will not vary as long as the overall structure of the image remains
		 * the same; this can survive gamma and color histogram adjustments without a
		 * problem.
		 */
        var hash = ""
        for (x in 0 until smallerSize) {
            for (y in 0 until smallerSize) {
                if (x != 0 && y != 0) {
                    hash += if (dctVals[x][y] > avg) "1" else "0"
                }
            }
        }
        return hash
    }

    private fun resize(image: BufferedImage, width: Int, height: Int): BufferedImage {
        val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = resizedImage.createGraphics()
        g.drawImage(image, 0, 0, width, height, null)
        g.dispose()
        return resizedImage
    }

    private val colorConvert: ColorConvertOp = ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
    private fun grayscale(img: BufferedImage): BufferedImage {
        colorConvert.filter(img, img)
        return img
    }

    private fun applyDCT(f: Array<DoubleArray>): Array<DoubleArray> {
        val N = size
        val F = Array(N) { DoubleArray(N) }
        for (u in 0 until N) {
            for (v in 0 until N) {
                var sum = 0.0
                for (i in 0 until N) {
                    for (j in 0 until N) {
                        sum += (Math.cos((2 * i + 1) / (2.0 * N) * u * Math.PI)
                                * Math.cos((2 * j + 1) / (2.0 * N) * v * Math.PI) * f[i][j])
                    }
                }
                sum *= c[u] * c[v] / 4.0
                F[u][v] = sum
            }
        }
        return F
    }

    @Throws(Exception::class)
    fun distance(srcUrl: URL, canUrl: URL): Int {
        val imgStr = getHash(srcUrl.openStream())
        val canStr = getHash(canUrl.openStream())
        return this.distance(imgStr, canStr)
    }

    @Throws(Exception::class)
    fun distance(srcFile: File?, canFile: File?): Int {
        val imageSrcFile = getHash(FileInputStream(srcFile))
        val imageCanFile = getHash(FileInputStream(canFile))
        return this.distance(imageSrcFile, imageCanFile)
    }

    fun match(imgStr: String, canStr: String): Boolean {
        return matchSimilar(imgStr, canStr)
    }

    fun match(imgStr: String, canStr: String, limiar: Double): Boolean {
        return if (imgStr.isEmpty() || canStr.isEmpty()) false else this.distance(imgStr, canStr) <= limiar
    }

    fun matchEqual(imgStr: String, canStr: String): Boolean {
        return if (imgStr.isEmpty() || canStr.isEmpty()) false else this.distance(imgStr, canStr) <= EQUAL
    }

    fun matchSimilar(imgStr: String, canStr: String): Boolean {
        return if (imgStr.isEmpty() || canStr.isEmpty()) false else this.distance(imgStr, canStr) <= SIMILAR
    }

    fun matchLimiar(imgStr: String, canStr: String, limiar: Double): Int {
        return if (imgStr.isEmpty() || canStr.isEmpty()) (limiar + 1).toInt() else this.distance(imgStr, canStr)
    }

   
}