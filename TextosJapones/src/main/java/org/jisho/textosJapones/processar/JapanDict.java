package org.jisho.textosJapones.processar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URL;

public class JapanDict {

    private static final Logger LOGGER = LoggerFactory.getLogger(JapanDict.class);

    final private static String LINK = "https://www.japandict.com/?s={kanji}&lang=eng";

    public static String processa(String kanji) {
        try {
            String url = LINK.replace("{kanji}", kanji);
            Document pagina = null;

            try {
                pagina = Jsoup.parse(new URL(url).openStream(), "ISO-8859-1", url);
            } catch (FileNotFoundException e) {
                
                LOGGER.error(e.getMessage(), e);
                return "";
            }

            Elements grupos = pagina.getElementsByClass("list-group-item");

            if (grupos == null || grupos.size() == 0)
                return "";

            for (Element elemento : grupos) {

                Elements traducoes = elemento.getElementsByAttributeValue("lang", "en");

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
            
            LOGGER.error(e.getMessage(), e);
            return "";
        }
    }
}
