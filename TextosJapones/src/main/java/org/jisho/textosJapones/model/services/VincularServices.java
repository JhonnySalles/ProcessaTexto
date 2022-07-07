package org.jisho.textosJapones.model.services;

import java.util.List;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.VincularDao;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import javafx.collections.ObservableList;

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

	public void reorderDoublePages(ObservableList<VinculoPagina> vinculado,
			ObservableList<VinculoPagina> naoVinculado) {
		reorderDoublePages(vinculado, naoVinculado, false);
	}

	public void reorderDoublePages(ObservableList<VinculoPagina> vinculado, ObservableList<VinculoPagina> naoVinculado,
			Boolean isUsePaginaDuplaCalculada) {
		if (vinculado == null || vinculado.isEmpty())
			return;

		Integer padding = 1;

		for (VinculoPagina pagina : vinculado) {
			int index = vinculado.indexOf(pagina);

			if (pagina.isImagemDupla || (isUsePaginaDuplaCalculada && pagina.isVinculadoEsquerdaPaginaDupla))
				continue;

			if ((index + padding) >= vinculado.size())
				break;

			VinculoPagina proximo = vinculado.get(index + padding);
			if (proximo.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
				do {
					padding++;
					if ((index + padding) >= vinculado.size())
						break;
					proximo = vinculado.get(index + padding);
				} while (proximo.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA);
			}

			if ((index + padding) >= vinculado.size())
				break;

			if (proximo.isImagemDupla || (isUsePaginaDuplaCalculada && pagina.isVinculadoEsquerdaPaginaDupla)) {
				if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
					continue;
				else
					pagina.mesclar(proximo);
				proximo.limparVinculado();
			} else {
				if (isUsePaginaDuplaCalculada) {
					if (pagina.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
						pagina.addVinculoEsquerda(proximo);
						proximo.limparVinculadoEsquerda();

						if (!pagina.isVinculadoEsquerdaPaginaDupla) {
							if (proximo.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA) {
								pagina.addVinculoDireita(proximo);
								proximo.limparVinculadoDireita();
							} else {
								padding++;
								if ((index + padding) >= vinculado.size()) {
									padding--;
									continue;
								}

								proximo = vinculado.get(index + padding);
								if (proximo.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA
										&& !proximo.isImagemDupla && !proximo.isVinculadoEsquerdaPaginaDupla) {
									pagina.addVinculoDireitaApartirEsquerda(proximo);
									proximo.limparVinculadoEsquerda();
								} else
									padding--;
							}
						}
					} else {
						if (proximo.isVinculadoEsquerdaPaginaDupla)
							continue;
						else {
							pagina.addVinculoDireitaApartirEsquerda(proximo);
							proximo.limparVinculadoEsquerda();
						}
					}

				} else {
					if (pagina.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
						pagina.addVinculoEsquerda(proximo);
						proximo.limparVinculadoEsquerda();

						if (proximo.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA) {
							pagina.addVinculoDireita(proximo);
							proximo.limparVinculadoDireita();
						} else {
							padding++;
							if ((index + padding) >= vinculado.size()) {
								padding--;
								continue;
							}

							proximo = vinculado.get(index + padding);
							if (!proximo.isImagemDupla
									&& proximo.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA) {
								pagina.addVinculoEsquerdaApartirDireita(proximo);
								proximo.limparVinculadoEsquerda();
							} else
								padding--;
						}
					} else {
						pagina.addVinculoDireitaApartirEsquerda(proximo);
						proximo.limparVinculadoEsquerda();
					}
				}
			}
		}

		if (!naoVinculado.isEmpty()) {
			List<VinculoPagina> paginasNaoVinculado = naoVinculado.stream().sorted((VinculoPagina a,
					VinculoPagina b) -> a.getVinculadoEsquerdaPagina().compareTo(a.getVinculadoEsquerdaPagina()))
					.collect(Collectors.toList());

			for (VinculoPagina pagina : vinculado) {
				if (paginasNaoVinculado.isEmpty())
					break;

				if (pagina.isImagemDupla || (isUsePaginaDuplaCalculada && pagina.isVinculadoEsquerdaPaginaDupla))
					continue;

				if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
					pagina.addVinculoDireitaApartirEsquerda(paginasNaoVinculado.remove(0));
				else if (isUsePaginaDuplaCalculada) {
					VinculoPagina notLinked = paginasNaoVinculado.remove(0);
					pagina.addVinculoDireita(notLinked);

					if (notLinked.isVinculadoEsquerdaPaginaDupla)
						continue;

					if (paginasNaoVinculado.isEmpty())
						break;
					pagina.addVinculoDireitaApartirEsquerda(paginasNaoVinculado.remove(0));
				} else {
					pagina.addVinculoDireita(paginasNaoVinculado.remove(0));
					if (paginasNaoVinculado.isEmpty())
						break;

					pagina.addVinculoDireitaApartirEsquerda(paginasNaoVinculado.remove(0));
				}
			}

			naoVinculado.clear();
			if (!paginasNaoVinculado.isEmpty())
				naoVinculado.addAll(paginasNaoVinculado);
		}

	}

	/*
	 * private void reorderSimplePages(isNotify: Boolean = true) { if
	 * (mFileLink.value == null || mFileLink.value!!.path.isEmpty()) return
	 * 
	 * val hasDualImage = mPagesLink.value?.any { it.dualImage } ?: false
	 * 
	 * if (hasDualImage) { if (isNotify) notifyMessages(Pages.LINKED,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SIMPLE_PAGES_START)
	 * 
	 * val indexChanges = mutableSetOf<Int>() val pagesLink = mPagesLink.value!! var
	 * amount = 0 pagesLink.forEach { if (it.dualImage) amount += 1 } var
	 * amountNotLink = (amount * 2) - pagesLink.size
	 * 
	 * if (amountNotLink > 0) { for (i in (pagesLink.size - 1) downTo 0) { val item
	 * = pagesLink[i] if (item.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY)
	 * continue
	 * 
	 * val page = pagesLink[i] if (item.dualImage) { addNotLinked(page)
	 * amountNotLink -= 2 page.clearFileLink() } else if (item.fileLinkPage !=
	 * PageLinkConsts.VALUES.PAGE_EMPTY) { addNotLinked(page) amountNotLink--
	 * page.clearLeftFileLink() }
	 * 
	 * if (amountNotLink < 1) break }
	 * 
	 * mPagesNotLinked.value!!.sortBy { it.fileLinkPage }
	 * 
	 * if (isNotify) notifyMessages(Pages.NOT_LINKED,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE) }
	 * 
	 * var padding = 0 for (i in (pagesLink.size - 1) downTo 0) { if
	 * (pagesLink[i].fileLinkPage != PageLinkConsts.VALUES.PAGE_EMPTY) { padding = i
	 * +1 break } }
	 * 
	 * val pagesLinkTemp = mutableListOf<PageLink>()
	 * 
	 * for (i in pagesLink.size - 1 downTo 0) { val newPage = PageLink(pagesLink[i])
	 * pagesLinkTemp.add(newPage)
	 * 
	 * if (i - padding >= 0) { val page = pagesLink[i - padding]
	 * 
	 * if (page.dualImage) { newPage.addLeftFromRightFileLinkImage(page)
	 * page.clearRightFileLink() padding-- } else newPage.addLeftFileLinkImage(page)
	 * }
	 * 
	 * indexChanges.addAll(arrayOf(i, i - padding)) }
	 * 
	 * pagesLinkTemp.sortBy { it.mangaPage } mPagesLink.value =
	 * ArrayList(pagesLinkTemp)
	 * 
	 * if (isNotify) { for (index in indexChanges) notifyMessages(Pages.LINKED,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, index)
	 * 
	 * notifyMessages(Pages.LINKED,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SIMPLE_PAGES_FINISHED) } }
	 * }
	 * 
	 * private void autoReorderDoublePages(type: Pages, isClear: Boolean = false,
	 * isNotify: Boolean = true) { if (mFileLink.value == null ||
	 * mFileLink.value!!.path.isEmpty()) return
	 * 
	 * if (isNotify) notifyMessages(type,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_AUTO_PAGES_START)
	 * 
	 * val hasDualImage = if (isClear) { reorderSimplePages(false) false } else
	 * mPagesLink.value?.any { it.dualImage } ?: false
	 * 
	 * if (!hasDualImage && (mFileLink.value?.id == null || isClear)) { val
	 * indexChanges = mutableSetOf<Int>() val pagesLink = mPagesLink.value!! val
	 * lastIndex = pagesLink.size - 1 for ((index, page) in pagesLink.withIndex()) {
	 * if (page.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY || index >=
	 * lastIndex) continue
	 * 
	 * if (page.isMangaDualPage && page.isFileLeftDualPage) continue
	 * 
	 * if (page.isMangaDualPage) { val nextPage = pagesLink[index + 1]
	 * 
	 * if (nextPage.fileLinkPage == PageLinkConsts.VALUES.PAGE_EMPTY ||
	 * nextPage.isFileLeftDualPage) continue
	 * 
	 * indexChanges.addAll(arrayOf(index, index + 1))
	 * page.addRightFromLeftFileLinkImage(nextPage) nextPage.clearFileLink()
	 * 
	 * for ((idxNext, next) in pagesLink.withIndex()) { if (idxNext < (index + 1) ||
	 * idxNext >= lastIndex) continue
	 * 
	 * val aux = pagesLink[idxNext + 1] next.addLeftFileLinkImage(aux)
	 * aux.clearLeftFileLink() indexChanges.addAll(arrayOf(idxNext, idxNext + 1)) }
	 * } else if (page.isFileLeftDualPage) { if (pagesLink[index + 1].fileLinkPage
	 * == PageLinkConsts.VALUES.PAGE_EMPTY) continue
	 * 
	 * indexChanges.addAll(arrayOf(index, index + 1)) var indexEmpty = lastIndex for
	 * (i in (index + 1) until lastIndex) { if (pagesLink[i].fileLinkPage ==
	 * PageLinkConsts.VALUES.PAGE_EMPTY) { indexEmpty = i break } }
	 * 
	 * addNotLinked(pagesLink[indexEmpty])
	 * 
	 * for (i in indexEmpty downTo (index + 2)) { val aux = pagesLink[i - 1]
	 * pagesLink[i].addLeftFileLinkImage(aux) aux.clearLeftFileLink()
	 * indexChanges.addAll(arrayOf(i, i - 1)) } } }
	 * 
	 * if (isNotify) for (index in indexChanges) notifyMessages(type,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_ITEM_CHANGE, index) }
	 * 
	 * if (isNotify) notifyMessages(type,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_AUTO_PAGES_FINISHED) }
	 * 
	 * private void reorderBySortPages() { if (mFileLink.value == null ||
	 * mFileLink.value!!.path.isEmpty()) return
	 * 
	 * notifyMessages(Pages.LINKED,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SORTED_PAGES_START)
	 * 
	 * val pagesNotLink = mPagesNotLinked.value!! val pagesLink = mPagesLink.value!!
	 * val pagesLinkTemp = mutableListOf<PageLink>() val pagesNotLinkTemp =
	 * arrayListOf<PageLink>() var maxNumPage = 0
	 * 
	 * for (page in pagesLink) { if (page.fileLinkPage > maxNumPage) maxNumPage =
	 * page.fileLinkPage
	 * 
	 * if (page.fileRightLinkPage > maxNumPage) maxNumPage = page.fileLinkPage }
	 * 
	 * for (page in pagesNotLink) { if (page.fileLinkPage > maxNumPage) maxNumPage =
	 * page.fileLinkPage }
	 * 
	 * for (page in pagesLink) { if (page.mangaPage ==
	 * PageLinkConsts.VALUES.PAGE_EMPTY) continue
	 * 
	 * if (page.mangaPage > maxNumPage) break else { val pageLink = PageLink(page)
	 * pagesLinkTemp.add(pageLink)
	 * 
	 * val findPageLink = pagesLink.find { it.fileLinkPage == page.mangaPage ||
	 * it.fileRightLinkPage == page.mangaPage } ?: pagesNotLink.find {
	 * it.fileLinkPage == page.mangaPage }
	 * 
	 * if (findPageLink != null) { if (pageLink.fileRightLinkPage == page.mangaPage)
	 * pageLink.addLeftFromRightFileLinkImage(findPageLink) else
	 * pageLink.addLeftFileLinkImage(findPageLink) } } }
	 * 
	 * if (maxNumPage >= pagesLinkTemp.size) { for (numPage in pagesLinkTemp.size
	 * until maxNumPage) { val pageLink = pagesLink.find { it.fileLinkPage ==
	 * numPage || it.fileRightLinkPage == numPage } ?: pagesNotLink.find {
	 * it.fileLinkPage == numPage }
	 * 
	 * if (pageLink != null) { if (pageLink.fileLinkPage == numPage)
	 * pagesNotLinkTemp.add( PageLink( pageLink.idFile, true, pageLink.fileLinkPage,
	 * pageLink.fileLinkPages, pageLink.fileLinkPageName, pageLink.fileLinkPagePath,
	 * pageLink.isFileLeftDualPage, pageLink.imageLeftFileLinkPage ) ) else
	 * pagesNotLinkTemp.add( PageLink( pageLink.idFile, true,
	 * pageLink.fileRightLinkPage, pageLink.fileLinkPages,
	 * pageLink.fileRightLinkPageName, pageLink.fileRightLinkPagePath,
	 * pageLink.isFileRightDualPage, pageLink.imageRightFileLinkPage ) ) } } }
	 * 
	 * pagesLink.clear() pagesNotLink.clear() pagesLinkTemp.sortBy { it.mangaPage }
	 * pagesNotLinkTemp.sortBy { it.fileLinkPage }
	 * 
	 * mPagesLink.value = ArrayList(pagesLinkTemp) mPagesNotLinked.value =
	 * pagesNotLinkTemp
	 * 
	 * notifyMessages(Pages.LINKED,
	 * PageLinkConsts.MESSAGES.MESSAGE_PAGES_LINK_REORDER_SORTED_PAGES_FINISHED) }
	 */

}
