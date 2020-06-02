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
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.notification.Alertas;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

public class SudachiTokenizer {

	private VocabularioServices vocabServ;
	private Set<String> repetido = new HashSet<String>();
	private List<Vocabulario> vocabNovo = new ArrayList<>();

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

	public void processaTexto(FrasesController cnt) throws ExcessaoBd {
		setVocabularioServices(new VocabularioServices());

		String[] texto = cnt.getTextoOrigem().split("\n");
		String processado = "";

		vocabNovo.clear();
		repetido.clear();

		cnt.setPalavra(texto[0]);

		String settings_path;
		switch ((Dicionario) cnt.getDicionario()) {
		case SAMLL:
			settings_path = Paths.get("").toAbsolutePath().toString() + "/sudachi_smalldict.json";;
			break;
		case CORE:
			settings_path = Paths.get("").toAbsolutePath().toString() + "/sudachi_coredict.json";;
			break;
		case FULL:
			settings_path = Paths.get("").toAbsolutePath().toString() + "/sudachi_fulldict.json";;
			break;
		default:
			settings_path = Paths.get("").toAbsolutePath().toString() + "/sudachi_fulldict.json";;
		}
		
		try (FileInputStream input = new FileInputStream(settings_path);
				Dictionary dict = new DictionaryFactory().create("", readAll(input))) {
			tokenizer = dict.create();

			SplitMode mode;
			switch ((Modo) cnt.getModo()) {
			case A:
				mode = SplitMode.A;
				break;
			case B:
				mode = SplitMode.B;
				break;
			case C:
				mode = SplitMode.C;
				break;
			default:
				mode = SplitMode.C;
			}

			for (String txt : texto) {
				if (txt != texto[0] && !txt.isEmpty()) {
					for (Morpheme m : tokenizer.tokenize(mode, txt)) {
						if (m.surface().matches(pattern) && !m.surface().equalsIgnoreCase(texto[0])
								&& !repetido.contains(m.dictionaryForm())
								&& !cnt.getExcluido().contains(m.dictionaryForm())) {

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

			}

		} catch (IOException e) {
			e.printStackTrace();
			Alertas.ErroModal("Erro ao processar textos", e.getMessage());
		}

		cnt.setVocabulario(vocabNovo);
		cnt.setTextoDestino(processado);

	}

	private void setVocabularioServices(VocabularioServices vocabServ) {
		this.vocabServ = vocabServ;
	}
}
