package org.jisho.textosJapones.database.dao;

import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaTexto;
import org.jisho.textosJapones.model.entities.MangaVocabulario;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface MangaDao {

	void updateVolume(String base, MangaVolume obj) throws ExcessaoBd;

	void updateCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd;
	
	void updateCapitulo(String base, Long IdVolume, MangaCapitulo obj) throws ExcessaoBd;

	void updatePagina(String base, MangaPagina obj) throws ExcessaoBd;

	void updateTexto(String base, MangaTexto obj) throws ExcessaoBd;
	
	MangaVolume selectVolume(String base, String manga, Integer volume, Language linguagem) throws ExcessaoBd;
	
	MangaVolume selectVolume(String base, Long id) throws ExcessaoBd;

	MangaCapitulo selectCapitulo(String base, Long id) throws ExcessaoBd;
	
	MangaPagina selectPagina(String base, Long id) throws ExcessaoBd;

	List<MangaTabela> selectAll(String base) throws ExcessaoBd;

	List<MangaTabela> selectAll(String base, String manga, Integer volume, Float capitulo, Language linguagem)
			throws ExcessaoBd;

	List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd;

	List<MangaTabela> selectTabelas(Boolean todos, String base, String manga) throws ExcessaoBd;

	List<MangaTabela> selectTabelas(Boolean todos, String base, String manga, Integer volume) throws ExcessaoBd;

	List<MangaTabela> selectTabelas(Boolean todos, String base, String manga, Integer volume, Float capitulo)
			throws ExcessaoBd;

	List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo, Language linguagem, Boolean inverterTexto)
			throws ExcessaoBd;

	void updateCancel(String base, MangaPagina obj) throws ExcessaoBd;

	Long insertVolume(String base, MangaVolume obj) throws ExcessaoBd;

	Long insertCapitulo(String base, Long idVolume, MangaCapitulo obj) throws ExcessaoBd;

	Long insertPagina(String base, Long idCapitulo, MangaPagina obj) throws ExcessaoBd;

	Long insertTexto(String base, Long idPagina, MangaTexto obj) throws ExcessaoBd;
	
	void deleteVolume(String base, MangaVolume obj) throws ExcessaoBd;
	
	void deleteCapitulo(String base, MangaCapitulo obj) throws ExcessaoBd;
	
	void deletePagina(String base, MangaPagina obj) throws ExcessaoBd;
	
	void deleteTexto(String base, MangaTexto obj) throws ExcessaoBd;

	void deletarVocabulario(String base) throws ExcessaoBd;
	
	List<MangaVolume> selectTransferir(String baseOrigem) throws ExcessaoBd;

	public void updateProcessado(String base, String tabela, Long id) throws ExcessaoBd;

	public void insertVocabulario(String base, Long idVolume, Long idCapitulo, Long idPagina,
			Set<MangaVocabulario> vocabulario) throws ExcessaoBd;

	void createDatabase(String base) throws ExcessaoBd;

	public void createBaseVocabulario(String nome) throws ExcessaoBd;


}
