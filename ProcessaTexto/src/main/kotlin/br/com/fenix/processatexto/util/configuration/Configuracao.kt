package br.com.fenix.processatexto.util.configuration

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


object Configuracao {

    private val LOGGER: Logger = LoggerFactory.getLogger(Configuracao::class.java)

    private val secrets: Properties = Properties()
    private val properties: Properties = Properties()

    init {
        loadProperties()
        loadSecrets()
    }

    fun saveProperties() {
        try {
            FileOutputStream("app.properties").use { os ->
                properties.store(os, "")
            }
        } catch (e: IOException) {
            //Alertas.Tela_Alerta("Erro ao salvar o properties", e.message )
            LOGGER.error(e.message, e)
        }
    }

    private fun createProperties() {
        try {
            FileOutputStream("app.properties").use { os ->
                properties.clear()
                properties.setProperty("connection.server", "")
                properties.setProperty("connection.port", "")
                properties.setProperty("connection.user", "")
                properties.setProperty("connection.password", "")
                properties.setProperty("connection.database", "")
                properties.setProperty("caminho.mysql", "")
                properties.setProperty("caminho.winrar", "")
                properties.setProperty("caminho.commictagger", "")
                properties.setProperty("caminho.legenda", "")
                properties.store(os, "")
            }
        } catch (e: IOException) {
            //Alertas.Tela_Alerta("Erro ao salvar o properties", e.message )
            LOGGER.error(e.message, e)
        }
    }

    private fun loadProperties(): Properties {
        val f = File("app.properties")
        if (!f.exists())
            createProperties()
        try {
            FileInputStream("app.properties")
                .use { fs ->
                    properties.load(fs)
                    return properties
                }
        } catch (e: IOException) {
            //Alertas.Tela_Alerta("Erro ao carregar o properties", e.message )
            LOGGER.error(e.message, e)
            throw Exception("Erro ao carregar o properties")
        }
    }

    var server: String = ""
        set(value) {
            properties["connection.server"] = value
            field = value
        }
        get() = properties.getProperty("connection.server", "")

    var port: String = ""
        set(value) {
            properties["connection.port"] = value
            field = value
        }
        get() = properties.getProperty("connection.port", "")

    var user: String = ""
        set(value) {
            properties["connection.user"] = value
            field = value
        }
        get() = properties.getProperty("connection.user", "")

    var password: String = ""
        set(value) {
            properties["connection.password"] = value
            field = value
        }
        get() = properties.getProperty("connection.password", "")

    var database: String = ""
        set(value) {
            properties["connection.database"] = value
            field = value
        }
        get() = properties.getProperty("connection.database", "")

    var caminhoMysql: String = ""
        set(value) {
            properties["caminho.mysql"] = value
            field = value
        }
        get() = properties.getProperty("caminho.mysql", "")

    var caminhoCommicTagger: String = ""
        set(value) {
            properties["caminho.commictagger"] = value
            field = value
        }
        get() = properties.getProperty("caminho.commictagger", "")

    var caminhoLegenda: String = ""
        set(value) {
            properties["caminho.legenda"] = value
            field = value
        }
        get() = properties.getProperty("caminho.legenda", "")

    var caminhoWinrar: String = ""
        set(value) {
            properties["caminho.winrar"] = value
            field = value
        }
        get() = properties.getProperty("caminho.winrar", "")

    var caminhoSalvoArquivo: String = ""
        set(value) {
            properties["caminho.ultimo_salvo_arquivo"] = value
            field = value
            saveProperties()
        }
        get() = properties.getProperty("caminho.ultimo_salvo_arquivo", "")


    // -------------------------------------------------------------------------------------------------
    private fun loadSecrets(): Properties {
        val f = File("secrets.properties")
        if (!f.exists()) {
            try {
                FileOutputStream("secrets.properties").use { os ->
                    secrets.setProperty("my_anime_list_client_id", "")
                    secrets.store(os, "")
                }
            } catch (e: IOException) {
                //Alertas.Tela_Alerta("Erro ao salvar o secrets", e.message )
                LOGGER.error(e.message, e)
            }
        }
        try {
            FileInputStream("secrets.properties").use { fs ->
                secrets.load(fs)
                return secrets
            }
        } catch (e: IOException) {
            //Alertas.Tela_Alerta("Erro ao carregar o secrets", e.message )
            LOGGER.error(e.message, e)
            throw Exception("Erro ao carregar o secrets")
        }
    }

    val myAnimeListClient: String get() = secrets.getProperty("my_anime_list_client_id", "")
}