package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import javafx.beans.value.ChangeListener
import javafx.scene.control.CheckBox
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.util.*


@jakarta.persistence.Entity
data class Revisar(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "ID", nullable = false)
    private var id: UUID? = null,
    @Column(length = 250, nullable = false, unique = true)
    var vocabulario: String = "",
    @Column(name = "forma_basica", length = 250, nullable = true)
    var formaBasica: String = "",
    @Column(length = 250, nullable = true)
    var leitura: String = "",
    @Column(name = "leitura_novel", length = 250, nullable = true)
    var leituraNovel: String = "",
    @Column(nullable = true)
    var portugues: String = "",
    @Column(nullable = true)
    var ingles: String = "",
    @Column
    var aparece: Int = 0,
    @Transient
    private var _isRevisado: Boolean = false,
    @Column
    var isAnime: Boolean = false,
    @Column
    var isManga: Boolean = false,
    @Column
    var isNovel: Boolean = false
) : EntityBase<UUID?, Revisar>() {

    @Transient
    val revisado: CheckBox = CheckBox()

    @Transient
    private lateinit var listener: ChangeListener<Boolean>

    @Column(name = "revisado")
    var isRevisado: Boolean = _isRevisado
        set(value) {
            revisado.isSelected = isRevisado
            field = value
        }

    init {
        revisado.isSelected = isRevisado
        listener = ChangeListener<Boolean> { _, _, newValue ->
            try {
                revisado.selectedProperty().removeListener(listener)
                isRevisado = newValue
            } finally {
                revisado.selectedProperty().addListener(listener)
            }
        }
        revisado.selectedProperty().addListener(listener)
    }

    constructor(vocabulario: String) : this(null, vocabulario)

    constructor(vocabulario: String, formaBasica: String, leitura: String, leituraNovel: String) : this(null, vocabulario, formaBasica, leitura, leituraNovel)

    constructor(vocabulario: String, revisado: Boolean, isAnime: Boolean, isManga: Boolean, isNovel: Boolean) : this(null, vocabulario) {
        this.isAnime = isAnime
        this.isManga = isManga
        this.isNovel = isNovel
        this.revisado.isSelected = revisado
    }

    constructor(vocabulario: String, formaBasica: String, leitura: String, leituraNovel: String, revisado: Boolean, isAnime: Boolean, isManga: Boolean, isNovel: Boolean) :
            this(null, vocabulario, formaBasica, leitura, leituraNovel, "", "", 0, revisado, isAnime, isManga, isNovel) {
        this.revisado.isSelected = revisado
    }

    constructor(vocabulario: String, formaBasica: String, leitura: String, leituraNovel: String, portugues: String, ingles: String) :
            this(null, vocabulario, formaBasica, leitura, leituraNovel, portugues, ingles) {
    }

    override fun getId(): UUID? = id

    override fun create(id: UUID?): Revisar = Revisar(id)

    fun setId(id: UUID?) {
        this.id = id
    }

    @Override
    override fun toString(): String {
        return "$vocabulario, "
    }

    companion object {
        fun toVocabulario(revisar: Revisar): Vocabulario {
            return Vocabulario(revisar.id, revisar.vocabulario, revisar.formaBasica, revisar.leitura, revisar.leituraNovel, revisar.ingles, revisar.portugues)
        }

        fun toVocabulario(revisar: List<Revisar>): MutableList<Vocabulario> {
            val lista = mutableListOf<Vocabulario>()
            for (obj in revisar)
                lista.add(toVocabulario(obj))
            return lista
        }
    }
}