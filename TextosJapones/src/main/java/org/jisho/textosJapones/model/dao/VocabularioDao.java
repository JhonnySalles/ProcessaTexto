package org.jisho.textosJapones.model.dao;

import java.util.List;

import org.jisho.textosJapones.model.entities.Vocabulario;

public interface VocabularioDao {

	void insert(Vocabulario obj);

	void update(Vocabulario obj);

	void delete(Vocabulario obj);

	boolean exist(String vocabulario);

	Vocabulario select(String vocabulario);

	List<Vocabulario> selectAll();

}
