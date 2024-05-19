package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.EstatisticaDao
import br.com.fenix.processatexto.model.entities.processatexto.japones.Estatistica
import br.com.fenix.processatexto.controller.EstatisticaController.Tabela
import java.sql.SQLException
import java.util.*


class EstatisticaServices {

    private val dao: EstatisticaDao = DaoFactory.createEstatisticaDao()

    @Throws(SQLException::class)
    fun selectAll(): List<Estatistica> = dao.selectAll()

    @Throws(SQLException::class)
    fun select(kanji: String, leitura: String): Optional<Estatistica> = dao.select(kanji, leitura)

    @Throws(SQLException::class)
    fun select(kanji: String): MutableList<Estatistica> = dao.select(kanji)

    @Throws(SQLException::class)
    fun pesquisa(pesquisa: String): List<Tabela> = dao.pesquisa(pesquisa)

    @Throws(SQLException::class)
    fun insert(obj: Estatistica) {
        if (obj.kanji.isNotEmpty()) {
            if (obj.getId() == null)
                obj.setId(UUID.randomUUID())
            dao.insert(obj)
        }
    }

    @Throws(SQLException::class)
    fun insert(lista: List<Estatistica>) {
        for (obj in lista)
            insert(obj)
    }

    @Throws(SQLException::class)
    fun update(obj: Estatistica) = dao.update(obj)

    @Throws(SQLException::class)
    fun delete(obj: Estatistica) = dao.delete(obj)

}