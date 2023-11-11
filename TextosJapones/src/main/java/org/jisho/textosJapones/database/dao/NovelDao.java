package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.mangaextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface NovelDao {

    void updateVolume(String base, MangaVolume obj) throws ExcessaoBd;

    void updateCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd;

    void updateCapitulo(String base, UUID IdVolume, MangaCapitulo obj) throws ExcessaoBd;

    void updatePagina(String base, MangaPagina obj) throws ExcessaoBd;

    void updateTexto(String base, MangaTexto obj) throws ExcessaoBd;

    MangaVolume selectVolume(String base, String manga, Integer volume, Language linguagem) throws ExcessaoBd;

    MangaVolume selectVolume(String base, UUID id) throws ExcessaoBd;

    MangaCapitulo selectCapitulo(String base, UUID id) throws ExcessaoBd;

    MangaPagina selectPagina(String base, UUID id) throws ExcessaoBd;

    List<MangaTabela> selectAll(String base) throws ExcessaoBd;

    List<MangaTabela> selectAll(String base, String manga, Integer volume, Float capitulo, Language linguagem)
            throws ExcessaoBd;

    List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd;

    List<MangaTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga) throws ExcessaoBd;

    List<MangaTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga, Integer volume) throws ExcessaoBd;

    List<MangaTabela> selectTabelas(Boolean todos, Boolean isLike, String base, Language linguagem, String manga, Integer volume, Float capitulo)
            throws ExcessaoBd;

    List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo, Language linguagem, Boolean inverterTexto)
            throws ExcessaoBd;

    void updateCancel(String base, MangaPagina obj) throws ExcessaoBd;

    UUID insertVolume(String base, MangaVolume obj) throws ExcessaoBd;

    UUID insertCapitulo(String base, UUID idVolume, MangaCapitulo obj) throws ExcessaoBd;

    UUID insertPagina(String base, UUID idCapitulo, MangaPagina obj) throws ExcessaoBd;

    UUID insertTexto(String base, UUID idPagina, MangaTexto obj) throws ExcessaoBd;

    void deleteVolume(String base, MangaVolume obj) throws ExcessaoBd;

    void deleteCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd;

    void deletePagina(String base, MangaPagina obj) throws ExcessaoBd;

    void deleteTexto(String base, MangaTexto obj) throws ExcessaoBd;

    void deletarVocabulario(String base) throws ExcessaoBd;

    void updateProcessado(String base, String tabela, UUID id) throws ExcessaoBd;

    void insertVocabulario(String base, UUID idVolume, UUID idCapitulo, UUID idPagina,
                                  Set<MangaVocabulario> vocabulario) throws ExcessaoBd;

    List<MangaVolume> selectDadosTransferir(String base, String tabela) throws ExcessaoBd;
    List<String> getTabelasTransferir(String base, String tabela) throws ExcessaoBd;

    void createTabela(String base) throws ExcessaoBd;

    List<String> getTabelas() throws ExcessaoBd;

}
