package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.entities.subtitle.Legenda;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;
import java.util.UUID;

public interface LegendasDao {

	List<String> getTabelas() throws ExcessaoBd;

	void createTabela(String base) throws ExcessaoBd;

	void delete(String tabela, Legenda obj) throws ExcessaoBd;
	void insert(String tabela, Legenda obj) throws ExcessaoBd;
	void update(String tabela, Legenda obj) throws ExcessaoBd;

	void comandoUpdate(String update, Processar obj) throws ExcessaoBd;

	List<Processar> comandoSelect(String select) throws ExcessaoBd;

	void comandoDelete(String delete) throws ExcessaoBd;

	void comandoInsert(FilaSQL fila) throws ExcessaoBd;

	void comandoUpdate(FilaSQL fila) throws ExcessaoBd;

	Boolean existFila(String deleteSql) throws ExcessaoBd;

	List<FilaSQL> comandoSelect() throws ExcessaoBd;

}
