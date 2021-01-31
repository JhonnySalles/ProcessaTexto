package org.jisho.textosJapones.util.processar;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
}
