package br.com.fenix.processatexto

import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.database.JpaFactory
import br.com.fenix.processatexto.database.jpa.implement.RepositoryJpaImpl
import br.com.fenix.processatexto.model.entities.DadosConexao
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Driver
import br.com.fenix.processatexto.util.configuration.Configuracao
import org.slf4j.LoggerFactory


class TestsConfig {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(TestsConfig::class.java)

        const val DATABASE = "teste_"
        fun prepareDatabase() {
            if (!Configuracao.database.contains(DATABASE, true)) {
                LOGGER.info("Configurando conexão com a database de teste....")
                Configuracao.database = DATABASE + Configuracao.database
                JpaFactory.resetConnection()
                JdbcFactory.resetConnection()

                val repository = RepositoryJpaImpl<Long?, DadosConexao>(Conexao.PROCESSA_TEXTO)
                repository.query("DELETE FROM conexoes")
                val url = "jdbc:mysql://" + Configuracao.server + ":" + Configuracao.port
                for (conexao in Conexao.values())
                    if (conexao != Conexao.PROCESSA_TEXTO) {
                        val database = DATABASE + conexao.name
                        LOGGER.info("Atualizando a conexão da database ${conexao.name} - ${url} -- ${database}")
                        repository.query("INSERT INTO Conexoes (tipo, url, username, Password, base, driver) VALUES ('${conexao.name}', '${url}', '${Configuracao.user}', '${Configuracao.password}', '${database}', '${Driver.MYSQL}')")
                        repository.query("CREATE DATABASE IF NOT EXISTS " + database)
                        JdbcFactory.getFactory(conexao)
                    }

                LOGGER.info("Configuração das conexões concluida.")
            }
        }
    }
}