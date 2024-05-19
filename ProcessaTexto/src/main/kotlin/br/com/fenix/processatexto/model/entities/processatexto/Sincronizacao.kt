package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Conexao
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*


@Entity
data class Sincronizacao(
    @Id
    @Enumerated(EnumType.STRING)
    val conexao: Conexao = Conexao.PROCESSA_TEXTO,
    var envio: LocalDateTime = LocalDateTime.now(),
    var recebimento: LocalDateTime = LocalDateTime.now()
) : EntityBase<Conexao, Sincronizacao>() {

    constructor() : this(Conexao.PROCESSA_TEXTO) {}

    override fun getId(): Conexao = conexao

    override fun create(conexao: Conexao): Sincronizacao = Sincronizacao(conexao)

}