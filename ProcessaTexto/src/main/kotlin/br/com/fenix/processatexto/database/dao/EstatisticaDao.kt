package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.controller.EstatisticaController.Tabela
import br.com.fenix.processatexto.model.entities.comicinfo.ComicInfo
import br.com.fenix.processatexto.model.entities.processatexto.japones.Estatistica
import java.sql.SQLException
import java.util.*


interface EstatisticaDao : RepositoryDao<UUID?, Estatistica> {
    @Throws(SQLException::class)
    fun save(obj: Estatistica)

    @Throws(SQLException::class)
    fun delete(obj: Estatistica)

    @Throws(SQLException::class)
    fun select(kanji: String, leitura: String): Optional<Estatistica>

    @Throws(SQLException::class)
    fun select(kanji: String): MutableList<Estatistica>

    @Throws(SQLException::class)
    fun selectAll(): MutableList<Estatistica>

    @Throws(SQLException::class)
    fun pesquisa(pesquisa: String): List<Tabela>
}