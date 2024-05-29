package br.com.fenix.processatexto.service

import br.com.fenix.processatexto.database.DaoFactory
import br.com.fenix.processatexto.database.dao.NovelDao
import br.com.fenix.processatexto.model.entities.processatexto.VocabularioExterno
import br.com.fenix.processatexto.model.enums.Language
import org.jisho.textosJapones.model.entities.novelextractor.NovelTabela
import br.com.fenix.processatexto.model.entities.novelextractor.NovelVolume
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*


class NovelServices {

    private val LOGGER = LoggerFactory.getLogger(NovelServices::class.java)

    private val novelDao: NovelDao? = DaoFactory.createNovelDao()

    @get:Throws(SQLException::class)
    val tabelas: List<String> get() = novelDao!!.tabelas

    @Throws(SQLException::class)
    fun selectAll(base: String, novel: String, volume: Int, linguagem: Language): MutableList<NovelVolume> = novelDao!!.selectAll(base, novel, volume, linguagem)

    @Throws(SQLException::class)
    fun selectVolume(base: String, novel: String, volume: Int, linguagem: Language): Optional<NovelVolume> = novelDao!!.selectVolume(base, novel, volume, linguagem)

    @Throws(SQLException::class)
    fun selectVolume(base: String, id: UUID): Optional<NovelVolume> = novelDao!!.selectVolume(base, id)

    @Throws(SQLException::class)
    fun saveVolume(base: String, volumes: List<NovelVolume>) : List<NovelVolume> {
        val saved = arrayListOf<NovelVolume>()
        for (volume in volumes)
            saved.add(saveVolume(base, volume))

        return saved
    }

    @Throws(SQLException::class)
    fun saveVolume(base: String, volume: NovelVolume): NovelVolume {
        if (volume.getId() == null) {
            volume.setId(UUID.randomUUID())
            novelDao!!.insertVolume(base, volume)
        } else
            novelDao!!.updateVolume(base, volume)

        val idVolume = volume.getId()!!
        for (capitulo in volume.capitulos) {
            if (capitulo.id == null) {
                capitulo.id = UUID.randomUUID()
                novelDao.insertCapitulo(base, idVolume, capitulo)
            } else
                novelDao.updateCapitulo(base, capitulo)
            val idCapitulo: UUID = capitulo.id!!

            for (texto in capitulo.textos) {
                if (texto.id == null) {
                    texto.id = UUID.randomUUID()
                    novelDao.insertTexto(base, idCapitulo, texto)
                } else
                    novelDao.updateTexto(base, texto)
            }
        }

        if (volume.capa != null)
            novelDao.insertCapa(base, idVolume, volume.capa!!)

        return volume
    }

    @Throws(SQLException::class)
    fun deleteVolume(base: String, volumes: List<NovelVolume>) {
        for (volume in volumes)
            deleteVolume(base, volume)
    }

    @Throws(SQLException::class)
    fun deleteVolume(base: String, volume: NovelVolume) = novelDao!!.deleteVolume(base, volume)

    @Throws(SQLException::class)
    fun createTabela(base: String) = novelDao!!.createTabela(base)

    @Throws(SQLException::class)
    fun deleteTabela(base: String) = novelDao!!.deleteTabela(base)

    @Throws(SQLException::class)
    fun existTabela(tabela: String): Boolean {
        val base: String = novelDao!!.selectTabela(tabela)
        return base.isNotEmpty() && base.equals(tabela, ignoreCase = true)
    }

    @Throws(SQLException::class)
    fun salvarVolume(tabela: String, volume: NovelVolume) {
        if (!existTabela(tabela))
            createTabela(tabela)
        deleteExistingFile(tabela, volume.arquivo, volume.lingua)
        novelDao!!.insertVolume(tabela, volume)
    }

    @Throws(SQLException::class)
    fun deleteExistingFile(tabela: String, arquivo: String, linguagem: Language) {
        val saved: Optional<NovelVolume> = novelDao!!.selectVolume(tabela, arquivo, linguagem)
        if (saved.isPresent)
            novelDao.deleteVolume(tabela, saved.get())
    }

    @Throws(SQLException::class)
    fun updateCancel(base: String, obj: NovelVolume) = novelDao!!.updateCancel(base, obj)

    @Throws(SQLException::class)
    fun updateVocabularioVolume(base: String, volume: NovelVolume) {
        insertVocabularios(base, volume.getId(), null, volume.vocabularios)
        novelDao!!.updateProcessado(base, volume.getId()!!)
    }

    @Throws(SQLException::class)
    fun insertVocabularios(base: String, idVolume: UUID?, idCapitulo: UUID?, vocabularios: MutableSet<VocabularioExterno>) {
        novelDao!!.insertVocabulario(base, idVolume, idCapitulo, vocabularios)
    }

    @Throws(SQLException::class)
    fun selectTabelas(todos: Boolean, isLike: Boolean, base: String, linguagem: Language, novel: String): List<NovelTabela> {
        return novelDao!!.selectTabelas(todos, isLike, base, linguagem, novel)
    }

    @Throws(SQLException::class)
    fun delete(tabela: String, obj: NovelVolume) = novelDao!!.deleteVolume(tabela, obj)
}