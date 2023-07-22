package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.controller.EstatisticaController.Tabela;
import org.jisho.textosJapones.model.entities.Estatistica;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;

public interface EstatisticaDao {

	void insert(Estatistica obj) throws ExcessaoBd;

	void update(Estatistica obj) throws ExcessaoBd;

	void delete(Estatistica obj) throws ExcessaoBd;

	Estatistica select(String kanji, String leitura) throws ExcessaoBd;

	List<Estatistica> select(String kanji) throws ExcessaoBd;

	List<Estatistica> selectAll() throws ExcessaoBd;
	
	List<Tabela> pesquisa(String pesquisa) throws ExcessaoBd;

}
