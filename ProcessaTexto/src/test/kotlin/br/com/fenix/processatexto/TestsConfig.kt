package br.com.fenix.processatexto

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.JpaFactory
import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.model.entities.DadosConexao
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Driver
import br.com.fenix.processatexto.util.configuration.Configuracao
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.AnchorPane
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import java.net.URL
import java.sql.ResultSet


class TestsConfig {
    companion object {
        const val EXCLUIR_MENSAGEM = "Ignorado o teste de exclusão."
        const val TESTA_EXCLUIR = true
        const val LIMPA_LISTA = TESTA_EXCLUIR

        private val LOGGER = LoggerFactory.getLogger(TestsConfig::class.java)

        const val DATABASE = "teste_"
        fun prepareDatabase() {
            if (!Configuracao.database.contains(DATABASE, true)) {
                LOGGER.info("Configurando conexão com a database de teste....")
                Configuracao.database = DATABASE + Configuracao.database
                JpaFactory.resetConnection()
                JdbcFactory.resetConnection()

                val repository = RepositoryDao()
                repository.queryNative("DELETE FROM conexoes")
                val url = "jdbc:mysql://" + Configuracao.server + ":" + Configuracao.port
                for (conexao in Conexao.values())
                    if (conexao != Conexao.PROCESSA_TEXTO && !conexao.isExternal) {
                        val database = DATABASE + conexao.name
                        LOGGER.info("Atualizando a conexão da database ${conexao.name} - ${url} -- ${database}")
                        repository.queryNative("INSERT INTO Conexoes (tipo, url, username, Password, base, driver) VALUES ('${conexao.name}', '${url}', '${Configuracao.user}', '${Configuracao.password}', '${database}', '${Driver.MYSQL}')")
                        repository.queryNative("CREATE DATABASE IF NOT EXISTS " + database)
                        JdbcFactory.getFactory(conexao)
                    }

                LOGGER.info("Configuração das conexões concluida.")
            }
        }

        private class RepositoryDao() : RepositoryDaoBase<Long?, DadosConexao>(Conexao.PROCESSA_TEXTO) {
            override fun toEntity(rs: ResultSet): DadosConexao = DadosConexao(
                rs.getLong("id"),
                Conexao.valueOf(rs.getString("tipo")),
                rs.getString("url"),
                rs.getString("base"),
                rs.getString("username"),
                rs.getString("password"),
                Driver.valueOf(rs.getString("driver"))
            )
        }

        fun <T> createScene(fxml: URL): Pair<Scene, T> {
            val loader = FXMLLoader(fxml)
            val panel = loader.load<AnchorPane>()
            val scene = Scene(panel)
            scene.fill = Color.BLACK
            scene.stylesheets.add(TestsConfig::class.java.getResource("/css/Dark_Theme.css")!!.toExternalForm())
            return Pair(scene, loader.getController())
        }

        fun getIcon(url: String): Image = Image(TestsConfig::class.java.getResourceAsStream(url))
    }
}