package org.jisho.textosJapones.util.processar;

import java.io.IOException;
import java.util.regex.Pattern;

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
			e.printStackTrace();
		} 
		
		return retorno;
	}
}
