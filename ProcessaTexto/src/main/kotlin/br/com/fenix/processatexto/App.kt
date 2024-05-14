package br.com.fenix.processatexto

import br.com.fenix.processatexto.database.JpaFactory


object App {
    @JvmStatic
    fun main(args: Array<String>) {
        val session = JpaFactory.getFactory()
        //Run.main(args)
    }
}