package org.jisho.textosJapones.model.services;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.NovelDao;
import org.jisho.textosJapones.model.entities.novelextractor.NovelVolume;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NovelServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(NovelServices.class);

    private final NovelDao novelDao = DaoFactory.createNovelDao();

    public List<String> getTabelas() throws ExcessaoBd {
        return novelDao.getTabelas();
    }

    public void createTabela(String base) throws ExcessaoBd {
        novelDao.createTabela(base);
    }

    public Boolean existTabela(String tabela) throws ExcessaoBd {
        String base = novelDao.selectTabela(tabela);
        return base != null && base.equalsIgnoreCase(tabela);
    }

    public void salvarVolume(String tabela, NovelVolume volume) throws ExcessaoBd {
        if (!existTabela(tabela))
            createTabela(tabela);

        novelDao.insertVolume(tabela, volume);
    }

}
