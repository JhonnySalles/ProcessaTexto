package org.jisho.textosJapones.model.services;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.jisho.textosJapones.components.notification.Notificacoes;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.RevisarDao;
import org.jisho.textosJapones.database.dao.SincronizacaoDao;
import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Sincronizacao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Conexao;
import org.jisho.textosJapones.model.enums.Database;
import org.jisho.textosJapones.model.enums.Notificacao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SincronizacaoServices extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SincronizacaoServices.class);

    private final List<VocabularioDao> daoVocabulario;
    private final List<RevisarDao> daoRevisar;
    private final SincronizacaoDao dao;
    private Sincronizacao sincronizacao = null;
    private Firestore DB;
    private final DateTimeFormatter formaterData = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter formaterDataHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Boolean sincronizando = false;
    public static Boolean processar = false;

    private static ObservableList<Pair<Database, Vocabulario>> sincronizar = FXCollections.observableArrayList();

    private MenuPrincipalController controller;

    public void setObserver(ListChangeListener<? super Pair<Database, Vocabulario>> listener) {
        sincronizar.addListener(listener);
    }

    public SincronizacaoServices(MenuPrincipalController controller) {
        this.controller = controller;

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(this, 0, 5 * 60 * 1000);

        daoVocabulario = new ArrayList<>();
        daoVocabulario.add(DaoFactory.createVocabularioJaponesDao());
        daoVocabulario.add(DaoFactory.createVocabularioInglesDao());

        daoRevisar = new ArrayList<>();
        daoRevisar.add(DaoFactory.createRevisarJaponesDao());
        daoRevisar.add(DaoFactory.createRevisarInglesDao());

        dao = DaoFactory.createSincronizacaoDao();

        try {
            InputStream serviceAccount = new FileInputStream("secrets-firebase.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials).build();
            FirebaseApp.initializeApp(options);

            DB = FirestoreClient.getFirestore();

            sincronizacao = dao.select(Conexao.FIREBASE);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }

        consultar();
    }

    @Override
    public void run() {
        if (processar && !sincronizando)
            sincroniza();
    }

    public static void enviar(Database database, Vocabulario vocabulario) {
        sincronizar.add(new Pair<>(database, vocabulario));
    }

    public void consultar() {
        if (sincronizacao == null)
            return;

        for (VocabularioDao vocab : daoVocabulario) {
            try {
                List<Pair<Database, Vocabulario>> sinc = vocab.selectEnvio(sincronizacao.getEnvio()).parallelStream()
                        .filter(i -> sincronizar.parallelStream().noneMatch(s -> s.getKey().equals(vocab.getTipo()) && s.getValue().equals(i)))
                        .map(i -> new Pair<>(vocab.getTipo(), i)).collect(Collectors.toList());
                if (!sinc.isEmpty())
                    sincronizar.addAll(sinc);
            } catch (ExcessaoBd ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }


    Integer registros = 0;
    String vocabularios;
    String processados;
    private Boolean envia() throws Exception {
        Boolean processado = false;
        vocabularios = "";
        registros = 0;

        if (!sincronizar.isEmpty()) {
            LOGGER.info("Enviando dados a cloud... ");
            List<Pair<Database, Vocabulario>> sinc = sincronizar.parallelStream().sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey())).distinct().collect(Collectors.toList());
            try {
                sincronizar.clear();
                String envio = LocalDateTime.now().format(formaterDataHora);

                List<Database> bases = sinc.parallelStream().map(Pair::getKey).distinct().collect(Collectors.toList());
                for (Database db : bases) {
                    List<Vocabulario> env = sinc.parallelStream().filter(i -> i.getKey().equals(db)).map(Pair::getValue).collect(Collectors.toList());
                    if (!env.isEmpty()) {
                        DocumentReference docRef = DB.collection(db.toString()).document(formaterData.format(LocalDate.now()));
                        Map<String, Object> data = new HashMap<>();
                        for (Vocabulario voc : env) {
                            voc.sincronizacao = envio;
                            data.put(voc.getId().toString(), voc);
                        }
                        ApiFuture<WriteResult> result = docRef.set(data);
                        result.get();
                        registros += env.size();
                        LOGGER.info("Enviado dados a cloud: " + env.size() + " registros (" + db + "). ");
                    }
                }

                if (registros > 0) {
                    processados += "Enviado " + registros + " registro(s). " ;
                    if (!vocabularios.isEmpty())
                        vocabularios = vocabularios.substring(0, vocabularios.lastIndexOf(",")).trim();
                    Platform.runLater(() -> Notificacoes.notificacao(Notificacao.SUCESSO, "Concluído o envio de " + registros + " registros para cloud.", "Sincronizado: " + vocabularios));
                }
                LOGGER.info("Concluído envio de dados a cloud.");

                processado = true;
            } catch (Exception e) {
                sincronizar.addAll(sinc);
                LOGGER.error("Erro ao enviar dados a cloud, adicionado arquivos para novo ciclo.\n" + e.getMessage(), e);
                throw e;
            }
        }

        return processado;
    }

    private Boolean receber() throws Exception {
        Boolean processado = false;
        try {
            LOGGER.info("Recebendo dados a cloud.... ");
            List<Pair<Database, Vocabulario>> lista = new ArrayList<>();

            String atual = LocalDate.now().format(formaterData);

            for (VocabularioDao vocab : daoVocabulario) {
                ApiFuture<QuerySnapshot> query = DB.collection(vocab.getTipo().toString()).get();

                QuerySnapshot querySnapshot = query.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
                for (QueryDocumentSnapshot document : documents) {
                    LocalDate data = LocalDate.parse(document.getId(), formaterData);
                    if (sincronizacao.getRecebimento().toLocalDate().isAfter(data) && !atual.equalsIgnoreCase(document.getId()))
                        continue;

                    for(String key : document.getData().keySet()) {
                        HashMap<String, String> obj = (HashMap<String, String>) document.getData().get(key);
                        LocalDateTime sinc = LocalDateTime.parse(obj.get("sincronizacao"), formaterDataHora);
                        if (sinc.isAfter(sincronizacao.getRecebimento()))
                            lista.add(new Pair<>(vocab.getTipo(), new Vocabulario(key, obj)));
                    }
                }
            }

            LOGGER.info("Processando retorno dados a cloud: " + lista.size() + " registros.");

            vocabularios = "";
            registros = lista.size();

            for (Pair<Database, Vocabulario> sinc : lista) {
                for (VocabularioDao voc : daoVocabulario)
                    if (voc.getTipo().equals(sinc.getKey())) {
                        Vocabulario vocab = voc.select(sinc.getValue().getId());

                        if (vocab == null)
                            vocab = voc.select(sinc.getValue().getVocabulario(), sinc.getValue().getFormaBasica());

                        if (vocab != null) {
                            vocab.merge(sinc.getValue());
                            voc.update(vocab);
                        } else {
                            vocab = sinc.getValue();
                            voc.insert(vocab);
                        }

                        vocabularios += vocab.getVocabulario() + ", ";

                        for (RevisarDao rev : daoRevisar)
                            if (rev.getTipo().equals(sinc.getKey())) {
                                Revisar revisar = rev.select(vocab.getId());
                                if (revisar != null)
                                    rev.delete(revisar);

                                revisar = rev.select(vocab.getVocabulario(), vocab.getFormaBasica());

                                if (revisar != null)
                                    rev.delete(revisar);
                            }
                    }
            }

            if (registros > 0) {
                processados += "Recebido " + registros + " registro(s). " ;
                if (!vocabularios.isEmpty())
                    vocabularios = vocabularios.substring(0, vocabularios.lastIndexOf(",")).trim();
                Platform.runLater(() -> Notificacoes.notificacao(Notificacao.SUCESSO, "Concluído recebimento de " + lista.size() + " registros da cloud.", "Sincronizado: " + vocabularios));
            }

            processado = true;
            LOGGER.info("Concluído recebimento de dados a cloud.");
        } catch (Exception e) {
            LOGGER.error("Erro ao receber dados a cloud.\n" + e.getMessage(), e);
            throw e;
        }
        return processado;
    }

    public boolean sincroniza() {
        Boolean sincronizado = false;

        if (sincronizacao == null)
            return sincronizado;

        try {
            sincronizando = true;
            controller.animacaoSincronizacaoDatabase(true, false);

            processados = "";

            Boolean recebido = receber();
            Boolean enviado = envia();

            if (enviado)
                sincronizacao.setEnvio(LocalDateTime.now());

            if (recebido)
                sincronizacao.setRecebimento(LocalDateTime.now());

            if (enviado || recebido) {
                dao.update(sincronizacao);
                Platform.runLater(() -> controller.setLblLog(processados.trim()));
            } else
                Platform.runLater(() -> controller.setLblLog(""));

            sincronizado = true;
            controller.animacaoSincronizacaoDatabase(false, false);
        } catch (Exception e) {
            controller.animacaoSincronizacaoDatabase(false, true);
        } finally {
            sincronizando = false;
            return sincronizado;
        }
    }

    public Boolean  isConfigurado() {
        return sincronizacao != null;
    }

    public Boolean isSincronizando() {
        return sincronizando;
    }

    public Integer listSize() {
        return sincronizar.size();
    }

}
