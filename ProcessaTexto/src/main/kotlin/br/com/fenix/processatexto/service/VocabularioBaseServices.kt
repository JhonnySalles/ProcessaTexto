package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.processatexto.Vocabulario
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Database
import java.sql.SQLException
import java.util.*


abstract class VocabularioBaseServices {

    protected abstract val vocabularioDao: VocabularioDao
    protected abstract  val externos: List<VocabularioDao>

    @Throws(SQLException::class)
    protected fun updateExterno(vocab: Vocabulario) {
        SincronizacaoServices.enviar(Database.JAPONES, vocab)
        val vocabulario = VocabularioExterno(vocab.getId(), vocab.vocabulario, vocab.portugues, vocab.ingles, vocab.leitura, vocab.leituraNovel, true)
        for (dao in externos)
            dao.update(vocabulario)
    }

    @Throws(SQLException::class)
    fun selectAll(): List<Vocabulario> = vocabularioDao.selectAll()

    @Throws(SQLException::class)
    fun insertOrUpdate(lista: List<Vocabulario>): VocabularioBaseServices {
        for (obj in lista)
            insertOrUpdate(obj)
        return this
    }

    @Throws(SQLException::class)
    fun insertOrUpdate(obj: Vocabulario): VocabularioBaseServices {
        if (vocabularioDao.exist(obj.vocabulario))
            vocabularioDao.update(obj)
        else
            save(obj)
        updateExterno(obj)
        return this
    }

    @Throws(SQLException::class)
    protected fun save(obj: Vocabulario) {
        if (obj.getId() == null)
            obj.setId(UUID.randomUUID())
        vocabularioDao.insert(obj)
        updateExterno(obj)
    }

    @Throws(SQLException::class)
    fun insert(obj: Vocabulario): VocabularioBaseServices {
        if (obj.portugues.isNotEmpty()) {
            if (obj.getId() == null)
                obj.setId(UUID.randomUUID())
            vocabularioDao.insert(obj)
            updateExterno(obj)
        }
        return this
    }

    @Throws(SQLException::class)
    fun insert(lista: List<Vocabulario>): VocabularioBaseServices {
        for (obj in lista)
            insert(obj)
        return this
    }

    @Throws(SQLException::class)
    fun insertExclusao(exclusoes: List<String>) {
        for (exclusao in exclusoes)
            insertExclusao(exclusao)
    }

    @Throws(SQLException::class)
    fun insertExclusao(palavra: String): VocabularioBaseServices {
        vocabularioDao.insertExclusao(palavra.trim())
        return this
    }

    @Throws(SQLException::class)
    fun existeExclusao(palavra: String): Boolean = vocabularioDao.existeExclusao(palavra, palavra)

    @Throws(SQLException::class)
    fun existeExclusao(palavra: String, basico: String): Boolean = vocabularioDao.existeExclusao(palavra, basico)

    @Throws(SQLException::class)
    fun selectExclusao(): Set<String> = vocabularioDao.selectExclusao()

    @Throws(SQLException::class)
    fun update(obj: Vocabulario) {
        vocabularioDao.update(obj)
        updateExterno(obj)
    }

    @Throws(SQLException::class)
    fun delete(obj: Vocabulario) {
        vocabularioDao.delete(obj)
    }

    @Throws(SQLException::class)
    fun select(vocabulario: String, base: String): Optional<Vocabulario> = vocabularioDao.select(vocabulario, base)

    @Throws(SQLException::class)
    fun select(vocabulario: String): Optional<Vocabulario> = vocabularioDao.select(vocabulario)

    @Throws(SQLException::class)
    fun existe(palavra: String): Boolean = vocabularioDao.exist(palavra)
}