package br.com.fenix.processatexto.mock

import br.com.fenix.processatexto.model.entities.EntityBase


interface MockJpa<ID, E : EntityBase<ID, E>> : Mock<ID, E> { }