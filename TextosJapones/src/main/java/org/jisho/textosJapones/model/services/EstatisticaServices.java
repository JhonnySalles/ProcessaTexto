package org.jisho.textosJapones.model.services;

import java.util.List;

import org.jisho.textosJapones.controller.EstatisticaController.Tabela;
import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.EstatisticaDao;
import org.jisho.textosJapones.model.entities.Estatistica;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class EstatisticaServices {

	private EstatisticaDao estatisticaDao = DaoFactory.createEstatisticaDao();

	public List<Estatistica> selectAll() throws ExcessaoBd {
		return estatisticaDao.selectAll();
	}

	public Estatistica select(String kanji, String leitura) throws ExcessaoBd {
		return estatisticaDao.select(kanji, leitura);
	}

	public List<Estatistica> select(String kanji) throws ExcessaoBd {
		return estatisticaDao.select(kanji);
	}

	public List<Tabela> pesquisa(String pesquisa) throws ExcessaoBd {
		return estatisticaDao.pesquisa(pesquisa);
	}

	public void insert(Estatistica obj) throws ExcessaoBd {
		if (!obj.getKanji().isEmpty())
			estatisticaDao.insert(obj);
	}

	public void insert(List<Estatistica> lista) throws ExcessaoBd {
		for (Estatistica obj : lista)
			if (!obj.getKanji().isEmpty())
				estatisticaDao.insert(obj);
	}

	public void update(Estatistica obj) throws ExcessaoBd {
		estatisticaDao.update(obj);
	}

	public void delete(Estatistica obj) throws ExcessaoBd {
		estatisticaDao.delete(obj);
	}

}
