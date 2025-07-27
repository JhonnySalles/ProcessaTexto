package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Conexao
import java.sql.ResultSet
import java.util.*

open class RepositoryDaoSimples<ID, E : EntityBase<ID, E>>(conexao: Conexao) : RepositoryDaoBase<ID, E>(conexao) {

    override fun toEntity(rs: ResultSet): E {
        TODO("Not yet implemented")
    }

    override fun toID(id: String?): ID {
        TODO("Not yet implemented")
    }

    override fun getCustomParam(param: Objects): String {
        TODO("Not yet implemented")
    }

}