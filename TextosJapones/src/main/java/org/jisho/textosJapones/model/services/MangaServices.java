package org.jisho.textosJapones.model.services;

import java.util.List;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.MangaDao;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class MangaServices {

	private MangaDao vocabularioDao = DaoFactory.createMangaDao();

	public List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd {
		return vocabularioDao.selectTabelas(todos);
	}

	public List<MangaTabela> selectTabelas(Boolean todos, String base, String manga) throws ExcessaoBd {
		return vocabularioDao.selectTabelas(todos, base, manga);
	}

	public void updateVocabularioVolume(String base, List<MangaVolume> lista) throws ExcessaoBd {
		for (MangaVolume volume : lista)
			updateVocabularioVolume(base, volume);
	}

	public void updateVocabularioCapitulo(String base, List<MangaCapitulo> lista) throws ExcessaoBd {
		for (MangaCapitulo capitulo : lista)
			updateVocabularioCapitulo(base, capitulo);
	}

	public void updateVocabularioPagina(String base, List<MangaPagina> lista) throws ExcessaoBd {
		for (MangaPagina pagina : lista)
			updateVocabularioPagina(base, pagina);
	}

	public void updateVocabularioVolume(String base, MangaVolume volume) throws ExcessaoBd {
		vocabularioDao.updateVocabularioVolume(base, volume);
		updateVocabularioCapitulo(base, volume.getCapitulos());
	}

	public void updateVocabularioCapitulo(String base, MangaCapitulo capitulo) throws ExcessaoBd {
		vocabularioDao.updateVocabularioCapitulo(base, capitulo);
		updateVocabularioPagina(base, capitulo.getPaginas());

	}

	public void updateVocabularioPagina(String base, MangaPagina pagina) throws ExcessaoBd {
		vocabularioDao.updateVocabularioPagina(base, pagina);
	}

}
