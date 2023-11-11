package org.jisho.textosJapones.model.services;

import javafx.collections.ObservableList;
import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.MangaDao;
import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MangaServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(MangaServices.class);

    private final MangaDao mangaDao = DaoFactory.createMangaDao();

    public List<String> getTabelas() throws ExcessaoBd {
        return mangaDao.getTabelas();
    }

    public List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd {
        return mangaDao.selectTabelas(todos);
    }

    public List<MangaTabela> selectAll(String base, String manga, Integer volume, Float capitulo, Language linguagem)
            throws ExcessaoBd {
        return mangaDao.selectAll(base, manga, volume, capitulo, linguagem);
    }

    public List<MangaTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga) throws ExcessaoBd {
        return mangaDao.selectTabelas(todos, isLike, base, linguagem, manga);
    }

    public List<MangaTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga, Integer volume, Float capitulo)
            throws ExcessaoBd {
        return mangaDao.selectTabelas(todos, isLike, base, linguagem, manga, volume, capitulo);
    }

    public List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo,
                                               Language linguagem, Boolean inverterTexto) throws ExcessaoBd {
        return mangaDao.selectTabelasJson(base, manga, volume, capitulo, linguagem, inverterTexto);
    }

    public List<MangaVolume> selectDadosTransferir(String base, String tabela) throws ExcessaoBd {
        return mangaDao.selectDadosTransferir(base, tabela);
    }

    public List<String> getTabelasTransferir(String base, String tabela) throws ExcessaoBd {
        return mangaDao.getTabelasTransferir(base, tabela);
    }

    public void updateCancel(String base, MangaVolume obj) throws ExcessaoBd {
        for (MangaCapitulo capitulo : obj.getCapitulos())
            for (MangaPagina pagina : capitulo.getPaginas())
                mangaDao.updateCancel(base, pagina);
    }

    public void insertDadosTransferir(String base, MangaVolume volume) throws ExcessaoBd {
        UUID idVolume = mangaDao.insertVolume(base, volume);
        for (MangaCapitulo capitulo : volume.getCapitulos()) {
            UUID idCapitulo = mangaDao.insertCapitulo(base, idVolume, capitulo);
            for (MangaPagina pagina : capitulo.getPaginas()) {
                UUID idPagina = mangaDao.insertPagina(base, idCapitulo, pagina);
                for (MangaTexto texto : pagina.getTextos())
                    mangaDao.insertTexto(base, idPagina, texto);
            }
        }
    }

    public void updateVocabularioVolume(String base, MangaVolume volume) throws ExcessaoBd {
        insertVocabularios(base, volume.getId(), null, null, volume.getVocabularios());
        mangaDao.updateProcessado(base, "volumes", volume.getId());
    }

    public void updateVocabularioCapitulo(String base, MangaCapitulo capitulo) throws ExcessaoBd {
        insertVocabularios(base, null, capitulo.getId(), null, capitulo.getVocabularios());
        mangaDao.updateProcessado(base, "capitulos", capitulo.getId());
    }

    public void updateVocabularioPagina(String base, MangaPagina pagina) throws ExcessaoBd {
        insertVocabularios(base, null, null, pagina.getId(), pagina.getVocabularios());
        mangaDao.updateProcessado(base, "paginas", pagina.getId());
    }

    public void insertVocabularios(String base, UUID idVolume, UUID idCapitulo, UUID idPagina,
                                   Set<MangaVocabulario> vocabularios) throws ExcessaoBd {
        mangaDao.insertVocabulario(base, idVolume, idCapitulo, idPagina, vocabularios);
    }

    public void createTabela(String base) throws ExcessaoBd {
        mangaDao.createTabela(base);
    }

    private Boolean limpeza = true;

    public void salvarAjustes(ObservableList<MangaTabela> tabelas) throws ExcessaoBd {
        limpeza = true;
        for (MangaTabela tabela : tabelas)
            for (MangaVolume volume : tabela.getVolumes()) {
                if (limpeza && volume.getLingua().compareTo(Language.JAPANESE) == 0) {
                    limpeza = false;
                    mangaDao.deletarVocabulario(tabela.getBase());
                }

                if (volume.getId() == null)
                    volume.setId(mangaDao.insertVolume(tabela.getBase(), volume));
                else if (volume.isAlterado()) {
                    if (volume.isItemExcluido()) {
                        MangaVolume aux = mangaDao.selectVolume(tabela.getBase(), volume.getId());
                        if (aux != null) {
                            aux.getCapitulos().forEach(anterior -> {
                                Boolean existe = false;
                                for (MangaCapitulo atual : volume.getCapitulos())
                                    if (atual.getId().compareTo(anterior.getId()) == 0) {
                                        existe = true;
                                        break;
                                    }

                                if (!existe)
                                    try {
                                        mangaDao.deleteCapitulo(tabela.getBase(), anterior);
                                    } catch (ExcessaoBd e) {

                                        LOGGER.error(e.getMessage(), e);
                                    }
                            });
                        }
                    }
                    mangaDao.updateVolume(tabela.getBase(), volume);
                }

                if (volume.getCapitulos().isEmpty())
                    mangaDao.deleteVolume(tabela.getBase(), volume);
                else
                    for (MangaCapitulo capitulo : volume.getCapitulos())
                        if (capitulo.isAlterado()) {
                            if (capitulo.isItemExcluido()) {
                                MangaCapitulo aux = mangaDao.selectCapitulo(tabela.getBase(), capitulo.getId());
                                if (aux != null) {
                                    aux.getPaginas().forEach(anterior -> {
                                        Boolean existe = false;
                                        for (MangaPagina atual : capitulo.getPaginas())
                                            if (atual.getId().compareTo(anterior.getId()) == 0) {
                                                existe = true;
                                                break;
                                            }

                                        if (!existe)
                                            try {
                                                mangaDao.deletePagina(tabela.getBase(), anterior);
                                            } catch (ExcessaoBd e) {

                                                LOGGER.error(e.getMessage(), e);
                                            }
                                    });
                                }
                            }

                            for (MangaPagina pagina : capitulo.getPaginas())
                                if (pagina.isItemExcluido()) {
                                    MangaPagina aux = mangaDao.selectPagina(tabela.getBase(), pagina.getId());
                                    if (aux != null) {
                                        aux.getTextos().forEach(anterior -> {
                                            Boolean existe = false;
                                            for (MangaTexto atual : pagina.getTextos())
                                                if (atual.getId().compareTo(anterior.getId()) == 0) {
                                                    existe = true;
                                                    break;
                                                }

                                            if (!existe)
                                                try {
                                                    mangaDao.deleteTexto(tabela.getBase(), anterior);
                                                } catch (ExcessaoBd e) {

                                                    LOGGER.error(e.getMessage(), e);
                                                }
                                        });
                                    }
                                }

                            mangaDao.updateCapitulo(tabela.getBase(), volume.getId(), capitulo);
                        }
            }
    }

    public void salvarTraducao(String base, MangaVolume volume) throws ExcessaoBd {
        mangaDao.deleteVolume(base, volume);
        insertDadosTransferir(base, volume);
    }

}
