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
import javafx.scene.image.Image;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.controller.mangas.MangasProcessarController;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.entities.novelextractor.*;
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

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessarNovel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessarNovel.class);

    private final VocabularioJaponesServices vocabularioJaponesService = new VocabularioJaponesServices();
    private final VocabularioInglesServices vocabularioInglesService = new VocabularioInglesServices();
    private final RevisarJaponesServices serviceJaponesRevisar = new RevisarJaponesServices();
    private final RevisarInglesServices serviceInglesRevisar = new RevisarInglesServices();

    private final MangasProcessarController controller;
    private final NovelServices serviceNovel = new NovelServices();
    private final ProcessarPalavra desmembra = new ProcessarPalavra();
    private Site siteDicionario;
    private Boolean desativar = false;
    private Integer traducoes = 0;
    private final Set<NovelVocabulario> vocabHistorico = new HashSet<>();
    private final Set<String> validaHistorico = new HashSet<>();

    public ProcessarNovel(MangasProcessarController controller) {
        this.controller = controller;
    }

    public void setDesativar(Boolean desativar) {
        this.desativar = desativar;
    }

    private Integer V, C, Progress, Size;
    private final Set<NovelVocabulario> vocabVolume = new HashSet<>();
    private final Set<NovelVocabulario> vocabCapitulo = new HashSet<>();
    private final Set<String> vocabValida = new HashSet<>();

    private final DoubleProperty propTexto = new SimpleDoubleProperty(.0);
    private final DoubleProperty propCapitulo = new SimpleDoubleProperty(.0);
    private final DoubleProperty propVolume = new SimpleDoubleProperty(.0);
    private final DoubleProperty propTabela = new SimpleDoubleProperty(.0);

    private Boolean error;
    final private String japanese = "[\u3041-\u9FAF]";

    private String getTabela(String texto) {
        String tabela = "temp";
        String nome = "";
        if (texto.matches(japanese)) {
            List<Morpheme> m = tokenizer.tokenize(mode, texto);
            if (!m.isEmpty())
                nome = m.get(0).readingForm().substring(0,1);

            switch (nome) {
                case "あ" :
                case "か" :
                case "さ" :
                case "た" :
                case "な" :
                case "は" :
                case "ま" :
                case "や" :
                case "ら" :
                case "わ" :
                    tabela = "a";
                    break;

                case "え" :
                case "け" :
                case "せ" :
                case "て" :
                case "ね" :
                case "へ" :
                case "め" :
                case "れ" :
                    tabela = "e";
                    break;

                case "い" :
                case "き" :
                case "し" :
                case "ち" :
                case "に" :
                case "ひ" :
                case "み" :
                case "り" :
                    tabela = "i";
                    break;

                case "お" :
                case "こ" :
                case "そ" :
                case "と" :
                case "の" :
                case "ほ" :
                case "も" :
                case "よ" :
                case "ろ" :
                case "ん" :
                    tabela = "o";
                    break;

                case "う" :
                case "く" :
                case "す" :
                case "つ" :
                case "ぬ" :
                case "ふ" :
                case "む" :
                case "ゆ" :
                case "る" :
                case "を" :
                    tabela = "u";
                    break;
            }
        } else
            tabela = nome.substring(0, 1);

        return tabela;
    }

    public void processarArquivos(String tabela, Language linguagem, File caminho) {
        error = false;
        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo().setText("Novels - Processar arquivos");
        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        Task<Void> processarArquivos = new Task<Void>() {

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
                        propVolume.set(.0);
                        propCapitulo.set(.0);
                        propTexto.set(.0);

                        updateMessage("Calculando tempo necessário...");
                        Progress = 0;
                        Size = 0;

                        List<File> arquivos = new ArrayList<>();

                        for (File arquivo : caminho.listFiles())
                            if (arquivo.getName().substring(arquivo.getName().lastIndexOf('.') + 1).equalsIgnoreCase("txt"))
                                arquivos.add(arquivo);

                        desativar = false;
                        for (File arquivo : arquivos) {
                            updateMessage("Importando texto do arquivo " + arquivo.getName() + "...");
                            String arq = arquivo.getName().substring(0, arquivo.getName().lastIndexOf("."));
                            String nome;
                            if (arq.toLowerCase().contains("volume"))
                                nome = arq.substring(0, arq.toLowerCase().lastIndexOf("volume"));
                            else if (arq.toLowerCase().contains("vol."))
                                nome = arq.substring(0, arq.toLowerCase().lastIndexOf("vol."));
                            else
                                nome = arq.substring(0, arq.lastIndexOf("."));

                            String titulo = "";
                            if (nome.matches("[a-zA-Z\\d]"))
                                titulo = nome;

                            Integer volume = 0;
                            if (arq.toLowerCase().contains("volume"))
                                volume = Integer.valueOf(arq.substring(arq.toLowerCase().lastIndexOf("volume") + 6).trim());
                            else if (arq.toLowerCase().contains("vol."))
                                volume = Integer.valueOf(arq.substring(arq.toLowerCase().lastIndexOf("vol.") + 4).trim());

                            NovelVolume novel = new NovelVolume(null, nome, "", titulo, "", arquivo.getName(), "", volume, linguagem, false);

                            if (new File(nome + ".jpg").exists()) {
                                Image imagem = new Image(new FileInputStream(nome + ".jpg"));
                                novel.setCapa(new NovelCapa(null, novel.getNovel(), novel.getVolume(), novel.getLingua(), imagem));
                            }

                            ArrayList<NovelTexto> textos = new ArrayList<>();

                            Boolean index = true;
                            HashMap<Integer, String> indices = new HashMap<>();

                            FileReader fr = new FileReader(arquivo);
                            try (BufferedReader br = new BufferedReader(fr)) {
                                Integer seq = 0;
                                String line;
                                while ((line = br.readLine()) != null) {
                                    if (line.trim().isEmpty())
                                        continue;
                                    seq++;
                                    textos.add(new NovelTexto(null, line, seq));

                                    if (index && line.contains("*")) {
                                        indices.put(seq, line.replace("*", "").trim());
                                        index = line.contains("Índice:") || line.contains("*");
                                    }
                                }
                            }

                            if (!indices.isEmpty()) {
                                indices.keySet().stream().sorted(Comparator.reverseOrder()).forEach(k -> {
                                    NovelCapitulo capitulo = new NovelCapitulo(null, novel.getNovel(), novel.getVolume(), 0f, k, novel.getLingua(), false, false);
                                    for (int i = textos.size(); i >= 0; i--) {
                                        capitulo.addTexto(textos.remove(i));
                                        if (textos.get(i).getTexto().compareToIgnoreCase(indices.get(k)) == 0)
                                            break;
                                    }
                                    novel.addCapitulos(capitulo);
                                });

                                if (!textos.isEmpty()) {
                                    NovelCapitulo capitulo = novel.getCapitulos().get(novel.getCapitulos().size());
                                    for (int i = textos.size(); i >= 0; i--)
                                        capitulo.addTexto(textos.remove(i));
                                }

                                for (NovelCapitulo capitulo : novel.getCapitulos())
                                    capitulo.setTextos(capitulo.getTextos().parallelStream().sorted(Comparator.comparing(NovelTexto::getSequencia)).collect(Collectors.toList()));

                                novel.setCapitulos(novel.getCapitulos().parallelStream().sorted(Comparator.comparing(NovelCapitulo::getSequencia)).collect(Collectors.toList()));
                            } else {
                                NovelCapitulo capitulo = new NovelCapitulo(null, novel.getNovel(), novel.getVolume(), 0f, 0, novel.getLingua(), false, false);
                                capitulo.setTextos(textos);
                                novel.addCapitulos(capitulo);
                            }

                            NovelTabela obj;
                            if (!tabela.isEmpty())
                                obj = new NovelTabela(tabela, new ArrayList<>());
                            else
                                obj = new NovelTabela(getTabela(nome), new ArrayList<>());

                            obj.addVolume(novel);

                            if (desativar)
                                break;

                            updateMessage("Processando textos do arquivo " + arquivo.getName() + "...");

                            switch (linguagem) {
                                case JAPANESE:
                                    processarJapones(obj);
                                    break;
                                case ENGLISH:
                                    processarIngles(obj);
                                    break;
                            }

                            if (desativar)
                                break;

                            updateMessage("Salvando textos do arquivo " + arquivo.getName() + "...");
                            for (NovelVolume vol : obj.getVolumes())
                                serviceNovel.salvarVolume(obj.getBase(), vol);

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
        progress.getBarraProgresso().progressProperty().bind(propVolume);
        controller.getBarraProgressoVolumes().progressProperty().bind(propCapitulo);
        controller.getBarraProgressoPaginas().progressProperty().bind(processarArquivos.progressProperty());
        progress.getLog().textProperty().bind(processarArquivos.messageProperty());
        Thread t = new Thread(processarArquivos);
        t.start();
    }

    private void processarJapones(NovelTabela tabela) throws ExcessaoBd {
        try (Dictionary dict = new DictionaryFactory().create("",
                SudachiTokenizer.readAll(new FileInputStream(SudachiTokenizer
                        .getPathSettings(MenuPrincipalController.getController().getDicionario()))))) {
            tokenizer = dict.create();
            mode = SudachiTokenizer.getModo(MenuPrincipalController.getController().getModo());
            siteDicionario = MenuPrincipalController.getController().getSite();

            validaHistorico.clear();
            desativar = false;

            V = 0;
            for (NovelVolume volume : tabela.getVolumes()) {
                V++;

                vocabVolume.clear();
                C = 0;
                for (NovelCapitulo capitulo : volume.getCapitulos()) {
                    C++;

                    vocabCapitulo.clear();
                    vocabValida.clear();
                    for (NovelTexto texto : capitulo.getTextos())
                        gerarVocabulario(texto.getTexto());

                    capitulo.setVocabularios(vocabCapitulo);
                    capitulo.setProcessado(true);
                    propCapitulo.set((double) C / volume.getCapitulos().size());

                    if (desativar)
                        break;
                }
                volume.setVocabularios(vocabVolume);
                volume.setProcessado(true);
                propVolume.set((double) V / tabela.getVolumes().size());
            }

        } catch (IOException e) {

            LOGGER.error(e.getMessage(), e);
            error = false;
        }

    }

    private String getSignificado(String kanji) {
        if (kanji.trim().isEmpty())
            return "";

        Platform.runLater(() -> MenuPrincipalController.getController().getLblLog().setText(kanji + " : Obtendo significado."));
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
    private Tokenizer tokenizer;
    private SplitMode mode;

    private void gerarVocabulario(String frase) throws ExcessaoBd {
        for (Morpheme m : tokenizer.tokenize(mode, frase)) {
            if (m.surface().matches(pattern)) {
                if (validaHistorico.contains(m.dictionaryForm())) {
                    NovelVocabulario vocabulario = vocabHistorico.stream()
                            .filter(vocab -> m.dictionaryForm().equalsIgnoreCase(vocab.getPalavra())).findFirst()
                            .orElse(null);
                    if (vocabulario != null) {
                        vocabCapitulo.add(vocabulario);
                        vocabVolume.add(vocabulario);
                        continue;
                    }
                }

                if (!vocabValida.contains(m.dictionaryForm())) {
                    Vocabulario palavra = vocabularioJaponesService.select(m.surface(), m.dictionaryForm());

                    if (palavra != null) {
                        NovelVocabulario vocabulario = null;
                        if (palavra.getPortugues().substring(0, 2).matches(japanese))
                            vocabulario = new NovelVocabulario(m.dictionaryForm(), palavra.getPortugues(),
                                    palavra.getIngles(), m.readingForm());
                        else
                            vocabulario = new NovelVocabulario(m.dictionaryForm(), palavra.getPortugues(),
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
                        vocabCapitulo.add(vocabulario);
                        vocabVolume.add(vocabulario);
                    } else {
                        Revisar revisar = serviceJaponesRevisar.select(m.surface(), m.dictionaryForm());
                        if (revisar == null) {
                            revisar = new Revisar(m.surface(), m.dictionaryForm(), m.readingForm(), false, false, false, true);
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
                            if (!revisar.isNovel()) {
                                revisar.setNovel(true);
                                serviceJaponesRevisar.setIsNovel(revisar);
                            }

                            serviceJaponesRevisar.incrementaVezesAparece(revisar.getVocabulario());
                        }

                        NovelVocabulario vocabulario = new NovelVocabulario(m.dictionaryForm(), revisar.getPortugues(),
                                revisar.getIngles(), m.readingForm(), false);

                        validaHistorico.add(m.dictionaryForm());
                        vocabHistorico.add(vocabulario);

                        vocabValida.add(m.dictionaryForm());
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

    public void processarIngles(NovelTabela tabela) throws ExcessaoBd {
        propTabela.set(.0);
        propVolume.set(.0);
        propCapitulo.set(.0);
        validaHistorico.clear();

        palavraValida.clear();

        V = 0;
        for (NovelVolume volume : tabela.getVolumes()) {
            V++;

            vocabVolume.clear();
            C = 0;
            for (NovelCapitulo capitulo : volume.getCapitulos()) {
                C++;

                vocabCapitulo.clear();
                vocabValida.clear();
                for (NovelTexto texto : capitulo.getTextos()) {
                    if (texto.getTexto() != null && !texto.getTexto().isEmpty()) {
                        Set<String> palavras = Stream.of(texto.getTexto().split(" "))
                                .map(txt -> txt.replaceAll("\\W", ""))
                                .filter(txt -> !txt.trim().contains(" ") && !txt.isEmpty())
                                .collect(Collectors.toSet());

                        for (String palavra : palavras) {

                            if (palavra.matches("[\\d|\\W]"))
                                continue;

                            if (validaHistorico.contains(palavra)) {
                                NovelVocabulario vocabulario = vocabHistorico.stream().filter(
                                                vocab -> palavra.equalsIgnoreCase(vocab.getPalavra()))
                                        .findFirst().orElse(null);
                                if (vocabulario != null) {
                                    vocabCapitulo.add(vocabulario);
                                    vocabVolume.add(vocabulario);
                                    continue;
                                }
                            }

                            if (!vocabValida.contains(palavra)) {
                                Vocabulario salvo = vocabularioInglesService.select(palavra);

                                if (salvo != null) {
                                    NovelVocabulario vocabulario = new NovelVocabulario(palavra, salvo.getPortugues());

                                    validaHistorico.add(palavra);
                                    vocabHistorico.add(vocabulario);

                                    vocabValida.add(palavra);
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
                                        revisar = new Revisar(palavra, false, false, false, true);
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
                                        if (!revisar.isNovel()) {
                                            revisar.setNovel(true);
                                            serviceInglesRevisar.setIsNovel(revisar);
                                        }

                                        serviceInglesRevisar.incrementaVezesAparece(revisar.getVocabulario());
                                    }

                                    NovelVocabulario vocabulario = new NovelVocabulario(palavra, revisar.getPortugues(), "", "", false);

                                    validaHistorico.add(palavra);
                                    vocabHistorico.add(vocabulario);

                                    vocabValida.add(palavra);
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
                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), Progress,
                                    Size, Type.NORMAL);
                    });
                }

                capitulo.setVocabularios(vocabCapitulo);
                capitulo.setProcessado(true);
                propCapitulo.set((double) C / volume.getCapitulos().size());

                if (desativar)
                    break;
            }
            volume.setVocabularios(vocabVolume);
            volume.setProcessado(true);
            propVolume.set((double) V / tabela.getVolumes().size());

            if (desativar) {
                break;
            }
        }

    }

}
