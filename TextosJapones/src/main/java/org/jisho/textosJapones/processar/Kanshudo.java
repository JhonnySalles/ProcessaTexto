package org.jisho.textosJapones.processar;

import java.io.FileNotFoundException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Kanshudo {

	//final private static String LINK_WORD = "https://www.kanshudo.com/searchw?q={kanji}";
	final private static String LINK_NAME = "https://www.kanshudo.com/searchn?q={kanji}";

	public static String processa(String kanji) {
		String retorno = getNome(kanji);

		/*if (retorno.isEmpty())
			retorno = getSignificado(kanji);*/

		return retorno;
	}

	final private static String allFlag = ".*";
	final private static String japanese = "[\u3041-\u9FAF]";

	private static String getNome(String kanji) {
		try {
			String url = LINK_NAME.replace("{kanji}", kanji);

			Document pagina = null;

			try {
				pagina = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return "";
			}

			Elements nomes = pagina.getElementsByClass("name_readings");

			if (nomes == null || nomes.size() == 0)
				return "";

			String resultado = "";
			for (Element elemento : nomes) {
				if (!elemento.text().isEmpty())
					resultado += elemento.text().concat("; ");
			}

			if (!resultado.isEmpty())
				resultado = resultado.replaceAll("\\(click the name to view details\\)", "")
						.replaceAll("Common reading:", "").replaceAll("unclassified ", "").trim();

			if (resultado.matches(allFlag + japanese + allFlag))
				resultado = resultado.replaceAll(japanese, "").trim();

			return resultado;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	/*private static String getSignificado(String kanji) {
		try {
			String url = LINK_WORD.replace("{kanji}", kanji);

			Document pagina = null;

			try {
				pagina = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
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
			e.printStackTrace();
			return "";
		}
	}*/
}
