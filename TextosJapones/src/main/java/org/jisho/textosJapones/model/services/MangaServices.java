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

import javafx.collections.ObservableList;

public class MangaServices {

	private MangaDao mangaDao = DaoFactory.createMangaDao();

	public List<MangaTabela> selectTabelas(Boolean todos) throws ExcessaoBd {
		return mangaDao.selectTabelas(todos);
	}

	public List<MangaTabela> selectAll(String base, String manga, Integer volume, Float capitulo, Language linguagem)
			throws ExcessaoBd {
		return mangaDao.selectAll(base, manga, volume, capitulo, linguagem);
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
		Long idVolume = mangaDao.insertVolume(base, volume, true);
		for (MangaCapitulo capitulo : volume.getCapitulos()) {
			Long idCapitulo = mangaDao.insertCapitulo(base, idVolume, capitulo, true);
			for (MangaPagina pagina : capitulo.getPaginas()) {
				Long idPagina = mangaDao.insertPagina(base, idCapitulo, pagina, true);
				for (MangaTexto texto : pagina.getTextos())
					mangaDao.insertTexto(base, idPagina, texto, true);
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

	private Boolean limpeza = true;
	public void salvarAjustes(ObservableList<MangaTabela> tabelas) throws ExcessaoBd {
		limpeza = true;
		for (MangaTabela tabela : tabelas)
			for (MangaVolume volume : tabela.getVolumes()) {
				if (limpeza && volume.getLingua().compareTo(Language.JAPANESE) == 0) {
					limpeza = false;
					mangaDao.deletarVocabulario(tabela.getBase());
				}
				
				if (volume.getId() == null || volume.getId().compareTo(0L) == 0)
					volume.setId(mangaDao.insertVolume(tabela.getBase(), volume, false));
				else if (volume.isAlterado()) {
					if (volume.isItemExcluido()) {
						MangaVolume aux = mangaDao.selectVolume(tabela.getBase(), volume.getId());
						if (aux != null) {
							aux.getCapitulos().forEach(anterior -> {
								Boolean existe = false;
								for (MangaCapitulo atual : volume.getCapitulos())
									if (atual.getId().compareTo(anterior.getId()) == 0) {
										existe = true;
										break;
									}

								if (!existe)
									try {
										mangaDao.deleteCapitulo(tabela.getBase(), anterior);
									} catch (ExcessaoBd e) {
										e.printStackTrace();
									}
							});
						}
					}
					mangaDao.updateVolume(tabela.getBase(), volume);
				}

				if (volume.getCapitulos().isEmpty())
					mangaDao.deleteVolume(tabela.getBase(), volume);
				else
					for (MangaCapitulo capitulo : volume.getCapitulos())
						if (capitulo.isAlterado()) {
							if (capitulo.isItemExcluido()) {
								MangaCapitulo aux = mangaDao.selectCapitulo(tabela.getBase(), capitulo.getId());
								if (aux != null) {
									aux.getPaginas().forEach(anterior -> {
										Boolean existe = false;
										for (MangaPagina atual : capitulo.getPaginas())
											if (atual.getId().compareTo(anterior.getId()) == 0) {
												existe = true;
												break;
											}

										if (!existe)
											try {
												mangaDao.deletePagina(tabela.getBase(), anterior);
											} catch (ExcessaoBd e) {
												e.printStackTrace();
											}
									});
								}
							}
							
							for (MangaPagina pagina : capitulo.getPaginas())
								if (pagina.isItemExcluido()) {
									MangaPagina aux = mangaDao.selectPagina(tabela.getBase(), pagina.getId());
									if (aux != null) {
										aux.getTextos().forEach(anterior -> {
											Boolean existe = false;
											for (MangaTexto atual : pagina.getTextos())
												if (atual.getId().compareTo(anterior.getId()) == 0) {
													existe = true;
													break;
												}

											if (!existe)
												try {
													mangaDao.deleteTexto(tabela.getBase(), anterior);
												} catch (ExcessaoBd e) {
													e.printStackTrace();
												}
										});
									}
								}

							mangaDao.updateCapitulo(tabela.getBase(), volume.getId(), capitulo);
						}
			}
	}

	public void salvarTraducao(String base, MangaVolume volume) throws ExcessaoBd {
		mangaDao.deleteVolume(base, volume);
		Long idVolume = mangaDao.insertVolume(base, volume, false);
		for (MangaCapitulo capitulo : volume.getCapitulos()) {
			Long idCapitulo = mangaDao.insertCapitulo(base, idVolume, capitulo, false);
			for (MangaPagina pagina : capitulo.getPaginas()) {
				Long idPagina = mangaDao.insertPagina(base, idCapitulo, pagina, false);
				for (MangaTexto texto : pagina.getTextos())
					mangaDao.insertTexto(base, idPagina, texto, false);
			}
		}
	}

}
