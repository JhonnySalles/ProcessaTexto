package br.com.fenix.processatexto.model.entities.mangaextractor

import br.com.fenix.processatexto.model.entities.Manga
import br.com.fenix.processatexto.model.enums.Language
import java.awt.image.BufferedImage
import java.util.*

data class MangaCapa(
    var id: UUID? = null,
    override var manga: String = "",
    override var volume: Int = 0,
    var lingua: Language = Language.PORTUGUESE,
    var arquivo: String = "",
    var extenssao: String = "",
    var imagem: BufferedImage? = null
) : Manga() {}