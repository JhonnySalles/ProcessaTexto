package org.jisho.textosJapones.processar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.net.URL;

public class Tangorin {

	final private static String LINK = "https://tangorin.com/words?search={kanji}";
	final private static String LINK_NOME = "https://tangorin.com/names?search={kanji}";

	public static String processa(String kanji) {
		String retorno = getSignificado(kanji);

		if (retorno.isEmpty())
			retorno = getNome(kanji);

		return retorno;
	}

	public static String getSignificado(String kanji) {
		try {
			String retorno = "";
			String url = LINK.replace("{kanji}", kanji);
			Document pagina = null;

			try {
				pagina = Jsoup.parse(new URL(url).openStream(), "ISO-8859-1", url);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return "";
			}

			Elements grupo = pagina.getElementsByClass("results-group");

			if (grupo != null && grupo.size() > 0) {
				for (Element elemento : grupo) {

					Elements Conteudos = elemento.getElementsByAttributeValue("lang", "en");

					if (Conteudos != null && Conteudos.size() > 0) {
						String resultado = "";
						for (Element conteudo : Conteudos)
							if (!conteudo.text().isEmpty())
								resultado += conteudo.text() + "; ";

						retorno = resultado;
						break;
					}
				}
			}
			
			if (retorno.contains("; "))
				retorno = retorno.substring(0, retorno.lastIndexOf("; "));

			return retorno;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private static String getNome(String kanji) {
		try {
			String retorno = "";
			String url = LINK_NOME.replace("{kanji}", kanji);
			Document pagina = null;

			try {
				pagina = Jsoup.parse(new URL(url).openStream(), "ISO-8859-1", url);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return "";
			}
			
			Elements grupo = pagina.getElementsByClass("results-group");

			if (grupo != null && grupo.size() > 0) {
				for (Element elemento : grupo) {

					Elements nomes = elemento.getElementsByClass("entry entry-border names");

					if (nomes != null && nomes.size() > 0) {
						String resultado = "";
						for (Element nome : nomes) {
							if (!nome.text().isEmpty())
								resultado += nome.text() + "; ";
						}	

						retorno = resultado;
						break;
					}
				}
			}
			
			if (retorno.contains("; "))
				retorno = retorno.substring(0, retorno.lastIndexOf("; "));
			
			return retorno;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
}
