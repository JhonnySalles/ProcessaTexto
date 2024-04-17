package org.jisho.textosJapones.model.services;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import javafx.util.Pair;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.RevisarDao;
import org.jisho.textosJapones.database.dao.SincronizacaoDao;
import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.DadosConexao;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.entities.Sincronizacao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Conexao;
import org.jisho.textosJapones.model.enums.Database;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.util.Prop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
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
    private final DateTimeFormatter formaterDia = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter formaterDataHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Boolean sincronizando = false;
    public static Boolean processar = false;

    private static List<Pair<Database, Vocabulario>> sincronizar = new ArrayList<>();

    private MenuPrincipalController controller;

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
            DadosConexao conexao = org.jisho.textosJapones.database.mysql.DB.findConnection(Conexao.FIREBASE);
            InputStream serviceAccount = new FileInputStream("secrets-firebase.json");
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            FirebaseOptions options = FirebaseOptions.builder().setProjectId(conexao.getUsuario()).setCredentials(credentials).build();
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
                List<Pair<Database, Vocabulario>> sinc = vocab.selectEnvio(sincronizacao.getEnvio()).parallelStream().map(i -> new Pair<>(vocab.getTipo(), i)).collect(Collectors.toList());
                if (!sinc.isEmpty())
                    sincronizar.addAll(sinc);
            } catch (ExcessaoBd ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }

    private void envia() throws Exception {
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
                        DocumentReference docRef = DB.collection(db.toString()).document(formaterDia.format(LocalDateTime.now()));
                        Map<String, Object> data = new HashMap<>();
                        for (Vocabulario voc : env) {
                            voc.sincronizacao = envio;
                            data.put(voc.getId().toString(), voc);
                        }
                        ApiFuture<WriteResult> result = docRef.set(data);
                        LOGGER.info("Enviado dados a cloud: " + env.size() + " registros (" + db + ").");
                    }
                }
                LOGGER.info("Concluído envio de dados a cloud.");

                sincronizacao.setEnvio(LocalDateTime.now());
                dao.update(sincronizacao);
            } catch (Exception e) {
                sincronizar.addAll(sinc);
                LOGGER.error("Erro ao enviar dados a cloud, adicionado arquivos para novo ciclo.\n" + e.getMessage(), e);
                throw e;
            }
        }
    }

    private void receber() throws Exception {
        try {
            LOGGER.info("Recebendo dados a cloud.... ");
            List<Pair<Database, Vocabulario>> lista = new ArrayList<>();

            String atual = LocalDateTime.now().format(formaterDia);

            for (VocabularioDao vocab : daoVocabulario) {
                ApiFuture<QuerySnapshot> query = DB.collection(vocab.getTipo().toString()).get();

                QuerySnapshot querySnapshot = query.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
                for (QueryDocumentSnapshot document : documents) {
                    LocalDateTime data = LocalDateTime.parse(document.getId(), formaterDia);
                    if (sincronizacao.getRecebimento().isAfter(data) && !atual.equalsIgnoreCase(document.getId()))
                        continue;

                    Vocabulario item = document.toObject(Vocabulario.class);
                    LocalDateTime sinc = LocalDateTime.parse(item.sincronizacao, formaterDataHora);
                    if (sinc.isAfter(sincronizacao.getRecebimento()))
                        lista.add(new Pair<>(vocab.getTipo(), item));
                }
            }

            LOGGER.info("Processando retorno dados a cloud: " + lista.size() + " itens.");

            for (Pair<Database, Vocabulario> sinc : lista) {
                for (VocabularioDao voc : daoVocabulario)
                    if (voc.getTipo().equals(sinc.getKey())) {
                        Vocabulario vocab = voc.select(sinc.getValue().getId());

                        if (voc == null)
                            vocab = voc.select(sinc.getValue().getVocabulario(), sinc.getValue().getFormaBasica());

                        if (voc != null) {
                            vocab.merge(sinc.getValue());
                            voc.update(vocab);
                        } else {
                            vocab = sinc.getValue();
                            voc.insert(vocab);
                        }

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

            sincronizacao.setRecebimento(LocalDateTime.now());
            dao.update(sincronizacao);

            LOGGER.info("Concluído recebimento de dados a cloud.");
        } catch (Exception e) {
            LOGGER.error("Erro ao receber dados a cloud.\n" + e.getMessage(), e);
            throw e;
        }
    }

    public boolean sincroniza() {
        Boolean sincronizado = false;

        if (sincronizacao == null)
            return sincronizado;

        try {
            sincronizando = true;
            controller.animacaoSincronizacaoDatabase(true, false);
            envia();
            receber();
            sincronizado = true;
            controller.animacaoSincronizacaoDatabase(false, false);
        } catch (Exception e) {
            controller.animacaoSincronizacaoDatabase(false, true);
        } finally {
            sincronizando = false;
            return sincronizado;
        }
    }

}
