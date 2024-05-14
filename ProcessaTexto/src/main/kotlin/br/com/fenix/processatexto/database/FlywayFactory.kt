package br.com.fenix.processatexto.database

import br.com.fenix.processatexto.model.entities.DadosConexao
import br.com.fenix.processatexto.model.enums.Conexao
import org.flywaydb.core.Flyway
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object FlywayFactory {

    private val LOGGER: Logger = LoggerFactory.getLogger(FlywayFactory::class.java)

    private val executed : MutableSet<Conexao> = mutableSetOf()

    private val schema = "filesystem:./src/main/resources/db/migration"
    private fun getSchema(tipo: Conexao): Array<String> {
        return when (tipo) {
            Conexao.PROCESSA_TEXTO -> arrayOf("$schema/processatexto")
            Conexao.MANGA_EXTRACTOR -> arrayOf("$schema/mangaextractor")
            Conexao.NOVEL_EXTRACTOR -> arrayOf("$schema/novelextractor")
            Conexao.DECKSUBTITLE -> arrayOf("$schema/decksutitle")
            Conexao.TEXTO_INGLES -> arrayOf("$schema/textoingles")
            Conexao.TEXTO_JAPONES -> arrayOf("$schema/textojapones")
            else -> throw Exception("Não é possível realizar o migration, base não suportada ou não configurada.")
        }
    }

    fun migrate(conexao: DadosConexao) {
        if (executed.contains(conexao.tipo))
            return

        executed.add(conexao.tipo)
        val flyway = Flyway.configure()
            .locations(*getSchema(conexao.tipo))
            .dataSource(conexao.url + "/" + conexao.base + "?createDatabaseIfNotExist=true", conexao.usuario, conexao.senha)
            .load();
        flyway.migrate();
    }
}