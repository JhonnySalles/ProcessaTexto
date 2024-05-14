package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Conexao
import java.time.LocalDateTime
import java.util.*
import jakarta.persistence.*

@Entity
data class Sincronizacao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true, length = 11)
    var id: Long,
    @Enumerated(EnumType.STRING)
    val conexao: Conexao = Conexao.PROCESSA_TEXTO,
    var envio: LocalDateTime = LocalDateTime.now(),
    var recebimento: LocalDateTime = LocalDateTime.now()
) : EntityBase<Long, Sincronizacao>() {

    constructor() : this(0) {}

    override fun getId(): Long = id

    override fun create(id: Long): Sincronizacao = Sincronizacao(id)

}