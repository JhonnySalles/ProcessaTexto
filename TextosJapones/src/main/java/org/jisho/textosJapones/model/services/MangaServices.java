package org.jisho.textosJapones.model.services;

import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.MangaDao;
import org.jisho.textosJapones.model.entities.MangaCapitulo;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaTabela;
import org.jisho.textosJapones.model.entities.MangaTexto;
import org.jisho.textosJapones.model.entities.MangaVocabulario;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

public class MangaServices {

	private MangaDao vocabularioDao = DaoFactory.createMangaDao();

	public List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd {
		return vocabularioDao.selectTabelas(todos);
	}

	public List<MangaTabela> selectTabelas(Boolean todos, String base, String manga) throws ExcessaoBd {
		return vocabularioDao.selectTabelas(todos, base, manga);
	}

	public List<MangaTabela> selectTabelas(Boolean todos, String base, String manga, Integer volume, Float capitulo)
			throws ExcessaoBd {
		return vocabularioDao.selectTabelas(todos, base, manga, volume, capitulo);
	}
	
	public List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo, Language linguagem, Boolean inverterTexto)
			throws ExcessaoBd {
		return vocabularioDao.selectTabelasJson(base, manga, volume, capitulo, linguagem, inverterTexto);
	}

	public List<MangaVolume> selectDadosTransferir(String baseOrigem) throws ExcessaoBd {
		return vocabularioDao.selectTransferir(baseOrigem);
	}

	public void updateCancel(String base, MangaVolume obj) throws ExcessaoBd {
		for (MangaCapitulo capitulo : obj.getCapitulos())
			for (MangaPagina pagina : capitulo.getPaginas())
				vocabularioDao.updateCancel(base, pagina);
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

	public void updateVocabularioVolume(String base, MangaVolume volume) throws ExcessaoBd {
		insertVocabulario(base, volume.getId(), null, null, volume.getVocabulario());
		vocabularioDao.updateProcessado(base, "volumes", volume.getId());
	}

	public void updateVocabularioCapitulo(String base, MangaCapitulo capitulo) throws ExcessaoBd {
		insertVocabulario(base, null, capitulo.getId(), null, capitulo.getVocabulario());
		vocabularioDao.updateProcessado(base, "capitulos", capitulo.getId());
	}

	public void updateVocabularioPagina(String base, MangaPagina pagina) throws ExcessaoBd {
		insertVocabulario(base, null, null, pagina.getId(), pagina.getVocabulario());
		vocabularioDao.updateProcessado(base, "paginas", pagina.getId());
	}

	public void insertVocabulario(String base, Long idVolume, Long idCapitulo, Long idPagina,
			Set<MangaVocabulario> vocabulario) throws ExcessaoBd {
		vocabularioDao.insertVocabulario(base, idVolume, idCapitulo, idPagina, vocabulario);
	}

	public void createDataBase(String base) throws ExcessaoBd {
		vocabularioDao.createDatabase(base);
	}

	public void createBaseVocabulario(String base) throws ExcessaoBd {
		vocabularioDao.createBaseVocabulario(base);
	}

}
