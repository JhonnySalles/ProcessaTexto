package org.jisho.textosJapones.model.dao;

import java.util.List;

import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface ProcessarDao {

	void update(String update, Processar obj) throws ExcessaoBd;
	
	List<Processar> select(String select) throws ExcessaoBd;
	
	void exclusao(String exclusao) throws ExcessaoBd;

}
