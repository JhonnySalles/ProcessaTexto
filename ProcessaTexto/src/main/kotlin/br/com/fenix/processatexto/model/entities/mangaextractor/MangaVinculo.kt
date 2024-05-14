package br.com.fenix.processatexto.model.entities.mangaextractor

import com.google.gson.annotations.Expose

data class MangaVinculo(
    @Expose var manga: MangaVolume? = null,
    @Expose var vinculos: MutableList<MangaVolume> = mutableListOf()
) {
    init {
        this.manga?.let { it.isVinculo = true }
    }

    fun addVinculo(vinculo: MangaVolume) = vinculos.add(vinculo)

}