package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.database.dao.VocabularioDao
import java.sql.SQLException
import java.util.*


class RevisarInglesServices : RevisarBaseServices() {

    override val revisarDao: RevisarDao = DaoFactory.createRevisarInglesDao()
    override val externos: List<VocabularioDao> = DaoFactory.vocabularioExternos

    @Throws(SQLException::class)
    fun isValido(palavra: String): String = revisarDao.isValido(palavra)

}