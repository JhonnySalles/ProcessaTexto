package org.jisho.textosJapones.model.services;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.NovelDao;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaCapitulo;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaPagina;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaVolume;
import org.jisho.textosJapones.model.entities.novelextractor.NovelTabela;
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

        deleteExistingFile(tabela, volume.getArquivo(), volume.getLingua());
        novelDao.insertVolume(tabela, volume);
    }

    public void deleteExistingFile(String tabela, String arquivo, Language linguagem) throws ExcessaoBd {
        NovelVolume saved = novelDao.selectVolume(tabela, arquivo, linguagem);
        if (saved != null)
            novelDao.deleteVolume(tabela, saved);
    }

    public void updateCancel(String base, NovelVolume obj) throws ExcessaoBd {
        novelDao.updateCancel(base, obj);
    }

    public void updateVocabularioVolume(String base, NovelVolume volume) throws ExcessaoBd {
        insertVocabularios(base, volume.getId(), null, volume.getVocabularios());
        novelDao.updateProcessado(base, volume.getId());
    }

    public void insertVocabularios(String base, UUID idVolume, UUID idCapitulo, Set<VocabularioExterno> vocabularios) throws ExcessaoBd {
        novelDao.insertVocabulario(base, idVolume, idCapitulo, vocabularios);
    }

    public List<NovelTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga) throws ExcessaoBd {
        return novelDao.selectTabelas(todos, isLike, base, linguagem, manga);
    }

}
