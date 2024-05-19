package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.entities.processatexto.Processar
import java.sql.SQLException
import br.com.fenix.processatexto.model.entities.subtitle.Legenda


interface LegendasDao {
    @get:Throws(SQLException::class)
    val tabelas: List<String>

    @Throws(SQLException::class)
    fun createTabela(base: String)

    @Throws(SQLException::class)
    fun delete(tabela: String, obj: Legenda)

    @Throws(SQLException::class)
    fun insert(tabela: String, obj: Legenda)

    @Throws(SQLException::class)
    fun update(tabela: String, obj: Legenda)

    @Throws(SQLException::class)
    fun comandoUpdate(update: String, obj: Processar)

    @Throws(SQLException::class)
    fun comandoSelect(select: String): MutableList<Processar>

    @Throws(SQLException::class)
    fun comandoDelete(delete: String)

    @Throws(SQLException::class)
    fun comandoInsert(fila: FilaSQL)

    @Throws(SQLException::class)
    fun comandoUpdate(fila: FilaSQL)

    @Throws(SQLException::class)
    fun existFila(deleteSql: String): Boolean

    @Throws(SQLException::class)
    fun comandoSelect(): MutableList<FilaSQL>
}