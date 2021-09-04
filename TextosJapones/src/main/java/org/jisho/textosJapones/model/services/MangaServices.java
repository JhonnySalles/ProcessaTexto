package org.jisho.textosJapones.model.services;

import java.util.List;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.MangaDao;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaTexto;
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

	public List<MangaVolume> selectDadosTransferir(String baseOrigem) throws ExcessaoBd {
		return vocabularioDao.selectTransferir(baseOrigem);
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

	public void insertDadosTransferir(String base, MangaVolume volume) throws ExcessaoBd {
		Long idVolume = vocabularioDao.insertVolume(base, volume);
		for (MangaCapitulo capitulo : volume.getCapitulos()) {
			Long idCapitulo = vocabularioDao.insertCapitulo(base, idVolume, capitulo);
			for (MangaPagina pagina : capitulo.getPaginas()) {
				Long idPagina = vocabularioDao.insertPagina(base, idCapitulo, pagina);
				for (MangaTexto texto : pagina.getTextos()) {
					vocabularioDao.insertTexto(base, idPagina, texto);
				}
			}
		}
	}
	
	public void createDataBase(String base) throws ExcessaoBd {
		vocabularioDao.createDatabase(base);
	}

}
