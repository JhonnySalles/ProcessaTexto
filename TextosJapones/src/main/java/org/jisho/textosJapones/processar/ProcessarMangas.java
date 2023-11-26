package org.jisho.textosJapones.processar;

import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.controller.mangas.MangasProcessarController;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.enums.Site;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessarMangas {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessarMangas.class);

    private final VocabularioJaponesServices vocabularioJaponesService = new VocabularioJaponesServices();
    private final VocabularioInglesServices vocabularioInglesService = new VocabularioInglesServices();
    private final RevisarJaponesServices serviceJaponesRevisar = new RevisarJaponesServices();
    private final RevisarInglesServices serviceInglesRevisar = new RevisarInglesServices();

    private final MangasProcessarController controller;
    private final MangaServices serviceManga = new MangaServices();
    private final ProcessarPalavra desmembra = new ProcessarPalavra();
    private Site siteDicionario;
    private Boolean desativar = false;
    private Integer traducoes = 0;
    private final Set<MangaVocabulario> vocabHistorico = new HashSet<>();
    private final Set<String> validaHistorico = new HashSet<>();

    public ProcessarMangas(MangasProcessarController controller) {
        this.controller = controller;
    }

    public void setDesativar(Boolean desativar) {
        this.desativar = desativar;
    }

    private Integer V, C, Progress, Size;
    private final Set<MangaVocabulario> vocabVolume = new HashSet<>();
    private final Set<MangaVocabulario> vocabCapitulo = new HashSet<>();
    private final Set<MangaVocabulario> vocabPagina = new HashSet<>();
    private final Set<String> vocabValida = new HashSet<>();

    private final DoubleProperty propCapitulo = new SimpleDoubleProperty(.0);
    private final DoubleProperty propVolume = new SimpleDoubleProperty(.0);
    private final DoubleProperty propTabela = new SimpleDoubleProperty(.0);

    private Boolean error;

    public void processarTabelasJapones(List<MangaTabela> tabelas) {
        error = false;
        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo().setText("Mangas - Processar vocabulário");
        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        Task<Void> processarTabela = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                try {
                    try (Dictionary dict = new DictionaryFactory().create("",
                            SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer
                                    .getPathSettings(MenuPrincipalController.getController().getDicionario()))))) {
                        tokenizer = dict.create();
                        mode = SudachiTokenizer.getModo(MenuPrincipalController.getController().getModo());
                        siteDicionario = MenuPrincipalController.getController().getSite();

                        validaHistorico.clear();
                        propTabela.set(.0);
                        propVolume.set(.0);
                        propCapitulo.set(.0);

                        updateMessage("Calculando tempo necessário...");
                        Progress = 0;
                        Size = 0;
                        tabelas.stream().filter(t -> t.isProcessar()).collect(Collectors.toList())
                                .forEach(tabela -> tabela.getVolumes().stream().filter(v -> v.isProcessar())
                                        .collect(Collectors.toList())
                                        .forEach(volume -> volume.getCapitulos().stream().filter(c -> c.isProcessar())
                                                .collect(Collectors.toList())
                                                .forEach(capitulo -> capitulo.getPaginas().stream()
                                                        .filter(p -> p.isProcessar()).collect(Collectors.toList())
                                                        .forEach(pagina -> pagina.getTextos().forEach(texto -> {
                                                            if (texto.isProcessar())
                                                                Size++;
                                                        })))));
                        updateMessage("Iniciando...");
                        desativar = false;
                        for (MangaTabela tabela : tabelas) {
                            if (!tabela.isProcessar())
                                continue;

                            V = 0;
                            for (MangaVolume volume : tabela.getVolumes()) {
                                V++;

                                if (!volume.isProcessar() || !volume.getLingua().equals(Language.JAPANESE)) {
                                    propVolume.set((double) V / tabela.getVolumes().size());

                                    if (!volume.isProcessar())
                                        updateMessage("IGNORADO - Manga: " + volume.getManga());
                                    else
                                        updateMessage("IGNORADO - Linguagem: " + volume.getLingua());

                                    continue;
                                }

                                vocabVolume.clear();
                                C = 0;
                                for (MangaCapitulo capitulo : volume.getCapitulos()) {
                                    C++;

                                    if (!capitulo.isProcessar()) {
                                        updateMessage("IGNORADO - Manga: " + volume.getManga() + " - Capitulo "
                                                + capitulo.getCapitulo());
                                        propCapitulo.set((double) C / volume.getCapitulos().size());
                                        continue;
                                    }

                                    vocabCapitulo.clear();
                                    int p = 0;
                                    for (MangaPagina pagina : capitulo.getPaginas()) {
                                        p++;
                                        updateProgress(p, capitulo.getPaginas().size());

                                        if (!pagina.isProcessar()) {
                                            updateMessage("IGNORADO - Manga: " + volume.getManga() + " - Capitulo: "
                                                    + capitulo.getCapitulo() + " - Página: " + pagina.getNomePagina());
                                            continue;
                                        }

                                        updateMessage("Processando " + V + " de " + tabela.getVolumes().size()
                                                + " volumes." + " Manga: " + volume.getManga() + " - Capitulo: "
                                                + capitulo.getCapitulo() + " - Página: " + pagina.getNomePagina());

                                        vocabPagina.clear();
                                        vocabValida.clear();
                                        for (MangaTexto texto : pagina.getTextos())
                                            gerarVocabulario(texto.getTexto());

                                        pagina.setVocabularios(vocabPagina);
                                        pagina.setProcessado(true);
                                        serviceManga.updateVocabularioPagina(tabela.getBase(), pagina);

                                        if (desativar)
                                            break;
                                    }
                                    capitulo.setVocabularios(vocabCapitulo);
                                    capitulo.setProcessado(true);
                                    serviceManga.updateVocabularioCapitulo(tabela.getBase(), capitulo);
                                    propCapitulo.set((double) C / volume.getCapitulos().size());

                                    if (desativar)
                                        break;
                                }
                                volume.setVocabularios(vocabVolume);
                                volume.setProcessado(true);
                                serviceManga.updateVocabularioVolume(tabela.getBase(), volume);
                                propVolume.set((double) V / tabela.getVolumes().size());

                                if (desativar) {
                                    updateMessage("Revertendo a ultima alteração do Manga: " + volume.getManga()
                                            + " - Volume: " + volume.getVolume().toString());
                                    serviceManga.updateCancel(tabela.getBase(), volume);
                                    break;
                                }
                            }

                            if (desativar)
                                break;
                        }

                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                        error = false;
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    error = false;
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.failed();
                if (error)
                    AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(), controller.getRoot(), null,
                            "Erro", "Erro ao processar a lista.");
                else if (!desativar)
                    AlertasPopup.AvisoModal(controller.getControllerPai().getStackPane(), controller.getRoot(), null,
                            "Aviso", "Mangas processadas com sucesso.");

                progress.getBarraProgresso().progressProperty().unbind();
                controller.getBarraProgressoVolumes().progressProperty().unbind();
                controller.getBarraProgressoCapitulos().progressProperty().unbind();
                controller.getBarraProgressoPaginas().progressProperty().unbind();
                progress.getLog().textProperty().unbind();
                controller.habilitar();

                MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
            }

            @Override
            protected void failed() {
                super.failed();
                LOGGER.warn("Erro na thread de processamento da tabela: " + super.getMessage());
                System.out.print("Erro na thread de processamento da tabela: " + super.getMessage());
            }
        };
        progress.getBarraProgresso().progressProperty().bind(propTabela);
        controller.getBarraProgressoVolumes().progressProperty().bind(propVolume);
        controller.getBarraProgressoCapitulos().progressProperty().bind(propCapitulo);
        controller.getBarraProgressoPaginas().progressProperty().bind(processarTabela.progressProperty());
        progress.getLog().textProperty().bind(processarTabela.messageProperty());
        Thread t = new Thread(processarTabela);
        t.start();
    }

    private String getSignificado(String kanji) {
        if (kanji.trim().isEmpty())
            return "";

        Platform.runLater(
                () -> MenuPrincipalController.getController().getLblLog().setText(kanji + " : Obtendo significado."));
        String resultado = "";
        switch (siteDicionario) {
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
        Platform.runLater(() -> MenuPrincipalController.getController().getLblLog()
                .setText(palavra + " : Desmembrando a palavra."));
        resultado = processaPalavras(
                desmembra.processarDesmembrar(palavra, MenuPrincipalController.getController().getDicionario(), Modo.B),
                Modo.B);

        if (resultado.isEmpty())
            resultado = processaPalavras(desmembra.processarDesmembrar(palavra,
                    MenuPrincipalController.getController().getDicionario(), Modo.A), Modo.A);

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
                    resultado = processaPalavras(desmembra.processarDesmembrar(palavra,
                            MenuPrincipalController.getController().getDicionario(), Modo.A), Modo.A);
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

    final private String pattern = ".*[\u4E00-\u9FAF].*";
    final private String japanese = ".*[\u3041-\u9FAF].*";
    private Tokenizer tokenizer;
    private SplitMode mode;

    private void gerarVocabulario(String frase) throws ExcessaoBd {
        for (Morpheme m : tokenizer.tokenize(mode, frase)) {
            if (m.surface().matches(pattern)) {
                if (validaHistorico.contains(m.dictionaryForm())) {
                    MangaVocabulario vocabulario = vocabHistorico.stream()
                            .filter(vocab -> m.dictionaryForm().equalsIgnoreCase(vocab.getPalavra())).findFirst()
                            .orElse(null);
                    if (vocabulario != null) {
                        vocabPagina.add(vocabulario);
                        vocabCapitulo.add(vocabulario);
                        vocabVolume.add(vocabulario);
                        continue;
                    }
                }

                if (!vocabValida.contains(m.dictionaryForm())) {
                    Vocabulario palavra = vocabularioJaponesService.select(m.surface(), m.dictionaryForm());

                    if (palavra != null) {
                        MangaVocabulario vocabulario = null;
                        if (palavra.getPortugues().substring(0, 2).matches(japanese))
                            vocabulario = new MangaVocabulario(m.dictionaryForm(), palavra.getPortugues(),
                                    palavra.getIngles(), m.readingForm());
                        else
                            vocabulario = new MangaVocabulario(m.dictionaryForm(), palavra.getPortugues(),
                                    palavra.getIngles(), m.readingForm());

                        // Usado apenas para correção em formas em branco.
                        if (palavra.getFormaBasica().isEmpty()) {
                            palavra.setFormaBasica(m.dictionaryForm());
                            palavra.setLeitura(m.readingForm());
                            vocabularioJaponesService.update(palavra);
                        }

                        validaHistorico.add(m.dictionaryForm());
                        vocabHistorico.add(vocabulario);

                        vocabValida.add(m.dictionaryForm());
                        vocabPagina.add(vocabulario);
                        vocabCapitulo.add(vocabulario);
                        vocabVolume.add(vocabulario);
                    } else {
                        Revisar revisar = serviceJaponesRevisar.select(m.surface(), m.dictionaryForm());
                        if (revisar == null) {
                            revisar = new Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), "", false, false, true, false);
                            Platform.runLater(() -> MenuPrincipalController.getController().getLblLog()
                                    .setText(m.surface() + " : Vocabulário novo."));
                            revisar.setIngles(getSignificado(revisar.getVocabulario()));

                            if (revisar.getIngles().isEmpty())
                                revisar.setIngles(getSignificado(revisar.getFormaBasica()));

                            if (revisar.getIngles().isEmpty())
                                revisar.setIngles(getSignificado(getDesmembrado(revisar.getVocabulario())));

                            if (!revisar.getIngles().isEmpty()) {
                                try {
                                    traducoes++;

                                    if (traducoes > 3000) {
                                        traducoes = 0;
                                        MenuPrincipalController.getController().setContaGoogle(
                                                Util.next(MenuPrincipalController.getController().getContaGoogle()));
                                    }

                                    Platform.runLater(() -> MenuPrincipalController.getController().getLblLog()
                                            .setText(m.surface() + " : Obtendo tradução."));
                                    revisar.setPortugues(
                                            Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(),
                                                    Language.PORTUGUESE.getSigla(), revisar.getIngles(),
                                                    MenuPrincipalController.getController().getContaGoogle())));
                                } catch (IOException e) {
                                    
                                    LOGGER.error(e.getMessage(), e);
                                }
                            }
                            serviceJaponesRevisar.insert(revisar);
                            Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(""));
                        } else {
                            if (!revisar.isManga()) {
                                revisar.setManga(true);
                                serviceJaponesRevisar.setIsManga(revisar);
                            }

                            serviceJaponesRevisar.incrementaVezesAparece(revisar.getVocabulario());
                        }

                        MangaVocabulario vocabulario = new MangaVocabulario(m.dictionaryForm(), revisar.getPortugues(),
                                revisar.getIngles(), m.readingForm(), false);

                        validaHistorico.add(m.dictionaryForm());
                        vocabHistorico.add(vocabulario);

                        vocabValida.add(m.dictionaryForm());
                        vocabPagina.add(vocabulario);
                        vocabCapitulo.add(vocabulario);
                        vocabVolume.add(vocabulario);
                    }
                }
            }
        }
        Progress++;
        propTabela.set((double) Progress / Size);
        Platform.runLater(() -> {
            if (TaskbarProgressbar.isSupported())
                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, Type.NORMAL);
        });
    }

    private final Set<String> palavraValida = new HashSet<>();

    public void processarTabelasIngles(List<MangaTabela> tabelas) {
        error = false;
        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo().setText("Mangas - Processar vocabulário");
        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        Task<Void> processarTabela = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                try {

                    propTabela.set(.0);
                    propVolume.set(.0);
                    propCapitulo.set(.0);
                    validaHistorico.clear();

                    updateMessage("Calculando tempo necessário...");
                    Progress = 0;
                    Size = 0;
                    tabelas.stream().filter(t -> t.isProcessar()).collect(Collectors.toList())
                            .forEach(tabela -> tabela.getVolumes().stream().filter(v -> v.isProcessar())
                                    .collect(Collectors.toList())
                                    .forEach(volume -> volume.getCapitulos().stream().filter(c -> c.isProcessar())
                                            .collect(Collectors.toList())
                                            .forEach(capitulo -> capitulo.getPaginas().stream()
                                                    .filter(p -> p.isProcessar()).collect(Collectors.toList())
                                                    .forEach(pagina -> pagina.getTextos().forEach(texto -> {
                                                        if (texto.isProcessar())
                                                            Size++;
                                                    })))));
                    updateMessage("Iniciando...");
                    desativar = false;
                    for (MangaTabela tabela : tabelas) {
                        if (!tabela.isProcessar())
                            continue;

                        palavraValida.clear();

                        V = 0;
                        for (MangaVolume volume : tabela.getVolumes()) {
                            V++;

                            if (!volume.isProcessar() || !volume.getLingua().equals(Language.ENGLISH)) {
                                propVolume.set((double) V / tabela.getVolumes().size());

                                if (!volume.isProcessar())
                                    updateMessage("IGNORADO - Manga: " + volume.getManga());
                                else
                                    updateMessage("IGNORADO - Linguagem: " + volume.getLingua());

                                continue;
                            }

                            vocabVolume.clear();
                            C = 0;
                            for (MangaCapitulo capitulo : volume.getCapitulos()) {
                                C++;

                                if (!capitulo.isProcessar()) {
                                    updateMessage("IGNORADO - Manga: " + volume.getManga() + " - Capitulo "
                                            + capitulo.getCapitulo());
                                    propCapitulo.set((double) C / volume.getCapitulos().size());
                                    continue;
                                }

                                vocabCapitulo.clear();
                                int p = 0;
                                for (MangaPagina pagina : capitulo.getPaginas()) {
                                    p++;
                                    updateProgress(p, capitulo.getPaginas().size());

                                    if (!pagina.isProcessar()) {
                                        updateMessage("IGNORADO - Manga: " + volume.getManga() + " - Capitulo: "
                                                + capitulo.getCapitulo() + " - Página: " + pagina.getNomePagina());
                                        continue;
                                    }

                                    updateMessage("Processando " + V + " de " + tabela.getVolumes().size() + " volumes."
                                            + " Manga: " + volume.getManga() + " - Capitulo: " + capitulo.getCapitulo()
                                            + " - Página: " + pagina.getNomePagina());

                                    vocabPagina.clear();
                                    vocabValida.clear();
                                    for (MangaTexto texto : pagina.getTextos()) {
                                        if (texto.getTexto() != null && !texto.getTexto().isEmpty()) {
                                            Set<String> palavras = Stream.of(texto.getTexto().split(" "))
                                                    .map(txt -> txt.replaceAll("\\W", ""))
                                                    .filter(txt -> !txt.trim().contains(" ") && !txt.isEmpty())
                                                    .collect(Collectors.toSet());

                                            for (String palavra : palavras) {

                                                if (palavra.matches("[\\d|\\W]"))
                                                    continue;

                                                if (validaHistorico.contains(palavra)) {
                                                    MangaVocabulario vocabulario = vocabHistorico.stream().filter(
                                                                    vocab -> palavra.equalsIgnoreCase(vocab.getPalavra()))
                                                            .findFirst().orElse(null);
                                                    if (vocabulario != null) {
                                                        vocabPagina.add(vocabulario);
                                                        vocabCapitulo.add(vocabulario);
                                                        vocabVolume.add(vocabulario);
                                                        continue;
                                                    }
                                                }

                                                if (!vocabValida.contains(palavra)) {
                                                    Vocabulario salvo = vocabularioInglesService.select(palavra);

                                                    if (salvo != null) {
                                                        MangaVocabulario vocabulario = new MangaVocabulario(palavra,
                                                                salvo.getPortugues());

                                                        validaHistorico.add(palavra);
                                                        vocabHistorico.add(vocabulario);

                                                        vocabValida.add(palavra);
                                                        vocabPagina.add(vocabulario);
                                                        vocabCapitulo.add(vocabulario);
                                                        vocabVolume.add(vocabulario);
                                                    } else {

                                                        if (!palavraValida.contains(palavra.toLowerCase())) {
                                                            String valido = serviceInglesRevisar.isValido(palavra);

                                                            if (valido == null)
                                                                continue;

                                                            palavraValida.add(valido);
                                                        }

                                                        Revisar revisar = serviceInglesRevisar.select(palavra);
                                                        if (revisar == null) {
                                                            revisar = new Revisar(palavra, false, false, true, false);
                                                            Platform.runLater(() -> MenuPrincipalController
                                                                    .getController().getLblLog()
                                                                    .setText(palavra + " : Vocabulário novo."));

                                                            if (!revisar.getVocabulario().isEmpty()) {
                                                                try {
                                                                    traducoes++;

                                                                    if (traducoes > 3000) {
                                                                        traducoes = 0;
                                                                        MenuPrincipalController.getController()
                                                                                .setContaGoogle(Util
                                                                                        .next(MenuPrincipalController
                                                                                                .getController()
                                                                                                .getContaGoogle()));
                                                                    }

                                                                    Platform.runLater(() -> MenuPrincipalController
                                                                            .getController().getLblLog()
                                                                            .setText(palavra + " : Obtendo tradução."));
                                                                    revisar.setPortugues(Util.normalize(ScriptGoogle
                                                                            .translate(Language.ENGLISH.getSigla(),
                                                                                    Language.PORTUGUESE.getSigla(),
                                                                                    revisar.getVocabulario(),
                                                                                    MenuPrincipalController
                                                                                            .getController()
                                                                                            .getContaGoogle())));
                                                                } catch (IOException e) {
                                                                    
                                                                    LOGGER.error(e.getMessage(), e);
                                                                }
                                                            }

                                                            serviceInglesRevisar.insert(revisar);
                                                            Platform.runLater(() -> MenuPrincipalController
                                                                    .getController().getLblLog().setText(""));
                                                        } else {
                                                            if (!revisar.isManga()) {
                                                                revisar.setManga(true);
                                                                serviceInglesRevisar.setIsManga(revisar);
                                                            }

                                                            serviceInglesRevisar
                                                                    .incrementaVezesAparece(revisar.getVocabulario());
                                                        }

                                                        MangaVocabulario vocabulario = new MangaVocabulario(palavra,
                                                                revisar.getPortugues(), "", "", false);

                                                        validaHistorico.add(palavra);
                                                        vocabHistorico.add(vocabulario);

                                                        vocabValida.add(palavra);
                                                        vocabPagina.add(vocabulario);
                                                        vocabCapitulo.add(vocabulario);
                                                        vocabVolume.add(vocabulario);
                                                    }
                                                }

                                            }

                                        }

                                        Progress++;
                                        propTabela.set((double) Progress / Size);
                                        Platform.runLater(() -> {
                                            if (TaskbarProgressbar.isSupported())
                                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress, Size, Type.NORMAL);
                                        });
                                    }

                                    pagina.setVocabularios(vocabPagina);
                                    pagina.setProcessado(true);
                                    serviceManga.updateVocabularioPagina(tabela.getBase(), pagina);

                                    if (desativar)
                                        break;
                                }
                                capitulo.setVocabularios(vocabCapitulo);
                                capitulo.setProcessado(true);
                                serviceManga.updateVocabularioCapitulo(tabela.getBase(), capitulo);
                                propCapitulo.set((double) C / volume.getCapitulos().size());

                                if (desativar)
                                    break;
                            }
                            volume.setVocabularios(vocabVolume);
                            volume.setProcessado(true);
                            serviceManga.updateVocabularioVolume(tabela.getBase(), volume);
                            propVolume.set((double) V / tabela.getVolumes().size());

                            if (desativar) {
                                updateMessage("Revertendo a ultima alteração do Manga: " + volume.getManga()
                                        + " - Volume: " + volume.getVolume().toString());
                                serviceManga.updateCancel(tabela.getBase(), volume);
                                break;
                            }
                        }

                        if (desativar)
                            break;
                    }

                } catch (Exception e) {
                    
                    LOGGER.error(e.getMessage(), e);
                    error = false;
                }

                return null;
            }

            @Override
            protected void succeeded() {
                super.failed();
                if (error)
                    AlertasPopup.ErroModal(controller.getControllerPai().getStackPane(), controller.getRoot(), null,
                            "Erro", "Erro ao processar a lista.");
                else if (!desativar)
                    AlertasPopup.AvisoModal(controller.getControllerPai().getStackPane(), controller.getRoot(), null,
                            "Aviso", "Mangas processadas com sucesso.");

                progress.getBarraProgresso().progressProperty().unbind();
                controller.getBarraProgressoVolumes().progressProperty().unbind();
                controller.getBarraProgressoCapitulos().progressProperty().unbind();
                controller.getBarraProgressoPaginas().progressProperty().unbind();
                progress.getLog().textProperty().unbind();
                controller.habilitar();

                MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
            }

            @Override
            protected void failed() {
                super.failed();
                LOGGER.warn("Erro na thread de processamento da tabela: " + super.getMessage());
                System.out.print("Erro na thread de processamento da tabela: " + super.getMessage());
            }
        };
        progress.getBarraProgresso().progressProperty().bind(propTabela);
        controller.getBarraProgressoVolumes().progressProperty().bind(propVolume);
        controller.getBarraProgressoCapitulos().progressProperty().bind(propCapitulo);
        controller.getBarraProgressoPaginas().progressProperty().bind(processarTabela.progressProperty());
        progress.getLog().textProperty().bind(processarTabela.messageProperty());
        Thread t = new Thread(processarTabela);
        t.start();
    }

}
