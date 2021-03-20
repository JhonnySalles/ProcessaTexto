package org.jisho.textosJapones.util.processar;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TanoshiJapanese {

	final private static String LINK = "https://www.tanoshiijapanese.com/dictionary/index.cfm?j={kanji}";

	public static String processa(String kanji) {
		try {
			Document pagina = Jsoup.connect(LINK.replace("{kanji}", kanji)).get();

			Element CampoSignificado = pagina.getElementById("idEnglishMeaning");

			if (CampoSignificado == null)
				return "";

			Element TextoSignificado = CampoSignificado.getElementsByClass("en").get(0);
			Element ingles = TextoSignificado.select("ol").get(0);
			return ingles.text();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public static String[][] getFrase(String kanji) {
		String[][] retorno = {{"", "", ""}, {"", "", ""}};
		try {
			Document pagina = Jsoup.connect(LINK.replace("{kanji}", kanji)).get();

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
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		return retorno;
	}
}
