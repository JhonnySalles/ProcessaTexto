package org.jisho.textosJapones.model.services;

import java.util.List;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.Vocabulario;

public class VocabularioServices {

	private VocabularioDao vocabularioDao = DaoFactory.createVocabularioDao();

	public List<Vocabulario> selectAll() {
		return vocabularioDao.selectAll();
	}

	public void insertOrUpdate(List<Vocabulario> lista) {
		for (Vocabulario obj : lista) {
			if (vocabularioDao.exist(obj.getVocabulario()))
				vocabularioDao.update(obj);
			else
				vocabularioDao.insert(obj);
		}
	}

	public void insert(Vocabulario obj) {
		vocabularioDao.insert(obj);
	}

	public void insert(List<Vocabulario> lista) {
		for (Vocabulario obj : lista)
			vocabularioDao.insert(obj);
	}

	public void update(Vocabulario obj) {
		vocabularioDao.update(obj);
	}

	public void delete(Vocabulario obj) {
		vocabularioDao.delete(obj);
	}

	public Vocabulario select(String vocabulario) {
		return vocabularioDao.select(vocabulario);
	}

}
