package br.com.fenix.processatexto.database

import br.com.fenix.processatexto.database.dao.RepositoryDaoBase
import br.com.fenix.processatexto.model.entities.DadosConexao
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Driver
import br.com.fenix.processatexto.model.exceptions.DatabaseException
import br.com.fenix.processatexto.util.configuration.Configuracao
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.*
import java.util.*


object JdbcFactory {

    private val LOGGER: Logger = LoggerFactory.getLogger(JdbcFactory::class.java)

    private var conexao: DadosConexao = DadosConexao(
        "jdbc:mysql://" + Configuracao.server + ":" + Configuracao.port,
        Configuracao.database,
        Configuracao.user,
        Configuracao.password
    )

    private var default: Connection = buildDefault()
    private val external: MutableMap<Conexao, Connection> = mutableMapOf()
    private var repository: RepositoryDao = RepositoryDao(Conexao.PROCESSA_TEXTO)

    private fun buildDefault(): Connection = buildFactory(conexao)

    private fun buildFactory(dados: DadosConexao): Connection {
        FlywayFactory.migrate(dados)

        val properties = Properties()
        properties["user"] = dados.usuario
        properties["password"] = dados.senha
        properties["characterEncoding"] = "UTF-8"
        properties["useUnicode"] = "true"

        val url = dados.url + "/" + dados.base + "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"

        return DriverManager.getConnection(url, properties)
    }

    fun getFactory(): Connection = default

    fun getFactory(conexao: Conexao): Connection {
        return if (conexao == Conexao.PROCESSA_TEXTO)
            default
        else {
            if (!external.contains(conexao)) {
                val dados = getConfiguracao(conexao).orElseThrow { DatabaseException("Não encontrado a conexão ${conexao.name}") }
                external[conexao] = buildFactory(dados)
            }
            external[conexao] ?: throw Exception("Database ${conexao.name} não encontrada.")
        }
    }

    fun getConfiguracao(conexao: Conexao): Optional<DadosConexao> {
        val param = mapOf(Pair("tipo", conexao))
        // language=SQL
        val s = "SELECT * FROM Conexoes WHERE tipo = :tipo"
        return repository.queryEntity(s, param)
    }

    fun closeConnection() {
        try {
            default.close()
        } catch (e: SQLException) {
            LOGGER.error(e.message, e)
        }
    }

    fun closeStatement(st: Statement?) {
        if (st != null)
            try {
                st.close()
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
    }

    fun closeResultSet(rs: ResultSet?) {
        if (rs != null)
            try {
                rs.close()
            } catch (e: SQLException) {
                LOGGER.error(e.message, e)
            }
    }

    class RepositoryDao(conexao: Conexao) : RepositoryDaoBase<Long?, DadosConexao>(conexao) {
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

    fun testaConexao(conexao: Conexao): String {
        var connection: Connection? = null
        var conecta = ""
        try {
            val config = if (conexao == Conexao.PROCESSA_TEXTO)
                this.conexao
            else
                getConfiguracao(conexao).orElseThrow { Exception("Configuração não encontrada na tabela de bases") }

            val driverName = "com.mysql.cj.jdbc.Driver"
            Class.forName(driverName)
            val url = config.url + "/" + config.base
            connection = DriverManager.getConnection(url, config.usuario, config.senha)
            if (connection != null)
                conecta = config.base
            connection.close()
        } catch (e: ClassNotFoundException) { // Driver n�o encontrado
            println("O driver de conexão expecificado nao foi encontrado.")
            LOGGER.error(e.message, e)
        } catch (e: SQLException) {
            println("Nao foi possivel conectar ao Banco de Dados.")
            LOGGER.error(e.message, e)
        } catch (e: Exception) {
            println("Configuração não encontrada.")
            LOGGER.error(e.message, e)
        } finally {
            if (connection != null)
                connection.close()
        }
        return conecta
    }

    fun resetConnection() {
        conexao = DadosConexao("jdbc:mysql://" + Configuracao.server + ":" + Configuracao.port, Configuracao.database, Configuracao.user, Configuracao.password)

        default.close()
        default = buildDefault()
        repository = RepositoryDao(Conexao.PROCESSA_TEXTO)

        for (key in external.keys)
            external[key]?.close()

        external.clear()
    }
}