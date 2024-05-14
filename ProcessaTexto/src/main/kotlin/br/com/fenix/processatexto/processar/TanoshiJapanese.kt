package br.com.fenix.processatexto.processar

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.regex.Pattern


object TanoshiJapanese {

    private val LOGGER: Logger = LoggerFactory.getLogger(TanoshiJapanese::class.java)

    private const val LINK = "https://www.tanoshiijapanese.com/dictionary/index.cfm?j={kanji}"
    private const val SITE = "https://www.tanoshiijapanese.com/"

    @Throws(IOException::class)
    private fun getLink(link: String): Document {
        return Jsoup.connect(link).get()
    }

    private fun getSignificado(campo: Element): String {
        val textoSignificado: Element = campo.getElementsByClass("en")[0]
        val ingles: Element = textoSignificado.select("ol")[0]
        val linhas: List<Element> = ingles.select("li")
        var texto = ""
        if (linhas == null || linhas.isEmpty() || linhas.size === 1)
            texto = ingles.text()
        else {
            for (linha in linhas)
                if (linha.text().isNotEmpty())
                    texto += linha.text().plus("; ")

            if (texto.contains("; "))
                texto = texto.substring(0, texto.lastIndexOf("; "))
        }
        return texto
    }

    fun processa(kanji: String): String {
        return try {
            var pagina: Document = getLink(LINK.replace("{kanji}", kanji))
            var campoSignificado = pagina.getElementById("idEnglishMeaning")
            if (campoSignificado == null) {
                val ruby: Elements = pagina.getElementsByTag("ruby")
                for (rb in ruby) {
                    if (rb.getElementsByTag("rb")[0].text().equals(kanji, true)) {
                        val campoEntry: Elements = pagina.getElementsByClass("entrylinks")
                        val links: Elements = campoEntry[0].getElementsByTag("a")
                        var link = ""
                        for (ln in links) if (ln.text().contains("Entry Details")) {
                            link = ln.attr("href")
                            break
                        }
                        if (link === "")
                            continue

                        pagina = getLink(SITE + link.replaceFirst("\\.\\.", ""))
                        campoSignificado = pagina.getElementById("idEnglishMeaning")

                        if (campoSignificado != null)
                            return getSignificado(campoSignificado)
                    }
                }
                return ""
            }
            getSignificado(campoSignificado)
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
            ""
        }
    }

    fun getSentencas(pagina: Document): Array<Array<String>> {
        val retorno = arrayOf(arrayOf("", "", ""), arrayOf("", "", ""))
        val campoSentenca: Element = pagina.getElementById("idSampleSentences") ?: return retorno
        val sentencas: Elements = campoSentenca.getElementsByClass("sm")
        var i = 0
        for (sentenca in sentencas) {
            val frase = sentenca.getElementsByClass("jp").text()
            val traducao = sentenca.getElementsByClass("en").text()
            val link: String = sentenca.select("a").first()?.attr("abs:href") ?: ""
            if (frase.isNotEmpty()) {
                retorno[i][0] = frase
                retorno[i][1] = traducao
                retorno[i][2] = link
                if (i == 1) break
                i++
            }
        }
        return retorno
    }

    const val ENTRE_PARENTESES = "\\((.*?)\\)"
    const val PONTO = "·"
    fun encontraLink(kanji: String, pagina: Document): String {
        val messages: Elements = pagina.getElementsByClass("message")
        val parenteses: Pattern = Pattern.compile(ENTRE_PARENTESES, Pattern.MULTILINE)
        val ponto: Pattern = Pattern.compile(PONTO, Pattern.MULTILINE)
        try {
            for (elemento in messages) {
                val itens = elemento.getElementsByClass("jp")
                if (elemento.getElementsByClass("entrylinks").size <= 0)
                    continue
                val link: String = elemento.getElementsByClass("entrylinks").first()?.select("a")?.first()?.attr("abs:href") ?: ""
                for (item in itens) {
                    var palavra: String = item.text().trim()
                    if (parenteses.matcher(palavra).find())
                        palavra = palavra.replace(ENTRE_PARENTESES.toRegex(), "")
                    if (ponto.matcher(palavra).find())
                        palavra = palavra.replace(PONTO.toRegex(), "")

                    if (kanji.equals(palavra, ignoreCase = true))
                        return link
                }
            }
        } catch (e: Exception) {
            println(pagina.baseUri())
            LOGGER.error(e.message, e)
        }
        return ""
    }

    fun getFrase(kanji: String): Array<Array<String>> {
        var retorno = arrayOf(arrayOf("", "", ""), arrayOf("", "", ""))
        try {
            var pagina: Document = Jsoup.connect(LINK.replace("{kanji}", kanji)).get()
            if (pagina.getElementById("idSampleSentences") != null)
                retorno = getSentencas(pagina)
            else {
                val link = encontraLink(kanji, pagina)
                if (link.isNotEmpty()) {
                    pagina = Jsoup.connect(link).get()
                    retorno = getSentencas(pagina)
                }
            }
        } catch (e: IOException) {
            LOGGER.error(e.message, e)
        }
        return retorno
    }
}