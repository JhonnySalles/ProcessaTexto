package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.ComicInfoDao
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.database.dao.SincronizacaoDao
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Database
import br.com.fenix.processatexto.model.enums.Notificacao
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.Timestamp
import com.google.cloud.firestore.*
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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

    private val daoComicInfo: ComicInfoDao
    private val daoVocabulario: List<VocabularioDao>
    private val daoRevisar: List<RevisarDao>
    private val dao: SincronizacaoDao

    private var sincronizacao: Sincronizacao? = null
    private var mDB: Firestore? = null

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

        daoComicInfo = DaoFactory.createComicInfoDao()
        dao = DaoFactory.createSincronizacaoDao()

        try {
            val serviceAccount: InputStream = FileInputStream("secrets-firebase.json")
            val credentials: GoogleCredentials = GoogleCredentials.fromStream(serviceAccount)
            val options: FirebaseOptions = FirebaseOptions.builder().setCredentials(credentials).build()
            FirebaseApp.initializeApp(options)
            mDB = FirestoreClient.getFirestore()
            sincronizacao = dao.select(Conexao.FIREBASE).get()
        } catch (ex: Exception) {
            LOGGER.error(ex.message, ex)
        }

        consultar()

        sincronizarVocabulario.addListener {  observable: ListChangeListener.Change<out Pair<Database, Vocabulario>> ->
            if(observable.list.isEmpty())
                sincronizar.removeIf { it.first == br.com.fenix.processatexto.model.enums.Sincronizacao.VOCABULARIO }
            else if (sincronizar.any { it.first == br.com.fenix.processatexto.model.enums.Sincronizacao.VOCABULARIO })
                sincronizar[sincronizar.indexOfFirst { it.first == br.com.fenix.processatexto.model.enums.Sincronizacao.VOCABULARIO }] = Pair(br.com.fenix.processatexto.model.enums.Sincronizacao.VOCABULARIO, observable.list.size)
            else
                sincronizar.add(Pair(br.com.fenix.processatexto.model.enums.Sincronizacao.VOCABULARIO, observable.list.size))
        }
        sincronizarComicInfo.addListener {  observable: ListChangeListener.Change<out ComicInfo> ->
            if(observable.list.isEmpty())
                sincronizar.removeIf { it.first == br.com.fenix.processatexto.model.enums.Sincronizacao.COMICINFO }
            else if (sincronizar.any { it.first == br.com.fenix.processatexto.model.enums.Sincronizacao.COMICINFO })
                sincronizar[sincronizar.indexOfFirst { it.first == br.com.fenix.processatexto.model.enums.Sincronizacao.COMICINFO }] = Pair(br.com.fenix.processatexto.model.enums.Sincronizacao.COMICINFO, observable.list.size)
            else
                sincronizar.add(Pair(br.com.fenix.processatexto.model.enums.Sincronizacao.COMICINFO, observable.list.size))
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SincronizacaoServices::class.java)
        var processarRevisar = false
        var processarComicInfo = false

        private val sincronizarVocabulario: ObservableList<Pair<Database, Vocabulario>> = FXCollections.observableArrayList()
        private val sincronizarComicInfo: ObservableList<ComicInfo> = FXCollections.observableArrayList()
        private val sincronizar: ObservableList<Pair<br.com.fenix.processatexto.model.enums.Sincronizacao, Int>> = FXCollections.observableArrayList()

        fun enviar(database: Database, vocabulario: Vocabulario) = sincronizarVocabulario.add(Pair(database, vocabulario))
        fun enviar(comic: ComicInfo) = sincronizarComicInfo.add(comic)
    }

    fun setObserver(listener: ListChangeListener<Pair<br.com.fenix.processatexto.model.enums.Sincronizacao, Int>>) = sincronizar.addListener(listener)

    override fun run() {
        if ((processarRevisar || processarComicInfo) && !isSincronizando)
            sincroniza()
    }

    fun consultar() {
        if (sincronizacao == null)
            return

        for (vocab in daoVocabulario) {
            try {
                val sinc: List<Pair<Database, Vocabulario>> = vocab.selectEnvioVocabulario(sincronizacao!!.envio).parallelStream()
                    .filter { i -> sincronizarVocabulario.parallelStream().noneMatch { s -> s.first == vocab.tipo && s.second == i } }
                    .map { i -> Pair(vocab.tipo, i) }.collect(Collectors.toList())
                if (sinc.isNotEmpty())
                    sincronizarVocabulario.addAll(sinc)
            } catch (ex: SQLException) {
                LOGGER.error(ex.message, ex)
            }
        }

        try {
            val sinc: List<ComicInfo> = daoComicInfo.findEnvio(sincronizacao!!.envio).parallelStream()
                .filter { i -> sincronizarComicInfo.parallelStream().noneMatch { s: ComicInfo -> s.comic == i.comic } }
                .collect(Collectors.toList())
            if (sinc.isNotEmpty())
                sincronizarComicInfo.addAll(sinc)
        } catch (ex: SQLException) {
            LOGGER.error(ex.message, ex)
        }
    }

    private var registros = 0
    private var vocabularios: String = ""
    private var processados: String = ""

    @Throws(Exception::class)
    private fun enviaVocabulario(): Boolean {
        var processado = false
        vocabularios = ""
        registros = 0

        if (!sincronizarVocabulario.isEmpty()) {
            LOGGER.info("Enviando Vocabulário para cloud... ")
            val sinc = sincronizarVocabulario.parallelStream()
                .sorted { o1, o2 -> o2.first.compareTo(o1.first)  }
                .distinct().collect(Collectors.toList())
            try {
                sincronizarVocabulario.clear()
                val envio = LocalDateTime.now().format(formaterDataHora)
                val bases: List<Database> = sinc.parallelStream().map { it.first }.distinct().toList()
                for (db in bases) {
                    val env: List<Vocabulario> = sinc.parallelStream().filter { it.first == db }.map { it.second }.toList()
                    if (env.isNotEmpty()) {
                        val docRef = mDB!!.collection("VOCABULARIO $db").document(formaterData.format(LocalDate.now()))
                        val data: MutableMap<String, Any?> = HashMap()
                        for (voc in env) {
                            voc.sincronizacao = envio
                            data[voc.getId().toString()] = br.com.fenix.processatexto.model.entities.firebase.Vocabulario(voc)
                            vocabularios += voc.vocabulario + ", "
                        }

                        val document = docRef.get().get()
                        val olds = document.data
                        if (olds != null && olds.isNotEmpty()) {
                            for (key in olds.keys)
                                if (!data.containsKey(key))
                                    data[key] = olds[key]
                        }

                        val result = docRef.set(data)
                        result.get()
                        registros += env.size
                        LOGGER.info("Enviado Vocabulário para cloud: " + env.size + " registros (" + db + "). ")
                    }
                }
                if (registros > 0) {
                    processados += "Enviado $registros registro(s). "
                    if (vocabularios.isNotEmpty())
                        vocabularios = vocabularios.substring(0, vocabularios.lastIndexOf(",")).trim { it <= ' ' }
                    Platform.runLater { Notificacoes.notificacao(Notificacao.SUCESSO, "Concluído o envio de $registros registro(s) para cloud.", "Vocabulário: $vocabularios") }
                }
                LOGGER.info("Concluído envio de Vocabulário para cloud.")
                processado = true
            } catch (e: Exception) {
                sincronizarVocabulario.addAll(sinc)
                LOGGER.error("Erro ao enviar Vocabulários a cloud, adicionado arquivos para novo ciclo. ${e.message}", e)
                throw e
            }
        }

        return processado
    }

    @Throws(Exception::class)
    private fun receberVocabulario(): Boolean {
        var processado: Boolean
        try {
            LOGGER.info("Recebendo Vocabulário da cloud.... ")
            val lista: MutableList<Pair<Database, Vocabulario>> = ArrayList()
            val atual = LocalDate.now().format(formaterData)
            for (vocab in daoVocabulario) {
                val query = mDB!!.collection("VOCABULARIO " + vocab.tipo.toString()).get()
                val querySnapshot = query.get()
                val documents = querySnapshot.documents
                for (document in documents) {
                    val data = LocalDate.parse(document.id, formaterData)
                    if (sincronizacao!!.recebimento.toLocalDate().isAfter(data) && !atual.equals(document.id, ignoreCase = true))
                        continue

                    for (key in document.data.keys) {
                        val obj = document.data[key] as HashMap<String, String>
                        val sinc = LocalDateTime.parse(obj["sincronizacao"], formaterDataHora)
                        if (sinc.isAfter(sincronizacao!!.recebimento))
                            lista.add(Pair(vocab.tipo, br.com.fenix.processatexto.model.entities.firebase.Vocabulario.toVocabulario(key, obj)))
                    }
                }
            }
            LOGGER.info("Processando retorno de Vocabulário da cloud: " + lista.size + " registros.")
            vocabularios = ""
            registros = lista.size
            for (sinc in lista) {
                for (voc in daoVocabulario) if (voc.tipo == sinc.first) {
                    var vocab: Optional<Vocabulario> = voc.select(sinc.second.getId())
                    if (vocab.isEmpty)
                        vocab = voc.select(sinc.second.vocabulario, sinc.second.formaBasica)

                    if (vocab.isPresent) {
                        vocab.get().merge(sinc.second)
                        voc.updateManual(vocab.get())
                    } else {
                        vocab = Optional.of(sinc.second)
                        voc.insertManual(vocab.get())
                    }

                    vocabularios += vocab.get().vocabulario + ", "

                    for (rev in daoRevisar)
                        if (rev.tipo == sinc.first) {
                            var revisar = rev.select(vocab.get().getId()!!)
                            if (revisar.isPresent)
                                rev.delete(revisar.get())

                            revisar = rev.select(vocab.get().vocabulario, vocab.get().formaBasica)
                            if (revisar.isPresent)
                                rev.delete(revisar.get())
                        }
                }
            }
            if (registros > 0) {
                processados += "Recebido $registros registro(s). "
                if (vocabularios.isNotEmpty())
                    vocabularios = vocabularios.substring(0, vocabularios.lastIndexOf(",")).trim { it <= ' ' }
                Platform.runLater { Notificacoes.notificacao(Notificacao.SUCESSO, "Concluído recebimento de $registros registros(s) da cloud.", "Vocabulário: $vocabularios") }
            }
            processado = true
            LOGGER.info("Concluído recebimento de vocabulários da cloud.")
        } catch (e: Exception) {
            LOGGER.error("Erro ao receber dados a cloud. ${e.message}", e)
            throw e
        }
        return processado
    }

    @Throws(Exception::class)
    private fun enviaExclusao(): Boolean {
        var processado = false
        vocabularios = ""
        registros = 0

        val enviar: MutableList<Pair<Database, String>> = ArrayList()

        for (vocab in daoVocabulario) {
            try {
                val sinc: List<Pair<Database, String>> = vocab.selectExclusaoEnvio(sincronizacao!!.envio).parallelStream()
                    .filter(Objects::nonNull).filter { it.isNotEmpty() }
                    .map { i -> Pair(vocab.tipo, i) }.toList()
                if (sinc.isNotEmpty()) 
                    enviar.addAll(sinc)
            } catch (ex: SQLException) {
                LOGGER.error(ex.message, ex)
            }
        }

        if (enviar.isNotEmpty()) {
            LOGGER.info("Enviando exclusões de Vocabulário para cloud... ")
            try {
                val envio = LocalDateTime.now().format(formaterDataHora)
                val bases: List<Database> = enviar.parallelStream().map { it.first }.distinct().toList()

                for (db in bases) {
                    val env = enviar.parallelStream().filter { it.first == db }.map { it.second }.toList()

                    if (env.isNotEmpty()) {
                        val docRef = mDB!!.collection("EXCLUSAO $db").document(formaterData.format(LocalDate.now()))

                        val data: MutableMap<String, String> = HashMap()
                        for (exc in env)
                            data[exc] = envio

                        val document = docRef.get().get()
                        val olds = document.data
                        if (olds != null && olds.isNotEmpty()) {
                            for (key in olds.keys)
                                if (!data.containsKey(key))
                                    data[key] = olds[key].toString()
                        }
                        val result = docRef.set(data as Map<String, Any>)
                        result.get()
                        registros += env.size
                        LOGGER.info("Enviado exclusões de Vocabulário para cloud: " + env.size + " registros (" + db + "). ")
                    }
                }
                if (registros > 0) {
                    if (vocabularios.isNotEmpty())
                        vocabularios = vocabularios.substring(0, vocabularios.lastIndexOf(",")).trim { it <= ' ' }
                }
                LOGGER.info("Concluído envio de exclusões de Vocabulário para cloud.")
                processado = true
            } catch (e: Exception) {
                LOGGER.error("Erro ao enviar exclusões de Vocabulário para cloud. ${e.message}", e)
                throw e
            }
        }

        return processado
    }

    @Throws(Exception::class)
    private fun receberExclusao(): Boolean {
        var processado: Boolean
        try {
            LOGGER.info("Recebendo exclusões de Vocabulário da cloud.... ")
            val atual = LocalDate.now().format(formaterData)
            for (vocab in daoVocabulario) {
                val query = mDB!!.collection("EXCLUSAO " + vocab.tipo.toString()).get()
                val querySnapshot = query.get()
                val documents = querySnapshot.documents
                for (document in documents) {
                    val data = LocalDate.parse(document.id, formaterData)
                    if (sincronizacao!!.recebimento.toLocalDate().isAfter(data) && !atual.equals(document.id, ignoreCase = true))
                        continue

                    for (key in document.data.keys) {
                        val sinc = LocalDateTime.parse(document.data[key] as String, formaterDataHora)
                        if (sinc.isAfter(sincronizacao!!.recebimento))
                            vocab.insertExclusao(key)
                    }
                }
            }
            processado = true
            LOGGER.info("Concluído recebimento de exclusão de Vocabulário da cloud.")
        } catch (e: Exception) {
            LOGGER.error("Erro ao receber exclusões de Vocabulário da cloud. ${e.message}", e)
            throw e
        }
        return processado
    }

    private var comicInfo: String = ""
    @Throws(Exception::class)
    private fun receberComicInfo(): Boolean {
        var processado: Boolean
        try {
            LOGGER.info("Recebendo ComicInfo da cloud.... ")
            val lista: MutableList<ComicInfo> = ArrayList<ComicInfo>()
            val document = mDB!!.collection("COMICINFO")
            val index = document.document("_INDEX").get().get()
            if (index.data != null) for (item in index.data!!.keys) {
                val data = index.data!![item] as Timestamp?
                val sinc = data!!.toSqlTimestamp().toLocalDateTime()
                if (sinc.isAfter(sincronizacao!!.recebimento)) {
                    val comic = document.document(item).get().get()
                    if (comic.data != null)
                        lista.add(br.com.fenix.processatexto.model.entities.firebase.ComicInfo.toComicInfo(comic.data as HashMap<String, Any?>))
                }
            }

            LOGGER.info("Processando retorno do ComicInfo da cloud: " + lista.size + " registros.")
            comicInfo = ""
            registros = lista.size
            for (sinc in lista) {
                val comic = daoComicInfo.find(sinc.getId()!!, sinc.comic, sinc.languageISO!!)
                if (comic.isPresent) {
                    comic.get().merge(sinc)
                    daoComicInfo.update(comic.get(), isThrowsNotUpdate = false)
                } else
                    daoComicInfo.insert(sinc)
                comicInfo += sinc.comic + ", "
            }

            if (registros > 0) {
                processados += "ComicInfo recebido $registros registro(s). "
                if (comicInfo.isNotEmpty())
                    comicInfo = comicInfo.substring(0, comicInfo.lastIndexOf(",")).trim { it <= ' ' }
                Platform.runLater { Notificacoes.notificacao(Notificacao.SUCESSO, "Concluído recebimento de $registros registro(s) da cloud.", "ComicInfo: $comicInfo") }
            }
            processado = true
            LOGGER.info("Concluído recebimento de ComicInfo da cloud.")
        } catch (e: Exception) {
            LOGGER.error("Erro ao receber dados a cloud. ${e.message}", e)
            throw e
        }
        return processado
    }

    @Throws(Exception::class)
    private fun enviaComicInfo(): Boolean {
        var processado = false
        vocabularios = ""
        registros = 0
        if (!sincronizarComicInfo.isEmpty()) {
            LOGGER.info("Enviando ComicInfo para cloud... ")
            val sinc: List<ComicInfo> = sincronizarComicInfo.parallelStream()
                .sorted { o1, o2 -> o2.comic.compareTo(o1.comic) }.distinct().toList()
            try {
                sincronizarComicInfo.clear()
                if (sinc.isNotEmpty()) {
                    val document = mDB!!.collection("COMICINFO")
                    val docIndex = document.document("_INDEX").get().get()
                    val index: MutableMap<String, Date> = HashMap()
                    if (docIndex.exists() && docIndex.data != null) {
                        val item = docIndex.data ?: mapOf()
                        index.putAll(item as Map<String, Date>)
                    }

                    comicInfo = ""
                    val gson = GsonBuilder().create()
                    for (comic in sinc) {
                        val id: String = comic.comic
                        index[id] = Date()
                        val item: Map<String, Any> = Gson().fromJson(gson.toJson(br.com.fenix.processatexto.model.entities.firebase.ComicInfo(comic)), object : TypeToken<HashMap<String?, Any?>?>() {}.type)
                        document.document(id).set(item).get()
                        comicInfo += comic.comic + ", "
                    }
                    document.document("_INDEX").set(index as Map<String, Any>).get()
                    registros = sinc.size
                    LOGGER.info("Enviado ComicInfo para cloud: " + sinc.size + " registros. ")
                }

                if (registros > 0) {
                    processados += "Enviado $registros registro(s). "
                    if (comicInfo.isNotEmpty())
                        comicInfo = comicInfo.substring(0, comicInfo.lastIndexOf(",")).trim { it <= ' ' }

                    Platform.runLater { Notificacoes.notificacao(Notificacao.SUCESSO, "Concluído o envio de $registros registro(s) para cloud.", "ComicInfo: $comicInfo") }
                }
                LOGGER.info("Concluído envio de ComicInfo para cloud.")
                processado = true
            } catch (e: Exception) {
                sincronizarComicInfo.addAll(sinc)
                LOGGER.error("Erro ao enviar ComicInfo a cloud, adicionado arquivos para novo ciclo. ${e.message}", e)
                throw e
            }
        }
        return processado
    }

    fun sincroniza(isManual : Boolean = false): Boolean {
        var sincronizado = false

        if (sincronizacao == null)
            return sincronizado

        try {
            isSincronizando = true
            controller.animacaoSincronizacaoDatabase(isProcessando = true, isErro = false)
            processados = ""
            var recebido = false
            var enviado = false

            if (processarRevisar || processarComicInfo || isManual) {
                recebido = receberVocabulario()
                enviado = enviaVocabulario()

                recebido = receberExclusao() || recebido
                enviado = enviaExclusao() || enviado

                recebido = receberComicInfo() || recebido
                enviado = enviaComicInfo() || enviado
            }

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
            controller.animacaoSincronizacaoDatabase(isProcessando = false, isErro = false)
        } catch (e: Exception) {
            controller.animacaoSincronizacaoDatabase(isProcessando = false, isErro = true)
        } finally {
            isSincronizando = false
        }
        return sincronizado
    }

    val isConfigurado: Boolean get() = sincronizacao != null

    fun listSize(): Int = sincronizarVocabulario.size + sincronizarComicInfo.size
}