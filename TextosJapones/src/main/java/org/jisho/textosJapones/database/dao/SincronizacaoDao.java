package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.Sincronizacao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.enums.Conexao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;
import java.util.Set;

public interface SincronizacaoDao {

	void update(Sincronizacao obj) throws ExcessaoBd;

	Sincronizacao select(Conexao tipo) throws ExcessaoBd;

}
