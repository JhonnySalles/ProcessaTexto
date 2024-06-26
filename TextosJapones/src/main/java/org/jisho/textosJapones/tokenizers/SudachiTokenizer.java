package org.jisho.textosJapones.tokenizers;

import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;
import com.worksap.nlp.sudachi.Dictionary;
import com.worksap.nlp.sudachi.DictionaryFactory;
import com.worksap.nlp.sudachi.Morpheme;
import com.worksap.nlp.sudachi.Tokenizer;
import com.worksap.nlp.sudachi.Tokenizer.SplitMode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.FrasesAnkiController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.entities.Kanji;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.KanjiServices;
import org.jisho.textosJapones.model.services.RevisarJaponesServices;
import org.jisho.textosJapones.model.services.VocabularioJaponesServices;
import org.jisho.textosJapones.processar.JapanDict;
import org.jisho.textosJapones.processar.Jisho;
import org.jisho.textosJapones.processar.TanoshiJapanese;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SudachiTokenizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SudachiTokenizer.class);

    private Api google = Api.API_GOOGLE;
    private FrasesAnkiController controller;
    private VocabularioJaponesServices vocabServ;
    private KanjiServices kanjiServ;
    private final Set<String> repetido = new HashSet<String>();
    private final List<Vocabulario> vocabNovo = new ArrayList<>();

    private final Runnable atualizaBarraWindows = () -> Platform.runLater(
            () -> TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, max, Type.NORMAL));

    private static int i = 0;
    private static int max = 0;

    public static Boolean DESATIVAR = false;

    public static String getPathSettings(Dicionario dicionario) {
        String settings_path = Paths.get("").toAbsolutePath().toString();
        switch (dicionario) {
            case SAMLL:
                settings_path += "/sudachi_smalldict.json";
                break;
            case CORE:
                settings_path += "/sudachi_coredict.json";
                break;
            case FULL:
                settings_path += "/sudachi_fulldict.json";
                break;
            default:
                settings_path += "/sudachi_fulldict.json";
        }

        return settings_path;
    }

    public static SplitMode getModo(Modo modo) {
        switch (modo) {
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

    public static String readAll(InputStream input) throws IOException {
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

        google = MenuPrincipalController.getController().getContaGoogle();
        controller.setPalavra(texto[0]);

        try (FileInputStream input = new FileInputStream(
                getPathSettings(MenuPrincipalController.getController().getDicionario()));
             Dictionary dict = new DictionaryFactory().create("", readAll(input))) {
            tokenizer = dict.create();

            i = 0;
            max = texto.length;
            SplitMode mode = getModo(MenuPrincipalController.getController().getModo());

            for (String txt : texto) {
                if (txt != texto[0] && !txt.isEmpty()) {
                    processado += processaTokenizer(mode, txt, false);
                    processado += "\n\n\n";
                }
                i++;
                atualizaProgresso();
            }

            concluiProgresso(false);

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            concluiProgresso(true);
            AlertasPopup.ErroModal("Erro ao processar textos", e.getMessage());
        }

        controller.setTextoDestino(processado);
        controller.setVocabulario(vocabNovo);
        processaListaNovo(false);
    }

    private void processaKanji() throws ExcessaoBd {
        String[] texto = controller.getTextoOrigem().split("\n");
        String processado = "";

        vocabNovo.clear();
        repetido.clear();

        google = MenuPrincipalController.getController().getContaGoogle();
        controller.setPalavra(texto[0]);

        try (FileInputStream input = new FileInputStream(
                getPathSettings(MenuPrincipalController.getController().getDicionario()));
             Dictionary dict = new DictionaryFactory().create("", readAll(input))) {
            tokenizer = dict.create();

            i = 0;
            max = texto.length;
            SplitMode mode = getModo(MenuPrincipalController.getController().getModo());

            for (String txt : texto) {
                if (!txt.isEmpty()) {
                    String tokenizer = processaTokenizer(mode, txt, false);
                    String kanjis = "";

                    for (char letra : (tokenizer.isEmpty() ? txt.toCharArray() : tokenizer.toCharArray())) {
                        String kanji = String.valueOf(letra);
                        if (kanji.matches(pattern)) {
                            Kanji palavra = kanjiServ.select(kanji);
                            if (palavra != null)
                                kanjis += kanji + " " + palavra.getPalavra() + " ";
                        }
                    }

                    if (tokenizer.isEmpty())
                        processado += kanjis.trim() + "\n\n";
                    else
                        processado += tokenizer + (!kanjis.isEmpty() ? " (" + kanjis.trim() + ")" : "") + "\n\n";
                }
                i++;
                atualizaProgresso();
            }

            concluiProgresso(false);

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            concluiProgresso(true);
            AlertasPopup.ErroModal("Erro ao processar textos", e.getMessage());
        }

        controller.setTextoDestino(processado);
        controller.setVocabulario(vocabNovo);
        processaListaNovo(false);
    }

    private void processaMusica() throws ExcessaoBd {
        MenuPrincipalController.getController().setAviso("Sudachi - Processar música");
        Task<Void> processar = new Task<Void>() {
            String[] texto;
            String processado = "";
            Dicionario dictionario = Dicionario.FULL;
            SplitMode mode = SplitMode.C;
            boolean erro = false;

            @Override
            public Void call() throws IOException, InterruptedException {
                DESATIVAR = false;
                vocabNovo.clear();
                Platform.runLater(() -> {
                    texto = controller.getTextoOrigem().split("\n");
                    dictionario = MenuPrincipalController.getController().getDicionario();
                    mode = getModo(MenuPrincipalController.getController().getModo());
                    google = MenuPrincipalController.getController().getContaGoogle();
                    controller.limpaVocabulario();
                    controller.desabilitaBotoes();
                });

                try (Dictionary dict = new DictionaryFactory().create("",
                        readAll(new FileInputStream(getPathSettings(dictionario))))) {
                    tokenizer = dict.create();

                    i = 1;
                    max = texto.length;

                    for (String txt : texto) {
                        updateProgress(i, max);
                        atualizaBarraWindows.run();

                        i++;
                        if (!txt.isEmpty()) {
                            processado += txt + "\n\n";

                            processado += processaTokenizer(mode, txt, true);

                            processado += "\n\n\n";
                        } else
                            processado += "\n";

                        if (DESATIVAR)
                            return null;
                    }

                } catch (IOException e) {
                    erro = true;
                    
                    LOGGER.error(e.getMessage(), e);
                    Platform.runLater(() -> AlertasPopup.ErroModal("Erro ao processar o textos", e.getMessage()));
                } catch (ExcessaoBd e) {
                    erro = true;
                    
                    LOGGER.error(e.getMessage(), e);
                    Platform.runLater(() -> AlertasPopup.ErroModal("Erro de conexao", e.getMessage()));
                } finally {
                    Platform.runLater(() -> {
                        if (erro)
                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), 1, 1, Type.ERROR);
                        else {
                            i = 1;
                            max = 1;
                            updateProgress(i, max);
                            atualizaBarraWindows.run();
                        }
                        controller.setVocabulario(vocabNovo);
                        controller.setTextoDestino(processado);
                        controller.habilitaBotoes();
                    });

                    if (erro)
                        TimeUnit.SECONDS.sleep(5);

                    Platform.runLater(() -> {
                        concluiProgresso(false);
                        processaListaNovo(true);
                    });
                }
                return null;
            }
        };

        Thread processa = new Thread(processar);
        processa.start();
    }

    private void processaVocabulario() throws ExcessaoBd {
        MenuPrincipalController.getController().setAviso("SUDACHI - Processar vocabulário obtendo frase do site Tanoshi Japanese.");
        Task<Void> processar = new Task<>() {
            String palavra = "";
            String[] palavras = {""};
            String vocabulario = "";
            Dicionario dictionario = Dicionario.FULL;
            SplitMode mode = SplitMode.C;
            boolean erro = false;
            boolean isExcel = false;

            @Override
            public Void call() throws IOException, InterruptedException {
                DESATIVAR = false;
                String ingles, portugues, leitura, expressao, significado, links;
                String[][] frase;

                vocabNovo.clear();
                Platform.runLater(() -> {
                    palavras = controller.getTextoOrigem().split("\n");
                    dictionario = MenuPrincipalController.getController().getDicionario();
                    mode = getModo(MenuPrincipalController.getController().getModo());
                    google = MenuPrincipalController.getController().getContaGoogle();
                    isExcel = controller.isListaExcel();
                    controller.limpaVocabulario();
                    controller.desabilitaBotoes();
                });

                try (Dictionary dict = new DictionaryFactory().create("",
                        readAll(new FileInputStream(getPathSettings(dictionario))))) {
                    tokenizer = dict.create();

                    i = 1;
                    max = palavras.length + 1;

                    for (String texto : palavras) {
                        ingles = "";
                        portugues = "";
                        leitura = "";

                        if (texto.contains("|")) {
                            String[] textos = texto.replace("||", "|").split("\\|");
                            palavra = textos[0];
                            ingles = textos[1] + ".";
                            if (textos.length > 2 && textos[2] != null)
                                leitura = textos[2];
                        } else
                            palavra = texto;

                        Platform.runLater(() -> {
                            MenuPrincipalController.getController().getLblLog()
                                    .setText("Processando vocabulário " + palavra + " - " + i + " de " + max);
                        });

                        updateProgress(i, max);
                        atualizaBarraWindows.run();

                        i++;
                        repetido.clear();
                        if (!palavra.trim().isEmpty()) {
                            links = "";

                            if (!ingles.isEmpty()) {
                                if (ingles.contains(";"))
                                    ingles = ingles.replaceAll(";", ", ");

                                if (ingles.contains("..") && !ingles.contains("..."))
                                    ingles = ingles.replaceAll("..", ".");

                                ingles = ingles.substring(0, 1).toUpperCase() + ingles.substring(1);

                                portugues = ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(), ingles, google);

                                if (!portugues.isEmpty())
                                    portugues = portugues.substring(0, 1).toUpperCase() + portugues.substring(1);
                            }

                            expressao = palavra + (isExcel ? "<br><br>" : "\n\n");
                            significado = portugues + (isExcel ? "<br><br>" : "\n\n");

                            frase = TanoshiJapanese.getFrase(palavra.trim());
                            for (int i = 0; i < 2; i++) {
                                if (!frase[i][0].isEmpty()) {
                                    String processado = processaTokenizer(mode, frase[i][0], false);
                                    String traduzido = ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(), frase[i][1], google);

                                    if (frase[i][0].contains("&quot;"))
                                        frase[i][0] = frase[i][0].replace("&quot;", "'");
                                    else if (frase[i][0].contains(";"))
                                        frase[i][0] += frase[i][0].replace(";", ",");

                                    if (processado.contains(";"))
                                        processado = processado.replace(";", ",");

                                    if (traduzido.contains("&quot;"))
                                        traduzido = traduzido.replace("&quot;", "'");

                                    if (traduzido.contains(";"))
                                        traduzido = traduzido.replace(";", ",");

                                    if (isExcel) {
                                        expressao += frase[i][0] + "<br><br>";
                                        significado += (processado.isBlank() ? "" : processado + "<br><br>") + traduzido + "<br><br>";
                                        links += (!frase[i][2].isEmpty() ? frase[i][2] + ";" : "");
                                    } else {
                                        expressao += frase[i][0] + "\n\n";
                                        significado += processado + "\n\n" + traduzido + "\n\n";
                                        links += (!frase[i][2].isEmpty() ? frase[i][2] + "\n" : "");
                                    }
                                } else {
                                    if (i == 0) {
                                        if (isExcel) {
                                            expressao = palavra + "<br><br>";
                                            significado = portugues + "<br><br>";
                                        } else
                                            expressao = palavra + "\n\n" + "***" + "\n\n";
                                    }
                                }
                                if (DESATIVAR)
                                    return null;
                            }

                            if (isExcel) {
                                expressao = expressao.substring(0, expressao.lastIndexOf("<br><br>"));
                                significado = significado.substring(0, significado.lastIndexOf("<br><br>"));
                            }

                            if (isExcel)
                                vocabulario += palavra + ";" + ingles + ";" + portugues + ";" + expressao + ";" + significado + ";" + (leitura.isEmpty() ? palavra : palavra + "[" + leitura + "]") + ";" + links + "\n";
                            else
                                vocabulario += palavra + "\n" + ingles + "\n" + portugues + "\n\n" + expressao + significado + links + "\n" + "-".repeat(20) + "\n";
                        }
                    }
                } catch (ExcessaoBd e) {
                    erro = true;
                    
                    LOGGER.error(e.getMessage(), e);
                    Platform.runLater(() -> AlertasPopup.ErroModal("Erro de conexao", e.getMessage()));
                } catch (Exception e) {
                    erro = true;

                    LOGGER.error(e.getMessage(), e);
                    Platform.runLater(() -> AlertasPopup.ErroModal("Erro ao processar o textos", e.getMessage()));
                } finally {
                    Platform.runLater(() -> {
                        if (erro)
                            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), 1, 1, Type.ERROR);
                        else {
                            i = 1;
                            max = 1;
                            updateProgress(i, max);
                            atualizaBarraWindows.run();
                        }
                        controller.setVocabulario(vocabNovo);
                        controller.setTextoDestino(vocabulario);
                        controller.habilitaBotoes();

                        MenuPrincipalController.getController().getLblLog().setText("");
                    });

                    if (erro)
                        TimeUnit.SECONDS.sleep(5);

                    Platform.runLater(() -> {
                        concluiProgresso(false);
                        processaListaNovo(true);
                    });
                }
                return null;
            }
        };

        Thread processa = new Thread(processar);
        processa.start();
    }

    private String processaTokenizer(SplitMode mode, String texto, Boolean repetido) throws ExcessaoBd {
        String processado = "";
        for (Morpheme m : tokenizer.tokenize(mode, texto)) {
            if (m.surface().matches(pattern) && !controller.getExcluido().contains(m.dictionaryForm())) {

                if (!repetido && this.repetido.contains(m.dictionaryForm()))
                    continue;

                this.repetido.add(m.dictionaryForm());

                Vocabulario palavra = vocabServ.select(m.surface(), m.dictionaryForm());
                if (palavra != null) {
                    processado += m.dictionaryForm() + " - " + palavra.getPortugues() + " ";

                    if (palavra.getFormaBasica().isEmpty() || palavra.getLeitura().isEmpty()) {
                        palavra.setFormaBasica(m.dictionaryForm());
                        palavra.setLeitura(m.readingForm());
                        vocabServ.update(palavra);
                    }
                } else {
                    processado += m.dictionaryForm() + " ** ";
                    if (!vocabNovo.stream().map(e -> e.getVocabulario()).anyMatch(m.surface()::equalsIgnoreCase))
                        vocabNovo.add(new Vocabulario(m.surface(), m.dictionaryForm(), m.readingForm(), ""));
                }
            }
        }

        return processado;
    }

    private void atualizaProgresso() {
        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, max, Type.NORMAL);
    }

    public void concluiProgresso(boolean erro) {
        if (erro)
            TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), 1, 1, Type.ERROR);
        else
            TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
    }

    private void configura() {
        setVocabularioServices(new VocabularioJaponesServices());
        setKanjiServices(new KanjiServices());
    }

    public void processa(FrasesAnkiController cnt) throws ExcessaoBd {
        controller = cnt;
        configura();

        switch (controller.getTipo()) {
            case TEXTO:
                processaTexto();
                break;
            case MUSICA:
                processaMusica();
                break;
            case VOCABULARIO:
                processaVocabulario();
                break;
            case KANJI:
                processaKanji();
                break;
            default:
                break;
        }
    }

    private void setVocabularioServices(VocabularioJaponesServices vocabServ) {
        this.vocabServ = vocabServ;
    }

    private void setKanjiServices(KanjiServices kanjiServ) {
        this.kanjiServ = kanjiServ;
    }

    public void corrigirLancados(FrasesAnkiController cnt) {
        controller = cnt;
        vocabServ = new VocabularioJaponesServices();

        List<Vocabulario> lista;
        try {
            lista = vocabServ.selectAll();
        } catch (ExcessaoBd e1) {
            e1.printStackTrace();
            return;
        }

        try (Dictionary dict = new DictionaryFactory().create("", readAll(
                new FileInputStream(getPathSettings(MenuPrincipalController.getController().getDicionario()))))) {
            tokenizer = dict.create();
            SplitMode mode = getModo(MenuPrincipalController.getController().getModo());

            for (Vocabulario vocabulario : lista) {
                for (Morpheme mp : tokenizer.tokenize(mode, vocabulario.getVocabulario()))
                    if (mp.dictionaryForm().equalsIgnoreCase(vocabulario.getVocabulario())) {
                        vocabulario.setFormaBasica(mp.dictionaryForm());
                        vocabulario.setLeitura(mp.readingForm());
                    }
            }
            vocabServ.insertOrUpdate(lista);

            concluiProgresso(false);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            concluiProgresso(true);
            AlertasPopup.ErroModal("Erro ao processar textos", e.getMessage());
        } catch (ExcessaoBd e) {
            concluiProgresso(true);
            
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal("Erro de conexao", e.getMessage());
        }
    }

    public void processaListaNovo(Boolean pesquisaSite) {
        MenuPrincipalController.getController().setAviso("Sudachi - Processar lista de novos registros");
        Task<Void> processar = new Task<Void>() {
            @Override
            public Void call() throws IOException, InterruptedException {
                try {
                    if (DESATIVAR)
                        return null;

                    RevisarJaponesServices service = new RevisarJaponesServices();

                    i = 0;
                    max = vocabNovo.size();
                    for (Vocabulario item : vocabNovo) {

                        Platform.runLater(() -> {
                            MenuPrincipalController.getController().getLblLog().setText(
                                    "Processando vocabulário novo " + item.getVocabulario() + " - " + i + " de " + max);
                        });

                        updateProgress(i, max);
                        atualizaBarraWindows.run();

                        i++;
                        if (item.getPortugues().isEmpty()) {

                            try {
                                if (service.existe(item.getVocabulario())) {
                                    Revisar revisar = service.select(item.getVocabulario());
                                    item.setIngles(revisar.getIngles());
                                    item.setPortugues(revisar.getPortugues());
                                    continue;
                                }
                            } catch (ExcessaoBd e) {
                                
                                LOGGER.error(e.getMessage(), e);
                            }

                            if (!pesquisaSite)
                                continue;

                            String signficado = TanoshiJapanese.processa(item.getFormaBasica());

                            if (signficado.isEmpty())
                                signficado = Jisho.processa(item.getFormaBasica());

                            if (signficado.isEmpty())
                                signficado = JapanDict.processa(item.getFormaBasica());

                            if (!signficado.isEmpty()) {
                                try {
                                    item.setIngles(signficado);
                                    item.setPortugues(ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(), signficado, google));
                                    service.insert(new Revisar(item.getVocabulario(), item.getFormaBasica(), item.getLeitura(), "", item.getPortugues(), signficado));
                                } catch (IOException io) {
                                    item.setPortugues(signficado);
                                    io.printStackTrace();
                                } catch (ExcessaoBd e) {
                                    
                                    LOGGER.error(e.getMessage(), e);
                                }
                            }
                        }

                        if (DESATIVAR)
                            return null;
                    }

                } finally {
                    updateProgress(i, max);
                    atualizaBarraWindows.run();

                    Platform.runLater(() -> controller.setVocabulario(vocabNovo));

                    TimeUnit.SECONDS.sleep(5);
                    Platform.runLater(() -> {
                        concluiProgresso(false);
                    });
                }
                return null;
            }
        };

        Thread processa = new Thread(processar);
        processa.start();
    }

}
