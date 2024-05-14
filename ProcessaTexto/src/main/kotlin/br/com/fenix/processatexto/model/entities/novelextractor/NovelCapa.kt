package org.jisho.textosJapones.model.entities.novelextractor

import br.com.fenix.processatexto.model.enums.Language
import java.awt.image.BufferedImage
import java.util.*

data class NovelCapa(
    var id: UUID? = null,
    var novel: String = "",
    var volume: Float = 0f,
    var lingua: Language,
    var arquivo: String = "",
    var extenssao: String = "",
    var imagem: BufferedImage? = null
) {}