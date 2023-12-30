package org.jisho.textosJapones.controller.mangas;

import com.jfoenix.controls.*;
import com.nativejavafx.taskbar.TaskbarProgressbar;
import com.nativejavafx.taskbar.TaskbarProgressbar.Type;
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
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.MangaServices;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MangasTraducaoController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MangasTraducaoController.class);

    @FXML
    private AnchorPane apRoot;

    @FXML
    private JFXComboBox<String> cbBase;

    @FXML
    private JFXTextField txtManga;

    @FXML
    private JFXComboBox<Language> cbLinguagem;

    @FXML
    private Spinner<Integer> spnVolume;

    @FXML
    private Spinner<Double> spnCapitulo;

    @FXML
    private JFXButton btnCarregar;

    @FXML
    private JFXButton btnTraduzir;

    @FXML
    private JFXCheckBox ckbMarcarTodos;

    @FXML
    private TreeTableView<Manga> treeBases;

    @FXML
    private TreeTableColumn<Manga, Boolean> treecMacado;

    @FXML
    private TreeTableColumn<Manga, String> treecBase;

    @FXML
    private TreeTableColumn<Manga, String> treecManga;

    @FXML
    private TreeTableColumn<Manga, Language> treecLinguagem;

    @FXML
    private TreeTableColumn<Manga, Integer> treecVolume;

    @FXML
    private TreeTableColumn<Manga, Float> treecCapitulo;

    private MangaServices service = new MangaServices();
    private ObservableList<MangaTabela> TABELAS;
    private Boolean PAUSAR;

    private MangasController controller;

    public void setControllerPai(MangasController controller) {
        this.controller = controller;
    }

    public MangasController getControllerPai() {
        return controller;
    }

    @FXML
    private void onBtnCarregar() {
        carregar();
    }

    @FXML
    private void onBtnTraduzir() {
        if (btnTraduzir.getAccessibleText().equalsIgnoreCase("GERANDO")) {
            PAUSAR = true;
            return;
        }

        if (TABELAS.size() == 0)
            AlertasPopup.AvisoModal("Aviso", "Nenhum item informado.");

        btnTraduzir.setAccessibleText("GERANDO");
        btnTraduzir.setText("Pausar");
        btnCarregar.setDisable(true);
        treeBases.setDisable(true);

        traduzir();
    }

    @FXML
    private void onBtnMarcarTodos() {
        marcarTodosFilhos(treeBases.getRoot(), ckbMarcarTodos.isSelected());
        treeBases.refresh();
    }

    public void habilitar() {
        treeBases.setDisable(false);
        btnTraduzir.setAccessibleText("GERAR");
        btnTraduzir.setText("Traduzir");
        btnCarregar.setDisable(false);
        TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
    }

    private Integer I, X, XSize, P, traducoes;
    private String error;

    private void traduzir() {
        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        PAUSAR = false;

        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

        progress.getTitulo().setText("Traduzindo...");
        Task<Void> traduzir = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                try {
                    error = "";

                    updateMessage("Traduzindo....");

                    I = 0;
                    traducoes = 0;
                    for (MangaTabela tabela : TABELAS) {
                        I++;

                        if (!tabela.isProcessar()) {
                            Platform.runLater(() -> {
                                if (TaskbarProgressbar.isSupported())
                                    TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, TABELAS.size(),
                                            Type.NORMAL);
                            });

                            continue;
                        }

                        XSize = tabela.getVolumes().size();
                        tabela.getVolumes().forEach(t -> {
                            if (t.isProcessar()) {
                                XSize += t.getCapitulos().size();
                                t.getCapitulos().forEach(c -> XSize += c.getPaginas().size());
                            }
                        });

                        X = 0;
                        for (MangaVolume volume : tabela.getVolumes()) {
                            X++;
                            if (!volume.isProcessar())
                                continue;

                            MangaVolume volumeTraduzido = new MangaVolume(null, volume.getManga(), volume.getVolume(),
                                    Language.PORTUGUESE_GOOGLE, volume.getArquivo());

                            for (MangaCapitulo capitulo : volume.getCapitulos()) {
                                X++;
                                if (!capitulo.isProcessar())
                                    continue;

                                MangaCapitulo capituloTraduzido = new MangaCapitulo(null, capitulo.getManga(),
                                        capitulo.getVolume(), capitulo.getCapitulo(), Language.PORTUGUESE_GOOGLE,
                                        capitulo.getScan(), capitulo.isExtra(), capitulo.isRaw());
                                volumeTraduzido.addCapitulos(capituloTraduzido);

                                P = 0;
                                for (MangaPagina pagina : capitulo.getPaginas()) {
                                    P++;
                                    X++;
                                    updateProgress(X, XSize);
                                    updateMessage(volume.getManga() + " - Volume " + volume.getVolume().toString()
                                            + " Capitulo " + capitulo.getCapitulo().toString() + " Página "
                                            + P + '/' + capitulo.getPaginas().size() + " - " + pagina.getNomePagina());

                                    MangaPagina paginaTraduzido = new MangaPagina(null, pagina.getNomePagina(), pagina.getNumero(), pagina.getHash());
                                    capituloTraduzido.addPaginas(paginaTraduzido);

                                    for (MangaTexto texto : pagina.getTextos()) {
                                        MangaTexto textoTraduzido = new MangaTexto(null, "", texto.getSequencia(), texto.getX1(), texto.getY1(), texto.getX2(), texto.getY2());
                                        paginaTraduzido.addTexto(textoTraduzido);

                                        traducoes++;

                                        if (traducoes > 3000) {
                                            traducoes = 0;
                                            MenuPrincipalController.getController().setContaGoogle(Util
                                                    .next(MenuPrincipalController.getController().getContaGoogle()));
                                        }

                                        textoTraduzido.setTexto(ScriptGoogle.translate(capitulo.getLingua().getSigla(),
                                                Language.PORTUGUESE.getSigla(), texto.getTexto(),
                                                MenuPrincipalController.getController().getContaGoogle()));

                                        if (PAUSAR)
                                            return null;
                                    }
                                }
                            }
                            service.salvarTraducao(tabela.getBase(), volumeTraduzido);
                        }

                        Platform.runLater(() -> {
                            if (TaskbarProgressbar.isSupported())
                                TaskbarProgressbar.showCustomProgress(Run.getPrimaryStage(), I, TABELAS.size(),
                                        Type.NORMAL);
                        });
                    }

                } catch (Exception e) {
                    
                    LOGGER.error(e.getMessage(), e);
                    error = e.getMessage();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.failed();
                Platform.runLater(() -> {

                    progress.getBarraProgresso().progressProperty().unbind();
                    progress.getLog().textProperty().unbind();
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage());
                    MenuPrincipalController.getController().destroiBarraProgresso(progress, "");

                    if (!error.isEmpty())
                        AlertasPopup.ErroModal("Erro", error);
                    else if (!PAUSAR) {
                        AlertasPopup.AvisoModal("Aviso", "Tradução concluida.");
                        TABELAS.clear();
                        DADOS.getChildren().clear();
                    }

                    habilitar();
                });

            }

            @Override
            protected void failed() {
                super.failed();
                LOGGER.warn("Erro na thread tradução: " + super.getMessage());
                System.out.print("Erro na thread tradução: " + super.getMessage());
            }
        };

        progress.getBarraProgresso().progressProperty().bind(traduzir.progressProperty());
        progress.getLog().textProperty().bind(traduzir.messageProperty());

        Thread t = new Thread(traduzir);
        t.start();
    }

    private String BASE;
    private String MANGA;
    private Integer VOLUME;
    private Float CAPITULO;
    private Language LINGUAGEM;
    private TreeItem<Manga> DADOS;

    private void carregar() {
        MenuPrincipalController.getController().getLblLog().setText("Carregando...");

        if (TaskbarProgressbar.isSupported())
            TaskbarProgressbar.showIndeterminateProgress(Run.getPrimaryStage());

        btnCarregar.setDisable(true);
        btnTraduzir.setDisable(true);
        treeBases.setDisable(true);
        BASE = cbBase.getValue() != null ? cbBase.getValue().trim() : "";
        MANGA = txtManga.getText().trim();
        VOLUME = spnVolume.getValue();
        CAPITULO = spnCapitulo.getValue().floatValue();
        LINGUAGEM = cbLinguagem.getSelectionModel().getSelectedItem();

        // Criacao da thread para que esteja validando a conexao e nao trave a tela.
        Task<Void> carregaItens = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                try {
                    service = new MangaServices();
                    TABELAS = FXCollections
                            .observableArrayList(service.selectAll(BASE, MANGA, VOLUME, CAPITULO, LINGUAGEM));
                    DADOS = getTreeData();
                } catch (ExcessaoBd e) {
                    
                    LOGGER.error(e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> {
                    treeBases.setRoot(DADOS);

                    MenuPrincipalController.getController().getLblLog().setText("");
                    TaskbarProgressbar.stopProgress(Run.getPrimaryStage());

                    ckbMarcarTodos.setSelected(true);
                    btnCarregar.setDisable(false);
                    btnTraduzir.setDisable(false);
                    treeBases.setDisable(false);
                });

            }

            @Override
            protected void failed() {
                super.failed();
                LOGGER.warn("Erro na thread de carregamento de itens: " + super.getMessage());
                System.out.print("Erro na thread de carregamento de itens: " + super.getMessage());
            }
        };
        Thread t = new Thread(carregaItens);
        t.start();
    }

    private TreeItem<Manga> getTreeData() {
        TreeItem<Manga> itmRoot = new TreeItem<Manga>(new Manga("...", ""));
        for (MangaTabela tabela : TABELAS) {
            tabela.setManga("...");
            TreeItem<Manga> itmTabela = new TreeItem<Manga>(tabela);
            TreeItem<Manga> itmManga = null;
            TreeItem<Manga> itmLingua = null;
            String volumeAnterior = "";
            Language linguagemAnterior = null;

            for (MangaVolume volume : tabela.getVolumes()) {

                // Implementa um nivel por tipo
                if (!volume.getManga().equalsIgnoreCase(volumeAnterior) || itmManga == null) {
                    volumeAnterior = volume.getManga();
                    itmManga = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(), "..."));
                    itmTabela.getChildren().add(itmManga);
                    itmTabela.setExpanded(true);

                    itmLingua = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(),
                            volume.getLingua().getSigla().toUpperCase()));
                    linguagemAnterior = volume.getLingua();
                    itmManga.getChildren().add(itmLingua);
                }

                if (linguagemAnterior == null || volume.getLingua().compareTo(linguagemAnterior) != 0) {
                    itmLingua = new TreeItem<Manga>(new Manga(tabela.getBase(), volume.getManga(),
                            volume.getLingua().getSigla().toUpperCase()));
                    linguagemAnterior = volume.getLingua();
                    itmManga.getChildren().add(itmLingua);
                }

                volume.setLinguagem(linguagemAnterior.getSigla().toUpperCase());
                volume.setBase(tabela.getBase());
                TreeItem<Manga> itmVolume = new TreeItem<Manga>(volume);

                for (MangaCapitulo capitulo : volume.getCapitulos()) {
                    capitulo.setBase(tabela.getBase());
                    capitulo.setNomePagina("...");
                    capitulo.setLinguagem(linguagemAnterior.getSigla().toUpperCase());
                    itmVolume.getChildren().add(new TreeItem<Manga>(capitulo));
                }

                itmLingua.getChildren().add(itmVolume);
            }
            itmRoot.getChildren().add(itmTabela);
            itmRoot.setExpanded(true);
        }
        return itmRoot;
    }

    private void marcarTodosFilhos(TreeItem<Manga> treeItem, Boolean newValue) {
        treeItem.getValue().setProcessar(newValue);
        treeItem.getChildren().forEach(treeItemNivel2 -> marcarTodosFilhos(treeItemNivel2, newValue));
    }

    private void ativaTodosPai(TreeItem<Manga> treeItem, Boolean newValue) {
        if (treeItem.getParent() != null) {
            treeItem.getParent().getValue().setProcessar(newValue);
            ativaTodosPai(treeItem.getParent(), newValue);
        }
    }

    private void editaColunas() {
        // ==== (CHECK-BOX) ===
        treecMacado.setCellValueFactory(
                new Callback<TreeTableColumn.CellDataFeatures<Manga, Boolean>, ObservableValue<Boolean>>() {

                    @Override
                    public ObservableValue<Boolean> call(TreeTableColumn.CellDataFeatures<Manga, Boolean> param) {
                        TreeItem<Manga> treeItem = param.getValue();
                        Manga item = treeItem.getValue();
                        SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(item.isProcessar());

                        booleanProp.addListener(new ChangeListener<Boolean>() {
                            @Override
                            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                                                Boolean newValue) {
                                item.setProcessar(newValue);
                                marcarTodosFilhos(treeItem, newValue);
                                if (newValue) // Somente ativa caso seja true, pois ao menos um nó precisa estar ativo
                                    ativaTodosPai(treeItem, newValue);

                                treeBases.refresh();
                            }
                        });

                        return booleanProp;
                    }
                });

        treecMacado.setCellFactory(new Callback<TreeTableColumn<Manga, Boolean>, TreeTableCell<Manga, Boolean>>() {
            @Override
            public TreeTableCell<Manga, Boolean> call(TreeTableColumn<Manga, Boolean> p) {
                CheckBoxTreeTableCellCustom<Manga, Boolean> cell = new CheckBoxTreeTableCellCustom<Manga, Boolean>();
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        });

    }

    private void linkaCelulas() {
        treecMacado.setCellValueFactory(new TreeItemPropertyValueFactory<Manga, Boolean>("processar"));
        treecBase.setCellValueFactory(new TreeItemPropertyValueFactory<>("base"));
        treecManga.setCellValueFactory(new TreeItemPropertyValueFactory<>("manga"));
        treecLinguagem.setCellValueFactory(new TreeItemPropertyValueFactory<>("linguagem"));
        treecVolume.setCellValueFactory(new TreeItemPropertyValueFactory<>("volume"));
        treecCapitulo.setCellValueFactory(new TreeItemPropertyValueFactory<>("capitulo"));
        treeBases.setShowRoot(false);

        editaColunas();

    }

    private final Robot robot = new Robot();

    public void initialize(URL arg0, ResourceBundle arg1) {

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
            if (autoCompletePopup.getFilteredSuggestions().isEmpty() || cbBase.showingProperty().get()
                    || cbBase.getEditor().getText().isEmpty())
                autoCompletePopup.hide();
            else
                autoCompletePopup.show(cbBase.getEditor());
        });

        cbBase.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER))
                    robot.keyPress(KeyCode.TAB);
            }
        });

        cbLinguagem.getItems().addAll(Language.ENGLISH, Language.JAPANESE);
        cbLinguagem.getSelectionModel().selectFirst();

        cbLinguagem.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ESCAPE))
                    cbLinguagem.getSelectionModel().clearSelection();
                else if (ke.getCode().equals(KeyCode.ENTER))
                    robot.keyPress(KeyCode.TAB);
            }
        });

        txtManga.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER))
                    robot.keyPress(KeyCode.TAB);
            }
        });

        spnVolume.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER))
                    robot.keyPress(KeyCode.TAB);
            }
        });

        spnCapitulo.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER))
                    robot.keyPress(KeyCode.TAB);
            }
        });

        spnVolume.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999, 0));
        spnCapitulo.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 99999, 0, 1));

        linkaCelulas();
    }

    public static URL getFxmlLocate() {
        return MangasTraducaoController.class.getResource("/view/mangas/MangaTraducao.fxml");
    }
}
