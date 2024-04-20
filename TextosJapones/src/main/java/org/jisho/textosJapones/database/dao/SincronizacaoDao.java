package org.jisho.textosJapones.database.dao;

import org.jisho.textosJapones.model.entities.Sincronizacao;
import org.jisho.textosJapones.model.enums.Conexao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public interface SincronizacaoDao {

	void update(Sincronizacao obj) throws ExcessaoBd;

	Sincronizacao select(Conexao tipo) throws ExcessaoBd;

}
