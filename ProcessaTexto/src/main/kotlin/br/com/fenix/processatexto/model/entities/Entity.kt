package br.com.fenix.processatexto.model.entities

interface Entity<ID, T> {
    fun getId(): ID?
    fun create(id: ID): T
}