package org.jisho.textosJapones.model.services;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.LegendasDao;
import org.jisho.textosJapones.database.mysql.DB;
import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.entities.subtitle.Legenda;
import org.jisho.textosJapones.model.enums.Conexao;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;
import java.util.UUID;

public class LegendasServices {

	private final LegendasDao processarDao = DaoFactory.createLegendasDao();

	public String getSchema() {
		return DB.getDados(Conexao.DECKSUBTITLE).getBase();
	}

	public List<String> getTabelas() throws ExcessaoBd {
		return processarDao.getTabelas();
	}

	private void criarTabela(String tabela) throws ExcessaoBd {
		if (processarDao.getTabelas().stream().filter(t -> t.equalsIgnoreCase(tabela)).findFirst().isEmpty())
			processarDao.createTabela(tabela);
	}

	public void salvar(String base, List<Legenda> legendas) throws ExcessaoBd {
		criarTabela(base);

		for (Legenda legenda : legendas) {
			if (legenda.getId() == null)
				processarDao.insert(base, legenda);
			else
				processarDao.update(base, legenda);
		}
	}

	public void comandoUpdate(String update, List<Processar> lista) throws ExcessaoBd {
		for (Processar obj : lista)
			processarDao.comandoUpdate(update, obj);
	}

	public void comandoUpdate(String update, Processar obj) throws ExcessaoBd {
		processarDao.comandoUpdate(update, obj);
	}

	public List<Processar> comandoSelect(String select) throws ExcessaoBd {
		return processarDao.comandoSelect(select);
	}

	public void comandoDelete(String delete) throws ExcessaoBd {
		processarDao.comandoDelete(delete);
	}

	public void insertOrUpdateFila(FilaSQL fila) throws ExcessaoBd {
		if (fila.getId() == null)
			processarDao.comandoInsert(fila);
		else
			processarDao.comandoUpdate(fila);
	}

	public List<FilaSQL> selectFila() throws ExcessaoBd {
		return processarDao.comandoSelect();
	}

	public Boolean existFila(String deleteSql) throws ExcessaoBd {
		return processarDao.existFila(deleteSql);
	}

}
