package org.jisho.textosJapones.tokenizers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jisho.textosJapones.controller.FrasesAnkiController;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.VocabularioJaponesServices;

import com.atilika.kuromoji.ipadic.Token;
import com.atilika.kuromoji.ipadic.Tokenizer;

public class KuromojiTokenizer {

	private VocabularioJaponesServices vocabServ;
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

	final private Tokenizer tokenizer = new Tokenizer();
	final private String pattern = ".*[\u4E00-\u9FAF].*";
	private int i;

	public void processaTexto(FrasesAnkiController cnt) {
		setVocabularioServices(new VocabularioJaponesServices());

		String[] texto = cnt.getTextoOrigem().split("\n");
		String processado = "";

		vocabNovo.clear();
		repetido.clear();

		cnt.setPalavra(texto[0]);
		try {
			for (String txt : texto) {
				if (txt != texto[0]) {
					if (!txt.isEmpty()) {
						List<Token> tokens = tokenizer.tokenize(txt);
						for (i = 0; i < tokens.size(); i++) {

							System.out.println(tokens.get(i).getSurface() + "\t" + tokens.get(i).getBaseForm() + "\t"
									+ tokens.get(i).getConjugationForm());

							if (tokens.get(i).getSurface().matches(pattern)
									&& !tokens.get(i).getSurface().equalsIgnoreCase(texto[0])
									&& !repetido.contains(tokens.get(i).getBaseForm())) {

								// Faz a validação se o próximo tokem também é um kanji, se for junta para ser
								// uma palavra só.
								if ((i + 1 < tokens.size()) && (tokens.get(i + 1).getSurface().matches(pattern))) {
									if (!texto[0].equalsIgnoreCase(
											tokens.get(i).getSurface() + tokens.get(i + 1).getSurface())
											&& !repetido.contains(
													tokens.get(i).getBaseForm() + tokens.get(i + 1).getBaseForm())) {

										Vocabulario palavra = vocabServ.select(
												tokens.get(i).getSurface() + tokens.get(i + 1).getSurface(),
												tokens.get(i).getBaseForm() + tokens.get(i + 1).getBaseForm());
										if (palavra != null) {
											processado += tokens.get(i).getBaseForm() + tokens.get(i + 1).getSurface()
													+ " " + palavra.getTraducao() + " ";

											if (palavra.getFormaBasica().isEmpty() || palavra.getLeitura().isEmpty()) {
												palavra.setFormaBasica(
														tokens.get(i).getBaseForm() + tokens.get(i + 1).getBaseForm());
												palavra.setLeitura(
														tokens.get(i).getReading() + tokens.get(i + 1).getReading());
												vocabServ.update(palavra);
											}
											repetido.add(tokens.get(i).getBaseForm() + tokens.get(i + 1).getBaseForm());
										} else {
											// Caso não encontre, irá verificar eles separadamente, no caso um laço duas
											// vezes.

											for (int x = 0; x < 2; x++) {
												palavra = vocabServ.select(tokens.get(i + x).getSurface(),
														tokens.get(i + x).getBaseForm());

												if (palavra != null) {
													processado += tokens.get(i + x).getBaseForm() + " "
															+ palavra.getTraducao() + " ";

													if (palavra.getFormaBasica().isEmpty()
															|| palavra.getLeitura().isEmpty()) {
														palavra.setFormaBasica(tokens.get(i + x).getBaseForm());
														palavra.setLeitura(tokens.get(i + x).getReading());
														vocabServ.update(palavra);
													}
												} else {
													List<Vocabulario> existe = vocabNovo.stream()
															.filter(p -> p.getVocabulario()
																	.equalsIgnoreCase(tokens.get(i).getSurface()))
															.collect(Collectors.toList());

													processado += tokens.get(i + x).getBaseForm() + " ** ";
													if (existe.size() < 1) {
														// naoEncontrado += tokens.get(i + x).getSurface() + " \n";

														vocabNovo.add(new Vocabulario(tokens.get(i + x).getSurface(),
																tokens.get(i + x).getBaseForm(),
																tokens.get(i + x).getReading(), ""));
													}
												}
												repetido.add(tokens.get(i + x).getBaseForm());
											}
										}
									}

									i++;
								} else {
									Vocabulario palavra = vocabServ.select(tokens.get(i).getSurface(),
											tokens.get(i).getBaseForm());

									if (palavra != null) {
										processado += tokens.get(i).getBaseForm() + " " + palavra.getTraducao() + " ";

										if (palavra.getFormaBasica().isEmpty() || palavra.getLeitura().isEmpty()) {
											palavra.setFormaBasica(tokens.get(i).getBaseForm());
											palavra.setLeitura(tokens.get(i).getReading());
											vocabServ.update(palavra);
										}
									} else {
										List<Vocabulario> existe = vocabNovo.stream().filter(
												p -> p.getVocabulario().equalsIgnoreCase(tokens.get(i).getSurface()))
												.collect(Collectors.toList());

										processado += tokens.get(i).getBaseForm() + " ** ";
										if (existe.size() < 1) {
											// naoEncontrado += tokens.get(i).getSurface() + " \n";

											vocabNovo.add(new Vocabulario(tokens.get(i).getSurface(),
													tokens.get(i).getBaseForm(), tokens.get(i).getReading(), ""));
										}
									}
									repetido.add(tokens.get(i).getBaseForm());
								}
							}
						}
						processado += "\n\n\n";
					}
				}
			}

		} catch (ExcessaoBd e) {
			System.out.println("Erro ao processar. Erro ao carregar informações do banco.");
			e.printStackTrace();
		}
		cnt.setVocabulario(vocabNovo);
		cnt.setTextoDestino(processado);
	}

	private void setVocabularioServices(VocabularioJaponesServices vocabServ) {
		this.vocabServ = vocabServ;
	}

}
