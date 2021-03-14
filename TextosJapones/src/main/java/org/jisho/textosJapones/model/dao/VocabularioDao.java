package org.jisho.textosJapones.model.dao;

import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface VocabularioDao {

	void insert(Vocabulario obj) throws ExcessaoBd;

	void update(Vocabulario obj) throws ExcessaoBd;

	void delete(Vocabulario obj) throws ExcessaoBd;

	boolean exist(String vocabulario);

	Vocabulario select(String vocabulario, String base) throws ExcessaoBd;
	
	Vocabulario select(String vocabulario) throws ExcessaoBd;

	List<Vocabulario> selectAll() throws ExcessaoBd;
	
	void insertExclusao(String palavra) throws ExcessaoBd;
	
	boolean existeExclusao(String palavra, String basico) throws ExcessaoBd;
	
	Set<String> selectExclusao() throws ExcessaoBd; 

}
