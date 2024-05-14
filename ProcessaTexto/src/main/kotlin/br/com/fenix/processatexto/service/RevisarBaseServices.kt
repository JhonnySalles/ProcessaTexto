package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.dao.RevisarDao
import br.com.fenix.processatexto.database.dao.VocabularioDao
import br.com.fenix.processatexto.model.entities.processatexto.Revisar
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import java.sql.SQLException
import java.util.*


abstract class RevisarBaseServices {

    protected abstract val revisarDao: RevisarDao
    protected abstract val externos: List<VocabularioDao>

    @Throws(SQLException::class)
    protected fun updateExterno(revisar: Revisar) {
        val vocabulario = VocabularioExterno(
            revisar.getId(),
            revisar.vocabulario,
            revisar.portugues,
            revisar.ingles,
            revisar.leitura,
            revisar.leituraNovel,
            revisar.revisado.isSelected
        )
        for (dao in externos)
            dao.update(vocabulario)
    }

    @Throws(SQLException::class)
    fun selectAll(): List<Revisar> = revisarDao.selectAll()

    @Throws(SQLException::class)
    fun selectTraduzir(quantidadeRegistros: Int): List<Revisar> = revisarDao.selectTraduzir(quantidadeRegistros)

    @Throws(SQLException::class)
    fun insertOrUpdate(lista: List<Revisar>): RevisarBaseServices {
        for (obj in lista)
            insertOrUpdate(obj)
        return this
    }

    @Throws(SQLException::class)
    fun insertOrUpdate(obj: Revisar): RevisarBaseServices {
        if (revisarDao.exist(obj.vocabulario))
            revisarDao.update(obj)
        else
            insert(obj)
        updateExterno(obj)
        return this
    }

    @Throws(SQLException::class)
    fun insert(obj: Revisar): RevisarBaseServices {
        if (obj.getId() == null)
            obj.setId(UUID.randomUUID())
        revisarDao.insert(obj)
        return this
    }

    @Throws(SQLException::class)
    fun insert(lista: List<Revisar>): RevisarBaseServices {
        for (obj in lista)
            insert(obj)
        return this
    }

    @Throws(SQLException::class)
    fun update(obj: Revisar) {
        revisarDao.update(obj)
        updateExterno(obj)
    }

    @Throws(SQLException::class)
    fun delete(obj: Revisar) = revisarDao.delete(obj)

    @Throws(SQLException::class)
    fun delete(vocabulario: String) = revisarDao.delete(vocabulario)

    @Throws(SQLException::class)
    fun delete(lista: List<Revisar>) {
        for (obj in lista)
            delete(obj)
    }

    @Throws(SQLException::class)
    fun select(vocabulario: String, base: String): Optional<Revisar> = revisarDao.select(vocabulario, base)

    @Throws(SQLException::class)
    fun select(vocabulario: String): Optional<Revisar> = revisarDao.select(vocabulario)

    @Throws(SQLException::class)
    fun existe(palavra: String): Boolean = revisarDao.exist(palavra)

    @Throws(SQLException::class)
    fun selectFrases(select: String): List<String> = revisarDao.selectFrases(select)

    @Throws(SQLException::class)
    fun selectQuantidadeRestante(): String = revisarDao.selectQuantidadeRestante()

    @Throws(SQLException::class)
    fun selectRevisar(pesquisar: String, isAnime: Boolean, isManga: Boolean, isNovel: Boolean): Optional<Revisar> = revisarDao.selectRevisar(pesquisar, isAnime, isManga, isNovel)

    @Throws(SQLException::class)
    fun selectSimilar(vocabulario: String, ingles: String): MutableList<Revisar> = revisarDao.selectSimilar(vocabulario, ingles)

    @Throws(SQLException::class)
    fun incrementaVezesAparece(vocabulario: String) = revisarDao.incrementaVezesAparece(vocabulario)

    @Throws(SQLException::class)
    fun setIsManga(obj: Revisar) = revisarDao.setIsManga(obj)

    @Throws(SQLException::class)
    fun setIsNovel(obj: Revisar) = revisarDao.setIsNovel(obj)
}