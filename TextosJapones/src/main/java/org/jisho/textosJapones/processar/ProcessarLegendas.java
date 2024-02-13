package org.jisho.textosJapones.processar;

import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.controller.legendas.LegendasImportarController;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarInglesServices;
import org.jisho.textosJapones.model.services.RevisarJaponesServices;
import org.jisho.textosJapones.model.services.VocabularioInglesServices;
import org.jisho.textosJapones.model.services.VocabularioJaponesServices;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.jisho.textosJapones.tokenizers.SudachiTokenizer;
import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessarLegendas {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessarLegendas.class);

    private final VocabularioJaponesServices vocabularioJapones = new VocabularioJaponesServices();
    private final RevisarJaponesServices revisarJapones = new RevisarJaponesServices();

    private final VocabularioInglesServices vocabularioIngles = new VocabularioInglesServices();
    private final RevisarInglesServices revisarIngles = new RevisarInglesServices();

    private final ProcessarPalavra desmembra = new ProcessarPalavra();
    private final LegendasImportarController controller;

    public ProcessarLegendas(LegendasImportarController controller) {
        this.controller = controller;
    }

    private Boolean error;
    public void processarLegendas(List<String> frases) {
        error = false;
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
                    LOGGER.error(e.getMessage(), e);
                    error = true;
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                if (error)
                    AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(),
                            controller.getControllerPai().getRoot(), null, "Erro", "Erro ao processar a lista.");
                else
                    AlertasPopup.AvisoModal(controller.getControllerPai().getStackPane(),
                            controller.getControllerPai().getRoot(), null, "Aviso", "Lista processada com sucesso.");

                progress.getBarraProgresso().progressProperty().unbind();
                progress.getLog().textProperty().unbind();
            }

            @Override
            protected void failed() {
                super.failed();
                LOGGER.warn("Erro na thread de processamento do vocabulário: " + super.getMessage());
                System.out.print("Erro na thread de processamento do vocabulário: " + super.getMessage());
            }
        };
        progress.getBarraProgresso().progressProperty().bind(processarVocabulario.progressProperty());
        progress.getLog().textProperty().bind(processarVocabulario.messageProperty());
        Thread t = new Thread(processarVocabulario);
        t.start();
    }

    public String processarJapones(Dicionario dicionario, Modo modo, String frase) {
        existe.clear();
        vocabulario.clear();
        String vocab = "";
        try (Dictionary dict = new DictionaryFactory().create("",
                SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer.getPathSettings(dicionario))))) {
            tokenizer = dict.create();
            mode = SudachiTokenizer.getModo(modo);

            vocab = gerarVocabularioJapones(frase);

            if (vocab.isEmpty() && mode.equals(SudachiTokenizer.getModo(Modo.C))) {
                mode = SudachiTokenizer.getModo(Modo.B);
                vocab = gerarVocabularioJapones(frase);
            }

            if (vocab.isEmpty() && mode.equals(SudachiTokenizer.getModo(Modo.B))) {
                mode = SudachiTokenizer.getModo(Modo.C);
                vocab = gerarVocabularioJapones(frase);
            }

        } catch (IOException | ExcessaoBd e) {
            vocab = "";
            
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(),
                    controller.getControllerPai().getRoot(), null, "Erro", "Erro ao processar a lista.");
        }

        return vocab.trim();
    }

    final private String pattern = ".*[\u4E00-\u9FAF].*";
    final private String japanese = ".*[\u3041-\u9FAF].*";
    private Tokenizer tokenizer;
    private SplitMode mode;

    private void processar(String frase) throws ExcessaoBd {
        for (Morpheme m : tokenizer.tokenize(mode, frase)) {
            if (m.surface().matches(pattern)) {
                Vocabulario palavra = vocabularioJapones.select(m.surface(), m.dictionaryForm());

                if (palavra == null) {
                    Revisar revisar = new Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), "", false, true, false, false);
                    revisarJapones.insert(revisar);
                }
            }
        }
    }

    private final Boolean usarRevisar = true;
    private final Set<Vocabulario> vocabHistorico = new HashSet<>();
    private final Set<String> validaHistorico = new HashSet<>();

    public Set<String> vocabulario = new HashSet<>();
    private final Set<String> existe = new HashSet<>();

    public void clearVocabulary() {
        vocabHistorico.clear();
        validaHistorico.clear();
        vocabulario.clear();
        existe.clear();
    }

    private String gerarVocabularioJapones(String frase) throws ExcessaoBd {
        String vocabularios = "";
        for (Morpheme m : tokenizer.tokenize(mode, frase)) {
            if (m.surface().matches(pattern)) {
                if (!existe.contains(m.dictionaryForm()) && !vocabularioJapones.existeExclusao(m.surface(), m.dictionaryForm())) {
                    existe.add(m.dictionaryForm());

                    Vocabulario palavra = null;
                    if (validaHistorico.contains(m.dictionaryForm()))
                        palavra = vocabHistorico.stream()
                                .filter(vocab -> m.dictionaryForm().equalsIgnoreCase(vocab.getVocabulario()))
                                .findFirst().orElse(null);

                    if (palavra != null)
                        vocabularios += m.dictionaryForm() + " - " + palavra.getPortugues() + " ";
                    else {
                        palavra = vocabularioJapones.select(m.surface(), m.dictionaryForm());
                        if (palavra != null) {
                            if (palavra.getPortugues().substring(0, 2).matches(japanese))
                                vocabularios += palavra.getPortugues() + " ";
                            else
                                vocabularios += m.dictionaryForm() + " - " + palavra.getPortugues() + " ";

                            // Usado apenas para correção em formas em branco.
                            if (palavra.getFormaBasica().isEmpty()) {
                                palavra.setFormaBasica(m.dictionaryForm());
                                palavra.setLeitura(m.readingForm());
                                vocabularioJapones.update(palavra);
                            }

                            validaHistorico.add(m.dictionaryForm());
                            vocabHistorico.add(palavra);

                            vocabulario.add(palavra.getFormaBasica());
                        } else if (usarRevisar) {
                            Revisar revisar = revisarJapones.select(m.surface(), m.dictionaryForm());
                            if (revisar != null) {
                                if (!revisar.getPortugues().isEmpty() && revisar.getPortugues().substring(0, 2).matches(japanese))
                                    vocabularios += revisar.getPortugues() + "¹ ";
                                else {
                                    vocabularios += m.dictionaryForm() + " - " + revisar.getPortugues() + "¹ ";
                                    validaHistorico.add(m.dictionaryForm());
                                    vocabHistorico.add(new Vocabulario(m.dictionaryForm(), revisar.getPortugues() + "¹"));
                                }
                                vocabulario.add(m.dictionaryForm());
                            } else {
                                revisar = new Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), "", false, true, false, false);
                                revisar.setIngles(getSignificado(revisar.getVocabulario()));

                                if (revisar.getIngles().isEmpty())
                                    revisar.setIngles(getSignificado(revisar.getFormaBasica()));

                                if (revisar.getIngles().isEmpty())
                                    revisar.setIngles(getSignificado(getDesmembrado(revisar.getVocabulario())));

                                if (!revisar.getIngles().isEmpty()) {
                                    try {
                                        Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(m.surface() + " : Obtendo tradução."));
                                        revisar.setPortugues(Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(),
                                                revisar.getIngles(), MenuPrincipalController.getController().getContaGoogle())));
                                    } catch (IOException e) {
                                        LOGGER.error(e.getMessage(), e);
                                    }
                                }
                                revisarJapones.insert(revisar);

                                if (!revisar.getPortugues().isEmpty() && revisar.getPortugues().substring(0, 2).matches(japanese))
                                    vocabularios += revisar.getPortugues() + "¹ ";
                                else {
                                    vocabularios += m.dictionaryForm() + " - " + revisar.getPortugues() + "¹ ";
                                    validaHistorico.add(m.dictionaryForm());
                                    vocabHistorico.add(new Vocabulario(m.dictionaryForm(), revisar.getPortugues() + "¹"));
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

    private String getSignificado(String kanji) {
        if (kanji.trim().isEmpty())
            return "";

        Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(kanji + " : Obtendo significado."));
        String resultado = "";
        switch (MenuPrincipalController.getController().getSite()) {
            case TODOS:
                resultado = TanoshiJapanese.processa(kanji);

                if (resultado.isEmpty())
                    resultado = JapanDict.processa(kanji);

                if (resultado.isEmpty())
                    resultado = Jisho.processa(kanji);
                break;
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
        Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(palavra + " : Desmembrando a palavra."));
        resultado = processaPalavras(desmembra.processarDesmembrar(palavra, MenuPrincipalController.getController().getDicionario(), Modo.B), Modo.B);

        if (resultado.isEmpty())
            resultado = processaPalavras(desmembra.processarDesmembrar(palavra, MenuPrincipalController.getController().getDicionario(), Modo.A), Modo.A);

        return resultado;
    }

    private String processaPalavras(List<String> palavras, Modo modo) {
        String desmembrado = "";
        try {
            for (String palavra : palavras) {
                String resultado = getSignificado(palavra);

                if (!resultado.trim().isEmpty())
                    desmembrado += palavra + " - " + resultado + "; ";
                else if (modo.equals(Modo.B)) {
                    resultado = processaPalavras(desmembra.processarDesmembrar(palavra, MenuPrincipalController.getController().getDicionario(), Modo.A), Modo.A);
                    if (!resultado.trim().isEmpty())
                        desmembrado += resultado;
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            desmembrado = "";
        }

        return desmembrado;
    }

    // -------------------------------------------------------------------------------------------------
    public String processarIngles(String frase) {
        existe.clear();
        vocabulario.clear();
        String vocab = "";

        try {
            Pattern ignore = Pattern.compile("[\\d]|[^a-zA-Z0-9_'çãáàéèíìúù]");
            if (frase != null && !frase.isEmpty()) {
                frase = frase.toLowerCase();
                Set<String> palavras = Stream.of(frase.split(" "))
                        .filter(txt -> !txt.trim().contains(" ") && !txt.isEmpty())
                        .collect(Collectors.toSet());

                for (String palavra : palavras) {
                    if (ignore.matcher(palavra).find())
                        continue;
                    vocab += gerarVocabularioIngles(palavra);
                }
            }
        } catch (ExcessaoBd e) {
            vocab = "";

            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(), controller.getControllerPai().getRoot(), null, "Erro", "Erro ao processar a lista.");
        }

        return vocab.trim();
    }

    private String gerarVocabularioIngles(String texto) throws ExcessaoBd {
        String vocab = "";

        if (!existe.contains(texto) && !vocabularioIngles.existeExclusao(texto)) {
            existe.add(texto);

            Vocabulario palavra = null;
            if (validaHistorico.contains(texto))
                palavra = vocabHistorico.stream()
                        .filter(vc -> texto.equalsIgnoreCase(vc.getVocabulario()))
                        .findFirst().orElse(null);

            if (palavra != null)
                vocab = "• " + texto.substring(0, 1).toUpperCase() + texto.substring(1) + " - " + palavra.getPortugues() + " ";
            else {
                palavra = vocabularioIngles.select(texto);
                if (palavra != null) {
                    vocab = "• " + texto.substring(0, 1).toUpperCase() + texto.substring(1) + " - " + palavra.getPortugues() + " ";

                    validaHistorico.add(texto);
                    vocabHistorico.add(palavra);
                    vocabulario.add(texto);
                } else if (usarRevisar) {
                    Revisar revisar = revisarIngles.select(texto);
                    if (revisar != null) {
                        vocab = "• " + texto.substring(0, 1).toUpperCase() + texto.substring(1) + " - " + revisar.getPortugues() + "¹ ";
                        validaHistorico.add(texto);
                        vocabHistorico.add(new Vocabulario(texto, revisar.getPortugues() + "¹"));
                        vocabulario.add(texto);
                    }  else {
                        revisar = new Revisar(texto, "", "", "", false, true, false, false);

                        try {
                            Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(texto + " : Obtendo tradução."));
                            revisar.setPortugues(Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(),
                                    texto, MenuPrincipalController.getController().getContaGoogle())));
                        } catch (IOException e) {
                            LOGGER.error(e.getMessage(), e);
                        }

                        revisarIngles.insert(revisar);

                        vocab = "• " + texto.substring(0, 1).toUpperCase() + texto.substring(1) + " - " + revisar.getPortugues() + "¹ ";
                        validaHistorico.add(texto);
                        vocabHistorico.add(new Vocabulario(texto, revisar.getPortugues() + "¹"));
                        vocabulario.add(texto);
                    }
                }
            }
        }

        return vocab;
    }

}
