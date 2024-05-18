package br.com.fenix.processatexto.database

import br.com.fenix.processatexto.database.jpa.implement.RepositoryDadosConexaoJpa
import br.com.fenix.processatexto.model.entities.DadosConexao
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.exceptions.DatabaseException
import br.com.fenix.processatexto.util.configuration.Configuracao
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


object JpaFactory {

    private val LOGGER: Logger = LoggerFactory.getLogger(JpaFactory::class.java)

    private var conexao: DadosConexao = DadosConexao(
        "jdbc:mysql://" + Configuracao.server + ":" + Configuracao.port,
        Configuracao.database,
        Configuracao.user,
        Configuracao.password
    )

    private var default: EntityManagerFactory = buildDefault()
    private val external: MutableMap<Conexao, EntityManagerFactory> = mutableMapOf()
    private var repository = RepositoryDadosConexaoJpa(Conexao.PROCESSA_TEXTO)

    private fun buildFactory(dados: DadosConexao): EntityManagerFactory {
        FlywayFactory.migrate(dados)

        val properties = Properties()
        properties["hibernate.connection.url"] = dados.url + "/" + dados.base + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
        properties["hibernate.connection.username"] = dados.usuario
        properties["hibernate.connection.password"] = dados.senha

        return Persistence.createEntityManagerFactory(dados.tipo.toString().lowercase(), properties)
    }

    private fun buildDefault(): EntityManagerFactory = buildFactory(conexao)

    fun getFactory(): EntityManagerFactory = default

    fun getFactory(conexao: Conexao): EntityManagerFactory {
        return if (conexao == Conexao.PROCESSA_TEXTO)
            default
        else {
            if (!external.contains(conexao)) {
                val param = mapOf(Pair("tipo", conexao))
                // language=SQL
                val s = "SELECT c FROM DadosConexao c WHERE c.tipo = :tipo"
                val dados = repository.queryEntity(s, param).orElseThrow { DatabaseException("Não encontrado a conexão ${conexao.name}") }
                external[conexao] = buildFactory(dados)
            }
            external[conexao] ?: throw Exception("Database ${conexao.name} não encontrada.")
        }
    }

    fun resetConnection() {
        conexao = DadosConexao("jdbc:mysql://" + Configuracao.server + ":" + Configuracao.port, Configuracao.database, Configuracao.user, Configuracao.password)

        default.close()
        default = buildDefault()
        repository = RepositoryDadosConexaoJpa(Conexao.PROCESSA_TEXTO)

        for (key in external.keys)
            external[key]?.close()

        external.clear()
    }
}