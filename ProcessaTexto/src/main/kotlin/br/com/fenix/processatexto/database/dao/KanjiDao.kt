package br.com.fenix.processatexto.database.dao

import br.com.fenix.processatexto.model.entities.processatexto.Kanji
import java.sql.SQLException
import java.util.*


interface KanjiDao {
    @Throws(SQLException::class)
    fun select(kanji: String): Optional<Kanji>
}