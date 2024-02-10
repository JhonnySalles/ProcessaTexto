package org.jisho.textosJapones.controller.legendas;

import com.jfoenix.controls.*;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.enums.Dicionario;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Modo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.LegendasServices;
import org.jisho.textosJapones.model.services.VocabularioInglesServices;
import org.jisho.textosJapones.model.services.VocabularioJaponesServices;
import org.jisho.textosJapones.processar.ProcessarLegendas;
import org.jisho.textosJapones.util.Prop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class LegendasVocabularioController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(LegendasVocabularioController.class);

    @FXML
    private AnchorPane apRoot;

    @FXML
    private JFXComboBox<Language> cbLinguagem;

    @FXML
    private JFXTextField txtCaminhoExportar;

    @FXML
    private JFXButton btnCaminhoExportar;

    @FXML
    private JFXTextField txtPipe;

    @FXML
    private JFXButton btnExclusao;

    @FXML
    private JFXButton btnSalvar;

    @FXML
    private JFXButton btnAtualizar;

    @FXML
    private JFXButton btnDeletar;

    @FXML
    private JFXButton btnProcessar;

    @FXML
    private JFXButton btnProcessarTudo;

    @FXML
    private JFXButton btnSalvarFila;

    @FXML
    private JFXButton btnExecutarFila;

    @FXML
    private JFXTextArea txtAreaSelect;

    @FXML
    private JFXTextArea txtAreaUpdate;

    @FXML
    private JFXTextArea txtAreaDelete;

    @FXML
    private JFXCheckBox cbExporta;

    @FXML
    private JFXCheckBox cbLimpeza;

    @FXML
    private JFXTextArea txtAreaVocabulario;

    @FXML
    private TableView<Processar> tbLista;

    @FXML
    private TableColumn<Processar, String> tcId;

    @FXML
    private TableColumn<Processar, String> tcOriginal;

    @FXML
    private TableColumn<Processar, String> tcVocabulario;

    private final LegendasServices service = new LegendasServices();
    private final VocabularioJaponesServices vocabularioJapones = new VocabularioJaponesServices();
    private final VocabularioInglesServices vocabularioIngles = new VocabularioInglesServices();
    private final ProcessarLegendas processar = new ProcessarLegendas(null);

    private LegendasController controller;

    public void setControllerPai(LegendasController controller) {
        this.controller = controller;
    }

    private void desabilitaBotoes() {
        btnSalvar.setDisable(true);
        btnDeletar.setDisable(true);
        btnAtualizar.setDisable(true);
        btnProcessar.setDisable(true);
        btnExclusao.setDisable(true);
        btnSalvarFila.setDisable(true);
        cbExporta.setDisable(true);
        cbLimpeza.setDisable(true);
    }

    private void habilitaBotoes() {
        btnSalvar.setDisable(false);
        btnDeletar.setDisable(false);
        btnAtualizar.setDisable(false);
        btnProcessar.setDisable(false);
        btnExclusao.setDisable(false);
        btnSalvarFila.setDisable(false);
        cbExporta.setDisable(false);
        cbLimpeza.setDisable(false);
    }

    private String selecionaPasta(String pasta) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecione a pasta de destino ou o arquivo");

        if (pasta != null && !pasta.isEmpty()) {
            File initial = new File(pasta);
            if (initial.isDirectory())
                chooser.setInitialDirectory(initial);
            else
                chooser.setInitialDirectory(new File(initial.getPath()));
        }

        File arquivo = chooser.showOpenDialog(null);

        if (arquivo == null)
            return "";
        else {
            if (arquivo.isDirectory())
                return arquivo.getAbsolutePath() + "\\arquivo.csv";
            else
                return arquivo.getAbsolutePath();
        }
    }

    @FXML
    private void onBtnCarregarCaminhoExportar() {
        String caminho = selecionaPasta(txtCaminhoExportar.getText());
        txtCaminhoExportar.setText(caminho);
    }
    @FXML
    private void onBtnSalvar() {
        if (txtAreaUpdate.getText().trim().isEmpty()
                || txtAreaUpdate.getText().trim().equalsIgnoreCase("UPDATE tabela SET campo3 = ? WHERE id = ?")) {
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta",
                    "Necessário informar um update e um delete para prosseguir com o salvamento.");
            return;
        }

        try {
            MenuPrincipalController.getController().getLblLog().setText("[LEGENDAS] Salvando as informações...");
            desabilitaBotoes();
            btnProcessarTudo.setDisable(true);

            if (!txtAreaDelete.getText().isEmpty() && !txtAreaDelete.getText()
                    .equalsIgnoreCase("UPDATE tabela SET campo3 = '' WHERE campo3 IS NOT NULL"))
                service.comandoDelete(txtAreaDelete.getText());

            List<Processar> update = tbLista.getItems().stream()
                    .filter(revisar -> !revisar.getVocabulario().trim().isEmpty()).collect(Collectors.toList());
            service.comandoUpdate(txtAreaUpdate.getText(), update);

            AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Salvo",
                    "Salvo com sucesso.");
            onBtnAtualizar();

        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro",
                    "Erro ao salvar as atualizações.");
        } finally {
            MenuPrincipalController.getController().getLblLog().setText("");

            habilitaBotoes();
            btnProcessarTudo.setDisable(false);
        }
    }

    @FXML
    private void onBtnAtualizar() {
        if (txtAreaSelect.getText().trim().isEmpty() || txtAreaSelect.getText().trim()
                .equalsIgnoreCase("SELECT campo1 AS ID, campo2 AS ORIGINAL FROM tabela")) {
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar um select para prosseguir com o salvamento.");
            return;
        }

        try {
            MenuPrincipalController.getController().getLblLog().setText("[LEGENDAS] Atualizando....");
            tbLista.setItems(FXCollections.observableArrayList(service.comandoSelect(txtAreaSelect.getText())));
            MenuPrincipalController.getController().getLblLog().setText("[LEGENDAS] Concluido....");
        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro", "Erro ao realizar a pesquisa.");
        }
    }

    @FXML
    private void onBtnDeletar() {
        if (txtAreaDelete.getText().trim().isEmpty() || txtAreaDelete.getText().trim()
                .equalsIgnoreCase("UPDATE tabela SET campo3 = '' WHERE campo3 IS NOT NULL")) {
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar um delete para prosseguir com a limpeza.");
            return;
        }

        try {
            MenuPrincipalController.getController().getLblLog().setText("[LEGENDAS] Iniciando o delete....");
            service.comandoDelete(txtAreaDelete.getText());

        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro", "Erro ao salvar as atualizações.");
        } finally {
            MenuPrincipalController.getController().getLblLog().setText("[LEGENDAS] Delete do vocabulario concluido.");
        }
    }

    private String getVocabulario(Dicionario dicionario, Modo modo, Language linguagem, String palavra) {
        String vocabulario = "";
        switch (linguagem) {
            case JAPANESE -> vocabulario = processar.processarJapones(dicionario, modo, palavra);
            case ENGLISH -> vocabulario = processar.processarIngles(palavra);
        }
        return vocabulario;
    }

    @FXML
    private void onBtnProcessar() {
        if (tbLista.getItems().isEmpty() || tbLista.getSelectionModel().getSelectedItem() == null)
            return;

        if (tbLista.getSelectionModel().getSelectedItem().getVocabulario().isEmpty()) {
            processar.vocabulario.clear();

            Language linguagem = Language.JAPANESE;
            if (cbLinguagem.getSelectionModel().getSelectedItem() == null || cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.TODOS))
                cbLinguagem.getSelectionModel().select(Language.JAPANESE);
            else
                linguagem = cbLinguagem.getSelectionModel().getSelectedItem();

            tbLista.getSelectionModel().getSelectedItem()
                    .setVocabulario(getVocabulario(MenuPrincipalController.getController().getDicionario(),
                            MenuPrincipalController.getController().getModo(), linguagem,
                            tbLista.getSelectionModel().getSelectedItem().getOriginal()));

            txtAreaVocabulario.setText(processar.vocabulario.stream().collect(Collectors.joining("\n")));
        }
        tbLista.refresh();
    }

    private static Boolean desativar = false;

    @FXML
    private void onBtnProcessarTudo() {
        if (btnProcessarTudo.getAccessibleText().equalsIgnoreCase("PROCESSANDO")) {
            desativar = true;
            return;
        }

        if (tbLista.getItems().isEmpty())
            return;

        desabilitaBotoes();
        btnExecutarFila.setDisable(true);

        tbLista.setDisable(true);

        btnProcessarTudo.setAccessibleText("PROCESSANDO");
        btnProcessarTudo.setText("Pausar");

        txtAreaVocabulario.setText("");

        desativar = false;

        processar.vocabulario.clear();

        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo().setText("Legendas - Processar Vocabulario");
        Task<Void> processarTudo = new Task<Void>() {
            List<Processar> lista = null;
            final Dicionario dicionario = MenuPrincipalController.getController().getDicionario();
            final Modo modo = MenuPrincipalController.getController().getModo();
            Integer i = 0;

            @Override
            public Void call() throws IOException, InterruptedException {
                lista = new ArrayList<>(tbLista.getItems());

                try {
                    Language linguagem = Language.JAPANESE;
                    if (cbLinguagem.getSelectionModel().getSelectedItem() == null || cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.TODOS))
                        cbLinguagem.getSelectionModel().select(Language.JAPANESE);
                    else
                        linguagem = cbLinguagem.getSelectionModel().getSelectedItem();

                    for (Processar item : lista) {

                        i++;
                        updateMessage("Processando item " + i + " de " + lista.size());
                        updateProgress(i, lista.size());

                        Platform.runLater(() -> {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, lista.size(), Type.NORMAL);
                        });

                        if (item.getVocabulario().isEmpty())
                            item.setVocabulario(getVocabulario(dicionario, modo, linguagem, item.getOriginal()));

                        if (desativar)
                            break;
                    }

                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    Platform.runLater(() -> tbLista.setItems(FXCollections.observableArrayList(lista)));

                    Platform.runLater(() -> {
                        btnProcessarTudo.setAccessibleText("PROCESSAR");
                        btnProcessarTudo.setText("Processar tudo");

                        habilitaBotoes();
                        btnExecutarFila.setDisable(false);

                        tbLista.setDisable(false);

                        progress.getBarraProgresso().progressProperty().unbind();
                        progress.getLog().textProperty().unbind();
                        MenuPrincipalController.getController().destroiBarraProgresso(progress, "");

                        TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
                        txtAreaVocabulario.setText(processar.vocabulario.stream().collect(Collectors.joining("\n")));

                        tbLista.refresh();
                    });
                }
                return null;
            }
        };

        Thread processa = new Thread(processarTudo);
        progress.getLog().textProperty().bind(processarTudo.messageProperty());
        progress.getBarraProgresso().progressProperty().bind(processarTudo.progressProperty());
        processa.start();
    }

    @FXML
    private void onBtnEclusao() {
        if (txtAreaVocabulario.getText().isEmpty())
            return;

        try {
            if (cbLinguagem.getSelectionModel().getSelectedItem() != null && cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH))
                vocabularioIngles.insertExclusao(new ArrayList<>(Arrays.asList(txtAreaVocabulario.getText().split("\n"))));
            else
                vocabularioJapones.insertExclusao(new ArrayList<>(Arrays.asList(txtAreaVocabulario.getText().split("\n"))));
            txtAreaVocabulario.setText("");
        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro", "Erro ao salvar a exclusao.");
        }
    }

    @FXML
    private void onBtnSalvarFila() {

        if (cbLimpeza.isSelected() || cbExporta.isSelected()) {
            if (cbLimpeza.isSelected() && txtAreaDelete.getText().trim().isEmpty()) {
                AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar um delete para a limpeza.");
                return;
            }

            if (cbExporta.isSelected() && txtAreaSelect.getText().trim().isEmpty()) {
                AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar um select para a exportação.");
                return;
            }
        } else if (txtAreaSelect.getText().trim().isEmpty() || txtAreaSelect.getText().trim().equalsIgnoreCase("SELECT campo1 AS ID, campo2 AS ORIGINAL FROM tabela")
                || txtAreaUpdate.getText().trim().isEmpty() || txtAreaUpdate.getText().trim().equalsIgnoreCase("UPDATE tabela SET campo3 = ? WHERE id = ?")
                || txtAreaDelete.getText().trim().isEmpty() || txtAreaDelete.getText().trim().equalsIgnoreCase("UPDATE tabela SET campo3 = '' WHERE campo3 IS NOT NULL")) {
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar um select, update e delete para gravar na lista.");
            return;
        } else if (cbLinguagem.getSelectionModel().getSelectedItem() == null || cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.TODOS)) {
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar uma linguagem para gravar na lista.");
            return;
        }

        try {
            service.insertOrUpdateFila(new FilaSQL(txtAreaSelect.getText().trim(), txtAreaUpdate.getText().trim(), txtAreaDelete.getText().trim(), cbLinguagem.getSelectionModel().getSelectedItem(), cbExporta.isSelected(), cbLimpeza.isSelected()));
            AlertasPopup.AvisoModal(controller.getStackPane(), controller.getRoot(), null, "Salvo", "Salvo com sucesso.");
        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
            AlertasPopup.ErroModal(controller.getStackPane(), controller.getRoot(), null, "Erro", "Erro ao salvar ao salvar a fila.");
        }
    }

    private Boolean validaProcessarFila() {
        Boolean valido = true;

        if (txtPipe.getText().isEmpty()) {
            valido = false;
            AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar um pipe para salvar o arquivo de exportação.");
        }

        if (!txtCaminhoExportar.getText().isEmpty()) {
            File arquivo = new File(txtCaminhoExportar.getText());

            if (arquivo.isDirectory()) {
                valido = false;
                AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Necessário informar um arquivo.");
            } else if (!new File(arquivo.getParent()).canWrite()) {
                valido = false;
                AlertasPopup.AlertaModal(controller.getStackPane(), controller.getRoot(), null, "Alerta", "Não é possível gravar no local informado.");
            }
        }

        return valido;
    }

    @FXML
    private void onBtnProcessarFila() {
        if (btnExecutarFila.getAccessibleText().equalsIgnoreCase("PROCESSANDO")) {
            desativar = true;
            return;
        }

        if (!validaProcessarFila())
            return;

        desabilitaBotoes();
        btnProcessarTudo.setDisable(true);

        btnExecutarFila.setAccessibleText("PROCESSANDO");
        btnExecutarFila.setText("Pausar");

        desativar = false;
        tbLista.getItems().clear();

        txtAreaSelect.setDisable(false);
        txtAreaUpdate.setDisable(false);
        txtAreaDelete.setDisable(false);

        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo().setText("Legendas - Processar Fila");
        Task<Void> processarFila = new Task<>() {
            List<Processar> lista = null;
            List<FilaSQL> fila = null;
            final Dicionario dicionario = MenuPrincipalController.getController().getDicionario();
            final Modo modo = MenuPrincipalController.getController().getModo();
            Integer i = 0, x = 0;
            final String pipe = txtPipe.getText();
            final File arquivo = (txtCaminhoExportar.getText().isEmpty() ? null : new File(txtCaminhoExportar.getText()));
            Language linguegem;

            @Override
            public Void call() throws IOException, InterruptedException {
                try {
                    fila = service.selectFila();

                    linguegem = cbLinguagem.getSelectionModel().getSelectedItem();
                    if (linguegem != null && !linguegem.equals(Language.TODOS))
                        fila = fila.stream().filter(f -> f.getLinguagem().equals(linguegem)).collect(Collectors.toList());

                    List<FilaSQL> temp = fila.stream().filter(f -> !f.isLimpeza() && !f.isExporta()).collect(Collectors.toList());

                    for (FilaSQL select : temp) {
                        x++;
                        Language lang = select.getLinguagem();

                        Platform.runLater(() -> {
                            txtAreaSelect.setText(select.getSelect());
                            txtAreaUpdate.setText(select.getUpdate());
                            txtAreaDelete.setText(select.getDelete());
                            txtAreaVocabulario.setText(select.getVocabulario());
                            cbLinguagem.getSelectionModel().select(lang);
                            cbExporta.setSelected(select.isExporta());
                            cbLimpeza.setSelected(select.isLimpeza());
                        });

                        processar.clearVocabulary();

                        try {
                            updateMessage("Limpando....");
                            service.comandoDelete(select.getDelete());

                            updateMessage("Pesquisando....");
                            lista = service.comandoSelect(select.getSelect());
                            i = 0;
                            for (Processar item : lista) {
                                i++;
                                updateMessage("Processando fila " + x + " de " + temp.size() + " - Processando item "
                                        + i + " de " + lista.size());
                                updateProgress(i, lista.size());

                                Platform.runLater(() -> {
                                    if (TaskbarProgressbar.isSupported())
                                        TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), i, lista.size(),
                                                Type.NORMAL);
                                });

                                item.setVocabulario(getVocabulario(dicionario, modo, lang, item.getOriginal()));

                                if (desativar)
                                    break;
                            }

                            if (desativar)
                                break;

                            updateMessage("Salvando....");
                            service.comandoUpdate(select.getUpdate(), lista);

                            select.setVocabulario(processar.vocabulario.stream().collect(Collectors.joining("\n")));
                            service.insertOrUpdateFila(select);
                        } catch (ExcessaoBd e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }

                    if (arquivo != null && !desativar) {
                        List<FilaSQL> select = fila.stream().filter(FilaSQL::isExporta).filter(f -> f.getLinguagem().equals(Language.JAPANESE)).collect(Collectors.toList());
                        if (!select.isEmpty()) {
                            File file = new File(arquivo.getParent(), arquivo.getName().substring(0, arquivo.getName().lastIndexOf(".")) + " Japones" + arquivo.getName().substring(arquivo.getName().lastIndexOf(".")));
                            updateMessage("Exportando arquivo " + file.getName());
                            lista = new ArrayList<>();

                            for (FilaSQL item : select)
                                lista.addAll(service.comandoSelect(item.getSelect()));

                            exportar(pipe, lista, file);
                        }

                        select = fila.stream().filter(FilaSQL::isExporta).filter(f -> f.getLinguagem().equals(Language.ENGLISH)).collect(Collectors.toList());
                        if (!select.isEmpty()) {
                            File file = new File(arquivo.getParent(), arquivo.getName().substring(0, arquivo.getName().lastIndexOf(".")) + " Ingles" + arquivo.getName().substring(arquivo.getName().lastIndexOf(".")));
                            updateMessage("Exportando arquivo " + file.getName());
                            lista = new ArrayList<>();

                            for (FilaSQL item : select)
                                lista.addAll(service.comandoSelect(item.getSelect()));

                            exportar(pipe, lista, file);
                        }

                        updateMessage("Executando a limpeza.");
                        for (FilaSQL item : fila.stream().filter(FilaSQL::isLimpeza).collect(Collectors.toList()))
                            service.comandoDelete(item.getDelete());

                        Properties props = Prop.loadProperties();
                        if (props.containsKey("ultimo_caminho_salvo"))
                            props.replace("ultimo_caminho_salvo", txtCaminhoExportar.getText());
                        else
                            props.put("ultimo_caminho_salvo", txtCaminhoExportar.getText());
                        Prop.save(props);
                    }

                } catch (ExcessaoBd e1) {
                    e1.printStackTrace();
                } finally {
                    if (!desativar)
                        updateMessage("Concluído....");

                    Platform.runLater(() -> {
                        btnExecutarFila.setAccessibleText("PROCESSAR");
                        btnExecutarFila.setText("Executar fila");

                        habilitaBotoes();
                        btnProcessarTudo.setDisable(false);
                        tbLista.setDisable(false);

                        progress.getBarraProgresso().progressProperty().unbind();
                        progress.getLog().textProperty().unbind();
                        MenuPrincipalController.getController().destroiBarraProgresso(progress, "");

                        TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

                        txtAreaSelect.setText("");
                        txtAreaUpdate.setText("");
                        txtAreaDelete.setText("");
                        txtAreaVocabulario.setText("");
                        cbLinguagem.getSelectionModel().select(linguegem);
                    });
                }
                return null;
            }
        };

        Thread processa = new Thread(processarFila);
        progress.getLog().textProperty().bind(processarFila.messageProperty());
        progress.getBarraProgresso().progressProperty().bind(processarFila.progressProperty());
        processa.start();
    }

    private void exportar(String pipe, List<Processar> lista, File arquivo) throws IOException {
        if (arquivo.exists())
            arquivo.delete();

        arquivo.createNewFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo))) {
            Integer index = 0;
            for (Processar item : lista) {
                index++;
                writer.append(item.getId()).append(pipe).append(item.getOriginal());
                if (index < lista.size())
                    writer.newLine();
            }
            writer.flush();
        }
    }

    public AnchorPane getRoot() {
        return apRoot;
    }

    private void editaColunas() {
        tcId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tcOriginal.setCellValueFactory(new PropertyValueFactory<>("original"));
        tcVocabulario.setCellValueFactory(new PropertyValueFactory<>("vocabulario"));

        tcOriginal.setCellFactory(TextFieldTableCell.forTableColumn());

        tcVocabulario.setCellFactory(TextFieldTableCell.forTableColumn());
        tcVocabulario.setOnEditCommit(e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setVocabulario(e.getNewValue().trim());
            tbLista.refresh();
            tbLista.requestFocus();
        });

    }

    private void linkaCelulas() {
        editaColunas();
    }

    ChangeListener exportaListenner;
    ChangeListener limpezaListenner;

    public void initialize(URL arg0, ResourceBundle arg1) {
        cbLinguagem.getItems().addAll(Language.TODOS, Language.JAPANESE, Language.ENGLISH);
        cbLinguagem.getSelectionModel().selectFirst();

        linkaCelulas();
        btnProcessarTudo.setAccessibleText("PROCESSAR");
        btnExecutarFila.setAccessibleText("PROCESSAR");

        exportaListenner = (ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
            if (cbExporta.isSelected()) {
                cbLimpeza.selectedProperty().removeListener(limpezaListenner);
                cbLimpeza.setSelected(false);
                txtAreaSelect.setDisable(false);
                txtAreaUpdate.setDisable(true);
                txtAreaDelete.setDisable(true);
                txtAreaUpdate.setText("");
                txtAreaDelete.setText("");
                cbLimpeza.selectedProperty().addListener(limpezaListenner);
            } else {
                if (!cbLimpeza.isSelected()) {
                    txtAreaSelect.setDisable(false);
                    txtAreaUpdate.setDisable(false);
                    txtAreaDelete.setDisable(false);
                }
            }
        };

        limpezaListenner = (ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
            if (cbLimpeza.isSelected()) {
                cbExporta.selectedProperty().removeListener(exportaListenner);
                cbExporta.setSelected(false);
                txtAreaSelect.setDisable(true);
                txtAreaUpdate.setDisable(true);
                txtAreaDelete.setDisable(false);
                txtAreaSelect.setText("");
                txtAreaUpdate.setText("");
                cbExporta.selectedProperty().addListener(exportaListenner);
            } else {
                if (!cbExporta.isSelected()) {
                    txtAreaSelect.setDisable(false);
                    txtAreaUpdate.setDisable(false);
                    txtAreaDelete.setDisable(false);
                }
            }
        };

        cbExporta.selectedProperty().addListener(exportaListenner);
        cbLimpeza.selectedProperty().addListener(limpezaListenner);

        Properties props = Prop.loadProperties();
        if (props.containsKey("ultimo_caminho_salvo"))
            txtCaminhoExportar.setText(props.getProperty("ultimo_caminho_salvo"));
    }

    public static URL getFxmlLocate() {
        return LegendasVocabularioController.class.getResource("/view/legendas/LegendasVocabulario.fxml");
    }

}
