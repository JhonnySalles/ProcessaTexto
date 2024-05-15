package br.com.fenix.processatexto.database.jpa.implement

import br.com.fenix.processatexto.database.jpa.RepositoryJpaBase
import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Conexao


open class RepositoryJpaImpl<ID, E : EntityBase<ID, E>>(conexao: Conexao) : RepositoryJpaBase<ID, E>(conexao) {

}