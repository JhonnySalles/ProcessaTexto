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

	private MangaDao mangaDao = DaoFactory.createMangaDao();

	public List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd {
		return mangaDao.selectTabelas(todos);
	}

	public List<MangaTabela> selectTabelas(Boolean todos, String base, String manga) throws ExcessaoBd {
		return mangaDao.selectTabelas(todos, base, manga);
	}

	public List<MangaTabela> selectTabelas(Boolean todos, String base, String manga, Integer volume, Float capitulo)
			throws ExcessaoBd {
		return mangaDao.selectTabelas(todos, base, manga, volume, capitulo);
	}

	public List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo,
			Language linguagem, Boolean inverterTexto) throws ExcessaoBd {
		return mangaDao.selectTabelasJson(base, manga, volume, capitulo, linguagem, inverterTexto);
	}

	public List<MangaVolume> selectDadosTransferir(String baseOrigem) throws ExcessaoBd {
		return mangaDao.selectTransferir(baseOrigem);
	}

	public void updateCancel(String base, MangaVolume obj) throws ExcessaoBd {
		for (MangaCapitulo capitulo : obj.getCapitulos())
			for (MangaPagina pagina : capitulo.getPaginas())
				mangaDao.updateCancel(base, pagina);
	}

	public void insertDadosTransferir(String base, MangaVolume volume) throws ExcessaoBd {
		Long idVolume = mangaDao.insertVolume(base, volume);
		for (MangaCapitulo capitulo : volume.getCapitulos()) {
			Long idCapitulo = mangaDao.insertCapitulo(base, idVolume, capitulo);
			for (MangaPagina pagina : capitulo.getPaginas()) {
				Long idPagina = mangaDao.insertPagina(base, idCapitulo, pagina);
				for (MangaTexto texto : pagina.getTextos()) {
					mangaDao.insertTexto(base, idPagina, texto);
				}
			}
		}
	}

	public void updateVocabularioVolume(String base, MangaVolume volume) throws ExcessaoBd {
		insertVocabularios(base, volume.getId(), null, null, volume.getVocabularios());
		mangaDao.updateProcessado(base, "volumes", volume.getId());
	}

	public void updateVocabularioCapitulo(String base, MangaCapitulo capitulo) throws ExcessaoBd {
		insertVocabularios(base, null, capitulo.getId(), null, capitulo.getVocabularios());
		mangaDao.updateProcessado(base, "capitulos", capitulo.getId());
	}

	public void updateVocabularioPagina(String base, MangaPagina pagina) throws ExcessaoBd {
		insertVocabularios(base, null, null, pagina.getId(), pagina.getVocabulario());
		mangaDao.updateProcessado(base, "paginas", pagina.getId());
	}

	public void insertVocabularios(String base, Long idVolume, Long idCapitulo, Long idPagina,
			Set<MangaVocabulario> vocabularios) throws ExcessaoBd {
		mangaDao.insertVocabulario(base, idVolume, idCapitulo, idPagina, vocabularios);
	}

	public void createDataBase(String base) throws ExcessaoBd {
		mangaDao.createDatabase(base);
	}

	public void createBaseVocabulario(String base) throws ExcessaoBd {
		mangaDao.createBaseVocabulario(base);
	}

}
