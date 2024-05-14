package br.com.fenix.processatexto.processar

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.net.URL

object JapanDict {

    private val LOGGER = LoggerFactory.getLogger(JapanDict::class.java)
    private const val LINK = "https://www.japandict.com/?s={kanji}&lang=eng"

    fun processa(kanji: String): String {
        return try {
            val url: String = LINK.replace("{kanji}", kanji)
            val pagina: Document = try {
                Jsoup.parse(URL(url).openStream(), "ISO-8859-1", url)
            } catch (e: FileNotFoundException) {
                LOGGER.error(e.message, e)
                return ""
            }

            val grupos = pagina.getElementsByClass("list-group-item")
            if (grupos == null || grupos.size === 0)
                return ""

            for (elemento in grupos) {
                val traducoes = elemento.getElementsByAttributeValue("lang", "en")
                if (traducoes != null && traducoes.size > 0) {
                    var resultado = ""
                    for (traducao in traducoes)
                        if (traducao.text().isNotEmpty())
                            resultado += traducao.text().plus("; ")

                    if (resultado.contains("; "))
                        resultado = resultado.substring(0, resultado.lastIndexOf("; "))

                    return resultado
                }
            }
            ""
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
            ""
        }
    }
}