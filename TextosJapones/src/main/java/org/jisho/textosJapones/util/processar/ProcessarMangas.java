package org.jisho.textosJapones.util.processar;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.controller.MangasController;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaTexto;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Site;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.MangaServices;
import org.jisho.textosJapones.model.services.RevisarServices;
import org.jisho.textosJapones.model.services.VocabularioServices;
import org.jisho.textosJapones.util.notification.AlertasPopup;
import org.jisho.textosJapones.util.scriptGoogle.ScriptGoogle;
import org.jisho.textosJapones.util.tokenizers.SudachiTokenizer;

import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class ProcessarMangas {

	private VocabularioServices vocabularioService = new VocabularioServices();
	private MangasController controller;
	private RevisarServices serviceRevisar = new RevisarServices();
	private MangaServices serviceManga = new MangaServices();
	private ProcessarPalavra desmembra = new ProcessarPalavra();
	private Api contaGoogle;
	private Site siteDicionario;
	private Boolean desativar = false;

	public ProcessarMangas(MangasController controller) {
		this.controller = controller;
	}

	public void setDesativar(Boolean desativar) {
		this.desativar = desativar;
	}

	private Integer y, z;
	private String msg;
	private Set<String> vocabVolume = new HashSet<>();
	private Set<String> vocabCapitulo = new HashSet<>();
	private Set<String> vocabPagina = new HashSet<>();

	public void processarTabelas(List<MangaTabela> tabelas) {
		// Criacao da thread para que esteja validando a conexao e nao trave a tela.
		Task<Void> verificaConexao = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				try (Dictionary dict = new DictionaryFactory().create("", SudachiTokenizer
						.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(controller.getDicionario()))))) {
					tokenizer = dict.create();
					mode = SudachiTokenizer.getModo(controller.getModo());
					contaGoogle = controller.getContaGoogle();
					siteDicionario = controller.getSiteTraducao();

					y = 0;
					desativar = false;
					for (MangaTabela tabela : tabelas) {
						y++;

						int x = 0;
						for (MangaVolume volume : tabela.getVolumes()) {
							x++;
							updateProgress(x, tabela.getVolumes().size());

							msg = "Processando " + x + " de " + tabela.getVolumes().size() + " volumes." + "\nManga: "
									+ volume.getManga();

							vocabVolume.clear();
							z = 0;
							for (MangaCapitulo capitulo : volume.getCapitulos()) {
								z++;
								updateMessage(msg + "\nCapitulo " + capitulo.getCapitulo());
								vocabCapitulo.clear();
								for (MangaPagina pagina : capitulo.getPaginas()) {
									vocabPagina.clear();
									for (MangaTexto texto : pagina.getTextos())
										gerarVocabulario(texto.getTexto());

									pagina.setVocabulario(vocabPagina.toString());
								}
								capitulo.setVocabulario(vocabCapitulo.toString());
								Platform.runLater(() -> controller.getBarraProgressoCapitulos()
										.setProgress(z / volume.getCapitulos().size()));
							}
							volume.setVocabulario(vocabVolume.toString());

							serviceManga.updateVocabularioVolume(tabela.getBase(), volume);

							if (desativar)
								break;
						}

						if (desativar)
							break;

						Platform.runLater(() -> {
							controller.getBarraProgressoVolumes().setProgress(y / tabelas.size());
							if (TaskbarProgressbar.isSupported())
								TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), y, tabelas.size(),
										Type.NORMAL);
						});
					}

				} catch (IOException e) {
					e.printStackTrace();
					AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
							"Erro ao processar a lista.");
				} finally {
					controller.limpar();
				}

				return null;
			}

			@Override
			protected void succeeded() {
				AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Aviso",
						"Mangas processadas com sucesso.");
				controller.getBarraProgressoGeral().progressProperty().unbind();
				controller.getLog().textProperty().unbind();
			}
		};
		controller.getBarraProgressoGeral().progressProperty().bind(verificaConexao.progressProperty());
		controller.getLog().textProperty().bind(verificaConexao.messageProperty());
		Thread t = new Thread(verificaConexao);
		t.start();
	}

	private String getSignificado(String kanji) {
		String resultado = "";
		switch (siteDicionario) {
		case JAPANESE_TANOSHI:
			resultado = TanoshiJapanese.processa(kanji);
			break;
		case JAPANDICT:
			resultado = JapanDict.processa(kanji);
			break;
		case JISHO:
			resultado = Jisho.processa(kanji);
			break;
		default:
		}

		return resultado;
	}

	private String getDesmembrado(String palavra) {
		String resultado = "";
		resultado = processaPalavras(desmembra.processarDesmembrar(palavra, controller.getDicionario(), Modo.B),
				Modo.B);

		if (resultado.isEmpty())
			resultado = processaPalavras(desmembra.processarDesmembrar(palavra, controller.getDicionario(), Modo.A),
					Modo.A);

		return resultado;
	}

	private String processaPalavras(List<String> palavras, Modo modo) {
		String desmembrado = "";
		for (String palavra : palavras) {
			String resultado = getSignificado(palavra);

			if (!resultado.trim().isEmpty())
				desmembrado += palavra + " - " + resultado + "; ";
			else if (modo.equals(Modo.B)) {
				resultado = processaPalavras(desmembra.processarDesmembrar(palavra, controller.getDicionario(), Modo.A),
						Modo.A);
				if (!resultado.trim().isEmpty())
					desmembrado += resultado;
			}
		}

		return desmembrado;
	}

	final private String pattern = ".*[\u4E00-\u9FAF].*";
	final private String japanese = ".*[\u3041-\u9FAF].*";
	private Tokenizer tokenizer;
	private SplitMode mode;

	private void gerarVocabulario(String frase) throws ExcessaoBd {
		for (Morpheme m : tokenizer.tokenize(mode, frase)) {
			if (m.surface().matches(pattern)) {
				if (!vocabPagina.contains(m.dictionaryForm())) {
					Vocabulario palavra = vocabularioService.select(m.surface(), m.dictionaryForm());

					if (palavra != null) {
						String vocabulario;
						if (palavra.getTraducao().substring(0, 2).matches(japanese))
							vocabulario = palavra.getTraducao() + " ";
						else
							vocabulario = m.dictionaryForm() + " - " + palavra.getTraducao() + " ";

						// Usado apenas para correção em formas em branco.
						if (palavra.getFormaBasica().isEmpty()) {
							palavra.setFormaBasica(m.dictionaryForm());
							palavra.setLeitura(m.readingForm());
							vocabularioService.update(palavra);
						}

						vocabPagina.add(vocabulario);
						vocabCapitulo.add(vocabulario);
						vocabVolume.add(vocabulario);
					} else {
						Revisar revisar = serviceRevisar.select(m.surface(), m.dictionaryForm());
						if (revisar == null) {
							revisar = new Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), false, false, true);

							revisar.setIngles(getSignificado(revisar.getVocabulario()));

							if (revisar.getIngles().isEmpty())
								revisar.setIngles(getSignificado(revisar.getFormaBasica()));

							if (revisar.getIngles().isEmpty())
								revisar.setIngles(getSignificado(getDesmembrado(revisar.getVocabulario())));

							if (!revisar.getIngles().isEmpty()) {
								try {
									revisar.setTraducao(ScriptGoogle.translate(Language.ENGLISH.getSigla(),
											Language.PORTUGUESE.getSigla(), revisar.getIngles(), contaGoogle));
								} catch (IOException e) {
									e.printStackTrace();
								}
							}

							serviceRevisar.insert(revisar);
						} else if (!revisar.isManga()) {
							revisar.setManga(true);
							serviceRevisar.update(revisar);
						}

						String vocabulario = m.dictionaryForm() + " - " + revisar.getTraducao() + "¹ ";
						vocabPagina.add(vocabulario);
						vocabCapitulo.add(vocabulario);
						vocabVolume.add(vocabulario);
					}
				}
			}
		}
	}

}
