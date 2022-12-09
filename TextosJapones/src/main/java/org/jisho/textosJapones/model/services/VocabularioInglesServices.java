package org.jisho.textosJapones.model.services;

import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.VocabularioDao;
import org.jisho.textosJapones.model.entities.Vocabulario;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class VocabularioInglesServices {

	private VocabularioDao vocabularioDao = DaoFactory.createVocabularioInglesDao();

	public VocabularioInglesServices insertOrUpdate(List<Vocabulario> lista) throws ExcessaoBd {
		for (Vocabulario obj : lista) {
			if (vocabularioDao.exist(obj.getVocabulario()))
				vocabularioDao.update(obj);
			else
				vocabularioDao.insert(obj);
		}

		return this;
	}

	public VocabularioInglesServices insertOrUpdate(Vocabulario obj) throws ExcessaoBd {
		if (vocabularioDao.exist(obj.getVocabulario()))
			vocabularioDao.update(obj);
		else
			vocabularioDao.insert(obj);

		return this;
	}

	public VocabularioInglesServices insert(Vocabulario obj) throws ExcessaoBd {
		if (!obj.getPortugues().isEmpty())
			vocabularioDao.insert(obj);

		return this;
	}

	public VocabularioInglesServices insert(List<Vocabulario> lista) throws ExcessaoBd {
		for (Vocabulario obj : lista)
			insert(obj);

		return this;
	}
	
	public void insertExclusao(List<String> exclusoes) throws ExcessaoBd {
		for (String exclusao : exclusoes)
			insertExclusao(exclusao);
	}

	public VocabularioInglesServices insertExclusao(String palavra) throws ExcessaoBd {
		vocabularioDao.insertExclusao(palavra.trim());
		return this;
	}

	public boolean existeExclusao(String palavra) throws ExcessaoBd {
		return vocabularioDao.existeExclusao(palavra, palavra);
	}
	
	public boolean existeExclusao(String palavra, String basico) throws ExcessaoBd {
		return vocabularioDao.existeExclusao(palavra, basico);
	}

	public Set<String> selectExclusao() throws ExcessaoBd {
		return vocabularioDao.selectExclusao();
	}

	public void update(Vocabulario obj) throws ExcessaoBd {
		vocabularioDao.update(obj);
	}

	public void delete(Vocabulario obj) throws ExcessaoBd {
		vocabularioDao.delete(obj);
	}

	public Vocabulario select(String vocabulario) throws ExcessaoBd {
		return vocabularioDao.select(vocabulario);
	}
	
	public boolean existe(String palavra) throws ExcessaoBd {
		return vocabularioDao.exist(palavra);
	}

}
