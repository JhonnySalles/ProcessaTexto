package org.jisho.textosJapones.controller.novels;

import com.jfoenix.controls.*;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.robot.Robot;
import javafx.util.Callback;
import org.jisho.textosJapones.Run;
import org.jisho.textosJapones.components.CheckBoxTreeTableCellCustom;
import org.jisho.textosJapones.components.notification.AlertasPopup;
import org.jisho.textosJapones.controller.BaseController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.entities.Novel;
import org.jisho.textosJapones.model.entities.novelextractor.NovelCapitulo;
import org.jisho.textosJapones.model.entities.novelextractor.NovelTabela;
import org.jisho.textosJapones.model.entities.novelextractor.NovelVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.NovelServices;
import org.jisho.textosJapones.processar.ProcessarNovels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class NovelsProcessarController implements Initializable, BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NovelsProcessarController.class);

    @FXML
    protected AnchorPane apRoot;

    @FXML
    private JFXButton btnCarregar;

    @FXML
    private JFXButton btnProcessar;

    @FXML
    private JFXComboBox<String> cbBase;

    @FXML
    private JFXTextField txtNovel;

    @FXML
    private JFXCheckBox ckbProcessados;

    @FXML
    private JFXCheckBox ckbMarcarTodos;

    @FXML
    private TreeTableView<Novel> treeBases;

    @FXML
    private TreeTableColumn<Novel, Boolean> treecMacado;

    @FXML
    private TreeTableColumn<Novel, String> treecBase;

    @FXML
    private TreeTableColumn<Novel, String> treecNovel;

    @FXML
    private TreeTableColumn<Novel, Float> treecVolume;

    @FXML
    private TreeTableColumn<Novel, Float> treecCapitulo;

    @FXML
    private JFXComboBox<Language> cbLinguagem;

    @FXML
    private ProgressBar barraProgressoVolumes;

    @FXML
    private ProgressBar barraProgressoCapitulos;

    private ProcessarNovels novels;
    private NovelServices service = new NovelServices();
    private ObservableList<NovelTabela> TABELAS;

    private NovelsController controller;

    public void setControllerPai(NovelsController controller) {
        this.controller = controller;
    }

    @Override
    public NovelsController getControllerPai() {
        return controller;
    }

    @FXML
    private void onBtnProcessar() {
        if (btnProcessar.getAccessibleText().equalsIgnoreCase("PROCESSANDO") && novels != null) {
            novels.setDesativar(true);
            return;
        }

        btnProcessar.setAccessibleText("PROCESSANDO");
        btnProcessar.setText("Pausar");
        btnCarregar.setDisable(true);

        if (novels == null)
            novels = new ProcessarNovels(this);

        treeBases.setDisable(true);
        MenuPrincipalController.getController().getLblLog().setText("Iniciando o processamento das novels...");
        novels.processarTabelas(TABELAS);
    }

    @FXML
    private void onBtnCarregar() {
        carregar();
    }

    @FXML
    private void onBtnMarcarTodos() {
        marcarTodosFilhos(treeBases.getRoot(), ckbMarcarTodos.isSelected());
        treeBases.refresh();
    }

    @Override
    public void habilitar() {
        treeBases.setDisable(false);
        MenuPrincipalController.getController().getLblLog().setText("");
        btnProcessar.setAccessibleText("PROCESSAR");
        btnProcessar.setText("Processar");
        btnCarregar.setDisable(false);
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
        getBarraProgresso().setProgress(0);
        getBarraProgressoCapitulos().setProgress(0);
    }

    @Override
    public AnchorPane getRoot() {
        return apRoot;
    }

    @Override
    public ProgressBar getBarraProgresso() {
        return barraProgressoVolumes;
    }

    public ProgressBar getBarraProgressoCapitulos() {
        return barraProgressoCapitulos;
    }

    private Boolean PROCESSADOS;
    private String BASE;
    private String NOVEL;
    private Language LINGUAGEM;
    private TreeItem<Novel> DADOS;

    private void carregar() {
        MenuPrincipalController.getController().getLblLog().setText("Carregando dados das novels...");
        btnCarregar.setDisable(true);
        btnProcessar.setDisable(true);
        treeBases.setDisable(true);

        PROCESSADOS = ckbProcessados.isSelected();
        BASE = cbBase.getValue() != null ? cbBase.getValue().trim() : "";
        NOVEL = txtNovel.getText().trim();
        LINGUAGEM = cbLinguagem.getSelectionModel().getSelectedItem();

        getBarraProgressoCapitulos().setProgress(-1);
        getBarraProgresso().setProgress(-1);

        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        Task<Void> carregaItens = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                try {
                    service = new NovelServices();
                    TABELAS = FXCollections.observableArrayList(service.selectTabelas(!PROCESSADOS, false, BASE, LINGUAGEM, NOVEL));
                    DADOS = getTreeData();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    treeBases.setRoot(DADOS);
                    MenuPrincipalController.getController().getLblLog().setText("");
                    ckbMarcarTodos.setSelected(true);
                    btnCarregar.setDisable(false);
                    btnProcessar.setDisable(false);
                    treeBases.setDisable(false);
                    getBarraProgressoCapitulos().setProgress(0);
                    getBarraProgresso().setProgress(0);
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
                });

            }
        };
        Thread t = new Thread(carregaItens);
        t.start();
    }

    private TreeItem<Novel> getTreeData() {
        TreeItem<Novel> itmRoot = new TreeItem<>(new Novel("...", ""));
        for (NovelTabela tabela : TABELAS) {
            tabela.setNovel("...");
            TreeItem<Novel> itmTabela = new TreeItem<>(tabela);
            TreeItem<Novel> itmNovel = null;
            String volumeAnterior = "";
            for (NovelVolume volume : tabela.getVolumes()) {
                // Implementa um nivel por tipo
                if (!volume.getNovel().equalsIgnoreCase(volumeAnterior) || itmNovel == null) {
                    volumeAnterior = volume.getNovel();
                    volume.setBase(tabela.getBase());
                    itmNovel = new TreeItem<>(new Novel(tabela.getBase(), volume.getNovel(), "..."));
                    itmTabela.getChildren().add(itmNovel);
                    itmTabela.setExpanded(true);
                }

                volume.setBase(tabela.getBase());
                TreeItem<Novel> itmVolume = new TreeItem<>(volume);

                for (NovelCapitulo capitulo : volume.getCapitulos()) {
                    capitulo.setBase(tabela.getBase());
                    itmVolume.getChildren().add(new TreeItem<>(capitulo));
                }

                itmNovel.getChildren().add(itmVolume);
            }
            itmRoot.getChildren().add(itmTabela);
            itmRoot.setExpanded(true);
        }
        return itmRoot;
    }

    private void marcarTodosFilhos(TreeItem<Novel> treeItem, Boolean newValue) {
        treeItem.getValue().setProcessar(newValue);
        treeItem.getChildren().forEach(treeItemNivel2 -> marcarTodosFilhos(treeItemNivel2, newValue));
    }

    private void ativaTodosPai(TreeItem<Novel> treeItem, Boolean newValue) {
        if (treeItem.getParent() != null) {
            treeItem.getParent().getValue().setProcessar(newValue);
            ativaTodosPai(treeItem.getParent(), newValue);
        }
    }

    private void editaColunas() {
        // ==== (CHECK-BOX) ===
        treecMacado.setCellValueFactory(
                param -> {
                    TreeItem<Novel> treeItem = param.getValue();
                    Novel item = treeItem.getValue();
                    SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(item.isProcessar());

                    booleanProp.addListener((observable, oldValue, newValue) -> {
                        item.setProcessar(newValue);
                        marcarTodosFilhos(treeItem, newValue);
                        if (newValue) // Somente ativa caso seja true, pois ao menos um nó precisa estar ativo
                            ativaTodosPai(treeItem, newValue);

                        treeBases.refresh();
                    });

                    return booleanProp;
                });

        treecMacado.setCellFactory(p -> {
            CheckBoxTreeTableCellCustom<Novel, Boolean> cell = new CheckBoxTreeTableCellCustom<Novel, Boolean>();
            cell.setAlignment(Pos.CENTER);
            return cell;
        });

    }

    private void linkaCelulas() {
        treecMacado.setCellValueFactory(new TreeItemPropertyValueFactory<>("processar"));
        treecBase.setCellValueFactory(new TreeItemPropertyValueFactory<>("base"));
        treecNovel.setCellValueFactory(new TreeItemPropertyValueFactory<>("novel"));
        treecVolume.setCellValueFactory(new TreeItemPropertyValueFactory<>("volume"));
        treecCapitulo.setCellValueFactory(new TreeItemPropertyValueFactory<>("capitulo"));
        treeBases.setShowRoot(false);

        editaColunas();
    }

    private final Robot robot = new Robot();

    public void initialize(URL arg0, ResourceBundle arg1) {
        cbLinguagem.getItems().addAll(Language.JAPANESE, Language.ENGLISH);
        cbLinguagem.getSelectionModel().selectFirst();

        try {
            cbBase.getItems().setAll(service.getTabelas());
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
            if (autoCompletePopup.getFilteredSuggestions().isEmpty() || cbBase.showingProperty().get() || cbBase.getEditor().getText().isEmpty())
                autoCompletePopup.hide();
            else
                autoCompletePopup.show(cbBase.getEditor());
        });

        cbBase.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        txtNovel.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        linkaCelulas();
    }

    public static URL getFxmlLocate() {
        return NovelsProcessarController.class.getResource("/view/novels/NovelProcessar.fxml");
    }
}
