package org.jisho.textosJapones.model.services;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.KanjiDao;
import org.jisho.textosJapones.model.entities.Kanji;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class KanjiServices {

    private final KanjiDao kanjiDao = DaoFactory.createKanjiDao();

    public Kanji select(String kanji) throws ExcessaoBd {
        return kanjiDao.select(kanji);
    }

}
