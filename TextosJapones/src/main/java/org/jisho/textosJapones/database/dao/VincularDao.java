package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaTabela;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaVinculo;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;
import java.util.UUID;

public interface VincularDao {

	void update(String base, Vinculo obj) throws ExcessaoBd;

	Vinculo select(String base, UUID id) throws ExcessaoBd;
	
	Vinculo select(String base, Integer volume, String mangaOriginal, Language original, String arquivoOriginal,
			String mangaVinculado, Language vinculado, String arquivoVinculado) throws ExcessaoBd;

	Vinculo select(String base, Integer volume, String mangaOriginal, String arquivoOriginal, String mangaVinculado,
			String arquivoVinculado) throws ExcessaoBd;

	Vinculo select(String base, Integer volume, String mangaOriginal, Language original, String mangaVinculado,
			Language vinculado) throws ExcessaoBd;

	void delete(String base, Vinculo obj) throws ExcessaoBd;

	UUID insert(String base, Vinculo obj) throws ExcessaoBd;

	Boolean createTabelas(String nome) throws ExcessaoBd;

	List<String> getMangas(String base, Language linguagem) throws ExcessaoBd;

	List<String> getTabelas() throws ExcessaoBd;

	List<MangaVinculo> selectVinculo(String base, String manga, Integer volume, Float capitulo, Language linguagem) throws ExcessaoBd;

	List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo, Language linguagem) throws ExcessaoBd;

}
