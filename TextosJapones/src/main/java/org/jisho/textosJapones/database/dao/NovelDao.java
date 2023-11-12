package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.novelextractor.*;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface NovelDao {

    NovelVolume selectVolume(String base, String novel, Integer volume, Language linguagem) throws ExcessaoBd;

    NovelVolume selectVolume(String base, UUID id) throws ExcessaoBd;

    NovelCapitulo selectCapitulo(String base, UUID id) throws ExcessaoBd;

    List<NovelTabela> selectAll(String base) throws ExcessaoBd;

    List<NovelTabela> selectAll(String base, String manga, Integer volume, Float capitulo, Language linguagem) throws ExcessaoBd;

    UUID insertVolume(String base, NovelVolume obj) throws ExcessaoBd;

    UUID insertCapitulo(String base, UUID idVolume, NovelCapitulo obj) throws ExcessaoBd;

    UUID insertTexto(String base, UUID idPagina, NovelTexto obj) throws ExcessaoBd;

    void deleteVolume(String base, NovelVolume obj) throws ExcessaoBd;

    void deleteCapitulo(String base, NovelCapitulo obj) throws ExcessaoBd;

    void deleteTexto(String base, NovelTexto obj) throws ExcessaoBd;

    void deleteVocabulario(String base) throws ExcessaoBd;

    void insertVocabulario(String base, UUID idVolume, UUID idCapitulo, Set<NovelVocabulario> vocabulario) throws ExcessaoBd;

    void createTabela(String base) throws ExcessaoBd;

    String selectTabela(String base) throws ExcessaoBd;

    List<String> getTabelas() throws ExcessaoBd;

}
