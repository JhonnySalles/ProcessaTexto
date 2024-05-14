package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Language
import br.com.fenix.processatexto.model.entities.mangaextractor.MangaVolume
import java.time.LocalDateTime
import java.util.*


data class Vinculo(
    private var id: UUID? = null,
    var base: String = "",
    var volume: Int = 0,
    var nomeArquivoOriginal: String = "",
    var linguagemOriginal: Language = Language.PORTUGUESE,
    var volumeOriginal: MangaVolume? = null,
    var nomeArquivoVinculado: String = "",
    var linguagemVinculado: Language = Language.PORTUGUESE,
    var volumeVinculado: MangaVolume? = null,
    var vinculados: MutableList<VinculoPagina> = mutableListOf(),
    var naoVinculados: MutableList<VinculoPagina> = mutableListOf(),
    val dataCriacao: LocalDateTime = LocalDateTime.now(),
    var ultimaAlteracao: LocalDateTime = LocalDateTime.now()
) : EntityBase<UUID?, Vinculo>() {

    @Override
    override fun toString(): String {
        return ("Vinculo [id=" + id + ", base=" + base + ", volume=" + volume + ", nomeArquivoOriginal="
                + nomeArquivoOriginal + ", nomeArquivoVinculado=" + nomeArquivoVinculado + ", linguagemOriginal="
                + linguagemOriginal + ", linguagemVinculado=" + linguagemVinculado + ", dataCriacao=" + dataCriacao
                + ", ultimaAlteracao=" + ultimaAlteracao + "]")
    }

    @Override
    override fun hashCode(): Int {
        return Objects.hash(base, linguagemOriginal, linguagemVinculado, nomeArquivoOriginal, nomeArquivoVinculado)
    }

    override fun getId(): UUID? = id

    fun setId(id: UUID?) {
        this.id = id
    }

    override fun create(id: UUID?): Vinculo = Vinculo(id, "", 0, "", Language.PORTUGUESE, MangaVolume())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vinculo

        if (base != other.base) return false
        if (nomeArquivoOriginal != other.nomeArquivoOriginal) return false
        if (linguagemOriginal != other.linguagemOriginal) return false
        if (nomeArquivoVinculado != other.nomeArquivoVinculado) return false
        if (linguagemVinculado != other.linguagemVinculado) return false

        return true
    }

}