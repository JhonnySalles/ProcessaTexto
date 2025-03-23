package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.comicinfo.ComicInfo;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ComicInfoDao {

    void insert(ComicInfo obj) throws ExcessaoBd;

    void update(ComicInfo obj) throws ExcessaoBd;

    ComicInfo select(String comic, String linguagem) throws ExcessaoBd;
    ComicInfo select(UUID id, String comic, String linguagem) throws ExcessaoBd;

    List<ComicInfo> selectEnvio(LocalDateTime ultimo) throws ExcessaoBd;

}
