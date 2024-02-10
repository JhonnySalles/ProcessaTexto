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

public class LegendasServices {

	private final LegendasDao dao = DaoFactory.createLegendasDao();

	public String getSchema() {
		return DB.getDados(Conexao.DECKSUBTITLE).getBase();
	}

	public List<String> getTabelas() throws ExcessaoBd {
		return dao.getTabelas();
	}

	private void criarTabela(String tabela) throws ExcessaoBd {
		if (dao.getTabelas().stream().filter(t -> t.equalsIgnoreCase(tabela)).findFirst().isEmpty())
			dao.createTabela(tabela);
	}

	public void salvar(String base, List<Legenda> legendas) throws ExcessaoBd {
		criarTabela(base);

		dao.delete(base, legendas.get(0));
		for (Legenda legenda : legendas) {
			if (legenda.getId() == null)
				dao.insert(base, legenda);
			else
				dao.update(base, legenda);
		}
	}

	public void comandoUpdate(String update, List<Processar> lista) throws ExcessaoBd {
		for (Processar obj : lista)
			dao.comandoUpdate(update, obj);
	}

	public void comandoUpdate(String update, Processar obj) throws ExcessaoBd {
		dao.comandoUpdate(update, obj);
	}

	public List<Processar> comandoSelect(String select) throws ExcessaoBd {
		return dao.comandoSelect(select);
	}

	public void comandoDelete(String delete) throws ExcessaoBd {
		dao.comandoDelete(delete);
	}

	public void insertOrUpdateFila(FilaSQL fila) throws ExcessaoBd {
		if (fila.getId() == null)
			dao.comandoInsert(fila);
		else
			dao.comandoUpdate(fila);
	}

	public List<FilaSQL> selectFila() throws ExcessaoBd {
		return dao.comandoSelect();
	}

	public Boolean existFila(String deleteSql) throws ExcessaoBd {
		return dao.existFila(deleteSql);
	}

}
