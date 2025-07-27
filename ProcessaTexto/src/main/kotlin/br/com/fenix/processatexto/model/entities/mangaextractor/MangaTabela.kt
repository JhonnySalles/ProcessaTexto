package br.com.fenix.processatexto.model.entities.mangaextractor

import br.com.fenix.processatexto.model.entities.Manga

data class MangaTabela(
    var volumes: MutableList<MangaVolume> = mutableListOf(),
    var vinculados: MutableList<MangaVinculo> = mutableListOf(),
    var quantidade: Int = 0
) : Manga() {

    constructor(base: String, volumes: MutableList<MangaVolume>) : this() {
        super.base = base
        this.volumes = volumes
        super.isProcessar = true
    }

    constructor(base: String, quantidade: Int, volumes: MutableList<MangaVolume>) :
            this(volumes, quantidade = quantidade) {
        super.base = base
        super.isProcessar = true
    }

    constructor(base: String, volumes: MutableList<MangaVolume>, vinculados: MutableList<MangaVinculo>) : this(volumes, vinculados) {
        super.base = base
        super.isProcessar = true
    }
}