package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;

public interface ProcessarDao {

	void update(String update, Processar obj) throws ExcessaoBd;

	List<Processar> select(String select) throws ExcessaoBd;

	void delete(String delete) throws ExcessaoBd;

	void insert(FilaSQL fila) throws ExcessaoBd;

	void update(FilaSQL fila) throws ExcessaoBd;

	List<FilaSQL> select() throws ExcessaoBd;

}
