package br.com.fenix.processatexto.model.entities.processatexto

import br.com.fenix.processatexto.model.entities.EntityBase
import br.com.fenix.processatexto.model.enums.Conexao
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import java.time.LocalDateTime


@Entity
data class Sincronizacao(
    @Id
    @Enumerated(EnumType.STRING)
    var conexao: Conexao = Conexao.PROCESSA_TEXTO,
    var envio: LocalDateTime = LocalDateTime.now(),
    var recebimento: LocalDateTime = LocalDateTime.now()
) : EntityBase<Conexao, Sincronizacao>() {

    constructor() : this(Conexao.PROCESSA_TEXTO) {}

    override fun getId(): Conexao = conexao
    override fun setId(id : Conexao?) { }

    override fun create(conexao: Conexao): Sincronizacao = Sincronizacao(conexao)

}