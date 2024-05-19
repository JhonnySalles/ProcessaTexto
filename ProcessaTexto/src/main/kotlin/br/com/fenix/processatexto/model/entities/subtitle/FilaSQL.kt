package br.com.fenix.processatexto.model.entities.subtitle

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Language
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*


@jakarta.persistence.Entity
@Table(name="_fila_sql")
data class FilaSQL(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ID", nullable = false, unique = true, length = 36)
    private val id: UUID? = null,
    var sequencial: Long = 0,
    @Column(name = "select_sql", nullable = true)
    var select: String = "",
    @Column(name = "update_sql", nullable = true)
    var update: String = "",
    @Column(name = "delete_sql", nullable = true)
    var delete: String = "",
    @Column(name = "vocabulario", nullable = true)
    var vocabulario: String = "",
    @Enumerated(EnumType.STRING)
    var linguagem: Language = Language.PORTUGUESE,
    var isExporta: Boolean = false,
    var isLimpeza: Boolean = false,
) : EntityBase<UUID?, FilaSQL>() {

    constructor(select: String, update: String, delete: String, linguagem: Language, isExporta: Boolean, isLimpeza: Boolean) :
            this(null, 0L, select, update, delete) {
        this.linguagem = linguagem
        this.isExporta = isExporta
        this.isLimpeza = isLimpeza
    }

    override fun getId(): UUID? = id

    override fun create(id: UUID?): FilaSQL = FilaSQL(id)

    @Override
    override fun toString(): String {
        return "FilaSQL [id=$id, select=$select, update=$update, delete=$delete, vocabulario=$vocabulario]"
    }
}