package org.jisho.textosJapones.model.dao;

import java.util.List;

import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface RevisarDao {

	void insert(Revisar obj) throws ExcessaoBd;

	void update(Revisar obj) throws ExcessaoBd;

	void delete(Revisar obj) throws ExcessaoBd;

	void delete(String vocabulario) throws ExcessaoBd;

	boolean exist(String vocabulario);

	Revisar select(String vocabulario, String base) throws ExcessaoBd;

	Revisar select(String vocabulario) throws ExcessaoBd;

	List<Revisar> selectAll() throws ExcessaoBd;

	List<String> selectFrases(String select) throws ExcessaoBd;

	List<Revisar> selectTraduzir() throws ExcessaoBd;

	String selectQuantidadeRestante() throws ExcessaoBd;

	Revisar selectRevisar(String pesquisar, Boolean isAnime, Boolean isManga) throws ExcessaoBd;

	List<Revisar> selectSimilar(String vocabulario, String ingles) throws ExcessaoBd;

	void incrementaVezesAparece(String vocabulario) throws ExcessaoBd;

	void setIsManga(Revisar obj) throws ExcessaoBd;
}
