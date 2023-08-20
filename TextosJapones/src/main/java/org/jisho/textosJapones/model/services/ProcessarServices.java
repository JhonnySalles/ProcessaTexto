package org.jisho.textosJapones.model.services;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.ProcessarDao;
import org.jisho.textosJapones.model.entities.FilaSQL;
import org.jisho.textosJapones.model.entities.Processar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import java.util.List;

public class ProcessarServices {

	private final ProcessarDao processarDao = DaoFactory.createProcessarDao();

	public void update(String update, List<Processar> lista) throws ExcessaoBd {
		for (Processar obj : lista)
			processarDao.update(update, obj);
	}

	public void update(String update, Processar obj) throws ExcessaoBd {
		processarDao.update(update, obj);
	}

	public List<Processar> select(String select) throws ExcessaoBd {
		return processarDao.select(select);
	}

	public void delete(String delete) throws ExcessaoBd {
		processarDao.delete(delete);
	}

	public void insertOrUpdateFila(FilaSQL fila) throws ExcessaoBd {
		if (fila.getId() == null)
			processarDao.insert(fila);
		else
			processarDao.update(fila);
	}

	public List<FilaSQL> selectFila() throws ExcessaoBd {
		return processarDao.select();
	}

}
