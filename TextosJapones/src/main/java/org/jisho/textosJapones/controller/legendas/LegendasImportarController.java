package org.jisho.textosJapones.controller.legendas;

import com.jfoenix.controls.*;
import com.mpatric.mp3agic.*;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.CheckBoxTableCellCustom;
import org.jisho.textosJapones.components.CheckBoxTreeTableCellCustom;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.comicinfo.BaseLista;
import org.jisho.textosJapones.model.entities.comicinfo.MAL;
import org.jisho.textosJapones.model.entities.subtitle.Arquivo;
import org.jisho.textosJapones.model.entities.subtitle.Legenda;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.LegendasServices;
import org.jisho.textosJapones.processar.ProcessarLegendas;
import org.jisho.textosJapones.util.constraints.Validadores;
import org.jisho.textosJapones.util.converter.IntegerConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class LegendasImportarController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegendasImportarController.class);

    @FXML
    private AnchorPane apRoot;

    @FXML
    private JFXComboBox<Language> cbLinguagem;

    @FXML
    private JFXComboBox<String> cbBase;

    @FXML
    private JFXTextField txtPipe;

    @FXML
    private JFXTextField txtNome;

    @FXML
    private JFXTextField txtPrefixoSom;

    @FXML
    private JFXTextField txtCaminho;

    @FXML
    private JFXButton btnCaminho;

    @FXML
    private JFXButton btnArquivo;

    @FXML
    private JFXButton btnProcessar;

    @FXML
    private JFXButton btnLimpar;

    @FXML
    private JFXCheckBox ckbVocabulario;

    @FXML
    private JFXCheckBox ckbMarcarTodos;

    @FXML
    private TableView<Arquivo> tbTabela;

    @FXML
    private TableColumn<Arquivo, Boolean> tcMarcado;

    @FXML
    private TableColumn<Arquivo, String> tcArquivo;

    @FXML
    private TableColumn<Arquivo, Integer> tcEpisodio;


    private ObservableList<Arquivo> ARQUIVOS = FXCollections.observableArrayList();
    private LegendasController controller;

    public void setControllerPai(LegendasController controller) {
        this.controller = controller;
    }

    public LegendasController getControllerPai() {
        return controller;
    }

    private Boolean desativar;

    private LegendasServices services = new LegendasServices();
    private final ProcessarLegendas processar = new ProcessarLegendas(null);

    @FXML
    private void onBtnLimpar() {
        limpar();
    }

    @FXML
    private void onBtnMarcarTodos() {
        for (Arquivo arquivo : ARQUIVOS)
            arquivo.setProcessar(ckbMarcarTodos.isSelected());
        tbTabela.refresh();
    }

    @FXML
    private void onBtnProcessar() {
        if (btnProcessar.getAccessibleText().equalsIgnoreCase("PROCESSANDO")) {
            desativar = true;
            return;
        }

        if (!valida())
            return;

        desabilitaBotoes();

        btnProcessar.setAccessibleText("PROCESSANDO");
        btnProcessar.setText("Pausar");

        desativar = false;

        String pipe = txtPipe.getText() == null || txtPipe.getText().trim().isEmpty() ? "\t" : txtPipe.getText();
        String nome = txtNome.getText().trim();
        Language lingua = cbLinguagem.getValue();
        String base  = cbBase.getEditor().getText();
        String prefix  = txtPrefixoSom.getText().trim();
        Boolean isVocab = ckbVocabulario.isSelected();

        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo().setText("Legendas - Processar arquivos");
        Task<Void> processar = new Task<>() {
            Integer i = 0;
            Boolean error = false;

            final Dicionario dicionario = MenuPrincipalController.getController().getDicionario();
            final Modo modo = MenuPrincipalController.getController().getModo();

            @Override
            public Void call() {
                try {
                    for (Arquivo arquivo : ARQUIVOS) {
                        i++;
                        updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size());
                        updateProgress(i, ARQUIVOS.size());

                        Platform.runLater(() -> {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, ARQUIVOS.size(), TaskbarProgressbar.Type.NORMAL);
                        });

                        if (!arquivo.isProcessar())
                            continue;

                        if (desativar)
                            break;

                        String path = arquivo.getArquivo().getParent();
                        File original = new File(path + "\\original");
                        if (!original.exists())
                            original.mkdirs();

                        File backup = new File(path + "\\original\\" + arquivo.getArquivo().getName());
                        if (!backup.exists()) {
                            updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size() + " - Gerando backup");

                            Files.copy(arquivo.getArquivo().toPath(), backup.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);

                            backup = new File(path + "\\original\\" + arquivo.getArquivo().getName().substring(0, arquivo.getArquivo().getName().lastIndexOf(".tsv")) + ".media");
                            backup.mkdirs();

                            File pastaMidia = new File(arquivo.getArquivo().getAbsolutePath().substring(0, arquivo.getArquivo().getAbsolutePath().lastIndexOf(".tsv")) + ".media");
                            if (pastaMidia.exists())
                                for (File midia : pastaMidia.listFiles())
                                    Files.copy(midia.toPath(), new File(backup, midia.getName()).toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
                        }

                        List<Legenda> legendas = new ArrayList<>();

                        File tmp = new File(arquivo.getArquivo().getAbsolutePath() + ".tmp");
                        if (tmp.exists())
                            tmp.delete();

                        tmp.createNewFile();

                        updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size() + " - Processando legendas");

                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmp))) {
                            try (BufferedReader reader = new BufferedReader(new FileReader(arquivo.getArquivo(), StandardCharsets.UTF_8))) {
                                Integer seq = 0;
                                String line;
                                Map<String, String> renomear = new HashMap<>();
                                while ((line = reader.readLine()) != null) {
                                    if (line.trim().isEmpty())
                                        continue;
                                    seq++;

                                    String idAnki = "", tempo = "", texto = "", traducao = "", som = "", imagem = "", vocabulario = "";

                                    String[] colunas = line.split(pipe);
                                    tempo = "0" + colunas[1].substring(colunas[1].lastIndexOf("_") + 1);
                                    tempo = tempo.substring(0, 2) + ":" + tempo.substring(3, 5) + ":"  + tempo.substring(6);

                                    for (var i = 2; i < colunas.length; i++) {
                                        if (colunas[i].contains("[sound:"))
                                            som = colunas[i];
                                        else if (colunas[i].contains("<img src="))
                                            imagem = colunas[i];
                                    }

                                    for (var i = 2; i < colunas.length; i++) {
                                        if (colunas[i].equalsIgnoreCase(som) || colunas[i].equalsIgnoreCase(imagem))
                                            continue;

                                        if (texto.isEmpty())
                                            texto = colunas[i];
                                        else {
                                            traducao = colunas[i];
                                            break;
                                        }
                                    }

                                    if (isVocab && !texto.isEmpty())
                                        vocabulario = getVocabulario(dicionario, modo, texto);

                                    legendas.add(new Legenda(seq, arquivo.getEpisodio(), lingua, tempo, texto, traducao, som, imagem, vocabulario));

                                    if (!som.isEmpty()) {
                                        String chave = som.replace("[sound:", "").replace("]", "");
                                        String novo = nome + " - Episódio " + String.format("%02d", arquivo.getEpisodio()) + (!prefix.isEmpty() ?  " " + prefix : "")  + " - " + tempo.replace(":", ".") + chave.substring(chave.lastIndexOf("."));
                                        renomear.put(chave, novo);
                                    }

                                    if (!imagem.isEmpty()) {
                                        String chave = imagem.replace("<img src=\"", "").replace("\">", "");
                                        String novo = nome + " - Episódio " + String.format("%02d", arquivo.getEpisodio()) + " - " + tempo.replace(":", ".") + chave.substring(chave.lastIndexOf("."));
                                        renomear.put(chave, novo);
                                    }

                                    idAnki = nome + " - Episódio " + String.format("%02d", arquivo.getEpisodio()) + " - " + tempo;
                                    writer.append(idAnki + pipe + (!som.isEmpty() ? som + pipe : "") + (!imagem.isEmpty() ? imagem + pipe : "") + texto + (isVocab ? pipe + vocabulario : "") + (!traducao.isEmpty() ? pipe + traducao : ""));
                                    writer.newLine();
                                }

                                updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size() + " - Salvando legendas");
                                services.salvar(base, legendas);

                                if (isVocab) {
                                    String schema = services.getSchema();
                                    String select = "SELECT id, texto FROM " + schema + "." + base + "  WHERE linguagem = " + '"' + lingua.getSigla().toUpperCase() + '"' + " AND vocabulario IS NOT NULL;";
                                    String update = "UPDATE " + schema + "." + base + " SET Vocabulario = ? WHERE id = ?;";
                                    String delete = "UPDATE " + schema + "." + base + " SET Vocabulario = '' WHERE Vocabulario IS NOT NULL;";

                                    if (!services.existFila(delete))
                                        services.insertOrUpdateFila(new FilaSQL(select, update, delete, false, false));

                                    delete = "UPDATE "+  schema + "." + base + " SET Vocabulario = NULL WHERE Vocabulario = '';";
                                    if (!services.existFila(delete))
                                        services.insertOrUpdateFila(new FilaSQL("", "", delete, false, true));
                                }

                                File pastaMidia = new File(arquivo.getArquivo().getAbsolutePath().substring(0, arquivo.getArquivo().getAbsolutePath().lastIndexOf(".tsv")) + ".media");
                                if (pastaMidia.exists()) {
                                    updateMessage("Processando arquivo " + i + " de " + ARQUIVOS.size() + " - Renomeando arquivos de midia");
                                    for (File midia : pastaMidia.listFiles()) {
                                        if (renomear.containsKey(midia.getName())) {
                                            if (midia.getName().toLowerCase().endsWith(".mp3")) {
                                                Mp3File mp3file = new Mp3File(midia);
                                                ID3v2 tag;
                                                if (mp3file.hasId3v2Tag())
                                                    tag = mp3file.getId3v2Tag();
                                                else {
                                                    tag = new ID3v24Tag();
                                                    mp3file.setId3v2Tag(tag);
                                                    tag.setTitle(midia.getName().substring(0, midia.getName().lastIndexOf(".")));
                                                }

                                                tag.setTrack(arquivo.getEpisodio().toString());
                                                tag.setArtist(nome);
                                                tag.setAlbumArtist(nome);
                                                tag.setAlbum("Episódio " + arquivo.getEpisodio().toString());
                                                tag.setGenreDescription("Anime");

                                                File novo = new File(midia.getParent(), renomear.get(midia.getName()));
                                                mp3file.save(novo.getAbsolutePath());
                                                midia.delete();
                                            } else
                                                midia.renameTo(new File(midia.getParent(), renomear.get(midia.getName())));
                                        }
                                    }
                                }
                            }
                            writer.flush();
                        }

                        arquivo.getArquivo().delete();
                        tmp.renameTo(arquivo.getArquivo());
                        arquivo.setProcessar(false);
                    }

                } catch (Exception e) {
                    LOGGER.error("Erro ao processar as legendas", e);
                    error = true;
                } finally {
                    if (!desativar)
                        updateMessage("Concluído....");

                    Platform.runLater(() -> {
                        btnProcessar.setAccessibleText("PROCESSAR");
                        btnProcessar.setText("Processar");

                        habilitaBotoes();
                        if (!desativar && !error)
                            limpar();

                        progress.getBarraProgresso().progressProperty().unbind();
                        progress.getLog().textProperty().unbind();
                        MenuPrincipalController.getController().destroiBarraProgresso(progress, "");

                        TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
                    });
                }
                return null;
            }
        };

        Thread processa = new Thread(processar);
        progress.getLog().textProperty().bind(processar.messageProperty());
        progress.getBarraProgresso().progressProperty().bind(processar.progressProperty());
        processa.start();
    }

    @FXML
    private void onBtnCarregarArquivo() {
        txtCaminho.setText(selecionaPasta(txtCaminho.getText(), true));
        carregaArquivos();
    }

    @FXML
    private void onBtnCarregarPasta() {
        txtCaminho.setText(selecionaPasta(txtCaminho.getText(), false));
        carregaArquivos();
    }

    private Boolean valida() {
        Boolean valido = true;

        if (cbBase.getValue() == null || cbBase.getValue().isEmpty()) {
            valido = false;
            cbBase.setUnFocusColor(Color.RED);
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar uma base.");
        }

        if (cbLinguagem.getValue() == null) {
            valido = false;
            cbLinguagem.setUnFocusColor(Color.RED);
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar uma linguagem.");
        }

        if (txtNome.getText().isEmpty()) {
            valido = false;
            txtNome.setUnFocusColor(Color.RED);
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar um nome.");
        }

        if (ARQUIVOS.isEmpty()) {
            valido = false;
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Nenhum arquivo para processar.");
        }

        return valido;
    }

    private void desabilitaBotoes() {
        btnLimpar.setDisable(true);
        cbBase.setDisable(true);
        cbLinguagem.setDisable(true);
        txtPipe.setDisable(true);
        txtNome.setDisable(true);
        txtPrefixoSom.setDisable(true);
        txtCaminho.setDisable(true);
        btnCaminho.setDisable(true);
        btnArquivo.setDisable(true);
        tbTabela.setDisable(true);
        ckbMarcarTodos.setDisable(true);
        ckbVocabulario.setDisable(true);
    }

    private void habilitaBotoes() {
        btnLimpar.setDisable(false);
        cbBase.setDisable(false);
        cbLinguagem.setDisable(false);
        txtPipe.setDisable(false);
        txtNome.setDisable(false);
        txtPrefixoSom.setDisable(false);
        txtCaminho.setDisable(false);
        btnCaminho.setDisable(false);
        btnArquivo.setDisable(false);
        tbTabela.setDisable(false);
        ckbMarcarTodos.setDisable(false);

        if (cbLinguagem.getValue() == Language.PORTUGUESE)
            ckbVocabulario.setDisable(false);
    }

    private String getVocabulario(Dicionario dicionario, Modo modo, String palavra) {
        return processar.processarVocabulario(dicionario, modo, palavra);
    }

    private String selecionaPasta(String pasta, Boolean isFile) {
        File arquivo;

        if (isFile) {
            FileChooser chooser = new FileChooser();
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
            chooser.setTitle("Selecione o arquivo.");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo de texto (*.tsv)", "*.tsv"));

            if (pasta != null && !pasta.isEmpty()) {
                File initial = new File(pasta);
                if (initial.isDirectory())
                    chooser.setInitialDirectory(initial);
                else
                    chooser.setInitialDirectory(new File(initial.getPath()));
            }

            arquivo = chooser.showOpenDialog(null);
        } else  {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
            chooser.setTitle("Selecione a pasta.");
            arquivo = chooser.showDialog(null);
        };

        if (arquivo == null)
            return "";
        else if (arquivo.isDirectory())
            return arquivo.getAbsolutePath();
        else
            return arquivo.getPath();
    }

    private Arquivo getArquivo(File arquivo) {
        Integer episodio = 0;
        try {
            episodio = Integer.valueOf(arquivo.getName().replaceAll("\\D", "").trim());
        }catch (Exception e) {
        }
        return new Arquivo(arquivo.getAbsolutePath(), arquivo.getName(), arquivo, episodio);
    }

    private void limpar() {
        txtCaminho.setText("");
        carregaArquivos();
    }

    private void carregaArquivos() {
        List<Arquivo> arquivos = new ArrayList<>();
        try {
            if (txtCaminho.getText() == null || txtCaminho.getText().trim().isEmpty())
                return;

            File pasta = new File(txtCaminho.getText());
            if (!pasta.exists()) {
                txtCaminho.setUnFocusColor(Color.RED);
                return;
            }

            if (pasta.isDirectory()) {
                for (File arq : pasta.listFiles())
                    if (arq.getName().endsWith(".tsv"))
                        arquivos.add(getArquivo(arq));
            } else
                arquivos.add(getArquivo(pasta));
        } finally {
            ARQUIVOS = FXCollections.observableArrayList(arquivos);
            tbTabela.setItems(ARQUIVOS);
            ckbMarcarTodos.setSelected(true);
            tbTabela.refresh();
        }
    }

    private void editaColunas() {
        tcMarcado.setCellValueFactory(new PropertyValueFactory<>("processar"));
        tcArquivo.setCellValueFactory(new PropertyValueFactory<>("nome"));
        tcEpisodio.setCellValueFactory(new PropertyValueFactory<>("episodio"));

        tcEpisodio.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerConverter()));
        tcEpisodio.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setEpisodio(e.getNewValue());
            tbTabela.requestFocus();
        });

        tcMarcado.setCellValueFactory(param -> {
            Arquivo item = param.getValue();
            SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(item.isProcessar());
            booleanProp.addListener((observable, oldValue, newValue) -> {
                item.setProcessar(newValue);
                tbTabela.refresh();
            });
            return booleanProp;
        });

        tcMarcado.setCellFactory(p -> {
            CheckBoxTableCellCustom<Arquivo, Boolean> cell = new CheckBoxTableCellCustom<>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
    }

    private void linkaCelulas() {
        editaColunas();
    }

    private final Robot robot = new Robot();

    public void initialize(URL arg0, ResourceBundle arg1) {
        linkaCelulas();
        btnProcessar.setAccessibleText("PROCESSAR");

        Validadores.setComboBoxNotEmpty(cbLinguagem, false);
        Validadores.setComboBoxNotEmpty(cbBase, true);
        Validadores.setTextFieldNotEmpty(txtNome);
        txtCaminho.focusedProperty().addListener((arg01, oldPropertyValue, newPropertyValue) -> {
            if (oldPropertyValue)
                carregaArquivos();
        });

        try {
            cbBase.getItems().setAll(services.getTabelas());
        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal("Erro ao carregar as tabelas", e.getMessage());
        }

        JFXAutoCompletePopup<String> autoCompletePopup = new JFXAutoCompletePopup<>();
        autoCompletePopup.getSuggestions().addAll(cbBase.getItems());

        autoCompletePopup.setSelectionHandler(event -> {
            cbBase.setValue(event.getObject());
        });

        cbBase.getEditor().textProperty().addListener(observable -> {
            autoCompletePopup.filter(item -> item.toLowerCase().contains(cbBase.getEditor().getText().toLowerCase()));
            if (autoCompletePopup.getFilteredSuggestions().isEmpty() || cbBase.showingProperty().get()
                    || cbBase.getEditor().getText().isEmpty())
                autoCompletePopup.hide();
            else
                autoCompletePopup.show(cbBase.getEditor());
        });

        cbBase.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        cbBase.focusedProperty().addListener((options, oldValue, newValue) -> {
            if (oldValue) {
                String base = cbBase.getEditor().getText();
                if (base != null) {
                    if (base.contains(" "))
                        cbBase.getEditor().setText(base.replaceAll(" ", "_").toLowerCase());
                    else
                        cbBase.getEditor().setText(base.toLowerCase());
                }
            }
        });

        cbLinguagem.getItems().addAll(Language.ENGLISH, Language.PORTUGUESE);
        cbLinguagem.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ESCAPE))
                cbLinguagem.getSelectionModel().clearSelection();
            else if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        cbLinguagem.getSelectionModel().select(Language.PORTUGUESE);
        cbLinguagem.setOnAction(e -> {
            if (cbLinguagem.getValue() != Language.PORTUGUESE) {
                ckbVocabulario.setSelected(false);
                ckbVocabulario.setDisable(true);
            } else
                ckbVocabulario.setDisable(false);
        });

        txtCaminho.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        txtNome.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        txtPrefixoSom.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });
    }

    public static URL getFxmlLocate() {
        return LegendasImportarController.class.getResource("/view/legendas/LegendasImportar.fxml");
    }

}
