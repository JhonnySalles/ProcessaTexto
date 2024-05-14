package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import java.util.*

data class Kanji(private val id: UUID?, val kanji: String, val palavra: String, val significado: String) : EntityBase<UUID?, Kanji>() {

    override fun getId(): UUID? = id
    override fun create(id: UUID?): Kanji = Kanji(id, "", "", "")

}