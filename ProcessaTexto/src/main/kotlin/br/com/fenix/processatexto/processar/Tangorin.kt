package br.com.fenix.processatexto.processar

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.net.URL


object Tangorin {

    private val LOGGER: Logger = LoggerFactory.getLogger(Tangorin::class.java)

    private const val LINK = "https://tangorin.com/words?search={kanji}"
    private const val LINK_NOME = "https://tangorin.com/names?search={kanji}"

    fun processa(kanji: String): String {
        var retorno = getSignificado(kanji)
        if (retorno.isEmpty())
            retorno = getNome(kanji)
        return retorno
    }

    fun getSignificado(kanji: String): String {
        return try {
            var retorno = ""
            val url: String = LINK.replace("{kanji}", kanji)
            val pagina: Document = try {
                Jsoup.parse(URL(url).openStream(), "ISO-8859-1", url)
            } catch (e: FileNotFoundException) {
                LOGGER.error(e.message, e)
                return ""
            }
            val grupo: Elements = pagina.getElementsByClass("results-group")
            if (grupo != null && grupo.size > 0) {
                for (elemento in grupo) {
                    val conteudos: Elements = elemento.getElementsByAttributeValue("lang", "en")
                    if (conteudos != null && conteudos.size > 0) {
                        var resultado = ""
                        for (conteudo in conteudos)
                            if (conteudo.text().isNotEmpty())
                                resultado += conteudo.text() + "; "
                        retorno = resultado
                        break
                    }
                }
            }
            if (retorno.contains("; ")) retorno = retorno.substring(0, retorno.lastIndexOf("; "))
            retorno
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
            ""
        }
    }

    private fun getNome(kanji: String): String {
        return try {
            var retorno = ""
            val url: String = LINK_NOME.replace("{kanji}", kanji)
            val pagina: Document = try {
                Jsoup.parse(URL(url).openStream(), "ISO-8859-1", url)
            } catch (e: FileNotFoundException) {
                LOGGER.error(e.message, e)
                return ""
            }
            val grupo: Elements = pagina.getElementsByClass("results-group")
            if (grupo != null && grupo.size > 0) {
                for (elemento in grupo) {
                    val nomes: Elements = elemento.getElementsByClass("entry entry-border names")
                    if (nomes != null && nomes.size > 0) {
                        var resultado = ""
                        for (nome in nomes) {
                            if (nome.text().isNotEmpty())
                                resultado += nome.text() + "; "
                        }
                        retorno = resultado
                        break
                    }
                }
            }
            if (retorno.contains("; ")) retorno = retorno.substring(0, retorno.lastIndexOf("; "))
            retorno
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
            ""
        }
    }
}