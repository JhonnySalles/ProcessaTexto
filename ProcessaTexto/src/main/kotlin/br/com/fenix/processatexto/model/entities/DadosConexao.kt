package br.com.fenix.processatexto.model.entities

import br.com.fenix.processatexto.model.enums.Conexao
import br.com.fenix.processatexto.model.enums.Driver
import jakarta.persistence.*

@jakarta.persistence.Entity
@Table(name = "conexoes")
data class DadosConexao(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true, length = 11)
    private val id: Long?,
    @Enumerated(EnumType.STRING)
    val tipo: Conexao,
    @Column(length = 100, nullable = true)
    val url: String,
    @Column(name = "base", length = 100, nullable = true)
    val base: String,
    @Column(name = "username", length = 250, nullable = true)
    val usuario: String,
    @Column(name = "password", length = 250, nullable = true)
    val senha: String,
    @Enumerated(EnumType.STRING)
    val driver: Driver
) : EntityBase<Long?, DadosConexao>() {
    constructor(url: String, base: String, usuario: String, senha: String) :
            this(0, Conexao.PROCESSA_TEXTO, url, base, usuario, senha, Driver.MYSQL)

    constructor() : this(0, Conexao.PROCESSA_TEXTO, "", "", "", "", Driver.MYSQL)

    override fun getId(): Long? = id

    override fun create(id: Long?): DadosConexao {
        TODO("Not yet implemented")
    }
}