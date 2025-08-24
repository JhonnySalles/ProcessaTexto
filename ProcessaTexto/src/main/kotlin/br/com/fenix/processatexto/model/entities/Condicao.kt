package br.com.fenix.processatexto.model.entities

import br.com.fenix.processatexto.model.enums.Igualdade

data class Condicao(
    val valor: Any,
    val igualdade: Igualdade? = null
)