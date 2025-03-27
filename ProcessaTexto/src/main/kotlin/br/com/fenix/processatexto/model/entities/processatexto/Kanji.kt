package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*

@Table(name = "kanjax_pt")
data class Kanji(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ID", nullable = false)
    private val id: UUID?,
    @Column(name = "kanji")
    val kanji: String,
    @Column(name = "palavra")
    val palavra: String,
    @Column(name = "significado")
    val significado: String
) : EntityBase<UUID?, Kanji>() {

    override fun getId(): UUID? = id
    override fun create(id: UUID?): Kanji = Kanji()

    constructor() : this(null, "", "", "")

}