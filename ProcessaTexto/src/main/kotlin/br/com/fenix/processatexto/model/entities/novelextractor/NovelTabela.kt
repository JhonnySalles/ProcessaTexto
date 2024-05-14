package org.jisho.textosJapones.model.entities.novelextractor

import br.com.fenix.processatexto.model.entities.Novel

data class NovelTabela(
    val volumes: MutableList<NovelVolume> = mutableListOf()
) : Novel() {

    constructor(
        tabela: String,
        volumes: MutableList<NovelVolume>
    ) : this(volumes) {
        this.base = tabela
    }

    fun addVolume(volume: NovelVolume) = volumes.add(volume)
}