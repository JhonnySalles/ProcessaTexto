package org.jisho.textosJapones.model.dao;

import java.util.List;

import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaTexto;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface MangaDao {

	void updateVocabularioVolume(String base, MangaVolume obj) throws ExcessaoBd;

	void updateVocabularioCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd;

	void updateVocabularioPagina(String base, MangaPagina obj) throws ExcessaoBd;

	void updateVolume(String base, MangaVolume obj) throws ExcessaoBd;

	void updateCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd;

	void updatePagina(String base, MangaPagina obj) throws ExcessaoBd;

	void updateTexto(String base, MangaTexto obj) throws ExcessaoBd;

	List<MangaVolume> selectAll(String base) throws ExcessaoBd;

	List<MangaVolume> selectAll(String base, String manga, Integer volume, Float capitulo) throws ExcessaoBd;

	List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd;

	List<MangaTabela> selectTabelas(Boolean todos, String base, String manga) throws ExcessaoBd;

	List<MangaTabela> selectTabelas(Boolean todos, String base, String manga, Integer volume) throws ExcessaoBd;

	List<MangaTabela> selectTabelas(Boolean todos, String base, String manga, Integer volume, Float capitulo)
			throws ExcessaoBd;
	
	void updateCancel(String base, MangaPagina obj) throws ExcessaoBd;

	Long insertVolume(String base, MangaVolume obj) throws ExcessaoBd;

	Long insertCapitulo(String base, Long idVolume, MangaCapitulo obj) throws ExcessaoBd;

	Long insertPagina(String base, Long idCapitulo, MangaPagina obj) throws ExcessaoBd;

	Long insertTexto(String base, Long idPagina, MangaTexto obj) throws ExcessaoBd;

	List<MangaVolume> selectTransferir(String baseOrigem) throws ExcessaoBd;
	
	void createDatabase(String base) throws ExcessaoBd;
	
}
