package br.com.fenix.processatexto.database.mysql

import br.com.fenix.processatexto.components.notification.AlertasPopup
import br.com.fenix.processatexto.components.notification.Notificacoes
import br.com.fenix.processatexto.controller.MenuPrincipalController
import br.com.fenix.processatexto.model.enums.Notificacao
import br.com.fenix.processatexto.util.configuration.Configuracao
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException


object Backup {
    private val LOGGER = LoggerFactory.getLogger(Backup::class.java)
    private val SEPARATOR = File.separator
    private var MYSQL_PATH: String? = null

    private fun criaPasta(caminho: String) {
        val arquivo = File(caminho)
        if (!arquivo.exists())
            arquivo.mkdir()
    }

    @Throws(Exception::class)
    private fun carregaCampos() {
        if (Configuracao.database.isEmpty())
            throw Exception("Erro, não existe nenhuma base nos arquivos de configuração.")

        if (Configuracao.caminhoMysql.isEmpty())
            throw Exception("Erro, caminho do mysql não definido no arquivo de configuração.")

        val arquivo = File(Configuracao.caminhoMysql)

        if (!arquivo.exists())
            throw Exception("Erro, caminho do mysql não encontrado.")

        MYSQL_PATH = (Configuracao.caminhoMysql + SEPARATOR) + "bin"
    }

    @Throws(IOException::class)
    private fun processaExportacao(caminho: File): String {
        criaPasta(caminho.path + SEPARATOR)
        val commando = MYSQL_PATH + SEPARATOR + "mysqldump.exe"
        val time: Long
        val time1: Long = System.currentTimeMillis()
        val pb = ProcessBuilder(
            commando, "--user=" + Configuracao.user,
            "--password=" + Configuracao.password, Configuracao.database,
            "--result-file=" + caminho.path + SEPARATOR + Configuracao.database + ".sql"
        )
        pb.start()
        val time2: Long = System.currentTimeMillis()
        time = (time2 - time1) / 1000
        return "Backup realizado com sucesso. Tempo decorrido ${time}s. ${caminho.path}${SEPARATOR}Backup$SEPARATOR${Configuracao.database}.sql"
    }

    @Throws(IOException::class)
    private fun processaImportacao(arquivo: File): String {
        val commando = MYSQL_PATH + SEPARATOR + "mysql.exe"
        val time: Long
        val time1: Long = System.currentTimeMillis()
        val pb = ProcessBuilder(
            commando, "--user=" + Configuracao.user,
            "--password=" + Configuracao.password, Configuracao.database, "-e",
            " source " + arquivo.absolutePath
        )
        pb.start()
        val time2: Long = System.currentTimeMillis()
        time = (time2 - time1) / 1000
        return "Backup importado com sucesso. Tempo decorrido " + time + "s."
    }

    fun importarBackup(cnt: MenuPrincipalController) {
        val backup = FileChooser()
        backup.initialDirectory = File(System.getProperty("user.home"))
        backup.title = "Carregar o backup."
        backup.extensionFilters.add(FileChooser.ExtensionFilter("Arquivo de texto (*.sql)", "*.sql"))
        val arquivo: File = backup.showOpenDialog(null)
        if (arquivo == null)
            cnt.cancelaBackup()
        else {
            try {
                carregaCampos()
                val resultado = processaImportacao(arquivo)
                cnt.importaConcluido(false)
                Notificacoes.notificacao(Notificacao.SUCESSO, "Backup realizado com sucesso", resultado)
            } catch (e: IOException) {
                LOGGER.error(e.message, e)
                cnt.importaConcluido(true)
                AlertasPopup.ErroModal("Erro ao tentar gerar backup da base", e.stackTrace.toString())
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
                cnt.importaConcluido(true)
                Notificacoes.notificacao(Notificacao.ERRO, "Erro ao realizar o backup", "Arquivo de configuração não encontrado ou nome da base não configurada.")
            }
        }
    }

    fun exportarBackup(cnt: MenuPrincipalController) {
        val backup = DirectoryChooser()
        backup.initialDirectory = File(System.getProperty("user.home"))
        backup.title = "Carregar o backup."
        val arquivo: File = backup.showDialog(null)
        if (arquivo == null)
            cnt.cancelaBackup()
        else {
            try {
                carregaCampos()
                val resultado = processaExportacao(arquivo)
                cnt.exportaConcluido(false)
                Notificacoes.notificacao(Notificacao.SUCESSO, "Backup realizado com sucesso", resultado)
            } catch (e: IOException) {
                LOGGER.error(e.message, e)
                cnt.exportaConcluido(true)
                AlertasPopup.ErroModal("Erro ao tentar gerar backup da base", e.stackTrace.toString())
            } catch (e: Exception) {
                LOGGER.error(e.message, e)
                cnt.exportaConcluido(true)
                Notificacoes.notificacao(Notificacao.ERRO, "Erro ao realizar o backup", "Arquivo de configuração não encontrado ou nome da base não configurada.")
            }
        }
    }
}