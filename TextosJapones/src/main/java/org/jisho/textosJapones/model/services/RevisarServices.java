package org.jisho.textosJapones.model.services;

import java.util.List;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.RevisarDao;
import org.jisho.textosJapones.model.entities.Revisar;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class RevisarServices {

	private RevisarDao revisarDao = DaoFactory.createRevisarDao();

	public List<Revisar> selectAll() throws ExcessaoBd {
		return revisarDao.selectAll();
	}

	public List<Revisar> selectTraduzir() throws ExcessaoBd {
		return revisarDao.selectTraduzir();
	}

	public RevisarServices insertOrUpdate(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista) {
			if (revisarDao.exist(obj.getVocabulario()))
				revisarDao.update(obj);
			else
				revisarDao.insert(obj);
		}

		return this;
	}

	public RevisarServices insertOrUpdate(Revisar obj) throws ExcessaoBd {
		if (revisarDao.exist(obj.getVocabulario()))
			revisarDao.update(obj);
		else
			revisarDao.insert(obj);

		return this;
	}

	public RevisarServices insert(Revisar obj) throws ExcessaoBd {
		revisarDao.insert(obj);
		return this;
	}

	public RevisarServices insert(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista)
			revisarDao.insert(obj);

		return this;
	}

	public void update(Revisar obj) throws ExcessaoBd {
		revisarDao.update(obj);
	}

	public void delete(Revisar obj) throws ExcessaoBd {
		revisarDao.delete(obj);
	}
	
	public void delete(String vocabulario) throws ExcessaoBd {
		revisarDao.delete(vocabulario);
	}

	public void delete(List<Revisar> lista) throws ExcessaoBd {
		for (Revisar obj : lista)
			delete(obj);
	}

	public Revisar select(String vocabulario, String base) throws ExcessaoBd {
		return revisarDao.select(vocabulario, base);
	}

	public Revisar select(String vocabulario) throws ExcessaoBd {
		return revisarDao.select(vocabulario);
	}

	public boolean existe(String palavra) throws ExcessaoBd {
		return revisarDao.exist(palavra);
	}

	public List<String> selectFrases(String select) throws ExcessaoBd {
		return revisarDao.selectFrases(select);
	}

	public String selectQuantidadeRestante() throws ExcessaoBd {
		return revisarDao.selectQuantidadeRestante();
	}

	public Revisar selectRevisar(String pesquisar) throws ExcessaoBd {
		return revisarDao.selectRevisar(pesquisar);
	}

	public List<Revisar> selectSimilar(String vocabulario, String ingles) throws ExcessaoBd {
		return revisarDao.selectSimilar(vocabulario, ingles);
	}
}
