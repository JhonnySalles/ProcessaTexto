package org.jisho.textosJapones.controller;

import com.google.firebase.database.*;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.util.Callback;
import org.jisho.textosJapones.components.notification.Notificacoes;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Triple;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Database;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.model.services.RevisarInglesServices;
import org.jisho.textosJapones.model.services.RevisarJaponesServices;
import org.jisho.textosJapones.model.services.VocabularioInglesServices;
import org.jisho.textosJapones.model.services.VocabularioJaponesServices;
import org.jisho.textosJapones.processar.*;
import org.jisho.textosJapones.processar.scriptGoogle.ScriptGoogle;
import org.jisho.textosJapones.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RevisarController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RevisarController.class);

    @FXML
    private AnchorPane apRoot;

    @FXML
    private JFXComboBox<Language> cbLinguagem;

    @FXML
    private Label lblRestantes;

    @FXML
    private JFXButton btnFormatar;

    @FXML
    private JFXCheckBox cbDuplicados;

    @FXML
    private JFXCheckBox cbSubstituirKanji;

    @FXML
    private JFXButton btnExcluir;

    @FXML
    private JFXButton btnSalvar;

    @FXML
    private JFXButton btnNovo;

    @FXML
    private JFXButton btnSalvarAux;

    @FXML
    private ListView<Triple<Vocabulario, Database, DatabaseReference>> lvProcesssar;

    @FXML
    private JFXCheckBox cbLegenda;

    @FXML
    private JFXCheckBox cbManga;

    @FXML
    private JFXCheckBox cbNovel;

    @FXML
    private JFXTextField txtVocabulario;

    @FXML
    private JFXTextField txtSimilar;

    @FXML
    private JFXCheckBox cbSimilar;

    @FXML
    private JFXTextField txtPesquisar;

    @FXML
    private JFXCheckBox cbCorrecao;

    @FXML
    private JFXButton btnTraduzir;

    @FXML
    private JFXButton btnJapaneseTanoshi;

    @FXML
    private JFXButton btnJapanDict;

    @FXML
    private JFXButton btnJisho;

    @FXML
    private JFXButton btnKanshudo;

    @FXML
    private JFXTextArea txtAreaIngles;

    @FXML
    private JFXTextArea txtAreaPortugues;

    @FXML
    private JFXTextField txtExclusao;

    private final RevisarJaponesServices revisarJapones = new RevisarJaponesServices();
    private final VocabularioJaponesServices vocabularioJapones = new VocabularioJaponesServices();

    private final RevisarInglesServices revisarIngles = new RevisarInglesServices();
    private final VocabularioInglesServices vocabularioIngles = new VocabularioInglesServices();

    private List<Revisar> similar;

    private ObservableList<Triple<Vocabulario, Database, DatabaseReference>> processsar = FXCollections.observableArrayList();
    private Revisar revisando;
    private Vocabulario corrigindo;
    private final Robot robot = new Robot();

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://bilingual-reader-272ac-default-rtdb.firebaseio.com/");
    private DatabaseReference reference = null;

    @FXML
    private void onBtnSalvar() {
        if ((revisando == null && corrigindo == null) || txtAreaPortugues.getText().isEmpty())
            return;

        String texto = txtAreaPortugues.getText();
        if (texto.contains("\n"))
            texto = texto.replaceAll("\n", " ").trim();

        Boolean error = false;
        if (revisando != null) {
            revisando.setPortugues(texto);
            revisando.setIngles(Util.removeDuplicate(revisando.getIngles()));

            Vocabulario palavra = Revisar.toVocabulario(revisando);

            for (Revisar obj : similar)
                obj.setPortugues(revisando.getPortugues());

            List<Vocabulario> lista = Revisar.toVocabulario(similar);

            try {
                if (cbLinguagem.getSelectionModel().getSelectedItem() != null && cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH)) {
                    vocabularioIngles.insert(palavra);
                    vocabularioIngles.insert(lista);

                    revisarIngles.delete(revisando);
                    revisarIngles.delete(similar);
                } else {
                    vocabularioJapones.insert(palavra);
                    vocabularioJapones.insert(lista);

                    revisarJapones.delete(revisando);
                    revisarJapones.delete(similar);
                }

                removeFiredac(palavra);
            } catch (ExcessaoBd e) {
                LOGGER.error(e.getMessage(), e);
                error = true;
            }

        } else if (corrigindo != null) {
            corrigindo.setPortugues(texto);

            try {
                if (cbLinguagem.getSelectionModel().getSelectedItem() != null && cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH))
                    vocabularioIngles.insertOrUpdate(corrigindo);
                else
                    vocabularioJapones.insertOrUpdate(corrigindo);

                removeFiredac(corrigindo);
            } catch (ExcessaoBd e) {
                LOGGER.error(e.getMessage(), e);
                error = true;
            }
        } else
            error = true;

        if (!error) {
            limpaCampos();
            pesquisar();
        }
    }

    @FXML
    private void onBtnExcluir() {
        if (revisando == null)
            return;

        Boolean error = false;
        if (revisando != null) {
            try {
                if (cbLinguagem.getSelectionModel().getSelectedItem() != null && cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH))
                    revisarIngles.delete(revisando);
                else
                    revisarJapones.delete(revisando);
            } catch (ExcessaoBd e) {

                LOGGER.error(e.getMessage(), e);
                error = true;
            }
        } else
            error = true;

        if (!error) {
            limpaCampos();
            pesquisar();
        }
    }

    @FXML
    private void onBtnFormatar() {
        if (txtAreaPortugues.getText().isEmpty())
            return;

        if (cbDuplicados.isSelected())
            txtAreaPortugues.setText(Util.removeDuplicate(txtAreaPortugues.getText()));
        else
            txtAreaPortugues.setText(Util.normalize(txtAreaPortugues.getText()));
    }

    @FXML
    private void onBtnNovo() {
        limpaCampos();
        pesquisar();
    }

    @FXML
    private void onBtnTraduzir() {
        if (txtAreaIngles.getText().isEmpty())
            return;

        try {
            String texto = Util.normalize(ScriptGoogle.translate(Language.ENGLISH.getSigla(), Language.PORTUGUESE.getSigla(), txtAreaIngles.getText(), MenuPrincipalController.getController().getContaGoogle()));
            txtAreaPortugues.setText(Util.normalize(texto));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @FXML
    private void onBtnJapaneseTanoshi() {
        if (txtVocabulario.getText().isEmpty())
            return;

        txtAreaIngles.setText(TanoshiJapanese.processa(txtVocabulario.getText()));
        onBtnTraduzir();
    }

    @FXML
    private void onBtnTangorin() {
        if (txtVocabulario.getText().isEmpty())
            return;

        txtAreaIngles.setText(Tangorin.processa(txtVocabulario.getText()));
        onBtnTraduzir();
    }

    @FXML
    private void onBtnJapanDict() {
        if (txtVocabulario.getText().isEmpty())
            return;

        txtAreaIngles.setText(JapanDict.processa(txtVocabulario.getText()));
        onBtnTraduzir();
    }

    @FXML
    private void onBtnJisho() {
        if (txtVocabulario.getText().isEmpty())
            return;

        txtAreaIngles.setText(Jisho.processa(txtVocabulario.getText()));
        onBtnTraduzir();
    }

    @FXML
    private void onBtnKanshudo() {
        if (txtVocabulario.getText().isEmpty())
            return;

        txtAreaIngles.setText(Kanshudo.processa(txtVocabulario.getText()));
        onBtnTraduzir();
    }

    public AnchorPane getRoot() {
        return apRoot;
    }

    public void setLegenda(Boolean ativo) {
        cbLegenda.setSelected(ativo);
    }

    public void setManga(Boolean ativo) {
        cbManga.setSelected(ativo);
    }

    public void setNovel(Boolean ativo) {
        cbNovel.setSelected(ativo);
    }

    private void limpaCampos() {
        try {
            String qtd = (cbLinguagem.getSelectionModel().getSelectedItem() != null && cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH)) ? revisarIngles.selectQuantidadeRestante() : revisarJapones.selectQuantidadeRestante();
            lblRestantes.setText("Restante " + qtd + " palavras.");
        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
            lblRestantes.setText("Restante 0 palavras.");
        }
        corrigindo = null;
        revisando = null;
        limpaTextos();
        txtPesquisar.setText("");
    }

    private void limpaTextos() {
        txtVocabulario.setText("");
        txtSimilar.setText("");
        txtAreaIngles.setText("");
        txtAreaPortugues.setText("");
    }

    private Boolean pesquisaCorrecao(String pesquisar) throws ExcessaoBd {
        corrigindo = (cbLinguagem.getSelectionModel().getSelectedItem() != null && cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH)) ? vocabularioIngles.select(pesquisar) : vocabularioJapones.select(pesquisar, pesquisar);
        if (corrigindo != null && !corrigindo.getPortugues().isEmpty()) {
            limpaTextos();
            cbCorrecao.setSelected(true);
            if (corrigindo != null) {
                txtVocabulario.setText(corrigindo.getVocabulario());
                txtAreaIngles.setText(corrigindo.getIngles());
                txtAreaPortugues.setText(Util.normalize(corrigindo.getPortugues()));
            }

            return true;
        } else
            return false;
    }

    private Boolean pesquisaRevisao(String pesquisar) throws ExcessaoBd {
        if (cbLinguagem.getSelectionModel().getSelectedItem() != null && cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH))
            revisando = revisarIngles.selectRevisar(pesquisar, cbLegenda.isSelected(), cbManga.isSelected(), cbNovel.isSelected());
        else
            revisando = revisarJapones.selectRevisar(pesquisar, cbLegenda.isSelected(), cbManga.isSelected(), cbNovel.isSelected());

        if (revisando != null) {
            limpaTextos();
            cbCorrecao.setSelected(false);
            if (cbSimilar.isSelected()) {
                if (cbLinguagem.getSelectionModel().getSelectedItem() != null && cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH))
                    similar = revisarIngles.selectSimilar(revisando.getVocabulario(), revisando.getIngles());
                else
                    similar = revisarJapones.selectSimilar(revisando.getVocabulario(), revisando.getIngles());
            } else
                similar = new ArrayList<>();

            txtVocabulario.setText(revisando.getVocabulario());
            txtAreaIngles.setText(revisando.getIngles());
            txtAreaPortugues.setText(Util.normalize(revisando.getPortugues()));

            if (similar.size() > 0)
                txtSimilar.setText(similar.stream().map(Revisar::getVocabulario).collect(Collectors.joining(", ")));
            else
                txtSimilar.setText("");

            return true;
        } else
            return false;
    }

    public void pesquisar() {
        txtPesquisar.setUnFocusColor(Color.web("#106ebe"));
        String pesquisar = txtPesquisar.getText().trim();

        try {
            cbCorrecao.selectedProperty().removeListener(listenerCorrecao);

            if (pesquisar.isEmpty())
                pesquisaRevisao(pesquisar);
            else if (cbCorrecao.isSelected() && pesquisaCorrecao(pesquisar))
                return;
            else if (pesquisaRevisao(pesquisar))
                return;
            else if (pesquisaCorrecao(pesquisar))
                return;

            if (!pesquisar.isEmpty()) {
                txtPesquisar.setUnFocusColor(Color.RED);

                if (cbCorrecao.isSelected())
                    limpaCampos();
            }
        } catch (ExcessaoBd e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            cbCorrecao.selectedProperty().addListener(listenerCorrecao);
        }
    }

    public void iniciaFirebase() {
        if (reference == null) {
            reference = database.getReference("anki");
            reference.addValueEventListener(new ValueEventListener() {

                private void excluir(DataSnapshot linguagem) {
                    try {
                        Database database = Database.valueOf(linguagem.getKey().toUpperCase());
                        switch (database) {
                            case INGLES:
                                for (DataSnapshot vocab : linguagem.getChildren()) {
                                    vocabularioIngles.insertExclusao(vocab.getKey());
                                    vocab.getRef().removeValueAsync();
                                }
                                break;
                            case JAPONES:
                                for (DataSnapshot vocab : linguagem.getChildren()) {
                                    vocabularioJapones.insertExclusao(vocab.getKey());
                                    vocab.getRef().removeValueAsync();
                                }
                                break;
                        }
                    } catch (Exception e) {
                        LOGGER.error("Erro ao processar a exclusão.", e);
                    }
                }

                private List<Triple<Vocabulario, Database, DatabaseReference>> processa(DataSnapshot linguagem) {
                    List<Triple<Vocabulario, Database, DatabaseReference>> lista = new ArrayList<>();
                    Database database = Database.valueOf(linguagem.getKey().toUpperCase());

                    for (DataSnapshot vocab : linguagem.getChildren()) {
                        HashMap<String, String> obj = (HashMap<String, String>) vocab.getValue();
                        Vocabulario vocabulario = new Vocabulario(obj.get("vocabulario"), obj.get("portugues"));
                        lista.add(new Triple<>(vocabulario, database, vocab.getRef()));
                    }

                    return lista;
                }

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try {
                        List<Triple<Vocabulario, Database, DatabaseReference>> lista = new ArrayList<>();

                        for (DataSnapshot linguagem : dataSnapshot.getChildren()) {
                            switch (linguagem.getKey().toUpperCase()) {
                                case "EXCLUIR":
                                    for (DataSnapshot excluir : linguagem.getChildren())
                                        excluir(excluir);
                                    break;
                                default:
                                    lista.addAll(processa(linguagem));
                            }
                        }

                        Platform.runLater(() -> processsar.setAll(lista));
                    } catch (Exception e) {
                        LOGGER.error("Erro ao obter a lista de revisão do firebase.", e);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    LOGGER.error("Erro ao obter a database do firebase.", databaseError);
                }
            });
        }
    }

    private void removeFiredac(Vocabulario vocabulario) {
        if (!processsar.isEmpty() && vocabulario != null) {
            Optional<Triple<Vocabulario, Database, DatabaseReference>> item = processsar.parallelStream().filter(i -> i.first.getVocabulario().equalsIgnoreCase(vocabulario.getVocabulario())).findFirst();
            if (item.isPresent()) {
                processsar.remove(item.get());
                item.get().third.removeValueAsync();
            }
        }
    }

    final private String allFlag = ".*";

    final private String japaneseCopy = "([\u3000-\u303f\u3040-\u309f\u30a0-\u30ff\uff00-\uff9f\u4e00-\u9faf\u3400-\u4dbf]+)\\s-\\s(((?! - ).)*¹?)";
    final private String englishCopy = "([A-Za-z0-9']+)\\s-\\s(((?! - ).)*¹?)";
    final private String japanese = "[\u3041-\u9FAF]";
    final private String notJapanese = "[A-Za-z0-9 ,;.à-úÀ-ú\\[\\]\\-\\(\\)]";
    private String frasePortugues = "";

    private ChangeListener<Boolean> listenerCorrecao;
    private ChangeListener<? super String> listenerReplace;

    public void initialize(URL arg0, ResourceBundle arg1) {
        cbLinguagem.getItems().addAll(Language.JAPANESE, Language.ENGLISH);
        cbLinguagem.getSelectionModel().selectFirst();
        cbLinguagem.setOnAction(e -> {
            switch (cbLinguagem.getSelectionModel().getSelectedItem()) {
                case JAPANESE -> cbSubstituirKanji.setSelected(true);
                case ENGLISH -> cbSubstituirKanji.setSelected(false);
            }
            limpaCampos();
            pesquisar();
        });

        limpaCampos();

        txtAreaPortugues.textProperty().addListener((o, oldVal, newVal) -> {
            if (cbSubstituirKanji.isSelected()) {
                if (newVal.matches(allFlag + japanese + allFlag))
                    txtAreaPortugues.setText(newVal.replaceFirst(" - ", "").replaceAll(japanese, ""));
            } else {
                if (newVal.contains("-"))
                    txtAreaPortugues.setText(newVal.substring(newVal.indexOf("-")).replaceFirst("-", "").replaceAll("¹", "").trim());
            }
        });

        listenerReplace = (o, oldVal, newVal) -> {
            try {
                txtPesquisar.textProperty().removeListener(listenerReplace);
                if (cbSubstituirKanji.isSelected()) {
                    Matcher matcher = Pattern.compile(japaneseCopy).matcher(newVal.trim());
                    if (matcher.find()) {
                        txtPesquisar.setText(matcher.group(1));
                        frasePortugues = matcher.group(2);
                    } else if (newVal.matches(allFlag + notJapanese + allFlag)) {
                        String texto = newVal.replaceFirst(" - ", "").replaceAll("¹", "");
                        txtPesquisar.setText(texto.replaceAll(notJapanese, ""));

                        if (newVal.matches(allFlag + japanese + allFlag))
                            frasePortugues = texto.replaceAll(japanese, "");
                    }
                } else {
                    Matcher matcher = Pattern.compile(englishCopy).matcher(newVal.trim());
                    if (matcher.find()) {
                        txtPesquisar.setText(matcher.group(1));
                        frasePortugues = matcher.group(2);
                    } else if (newVal.contains("-")) {
                        String texto = newVal.substring(0, newVal.indexOf("-")).replaceFirst("-", "").trim();
                        txtPesquisar.setText(texto);
                        frasePortugues = newVal.replaceFirst(texto, "").replaceFirst("-", "").replaceAll("¹", "").trim();
                    }
                }
            } finally {
                txtPesquisar.textProperty().addListener(listenerReplace);
            }
        };

        txtPesquisar.textProperty().addListener(listenerReplace);

        txtPesquisar.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (oldVal) {
                txtPesquisar.setUnFocusColor(Color.web("#106ebe"));
                pesquisar();

                if (revisando != null && !frasePortugues.isEmpty()) {
                    txtAreaPortugues.setText(frasePortugues);
                    onBtnFormatar();
                }

                frasePortugues = "";
            }
        });

        txtPesquisar.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        txtExclusao.textProperty().addListener((o, oldVal, newVal) -> {
            String exclusao = newVal.trim().toLowerCase();
            if (exclusao.contains("-"))
                txtExclusao.setText(exclusao.substring(0, exclusao.indexOf("-")).replaceFirst("-", "").trim());
        });

        txtExclusao.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (oldVal) {
                if (txtExclusao.getText() != null && !txtExclusao.getText().isEmpty()) {
                    try {
                        String exclusao = txtExclusao.getText().trim().toLowerCase();
                        Boolean sucesso = false;
                        switch (cbLinguagem.getSelectionModel().getSelectedItem()) {
                            case JAPANESE:
                                if (exclusao.matches(allFlag + japanese + allFlag)) {
                                    vocabularioJapones.insertExclusao(txtExclusao.getText().trim().toLowerCase());
                                    sucesso = true;
                                }
                                break;
                            case ENGLISH:
                                if (exclusao.matches(allFlag + "[A-Za-z\\d]" + allFlag)) {
                                    vocabularioIngles.insertExclusao(txtExclusao.getText().trim().toLowerCase());
                                    sucesso = true;
                                }
                                break;
                        }
                        if (sucesso) {
                            txtExclusao.setText("");
                            Notificacoes.notificacao(Notificacao.SUCESSO, "Salvo.", "Exclusão salvo com sucesso. (" + exclusao + ")");
                        } else
                            Notificacoes.notificacao(Notificacao.AVISO, "Alerta.", "Verifique a linguagem de exclusão ou a existência de caracteres especiais.");
                    } catch (ExcessaoBd e) {
                        Notificacoes.notificacao(Notificacao.ERRO, "Erro.", "Erro ao salvar exclusão. \n" + e.getMessage());
                    }
                }
            }
        });

        txtExclusao.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER))
                robot.keyPress(KeyCode.TAB);
        });

        listenerCorrecao = (o, oldVal, newVal) -> {
            if (newVal)
                pesquisar();
            else
                limpaCampos();
        };

        cbCorrecao.selectedProperty().addListener(listenerCorrecao);

        cbSimilar.selectedProperty().addListener((o, oldVal, newVal) -> {
            if (cbSimilar.isSelected())
                pesquisar();
            else {
                txtSimilar.setText("");
                similar = new ArrayList<>();
            }
        });

        cbLegenda.selectedProperty().addListener((o, oldVal, newVal) -> pesquisar());

        cbManga.selectedProperty().addListener((o, oldVal, newVal) -> pesquisar());

        cbNovel.selectedProperty().addListener((o, oldVal, newVal) -> pesquisar());

        lvProcesssar.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Triple<Vocabulario, Database, DatabaseReference>> call(ListView<Triple<Vocabulario, Database, DatabaseReference>> list) {
                ListCell<Triple<Vocabulario, Database, DatabaseReference>> cell = new ListCell<>() {
                    @Override
                    public void updateItem(Triple<Vocabulario, Database, DatabaseReference> item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(null);
                        if (empty || item == null)
                            setGraphic(null);
                        else
                            setText(item.first.getVocabulario());
                    }
                };

                return cell;
            }
        });

        lvProcesssar.setOnMouseClicked(click -> {
            if (click.getClickCount() == 1 && !lvProcesssar.getItems().isEmpty()) {
                Triple<Vocabulario, Database, DatabaseReference> voc = lvProcesssar.getSelectionModel().getSelectedItem();

                if (voc != null) {
                    switch (voc.second) {
                        case INGLES -> {
                            if (!cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.ENGLISH))
                                cbLinguagem.getSelectionModel().select(Language.ENGLISH);
                        }
                        case JAPONES -> {
                            if (!cbLinguagem.getSelectionModel().getSelectedItem().equals(Language.JAPANESE))
                                cbLinguagem.getSelectionModel().select(Language.JAPANESE);
                        }
                    }

                    txtPesquisar.setText(voc.first.getVocabulario());
                    pesquisar();
                    txtAreaPortugues.setText(voc.first.getPortugues());
                    onBtnFormatar();

                    Toolkit.getDefaultToolkit()
                            .getSystemClipboard()
                            .setContents(new StringSelection(voc.first.getVocabulario()), null);
                }
            }
        });

        processsar.addListener((ListChangeListener<? super Triple<Vocabulario, Database, DatabaseReference>>) (observable) -> lvProcesssar.setVisible(!observable.getList().isEmpty()));

        lvProcesssar.setItems(processsar);
        lvProcesssar.setVisible(false);
        lvProcesssar.managedProperty().bind(lvProcesssar.visibleProperty());

        pesquisar();
    }

    public static URL getFxmlLocate() {
        return RevisarController.class.getResource("/view/Revisar.fxml");
    }

}
