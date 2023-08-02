package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.Kanji;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface KanjiDao {
    Kanji select(String kanji) throws ExcessaoBd;

}
