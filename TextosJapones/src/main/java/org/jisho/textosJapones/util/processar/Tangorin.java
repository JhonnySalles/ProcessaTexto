package org.jisho.textosJapones.util.processar;

import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.concurrent.Worker.State;
import javafx.scene.web.WebEngine;

public class Tangorin {

	final private static String LINK = "https://tangorin.com/words?search={kanji}";

	public static void processa(String kanji, Consumer<String> actionInicializacao) {
		String url = LINK.replace("{kanji}", kanji);

		WebEngine engine = new WebEngine();

		engine.getLoadWorker().stateProperty().addListener((obs, oldValue, newValue) -> {
			System.out.println(newValue);
			if (newValue == State.SUCCEEDED) {
				String retorno = "";

				Document pagina = Jsoup.parse(engine.getDocument().toString());

				Elements grupo = pagina.getElementsByClass("results-group");

				if (grupo != null && grupo.size() > 0) {
					for (Element elemento : grupo) {

						Elements Conteudos = elemento.getElementsByAttributeValue("lang", "en");

						if (Conteudos != null && Conteudos.size() > 0) {
							String resultado = "";
							for (Element conteudo : Conteudos)
								if (!conteudo.text().isEmpty())
									resultado += conteudo.text() + "\n";

							retorno = resultado;
							break;
						}
					}
				}

				actionInicializacao.accept(retorno);
			}

		});

		engine.load(url);
	}
}
