package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.JdbcFactory
import br.com.fenix.processatexto.model.entities.subtitle.FilaSQL
import br.com.fenix.processatexto.model.entities.processatexto.Processar
import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.entities.subtitle.Legenda
import java.sql.SQLException


class LegendasServices {

    private val dao = DaoFactory.createLegendasDao()

    val schema: String
        get() {
            val conf = JdbcFactory.getConfiguracao(Conexao.DECKSUBTITLE)
            return if (conf.isPresent) conf.get().base else ""
        }

    @get:Throws(SQLException::class)
    val tabelas: List<String> get() = dao!!.tabelas

    @Throws(SQLException::class)
    private fun criarTabela(tabela: String) {
        if (dao!!.tabelas.stream().filter { t -> t.equals(tabela, ignoreCase = true) }.findFirst().isEmpty)
            dao!!.createTabela(tabela)
    }

    @Throws(SQLException::class)
    fun salvar(base: String, legendas: List<Legenda>) {
        criarTabela(base)
        dao!!.delete(base, legendas[0])
        for (legenda in legendas) {
            if (legenda.getId() == null)
                dao.insert(base, legenda)
            else
                dao.update(base, legenda)
        }
    }

    @Throws(SQLException::class)
    fun comandoUpdate(update: String, lista: List<Processar>) {
        for (obj in lista)
            dao!!.comandoUpdate(update, obj)
    }

    @Throws(SQLException::class)
    fun comandoUpdate(update: String, obj: Processar) = dao!!.comandoUpdate(update, obj)

    @Throws(SQLException::class)
    fun comandoSelect(select: String): MutableList<Processar> = dao!!.comandoSelect(select)

    @Throws(SQLException::class)
    fun comandoDelete(delete: String) = dao!!.comandoDelete(delete)

    @Throws(SQLException::class)
    fun insertOrUpdateFila(fila: FilaSQL) {
        if (fila.getId() == null)
            dao!!.comandoInsert(fila)
        else
            dao!!.comandoUpdate(fila)
    }

    @Throws(SQLException::class)
    fun selectFila(): MutableList<FilaSQL> = dao!!.comandoSelect()

    @Throws(SQLException::class)
    fun existFila(deleteSql: String): Boolean = dao!!.existFila(deleteSql)

}