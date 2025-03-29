package br.com.fenix.processatexto.model.entities

interface Entity<ID, T> {
    fun getId(): ID?
    fun setId(id: ID?)
    fun create(id: ID): T
}