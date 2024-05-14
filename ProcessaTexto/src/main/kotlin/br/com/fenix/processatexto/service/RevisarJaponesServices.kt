package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.database.dao.VocabularioDao


class RevisarJaponesServices : RevisarBaseServices() {

    override val revisarDao: RevisarDao = DaoFactory.createRevisarJaponesDao()
    override val externos: List<VocabularioDao> = DaoFactory.vocabularioExternos

}