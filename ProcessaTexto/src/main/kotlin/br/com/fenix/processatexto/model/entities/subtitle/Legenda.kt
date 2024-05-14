package br.com.fenix.processatexto.model.entities.subtitle

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Language
import java.util.*


data class Legenda(
    private var id: UUID? = null,
    var sequencia: Int,
    var episodio: Int,
    var linguagem: Language,
    var tempo: String,
    var texto: String,
    var traducao: String,
    var vocabulario: String,
    var som: String,
    var imagem: String
) : EntityBase<UUID?, Legenda>() {

    override fun getId(): UUID? = id

    override fun create(id: UUID?): Legenda = Legenda(id, 0, 0, Language.PORTUGUESE, "", "", "", "", "", "")

}