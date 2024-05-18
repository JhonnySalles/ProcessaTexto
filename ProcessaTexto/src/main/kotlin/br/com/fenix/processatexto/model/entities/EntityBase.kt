package br.com.fenix.processatexto.model.entities

abstract class EntityBase<ID, E>() : Entity<ID, E>, Cloneable {
    public override fun clone(): Any {
        return super.clone()
    }
}