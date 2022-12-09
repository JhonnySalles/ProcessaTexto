package org.jisho.textosJapones.processar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.controller.legendas.LegendasImportarController;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarJaponesServices;
import org.jisho.textosJapones.model.services.VocabularioJaponesServices;
import org.jisho.textosJapones.tokenizers.SudachiTokenizer;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

import javafx.concurrent.Task;

public class ProcessarLegendas {

	private VocabularioJaponesServices vocabularioService = new VocabularioJaponesServices();
	private RevisarJaponesServices service = new RevisarJaponesServices();
	private LegendasImportarController controller;

	public ProcessarLegendas(LegendasImportarController controller) {
		this.controller = controller;
	}

	public void processarLegendas(List<String> frases) {
		GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
		progress.getTitulo().setText("Legendas - Processar");
		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<Void> processarVocabulario = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try (Dictionary dict = new DictionaryFactory().create("", SudachiTokenizer.readAll(new FileInputStream(
						SudachiTokenizer.getPathSettings(MenuPrincipalController.getController().getDicionario()))))) {
					tokenizer = dict.create();
					mode = SudachiTokenizer.getModo(MenuPrincipalController.getController().getModo());

					int x = 0;
					for (String frase : frases) {
						x++;
						updateProgress(x, frases.size());
						updateMessage("Processando " + x + " de " + frases.size() + " registros.");
						processar(frase);
					}

				} catch (IOException e) {
					e.printStackTrace();
					AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(),
							controller.getControllerPai().getRoot(), null, "Erro", "Erro ao processar a lista.");
				}

				return null;
			}

			@Override
			protected void succeeded() {
				super.succeeded();
				AlertasPopup.AvisoModal(controller.getControllerPai().getStackPane(),
						controller.getControllerPai().getRoot(), null, "Aviso", "Lista processada com sucesso.");
				progress.getBarraProgresso().progressProperty().unbind();
				progress.getLog().textProperty().unbind();
			}

			@Override
			protected void failed() {
				super.failed();
				System.out.print("Erro na thread de processamento do vocabulário: " + super.getMessage());
			}
		};
		progress.getBarraProgresso().progressProperty().bind(processarVocabulario.progressProperty());
		progress.getLog().textProperty().bind(processarVocabulario.messageProperty());
		Thread t = new Thread(processarVocabulario);
		t.start();
	}

	public String processarVocabulario(Dicionario dicionario, Modo modo, String frase) {
		existe.clear();
		this.vocabulario.clear();
		String vocabulario = "";
		try (Dictionary dict = new DictionaryFactory().create("",
				SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(dicionario))))) {
			tokenizer = dict.create();
			mode = SudachiTokenizer.getModo(modo);

			vocabulario = gerarVocabulario(frase);

			if (vocabulario.isEmpty() && mode.equals(SudachiTokenizer.getModo(Modo.C))) {
				mode = SudachiTokenizer.getModo(Modo.B);
				vocabulario = gerarVocabulario(frase);
			}

			if (vocabulario.isEmpty() && mode.equals(SudachiTokenizer.getModo(Modo.B))) {
				mode = SudachiTokenizer.getModo(Modo.C);
				vocabulario = gerarVocabulario(frase);
			}

		} catch (IOException | ExcessaoBd e) {
			vocabulario = "";
			e.printStackTrace();
			AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(),
					controller.getControllerPai().getRoot(), null, "Erro", "Erro ao processar a lista.");
		}

		return vocabulario.trim();
	}

	final private String pattern = ".*[\u4E00-\u9FAF].*";
	final private String japanese = ".*[\u3041-\u9FAF].*";
	private Tokenizer tokenizer;
	private SplitMode mode;

	private void processar(String frase) throws ExcessaoBd {
		for (Morpheme m : tokenizer.tokenize(mode, frase)) {
			if (m.surface().matches(pattern)) {
				Vocabulario palavra = vocabularioService.select(m.surface(), m.dictionaryForm());

				if (palavra == null) {
					Revisar revisar = new Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), false, true, false);
					service.insert(revisar);
				}
			}
		}
	}

	private Boolean usarRevisar = true;
	private Set<Vocabulario> vocabHistorico = new HashSet<>();
	private Set<String> validaHistorico = new HashSet<>();

	public Set<String> vocabulario = new HashSet<>();
	private Set<String> existe = new HashSet<>();
	
	public void clearVocabulary() {
		vocabHistorico.clear();
		validaHistorico.clear();
		vocabulario.clear();
		existe.clear();
	}

	private String gerarVocabulario(String frase) throws ExcessaoBd {
		String vocabularios = "";
		for (Morpheme m : tokenizer.tokenize(mode, frase)) {
			if (m.surface().matches(pattern)) {
				if (!vocabularioService.existeExclusao(m.surface(), m.dictionaryForm())
						&& !existe.contains(m.dictionaryForm())) {
					existe.add(m.dictionaryForm());

					Vocabulario palavra = null;
					if (validaHistorico.contains(m.dictionaryForm()))
						palavra = vocabHistorico.stream()
								.filter(vocab -> m.dictionaryForm().equalsIgnoreCase(vocab.getVocabulario()))
								.findFirst().orElse(null);

					if (palavra != null)
						vocabularios += m.dictionaryForm() + " - " + palavra.getPortugues() + " ";
					else {
						palavra = vocabularioService.select(m.surface(), m.dictionaryForm());
						if (palavra != null) {
							if (palavra.getPortugues().substring(0, 2).matches(japanese))
								vocabularios += palavra.getPortugues() + " ";
							else
								vocabularios += m.dictionaryForm() + " - " + palavra.getPortugues() + " ";

							// Usado apenas para correção em formas em branco.
							if (palavra.getFormaBasica().isEmpty()) {
								palavra.setFormaBasica(m.dictionaryForm());
								palavra.setLeitura(m.readingForm());
								vocabularioService.update(palavra);
							}

							validaHistorico.add(m.dictionaryForm());
							vocabHistorico.add(palavra);

							vocabulario.add(palavra.getFormaBasica());
						} else if (usarRevisar) {
							Revisar revisar = service.select(m.surface(), m.dictionaryForm());
							if (revisar != null) {

								if (!revisar.getPortugues().isEmpty()
										&& revisar.getPortugues().substring(0, 2).matches(japanese))
									vocabularios += revisar.getPortugues() + "¹ ";
								else {
									vocabularios += m.dictionaryForm() + " - " + revisar.getPortugues() + "¹ ";
									validaHistorico.add(m.dictionaryForm());
									vocabHistorico
											.add(new Vocabulario(m.dictionaryForm(), revisar.getPortugues() + "¹"));
								}

								vocabulario.add(m.dictionaryForm());
							}
						}
					}

				}
			}
		}
		return vocabularios;
	}

}
