package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import jakarta.persistence.*
import java.util.UUID

@Entity
data class Estatistica(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID", nullable = false, unique = true)
    private var id: UUID? = null,
    @Column(length = 10, nullable = true)
    var kanji: String = "",
    @Column(length = 10, nullable = true)
    var tipo: String = "",
    @Column(length = 10, nullable = true)
    var leitura: String = "",
    @Column(nullable = true)
    var quantidade: Double = 0.0,
    @Column(nullable = true)
    var percentual: Float = 0f,
    @Column(nullable = true)
    var media: Double = 0.0,
    @Column(name = "percentual_medio", nullable = true)
    var percentMedia: Float = 0f,
    @Column(name = "cor_sequencial", nullable = true)
    var corSequencial: Int = 0,
    var isGerar : Boolean = false
) : EntityBase<UUID?, Estatistica>() {

    constructor(tipo: String) : this() {
        this.tipo = tipo
    }

    constructor(
        id: UUID, kanji: String, tipo: String, leitura: String, quantidade: Double, percentual: Float, media: Double,
        percentMedia: Float, corSequencial: Int
    ) : this() {
        this.id = id
        this.kanji = kanji
        this.tipo = tipo
        this.leitura = leitura
        this.quantidade = quantidade
        this.percentual = percentual
        this.media = media
        this.percentMedia = percentMedia
        this.corSequencial = corSequencial
    }

    override fun getId(): UUID? = id

    override fun create(id: UUID?): Estatistica = Estatistica(id)

    fun setId(id: UUID?) {
        this.id = id
    }

}