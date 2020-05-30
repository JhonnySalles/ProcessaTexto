package org.jisho.textosJapones.util.kanjiStatics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jisho.textosJapones.model.entities.Estatistica;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.EstatisticaServices;
import org.jisho.textosJapones.util.notification.Notificacoes;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javafx.stage.FileChooser;

public class ImportaEstatistica {

	final private static String PIPE = "\\|\\|";
	final private static String TITULO_HTML = "<!DOCTYPE html> <html lang=\"jp\"> <body>";
	final private static String RODAPE_HTML = "</body> </html>";
	private static List<Estatistica> estatisticas = new ArrayList<>();
	private static EstatisticaServices estatisticaServ;

	public static void importa() {
		FileChooser escolha = new FileChooser();
		escolha.setInitialDirectory(new File(System.getProperty("user.home")));
		escolha.setTitle("Carregar arquivo estatistica.");
		escolha.getExtensionFilters()
				.add(new FileChooser.ExtensionFilter("Arquivo de texto (*.txt, *.csv)", "*.txt", "*.csv"));
		File arquivo = escolha.showOpenDialog(null);

		if (arquivo != null)
			processa(arquivo);
	}

	private static void processa(File arquivo) {
		try {
			String linha = "";
			String kanji = "";
			String[] splt;

			BufferedReader br = new BufferedReader(new FileReader(arquivo.getAbsolutePath()));
			estatisticaServ = new EstatisticaServices();

			while (br.ready()) {
				linha = br.readLine();

				if (!linha.isEmpty()) {
					splt = linha.split(PIPE);
					kanji = splt[0];

					Document doc = Jsoup.parse(TITULO_HTML + splt[1] + RODAPE_HTML);
					Elements linhaTabela = doc.getElementsByTag("tr");

					// Linha
					int max = linhaTabela.size();
					int corSequencial = 0;
					String corSelecao = "";
					for (int i = 0; i < max; i++) {
						Elements colunaTabela = linhaTabela.get(i).getElementsByTag("td");

						Element tipo = colunaTabela.get(0);
						if (!tipo.text().equalsIgnoreCase("Total")) {
							Element leitura = colunaTabela.get(1);
							Element quantidade = colunaTabela.get(2);
							Element percentual = colunaTabela.get(3);
							Element media = colunaTabela.get(4);
							Element pecentMedia = colunaTabela.get(5);

							if (!corSelecao.equalsIgnoreCase(tipo.text())) {
								corSelecao = tipo.text();
								corSequencial = 0;
							}
							corSequencial++;

							estatisticas.add(new Estatistica(kanji, tipo.text(), leitura.text(),
									Double.valueOf(quantidade.text()),
									Float.valueOf(percentual.text().replace("%", "")), Double.valueOf(media.text()),
									Float.valueOf(pecentMedia.text().replace("%", "")), corSequencial));

						}
					}

				}
			}
			estatisticaServ.insert(estatisticas);
			Notificacoes.notificacao(Notificacao.SUCESSO, "Concluido", "Importação concluida com sucesso.");
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			Notificacoes.notificacao(Notificacao.ERRO, "Arquivo não encontrado",
					"Não foi possível carregar o arquivo para importação.");
		} catch (IOException e) {
			e.printStackTrace();
			Notificacoes.notificacao(Notificacao.ERRO, "Erro ao procesar o arquivo", e.getMessage().toString());
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			Notificacoes.notificacao(Notificacao.ERRO, "Erro ao salvar os dados", e.getMessage().toString());
		}

	}

}
