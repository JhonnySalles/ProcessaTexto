package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.database.dao.SincronizacaoDao
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Database
import br.com.fenix.processatexto.model.enums.Notificacao
import com.google.api.core.ApiFuture
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.InputStream
import java.sql.SQLException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors


class SincronizacaoServices(controller: MenuPrincipalController) : TimerTask() {

    private val daoVocabulario: List<VocabularioDao>
    private val daoRevisar: List<RevisarDao>
    private val dao: SincronizacaoDao
    private var sincronizacao: Sincronizacao? = null
    private var DB: Firestore? = null
    private val formaterData: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formaterDataHora: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    var isSincronizando = false
        private set
    private val controller: MenuPrincipalController

    init {
        this.controller = controller
        val timer = Timer(true)
        timer.scheduleAtFixedRate(this, 0, 5 * 60 * 1000)
        daoVocabulario = ArrayList()
        daoVocabulario.add(DaoFactory.createVocabularioJaponesDao())
        daoVocabulario.add(DaoFactory.createVocabularioInglesDao())
        daoRevisar = ArrayList()
        daoRevisar.add(DaoFactory.createRevisarJaponesDao())
        daoRevisar.add(DaoFactory.createRevisarInglesDao())
        dao = DaoFactory.createSincronizacaoDao()
        try {
            val serviceAccount: InputStream = FileInputStream("secrets-firebase.json")
            val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)
            val options: FirebaseOptions = FirebaseOptions.builder().setCredentials(credentials).build()
            FirebaseApp.initializeApp(options)
            DB = FirestoreClient.getFirestore()
            sincronizacao = dao.select(Conexao.FIREBASE).get()
        } catch (ex: Exception) {
            LOGGER.error(ex.message, ex)
        }
        consultar()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SincronizacaoServices::class.java)
        var processar = false
        private val sincronizar: ObservableList<Pair<Database, Vocabulario>> = FXCollections.observableArrayList()

        fun enviar(database: Database, vocabulario: Vocabulario) = sincronizar.add(Pair(database, vocabulario))
    }

    fun setObserver(listener: ListChangeListener<in Pair<Database, Vocabulario>>) = sincronizar.addListener(listener)

    override fun run() {
        if (processar && !isSincronizando)
            sincroniza()
    }

    fun consultar() {
        if (sincronizacao == null)
            return

        for (vocab in daoVocabulario) {
            try {
                val sinc: List<Pair<Database, Vocabulario>> = vocab.selectEnvio(sincronizacao!!.envio).parallelStream()
                    .filter { i -> sincronizar.parallelStream().noneMatch { s -> s.first.equals(vocab.tipo) && s.second.equals(i) } }
                    .map { i -> Pair(vocab.tipo, i) }.collect(Collectors.toList())

                if (sinc.isNotEmpty())
                    sincronizar.addAll(sinc)
            } catch (ex: SQLException) {
                LOGGER.error(ex.message, ex)
            }
        }
    }

    var registros = 0
    var vocabularios: String = ""
    var processados: String = ""


    @Throws(Exception::class)
    private fun envia(): Boolean {
        var processado = false
        vocabularios = ""
        registros = 0
        if (!sincronizar.isEmpty()) {
            LOGGER.info("Enviando dados a cloud... ")
            val sinc: List<Pair<Database, Vocabulario>> = sincronizar.parallelStream().sorted { o1, o2 -> o2.first.compareTo(o1.first) }
                .distinct().collect(Collectors.toList())
            try {
                sincronizar.clear()
                val envio: String = LocalDateTime.now().format(formaterDataHora)
                val bases = sinc.parallelStream().map { it.first }.distinct().collect(Collectors.toList())
                for (db in bases) {
                    val env = sinc.parallelStream().filter { i -> i.first == db }.map { it.second }.collect(Collectors.toList())
                    if (env.isNotEmpty()) {
                        val docRef: DocumentReference = DB!!.collection(db.toString()).document(formaterData.format(LocalDate.now()))
                        val data: MutableMap<String, Any> = mutableMapOf()
                        for (voc in env) {
                            voc.sincronizacao = envio
                            data[voc.getId().toString()] = voc
                        }
                        val result: ApiFuture<WriteResult> = docRef.set(data)
                        result.get()
                        registros += env.size
                        LOGGER.info("Enviado dados a cloud: " + env.size + " registros (" + db + "). ")
                    }
                }
                if (registros > 0) {
                    processados += "Enviado $registros registro(s). "
                    if (!vocabularios.isEmpty())
                        vocabularios = vocabularios.substring(0, vocabularios.lastIndexOf(",")).trim()
                    Platform.runLater { Notificacoes.notificacao(Notificacao.SUCESSO, "Concluído o envio de $registros registros para cloud.", "Sincronizado: $vocabularios") }
                }
                LOGGER.info("Concluído envio de dados a cloud.")
                processado = true
            } catch (e: Exception) {
                sincronizar.addAll(sinc)
                LOGGER.error("Erro ao enviar dados a cloud, adicionado arquivos para novo ciclo. ${e.message}", e)
                throw e
            }
        }
        return processado
    }

    @Throws(Exception::class)
    private fun receber(): Boolean {
        var processado: Boolean
        try {
            LOGGER.info("Recebendo dados a cloud.... ")
            val lista: MutableList<Pair<Database, Vocabulario>> = mutableListOf()
            val atual: String = LocalDate.now().format(formaterData)
            for (vocab in daoVocabulario) {
                val query: ApiFuture<QuerySnapshot> = DB!!.collection(vocab.tipo.toString()).get()
                val querySnapshot: QuerySnapshot = query.get()
                val documents: List<QueryDocumentSnapshot> = querySnapshot.documents
                for (document in documents) {
                    val data = LocalDate.parse(document.id, formaterData)

                    if (sincronizacao!!.recebimento.toLocalDate().isAfter(data) && !atual.equals(document.id, ignoreCase = true))
                        continue

                    for (key in document.data.keys) {
                        val obj: HashMap<String, String> = document.data[key] as HashMap<String, String>
                        val sinc = LocalDateTime.parse(obj["sincronizacao"], formaterDataHora)
                        if (sinc.isAfter(sincronizacao!!.recebimento))
                            lista.add(Pair(vocab.tipo, Vocabulario(key, obj)))
                    }
                }
            }
            LOGGER.info("Processando retorno dados a cloud: " + lista.size + " registros.")
            vocabularios = ""
            registros = lista.size
            for (sinc in lista) {
                for (voc in daoVocabulario) if (voc.tipo.equals(sinc.first)) {
                    var vocab = voc.select(sinc.second.getId()).orElseGet { null }

                    if (vocab == null)
                        vocab = voc.select(sinc.second.vocabulario, sinc.second.formaBasica).orElseGet { null }

                    if (vocab != null) {
                        vocab.merge(sinc.second)
                        voc.update(vocab)
                    } else {
                        vocab = sinc.second
                        voc.insert(vocab)
                    }
                    vocabularios += vocab.vocabulario + ", "
                    for (rev in daoRevisar)
                        if (rev.tipo == sinc.first) {
                            var revisar = rev.select(vocab.getId()!!)

                            revisar.ifPresent { rev.delete(it) }

                            revisar = rev.select(vocab.vocabulario, vocab.formaBasica)

                            revisar.ifPresent { rev.delete(it) }
                        }
                }
            }
            if (registros > 0) {
                processados += "Recebido $registros registro(s). "
                if (!vocabularios.isEmpty()) vocabularios = vocabularios.substring(0, vocabularios.lastIndexOf(",")).trim()
                Platform.runLater {
                    Notificacoes.notificacao(
                        Notificacao.SUCESSO,
                        "Concluído recebimento de " + lista.size + " registros da cloud.",
                        "Sincronizado: $vocabularios"
                    )
                }
            }
            processado = true
            LOGGER.info("Concluído recebimento de dados a cloud.")
        } catch (e: Exception) {
            LOGGER.error("Erro ao receber dados a cloud. ${e.message}".trimIndent(), e)
            throw e
        }
        return processado
    }

    fun sincroniza(): Boolean {
        var sincronizado = false

        if (sincronizacao == null)
            return sincronizado

        try {
            isSincronizando = true
            controller.animacaoSincronizacaoDatabase(true, false)
            processados = ""
            val recebido = receber()
            val enviado = envia()

            if (enviado)
                sincronizacao!!.envio = LocalDateTime.now()

            if (recebido)
                sincronizacao!!.recebimento = LocalDateTime.now()

            if (enviado || recebido) {
                dao.update(sincronizacao!!)
                Platform.runLater { controller.setLblLog(processados.trim()) }
            } else
                Platform.runLater { controller.setLblLog("") }
            sincronizado = true
            controller.animacaoSincronizacaoDatabase(false, false)
        } catch (e: Exception) {
            controller.animacaoSincronizacaoDatabase(false, true)
        } finally {
            isSincronizando = false
        }
        return sincronizado
    }

    val isConfigurado: Boolean get() = sincronizacao != null

    fun listSize(): Int = sincronizar.size
}