package org.jisho.textosJapones.model.services;

import java.util.List;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.VincularDao;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class VincularServices {

	private VincularDao dao = DaoFactory.createVincularDao();
	
	public void salvar(String base, Vinculo obj) throws ExcessaoBd {
		if (obj.getId() == null)
			insert(base, obj);
		else
			update(base, obj);
	}

	public void update(String base, Vinculo obj) throws ExcessaoBd {
		dao.update(base, obj);
	}

	public Vinculo select(String base, String manga, Integer volume, Language original, String arquivoOriginal,
			Language vinculado, String arquivoVinculado) throws ExcessaoBd {
		if (original != null && arquivoOriginal == null)
			return select(base, manga, volume, original, vinculado);
		else if (original == null && arquivoOriginal != null)
			return select(base, manga, volume, arquivoOriginal, arquivoVinculado);
		else
			return dao.select(base, manga, volume, original, arquivoOriginal, vinculado, arquivoVinculado);
	}

	public Vinculo select(String base, String manga, Integer volume, String original, String vinculado)
			throws ExcessaoBd {
		return dao.select(base, manga, volume, original, vinculado);
	}

	public Vinculo select(String base, String manga, Integer volume, Language original, Language vinculado)
			throws ExcessaoBd {
		return dao.select(base, manga, volume, original, vinculado);
	}

	public void delete(String base, Vinculo obj) throws ExcessaoBd {
		dao.delete(base, obj);
	}

	public Long insert(String base, Vinculo obj) throws ExcessaoBd {
		return dao.insert(base, obj);
	}

	public Boolean createTabelas(String nome) throws ExcessaoBd {
		return dao.createTabelas(nome);
	}
	
	public List<String> getTabelas() throws ExcessaoBd {
		return dao.getTabelas();
	}
	

}
