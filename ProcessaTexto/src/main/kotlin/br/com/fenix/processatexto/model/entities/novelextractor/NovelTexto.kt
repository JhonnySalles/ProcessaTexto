package org.jisho.textosJapones.model.entities.novelextractor

import com.google.gson.annotations.Expose
import java.util.*

data class NovelTexto(
    var id: UUID? = null,
    @Expose var texto: String = "",
    @Expose var sequencia: Int = 0
) {
    @Override
    override fun toString(): String {
        return "NovelTexto [id=$id, texto=$texto, sequencia=$sequencia]"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NovelTexto

        if (id != other.id) return false
        if (texto != other.texto) return false
        if (sequencia != other.sequencia) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + texto.hashCode()
        result = 31 * result + sequencia
        return result
    }

}