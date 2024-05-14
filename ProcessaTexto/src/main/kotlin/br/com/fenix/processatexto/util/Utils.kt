package br.com.fenix.processatexto.util

import br.com.fenix.processatexto.fileparse.Parse
import br.com.fenix.processatexto.fileparse.ParseFactory
import br.com.fenix.processatexto.fileparse.RarParse
import br.com.fenix.processatexto.model.enums.Api
import javafx.collections.FXCollections
import javafx.scene.Node
import javafx.scene.SnapshotParameters
import javafx.scene.control.ListView
import javafx.scene.control.TableView
import javafx.scene.control.skin.ListViewSkin
import javafx.scene.control.skin.TableViewSkin
import javafx.scene.control.skin.VirtualFlow
import javafx.scene.image.WritableImage
import javafx.scene.input.DataFormat
import javafx.util.Pair
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors


class Utils {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Utils::class.java)

        val VINCULO_ITEM_FORMAT = DataFormat("custom.item.vinculo")
        val NUMERO_PAGINA_ITEM_FORMAT = DataFormat("custom.item.numero.pagina")

        fun removeDuplicate(texto: String): String {
            var formatado = texto
            var separador: String = ""
            if (formatado.contains(";"))
                separador = "\\;"
            else if (formatado.contains(","))
                separador = "\\,"

            if (separador.isNotEmpty()) {
                val split = formatado.lowercase(Locale.getDefault()).split(separador.toRegex()).toMutableList()
                split[split.size - 1] = split[split.size - 1].replace(".", "")
                val limpo = split.parallelStream().map { i -> i.trim() }.distinct().collect(Collectors.joining(", "))
                formatado = "$limpo."
            }
            return normalize(formatado)
        }

        fun normalize(texto: String): String {
            if (texto.isEmpty())
                return ""

            var frase = texto.substring(0, 1).uppercase(Locale.getDefault()) + texto.substring(1).replace("¹".toRegex(), "").replace("; ".toRegex(), ", ") + "."
            if (frase.contains(".."))
                frase = frase.replace("\\.{2,}".toRegex(), ".")

            return frase
        }

        fun next(api: Api): Api {
            var next: Api = api
            next = when (next) {
                Api.CONTA_PRINCIPAL -> Api.CONTA_SECUNDARIA
                Api.CONTA_SECUNDARIA -> Api.CONTA_MIGRACAO_1
                Api.CONTA_MIGRACAO_1 -> Api.CONTA_MIGRACAO_2
                Api.CONTA_MIGRACAO_2 -> Api.CONTA_MIGRACAO_3
                Api.CONTA_MIGRACAO_3 -> Api.CONTA_MIGRACAO_4
                else -> Api.CONTA_PRINCIPAL
            }
            return next
        }

        private val PASTA_CACHE = File(".").absolutePath + "/cache/"
        private val random = Random()

        fun criaParse(arquivo: File): Parse {
            val parse: Parse = ParseFactory.create(arquivo)
            if (parse is RarParse)
                parse.setCacheDirectory(File(PASTA_CACHE, getNomeSemExtenssao(arquivo.name) + random.nextInt(1000)))
            return parse
        }

        fun destroiParse(parse: Parse?) {
            if (parse == null)
                return

            if (parse is RarParse)
                try {
                    parse.destroir()
                } catch (e: IOException) {
                    LOGGER.error(e.message, e)
                }
        }

        fun isJson(filename: String): Boolean = filename.lowercase(Locale.getDefault()).matches(".*\\.(json)$".toRegex())

        fun isImage(filename: String): Boolean = filename.lowercase(Locale.getDefault()).matches(".*\\.(jpg|jpeg|bmp|gif|png|webp)$".toRegex())

        fun isZip(filename: String): Boolean = filename.lowercase(Locale.getDefault()).matches(".*\\.(zip|cbz)$".toRegex())

        fun isRar(filename: String): Boolean = filename.lowercase(Locale.getDefault()).matches(".*\\.(rar|cbr)$".toRegex())

        fun isTarball(filename: String): Boolean = filename.lowercase(Locale.getDefault()).matches(".*\\.(cbt)$".toRegex())

        fun isSevenZ(filename: String): Boolean = filename.lowercase(Locale.getDefault()).matches(".*\\.(cb7|7z)$".toRegex())

        fun MD5(string: String): String {
            return try {
                val digest = MessageDigest.getInstance("MD5")
                digest.update(string.toByteArray(), 0, string.length)
                BigInteger(1, digest.digest()).toString(16)
            } catch (e: NoSuchAlgorithmException) {
                string.replace("/", ".")
            }
        }

        fun MD5(image: InputStream): String {
            return try {
                val buffer = ByteArray(1024)
                val digest = MessageDigest.getInstance("MD5")
                var numRead = 0
                while (numRead != -1) {
                    numRead = image.read(buffer)
                    if (numRead > 0) digest.update(buffer, 0, numRead)
                }
                val md5Bytes = digest.digest()
                var md5 = ""
                for (i in md5Bytes.indices)
                    md5 += Integer.toString((md5Bytes[i].toInt() and 0xff) + 0x100, 16).substring(1)

                md5
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
                throw e
            } finally {
                try {
                    image.close()
                } catch (e: IOException) {
                    LOGGER.error(e.message, e)
                }
            }
        }

        @Throws(IOException::class)
        fun toByteArray(`is`: InputStream): ByteArray {
            val output = ByteArrayOutputStream()
            return try {
                val b = ByteArray(4096)
                var n = 0
                while (`is`.read(b).also { n = it } != -1)
                    output.write(b, 0, n)

                output.toByteArray()
            } finally {
                output.close()
            }
        }

        fun getNome(path: String): String {
            var name = path
            if (name.contains("/"))
                name = name.substring(name.lastIndexOf("/") + 1)
            else if (name.contains("\\"))
                name = name.substring(name.lastIndexOf("\\") + 1)
            return name
        }

        fun getNomeSemExtenssao(path: String): String {
            var name = path
            if (name.contains("/"))
                name = name.substring(name.lastIndexOf("/") + 1)
            else if (name.contains("\\"))
                name = name.substring(name.lastIndexOf("\\") + 1)

            if (name.contains("."))
                name = name.substring(0, name.lastIndexOf("."))

            return name
        }

        fun getExtenssao(path: String): String {
            return if (path.contains(".")) path.substring(path.lastIndexOf(".")) else path
        }

        fun getCapitulo(path: String): Pair<Float, Boolean>? {
            var capitulo: Pair<Float, Boolean>? = null
            var numero = -1f
            var extra = false
            val pasta: String = getPasta(path).lowercase(Locale.getDefault())
            if (pasta.contains("capítulo"))
                numero = java.lang.Float.valueOf(pasta.substring(pasta.indexOf("capítulo")).replace("[^\\d.]".toRegex(), ""))
            else if (pasta.contains("capitulo"))
                numero = java.lang.Float.valueOf(pasta.substring(pasta.indexOf("capitulo")).replace("[^\\d.]".toRegex(), ""))
            else if (pasta.contains("extra")) {
                numero = java.lang.Float.valueOf(pasta.substring(pasta.indexOf("extra")).replace("[^\\d.]".toRegex(), ""))
                extra = true
            }
            if (numero > -1)
                capitulo = Pair(numero, extra)
            return capitulo
        }

        fun getPasta(path: String): String {
            // Two validations are needed, because the rar file only has the base values,
            // with the beginning already in the folder when it exists
            var pasta = path
            if (pasta.contains("/"))
                pasta = pasta.substring(0, pasta.lastIndexOf("/"))
            else if (pasta.contains("\\"))
                pasta = pasta.substring(0, pasta.lastIndexOf("\\"))

            if (pasta.contains("/"))
                pasta = pasta.substring(pasta.lastIndexOf("/") + 1)
            else if (pasta.contains("\\"))
                pasta = pasta.substring(pasta.lastIndexOf("\\") + 1)

            return pasta
        }

        fun getCaminho(path: String): String {
            var pasta = path
            if (pasta.contains("/"))
                pasta = pasta.substring(0, pasta.lastIndexOf("/"))
            else if (pasta.contains("\\"))
                pasta = pasta.substring(0, pasta.lastIndexOf("\\"))
            return pasta
        }

        fun getNumerosFinais(str: String): String {
            var numeros = ""
            val m = Pattern.compile("\\d+$").matcher(str)
            while (m.find())
                numeros = m.group()
            return numeros
        }

        fun getNomeNormalizadoOrdenacao(path: String): String {
            val nome = getNomeSemExtenssao(path)
            val ultimos = getNumerosFinais(nome)
            return if (ultimos.isEmpty()) getNome(path) else
                (nome.substring(0, nome.lastIndexOf(ultimos)) + String.format("%10s", ultimos).replace(' ', '0') + getExtenssao(path))
        }

        fun criaSnapshot(node: Node): WritableImage {
            val snapshotParams = SnapshotParameters()
            return node.snapshot(snapshotParams, null)
        }

        fun getFirstVisibleIndex(t: ListView<*>): Int? {
            return try {
                val ts = t.skin as ListViewSkin<*>
                val vf = ts.children[0] as VirtualFlow<*>
                // System.out.println("##### Scrolling last " + first);
                vf.firstVisibleCell.index
            } catch (ex: Exception) {
                println("##### Scrolling: Exception $ex")
                null
            }
        }

        fun getLastVisibleIndex(t: ListView<*>): Int? {
            return try {
                val ts = t.skin as ListViewSkin<*>
                val vf = ts.children[0] as VirtualFlow<*>
                // System.out.println("##### Scrolling last " + last);
                vf.lastVisibleCell.index
            } catch (ex: Exception) {
                println("##### Scrolling: Exception $ex")
                null
            }
        }

        fun getFirstVisibleIndex(t: TableView<*>): Int? {
            return try {
                val ts = t.skin as TableViewSkin<*>
                val vf = ts.children[1] as VirtualFlow<*>
                // System.out.println("##### Scrolling last " + first);
                vf.firstVisibleCell.index
            } catch (ex: Exception) {
                println("##### Scrolling: Exception $ex")
                null
            }
        }

        fun getLastVisibleIndex(t: TableView<*>): Int? {
            return try {
                val ts = t.skin as TableViewSkin<*>
                val vf = ts.children[1] as VirtualFlow<*>
                // System.out.println("##### Scrolling last " + last);
                vf.lastVisibleCell.index
            } catch (ex: Exception) {
                println("##### Scrolling: Exception $ex")
                null
            }
        }

        fun getCapitulos(parse: Parse, lista: MutableMap<String, Int>, tabela: ListView<String>) {
            val capitulos = getCapitulos(parse)
            lista.clear()
            lista.putAll(capitulos.key)
            tabela.setItems(FXCollections.observableArrayList(capitulos.value))
        }

        fun getCapitulos(parse: Parse): Pair<Map<String, Int>, List<String>> {
            if (parse == null) return Pair(
                HashMap(),
                ArrayList()
            )
            val capitulos: Map<String, Int> = parse.getPastas()
            val descricao: List<String> = ArrayList(capitulos.keys).sorted()
            return Pair(capitulos, descricao)
        }

        private val dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun convertToDateTime(str: String): LocalDateTime = LocalDateTime.parse(str, dateTime)

        fun convertToString(ldt: LocalDateTime): String = ldt.format(dateTime)
    }
}