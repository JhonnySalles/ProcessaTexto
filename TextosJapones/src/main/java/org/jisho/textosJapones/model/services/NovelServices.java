package org.jisho.textosJapones.model.services;

import javafx.collections.ObservableList;
import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.MangaDao;
import org.jisho.textosJapones.database.dao.NovelDao;
import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.entities.novelextractor.NovelVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NovelServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(NovelServices.class);

    private final NovelDao mangaDao = DaoFactory.createNovelDao();

    public void createTabela(String base) throws ExcessaoBd {
        mangaDao.createTabela(base);
    }

    public Boolean existTabela(String tabela) throws ExcessaoBd {
        String base = mangaDao.selectTabela(tabela);
        return base != null && base.equalsIgnoreCase(tabela);
    }

    public void salvarVolume(String tabela, NovelVolume volume) throws ExcessaoBd {
        if (!existTabela(tabela))
            createTabela(tabela);

        mangaDao.insertVolume(tabela, volume);
    }

}
