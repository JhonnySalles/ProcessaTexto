package org.jisho.textosJapones.util.tokenizers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jisho.textosJapones.controller.FrasesController;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Tipo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.notification.Alertas;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

public class SudachiTokenizer {

	private FrasesController controller;
	private VocabularioServices vocabServ;
	private Set<String> repetido = new HashSet<String>();
	private List<Vocabulario> vocabNovo = new ArrayList<>();

	private int i = 0;
	private int max = 0;

	/*
	 * private Runnable atualizaBarraWindows = new Runnable() {
	 * 
	 * @Override public void run() {
	 * TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, max,
	 * TaskbarProgressbar.Type.NORMAL); } };
	 */

	private String getPathSettings() {
		String settings_path = Paths.get("").toAbsolutePath().toString();
		switch ((Dicionario) controller.getDicionario()) {
		case SAMLL:
			settings_path += "/sudachi_smalldict.json";
			;
			break;
		case CORE:
			settings_path += "/sudachi_coredict.json";
			;
			break;
		case FULL:
			settings_path += "/sudachi_fulldict.json";
			;
			break;
		default:
			settings_path += "/sudachi_fulldict.json";
			;
		}

		return settings_path;
	}

	private SplitMode getModo() {
		switch ((Modo) controller.getModo()) {
		case A:
			return SplitMode.A;
		case B:
			return SplitMode.B;
		case C:
			return SplitMode.C;
		default:
			return SplitMode.C;
		}
	}

	// UNICODE RANGE : DESCRIPTION
	//
	// 3000-303F : punctuation
	// 3040-309F : hiragana
	// 30A0-30FF : katakana
	// FF00-FFEF : Full-width roman + half-width katakana
	// 4E00-9FAF : Common and uncommon kanji
	//
	// Non-Japanese punctuation/formatting characters commonly used in Japanese text
	// 2605-2606 : Stars
	// 2190-2195 : Arrows
	// u203B : Weird asterisk thing

	final private String pattern = ".*[\u4E00-\u9FAF].*";
	public Tokenizer tokenizer;

	static String readAll(InputStream input) throws IOException {
		InputStreamReader isReader = new InputStreamReader(input, StandardCharsets.UTF_8);
		BufferedReader reader = new BufferedReader(isReader);
		StringBuilder sb = new StringBuilder();
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			sb.append(line);
		}
		return sb.toString();
	}

	private void processaTexto() throws ExcessaoBd {
		String[] texto = controller.getTextoOrigem().split("\n");
		String processado = "";

		vocabNovo.clear();
		repetido.clear();

		controller.setPalavra(texto[0]);

		try (FileInputStream input = new FileInputStream(getPathSettings());
				Dictionary dict = new DictionaryFactory().create("", readAll(input))) {
			tokenizer = dict.create();

			i = 0;
			max = texto.length;
			SplitMode mode = getModo();

			for (String txt : texto) {
				if (txt != texto[0] && !txt.isEmpty()) {
					for (Morpheme m : tokenizer.tokenize(mode, txt)) {
						if (m.surface().matches(pattern) && !m.surface().equalsIgnoreCase(texto[0])
								&& !repetido.contains(m.dictionaryForm())
								&& !controller.getExcluido().contains(m.dictionaryForm())) {

							Vocabulario palavra = vocabServ.select(m.surface(), m.dictionaryForm());
							if (palavra != null) {
								processado += m.dictionaryForm() + " " + palavra.getTraducao() + " ";

								if (palavra.getFormaBasica().isEmpty() || palavra.getLeitura().isEmpty()) {
									palavra.setFormaBasica(m.dictionaryForm());
									palavra.setLeitura(m.readingForm());
									vocabServ.update(palavra);
								}
							} else {
								List<Vocabulario> existe = vocabNovo.stream()
										.filter(p -> p.getVocabulario().equalsIgnoreCase(m.surface()))
										.collect(Collectors.toList());

								processado += m.dictionaryForm() + " ** ";
								if (existe.size() < 1) {
									vocabNovo
											.add(new Vocabulario(m.surface(), m.dictionaryForm(), m.readingForm(), ""));
								}
							}
							repetido.add(m.dictionaryForm());
						}
					}
					processado += "\n\n\n";
				}
				i++;
				atualizaProgresso();
			}

			concluiProgresso(false);

		} catch (IOException e) {
			e.printStackTrace();
			concluiProgresso(true);
			Alertas.ErroModal("Erro ao processar textos", e.getMessage());
		}

		controller.setVocabulario(vocabNovo);
		controller.setTextoDestino(processado);
	}

	private void processaMusica() throws ExcessaoBd {
		String[] texto = controller.getTextoOrigem().split("\n");
		String processado = "";

		vocabNovo.clear();
		controller.limpaVocabulario();

		try (Dictionary dict = new DictionaryFactory().create("", readAll(new FileInputStream(getPathSettings())))) {
			tokenizer = dict.create();

			i = 1;
			max = texto.length;
			SplitMode mode = getModo();

			for (String txt : texto) {
				if (!txt.isEmpty()) {
					processado += txt + "\n\n";

					for (Morpheme m : tokenizer.tokenize(mode, txt)) {
						if (m.surface().matches(pattern) && !controller.getExcluido().contains(m.dictionaryForm())) {

							Vocabulario palavra = vocabServ.select(m.surface(), m.dictionaryForm());
							if (palavra != null) {
								processado += m.dictionaryForm() + " " + palavra.getTraducao() + " ";

								if (palavra.getFormaBasica().isEmpty() || palavra.getLeitura().isEmpty()) {
									palavra.setFormaBasica(m.dictionaryForm());
									palavra.setLeitura(m.readingForm());
									vocabServ.update(palavra);
								}
							} else {
								List<Vocabulario> existe = vocabNovo.stream()
										.filter(p -> p.getVocabulario().equalsIgnoreCase(m.surface()))
										.collect(Collectors.toList());

								processado += m.dictionaryForm() + " ** ";
								if (existe.size() < 1) {
									vocabNovo
											.add(new Vocabulario(m.surface(), m.dictionaryForm(), m.readingForm(), ""));
								}
							}
						}
					}
					processado += "\n\n\n";
				} else
					processado += "\n";
				i++;
				atualizaProgresso();
			}
			concluiProgresso(false);

		} catch (IOException e) {
			e.printStackTrace();
			concluiProgresso(true);
			Alertas.ErroModal("Erro ao processar textos", e.getMessage());
		}

		controller.setTextoDestino(processado);
	}

	private void atualizaProgresso() {
		controller.getBarraProgresso().setProgress(i / max);
		// Não há necessidade por enquanto.
		// Platform.runLater(atualizaBarraWindows);
	}

	public void concluiProgresso(boolean erro) {
		controller.getBarraProgresso().setProgress(0);
		// Não há necessidade por enquanto.
		/*
		 * if (erro) TaskbarProgressbar.showFullErrorProgress(Run.getPrimaryStage());
		 * else if (i >= max) TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
		 */
	}

	private void configura() {
		setVocabularioServices(new VocabularioServices());
	}

	public void processa(FrasesController cnt) throws ExcessaoBd {
		controller = cnt;
		configura();

		switch ((Tipo) controller.getTipo()) {
		case TEXTO:
			processaTexto();
			break;
		case MUSICA:
			processaMusica();
			break;
		}
	}

	private void setVocabularioServices(VocabularioServices vocabServ) {
		this.vocabServ = vocabServ;
	}
}
