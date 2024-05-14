package br.com.fenix.processatexto.processar

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.net.URL


object Kanshudo {

    private val LOGGER: Logger = LoggerFactory.getLogger(Kanshudo::class.java)

    //final private static String LINK_WORD = "https://www.kanshudo.com/searchw?q={kanji}";
    private const val LINK_NAME = "https://www.kanshudo.com/searchn?q={kanji}"
    fun processa(kanji: String): String {

        /*if (retorno.isEmpty())
			retorno = getSignificado(kanji);*/return getNome(kanji)
    }

    private const val allFlag = ".*"
    private const val japanese = "[\u3041-\u9FAF]"
    private fun getNome(kanji: String): String {
        return try {
            val url: String = LINK_NAME.replace("{kanji}", kanji)
            val pagina: Document = try {
                Jsoup.parse(URL(url).openStream(), "UTF-8", url)
            } catch (e: FileNotFoundException) {
                LOGGER.error(e.message, e)
                return ""
            }
            val nomes: Elements = pagina.getElementsByClass("name_readings")
            if (nomes == null || nomes.size === 0)
                return ""
            var resultado = ""
            for (elemento in nomes) {
                if (elemento.text().isNotEmpty())
                    resultado += elemento.text().plus("; ")
            }
            if (resultado.isNotEmpty())
                resultado = resultado.replace("\\(click the name to view details\\)", "")
                .replace("Common reading:", "").replace("unclassified ", "").trim()
            if (resultado.matches((allFlag + japanese + allFlag).toRegex()))
                resultado = resultado.replace(japanese.toRegex(), "").trim()

            resultado
        } catch (e: Exception) {
            LOGGER.error(e.message, e)
            ""
        }
    } /*private static String getSignificado(String kanji) {
		try {
			String url = LINK_WORD.replace("{kanji}", kanji);

			Document pagina = null;

			try {
				pagina = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
			} catch (FileNotFoundException e) {
				
				LOGGER.error(e.message, e);
				return "";
			}

			Elements grupos = pagina.getElementsByClass("search_allinone");

			if (grupos == null || grupos.size() == 0)
				return "";

			for (Element elemento : grupos) {

				Elements traducoes = elemento.getElementsByClass("vm");

				if (traducoes != null && traducoes.size() > 0) {
					String resultado = "";
					for (Element traducao : traducoes)
						if (!traducao.text().isEmpty())
							resultado += traducao.text().concat("; ");

					if (resultado.contains("; "))
						resultado = resultado.substring(0, resultado.lastIndexOf("; "));

					return resultado;
				}
			}

			return "";
		} catch (Exception e) {
			LOGGER.error(e.message, e);
			return "";
		}
	}*/
}