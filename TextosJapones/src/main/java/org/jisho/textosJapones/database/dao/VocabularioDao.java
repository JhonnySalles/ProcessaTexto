package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Database;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface VocabularioDao {

	Database getTipo();

	void insert(Vocabulario obj) throws ExcessaoBd;

	void update(Vocabulario obj) throws ExcessaoBd;

	void delete(Vocabulario obj) throws ExcessaoBd;

	boolean exist(String vocabulario);

	Vocabulario select(String vocabulario, String base) throws ExcessaoBd;
	
	Vocabulario select(String vocabulario) throws ExcessaoBd;

	Vocabulario select(UUID id) throws ExcessaoBd;

	List<Vocabulario> selectAll() throws ExcessaoBd;
	
	void insertExclusao(String palavra) throws ExcessaoBd;
	
	boolean existeExclusao(String palavra, String basico) throws ExcessaoBd;
	
	Set<String> selectExclusao() throws ExcessaoBd;

	List<Vocabulario> selectEnvioVocabulario(LocalDateTime ultimo) throws ExcessaoBd;

	Set<String> selectExclusaoEnvio(LocalDateTime ultimo) throws ExcessaoBd;

}
