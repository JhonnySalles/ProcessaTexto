package org.jisho.textosJapones.model.services;

import org.jisho.textosJapones.database.dao.ComicInfoDao;
import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.model.entities.comicinfo.ComicInfo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class ComicInfoServices {

    private final ComicInfoDao comicInfoDao = DaoFactory.createComicInfoDao();

    public ComicInfo select(String comic, String linguagem) throws ExcessaoBd {
        return comicInfoDao.select(comic, linguagem);
    }

    public void save(ComicInfo comic) throws ExcessaoBd {
        ComicInfo saved = select(comic.getComic(), comic.getLanguageISO());
        if (saved == null || saved.getId() == null)
            comicInfoDao.insert(comic);
        else {
            comic.setId(saved.getId());
            comicInfoDao.update(comic);
        }
    }
}
