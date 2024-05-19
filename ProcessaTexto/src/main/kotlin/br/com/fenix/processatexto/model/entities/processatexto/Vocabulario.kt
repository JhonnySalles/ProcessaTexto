package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import com.google.gson.annotations.Expose
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*


@jakarta.persistence.Entity
open class Vocabulario(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ID", nullable = false, unique = true, length = 36)
    private var id: UUID? = null,
    @Column(length = 250, nullable = false, unique = true)
    @Expose open var vocabulario: String = "",
    @Column(name = "forma_basica", length = 250, nullable = true)
    open var formaBasica: String = "",
    @Column(length = 250, nullable = true)
    @Expose open var leitura: String = "",
    @Column(name = "leitura_novel", length = 250, nullable = true)
    open var leituraNovel: String = "",
    @Column(nullable = true)
    @Expose open var ingles: String = "",
    @Column(nullable = true)
    @Expose open var portugues: String = ""
) : EntityBase<UUID?, Vocabulario>() {

    @Transient
    var sincronizacao: String = ""

    constructor(vocabulario: String) : this(null, vocabulario)

    constructor(vocabulario: String, portugues: String) : this(null, vocabulario) {
        this.portugues = portugues
    }

    constructor(id: UUID?, vocabulario: String, portugues: String) : this(id, vocabulario) {
        this.portugues = portugues
    }

    constructor(vocabulario: String, formaBasica: String, leitura: String, leituraNovel: String) : this(null, vocabulario, formaBasica, leitura, leituraNovel)

    constructor(id: String, obj: HashMap<String, String>) : this(UUID.fromString(id)) {
        vocabulario = obj["vocabulario"]!!
        formaBasica = obj["formaBasica"]!!
        leitura = obj["leitura"]!!
        leituraNovel = obj["leituraNovel"]!!
        portugues = obj["portugues"]!!
        ingles = obj["ingles"]!!
    }

    @Override
    override fun toString(): String {
        return vocabulario
    }

    fun merge(vocab: Vocabulario) {
        vocabulario = vocab.vocabulario
        formaBasica = vocab.formaBasica
        leitura = vocab.leitura
        leituraNovel = vocab.leituraNovel
        portugues = vocab.portugues
        ingles = vocab.ingles
    }

    override fun getId(): UUID? = id

    override fun create(id: UUID?): Vocabulario = Vocabulario(id)

    open fun setId(id: UUID?) {
        this.id = id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vocabulario

        if (id != other.id) return false
        if (vocabulario != other.vocabulario) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + vocabulario.hashCode()
        return result
    }

}