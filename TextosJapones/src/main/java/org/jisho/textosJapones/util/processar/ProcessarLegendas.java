package org.jisho.textosJapones.util.processar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.controller.LegendasController;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.tokenizers.SudachiTokenizer;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

import javafx.concurrent.Task;

public class ProcessarLegendas {

	private VocabularioServices vocabularioService = new VocabularioServices();
	private RevisarServices service = new RevisarServices();
	private LegendasController controller;

	public ProcessarLegendas(LegendasController controller) {
		this.controller = controller;
	}

	public void processarLegendas(List<String> frases) {
		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<Void> verificaConexao = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try (Dictionary dict = new DictionaryFactory().create("", SudachiTokenizer
						.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(controller.getDicionario()))))) {
					tokenizer = dict.create();
					mode = SudachiTokenizer.getModo(controller.getModo());

					int x = 0;
					for (String frase : frases) {
						x++;
						updateProgress(x, frases.size());
						updateMessage("Processando " + x + " de " + frases.size() + " registros.");
						processar(frase);
					}

				} catch (IOException e) {
					e.printStackTrace();
					AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
							"Erro ao processar a lista.");
				}

				return null;
			}

			@Override
			protected void succeeded() {
				AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Aviso",
						"Lista processada com sucesso.");
				controller.getBarraProgresso().progressProperty().unbind();
				controller.getLog().textProperty().unbind();
			}
		};
		controller.getBarraProgresso().progressProperty().bind(verificaConexao.progressProperty());
		controller.getLog().textProperty().bind(verificaConexao.messageProperty());
		Thread t = new Thread(verificaConexao);
		t.start();
	}

	public String processarVocabulario(Dicionario dicionario, Modo modo, String frase) {
		existe.clear();
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
			AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
					"Erro ao processar a lista.");
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
					Revisar revisar = new Revisar(m.surface(), m.dictionaryForm(), m.readingForm());
					service.insert(revisar);
				}
			}
		}
	}

	private Boolean usarRevisar = true;
	public Set<String> vocabulario = new HashSet<>();
	private Set<String> existe = new HashSet<>();
	
	private String gerarVocabulario(String frase) throws ExcessaoBd {
		String vocabularios = "";
		for (Morpheme m : tokenizer.tokenize(mode, frase)) {
			if (m.surface().matches(pattern)) {
				if (!vocabularioService.existeExclusao(m.surface(), m.dictionaryForm()) && !existe.contains(m.dictionaryForm())) {
					existe.add(m.dictionaryForm());
					Vocabulario palavra = vocabularioService.select(m.surface(), m.dictionaryForm());

					if (palavra != null) {
						if (palavra.getTraducao().substring(0, 2).matches(japanese))
							vocabularios += palavra.getTraducao() + " ";
						else
							vocabularios += m.dictionaryForm() + " - " + palavra.getTraducao() + " ";

						//Usado apenas para correção em formas em branco.
						if (palavra.getFormaBasica().isEmpty()) {
							palavra.setFormaBasica(m.dictionaryForm());
							palavra.setLeitura(m.readingForm());
							vocabularioService.update(palavra);
						}
							
						vocabulario.add(palavra.getFormaBasica());
					} else if (usarRevisar) {
						Revisar revisar = service.select(m.surface(), m.dictionaryForm());
						if (revisar != null) {

							if (revisar.getTraducao().substring(0, 2).matches(japanese))
								vocabularios += revisar.getTraducao() + "¹ ";
							else
								vocabularios += m.dictionaryForm() + " - " + revisar.getTraducao() + "¹ ";

							vocabulario.add(m.dictionaryForm());
						}
					}
				}
			}
		}
		return vocabularios;
	}

}
