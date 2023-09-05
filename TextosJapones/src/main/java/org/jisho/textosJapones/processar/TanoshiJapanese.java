package org.jisho.textosJapones.processar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class TanoshiJapanese {

    private static final Logger LOGGER = LoggerFactory.getLogger(TanoshiJapanese.class);

    final private static String LINK = "https://www.tanoshiijapanese.com/dictionary/index.cfm?j={kanji}";
    final private static String SITE = "https://www.tanoshiijapanese.com/";

    private static Document getLink(String link) throws IOException {
        return Jsoup.connect(link).get();
    }

    private static String getSignificado(Element campo) {
        Element TextoSignificado = campo.getElementsByClass("en").get(0);
        Element ingles = TextoSignificado.select("ol").get(0);
        List<Element> linhas = ingles.select("li");

        String texto = "";
        if (linhas == null || linhas.isEmpty() || linhas.size() == 1)
            texto = ingles.text();
        else {
            for (Element linha : linhas)
                if (!linha.text().isEmpty())
                    texto += linha.text().concat("; ");

            if (texto.contains("; "))
                texto = texto.substring(0, texto.lastIndexOf("; "));
        }
        return texto;
    }

    public static String processa(String kanji) {
        try {
            Document pagina = getLink(LINK.replace("{kanji}", kanji));

            Element CampoSignificado = pagina.getElementById("idEnglishMeaning");

            if (CampoSignificado == null) {
                Elements Ruby = pagina.getElementsByTag("ruby");
                for (Element rb : Ruby) {
                    if (rb.getElementsByTag("rb").get(0).text().equalsIgnoreCase(kanji)) {
                        Elements CampoEntry = pagina.getElementsByClass("entrylinks");
                        Elements Links = CampoEntry.get(0).getElementsByTag("a");
                        String link = "";
                        for (Element ln : Links)
                            if (ln.text().contains("Entry Details")) {
                                link = ln.attr("href");
                                break;
                            }


                        if (link == "")
                            continue;

                        pagina = getLink(SITE + link.replaceFirst("\\.\\.", ""));
                        CampoSignificado = pagina.getElementById("idEnglishMeaning");

                        if (CampoSignificado != null)
                            return getSignificado(CampoSignificado);
                    }
                }

                return "";
            }

            return getSignificado(CampoSignificado);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return "";
        }
    }

    public static String[][] getSentencas(Document pagina) {
        String[][] retorno = {{"", "", ""}, {"", "", ""}};

        Element CampoSentenca = pagina.getElementById("idSampleSentences");

        if (CampoSentenca == null)
            return retorno;

        Elements sentencas = CampoSentenca.getElementsByClass("sm");

        int i = 0;
        for (Element sentenca : sentencas) {
            String frase = sentenca.getElementsByClass("jp").text();
            String traducao = sentenca.getElementsByClass("en").text();
            String link = sentenca.select("a").first().attr("abs:href");

            if (frase != null && !frase.isEmpty()) {
                retorno[i][0] = frase;
                retorno[i][1] = traducao;
                retorno[i][2] = link;

                if (i == 1)
                    break;
                i++;
            }
        }

        return retorno;
    }

    final static String ENTRE_PARENTESES = "\\((.*?)\\)";
    final static String PONTO = "·";

    public static String encontraLink(String kanji, Document pagina) {
        Elements messages = pagina.getElementsByClass("message");

        Pattern parenteses = Pattern.compile(ENTRE_PARENTESES, Pattern.MULTILINE);
        Pattern ponto = Pattern.compile(PONTO, Pattern.MULTILINE);
        try {
            for (Element elemento : messages) {
                Elements itens = elemento.getElementsByClass("jp");

                if (elemento.getElementsByClass("entrylinks").size() <= 0)
                    continue;

                String link = elemento.getElementsByClass("entrylinks").first().select("a").first().attr("abs:href");

                for (Element item : itens) {
                    String palavra = item.text().trim();

                    if (parenteses.matcher(palavra).find())
                        palavra = palavra.replaceAll(ENTRE_PARENTESES, "");

                    if (ponto.matcher(palavra).find())
                        palavra = palavra.replaceAll(PONTO, "");

                    if (kanji.equalsIgnoreCase(palavra))
                        return link;
                }
            }
        } catch (Exception e) {
            System.out.println(pagina.baseUri());
            
            LOGGER.error(e.getMessage(), e);
        }

        return "";
    }

    public static String[][] getFrase(String kanji) {
        String[][] retorno = {{"", "", ""}, {"", "", ""}};
        try {
            Document pagina = Jsoup.connect(LINK.replace("{kanji}", kanji)).get();

            if (pagina.getElementById("idSampleSentences") != null)
                retorno = getSentencas(pagina);
            else {
                String link = encontraLink(kanji, pagina);
                if (!link.isEmpty()) {
                    pagina = Jsoup.connect(link).get();
                    retorno = getSentencas(pagina);
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return retorno;
    }
}
