package br.com.fenix.processatexto.model.entities.mangaextractor

import br.com.fenix.processatexto.model.entities.Entity
import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.enums.Language
import java.awt.image.BufferedImage
import java.util.*


data class MangaCapa(
    private var id: UUID? = null,
    override var manga: String = "",
    override var volume: Int = 0,
    var lingua: Language = Language.PORTUGUESE,
    var arquivo: String = "",
    var extenssao: String = "",
    var imagem: BufferedImage? = null
) : Manga(), Entity<UUID?, MangaCapa> {
    override fun getId(): UUID? = id
    override fun create(id: UUID?): MangaCapa {
        TODO("Not yet implemented")
    }

}