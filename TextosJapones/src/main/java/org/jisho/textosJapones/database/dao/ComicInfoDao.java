package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.comicinfo.ComicInfo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface ComicInfoDao {

    void insert(ComicInfo obj) throws ExcessaoBd;

    void update(ComicInfo obj) throws ExcessaoBd;

    ComicInfo select(String comic) throws ExcessaoBd;

}
