package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.VocabularioDao


class VocabularioJaponesServices : VocabularioBaseServices() {

    override val vocabularioDao: VocabularioDao = DaoFactory.createVocabularioJaponesDao()
    override val externos: List<VocabularioDao> = DaoFactory.vocabularioExternos

}