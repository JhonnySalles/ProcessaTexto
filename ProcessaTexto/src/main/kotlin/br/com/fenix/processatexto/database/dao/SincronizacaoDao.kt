package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.processatexto.Sincronizacao
import br.com.fenix.processatexto.model.enums.Conexao
import java.sql.SQLException
import java.util.*

interface SincronizacaoDao {
    @Throws(SQLException::class)
    fun insert(obj: Sincronizacao) : Conexao

    @Throws(SQLException::class)
    fun update(obj: Sincronizacao)

    @Throws(SQLException::class)
    fun delete(obj: Sincronizacao)

    @Throws(SQLException::class)
    fun select(tipo: Conexao): Optional<Sincronizacao>
}