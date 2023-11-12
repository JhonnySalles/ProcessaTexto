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

    public List<String> getTabelas() throws ExcessaoBd {
        return mangaDao.getTabelas();
    }

    public void createTabela(String base) throws ExcessaoBd {
        mangaDao.createTabela(base);
    }

    public String findTabela(String base) throws ExcessaoBd {
        return mangaDao.getTabelas().stream().findFirst().orElse("");
    }


    public void salvarVolume(String base, NovelVolume volume) throws ExcessaoBd {
        //insertVocabularios(base, volume.getId(), null, null, volume.getVocabularios());
        //mangaDao.updateProcessado(base, "volumes", volume.getId());
    }

}
