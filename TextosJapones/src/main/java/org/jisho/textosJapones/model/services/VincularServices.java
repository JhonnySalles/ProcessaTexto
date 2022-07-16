package org.jisho.textosJapones.model.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jisho.textosJapones.components.listener.VinculoServiceListener;
import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.MangaDao;
import org.jisho.textosJapones.database.dao.VincularDao;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.MangaVolume;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Pagina;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.util.Util;

import javafx.collections.ObservableList;
import javafx.util.Pair;

public class VincularServices {

	private VincularDao dao = DaoFactory.createVincularDao();
	private MangaDao mangaDao = DaoFactory.createMangaDao();

	public void salvar(String base, Vinculo obj) throws ExcessaoBd {
		if (base == null || base.isEmpty())
			return;

		if (obj.getId() == null)
			insert(base, obj);
		else
			update(base, obj);
	}

	public void update(String base, Vinculo obj) throws ExcessaoBd {
		if (base == null || base.isEmpty())
			return;

		dao.update(base, obj);
	}

	public MangaVolume selectVolume(String base, String manga, Integer volume, Language linguagem) {
		if (base == null || base.isEmpty())
			return null;

		try {
			return mangaDao.selectVolume(base, manga, volume, linguagem);
		} catch (ExcessaoBd e) {
			e.printStackTrace();
			return null;
		}
	}

	public Vinculo select(String base, Integer volume, String mangaOriginal, Language linguagemOriginal,
			String arquivoOriginal, String mangaVinculado, Language linguagemVinculado, String arquivoVinculado)
			throws ExcessaoBd {
		if (base == null || base.isEmpty())
			return null;

		if (linguagemOriginal != null && linguagemVinculado != null
				&& (mangaOriginal == null || mangaOriginal.isBlank())
				&& (arquivoOriginal == null || mangaOriginal.isBlank()))
			return select(base, volume, mangaOriginal, linguagemOriginal, mangaVinculado, linguagemVinculado);
		else if (linguagemOriginal == null && linguagemVinculado == null
				&& (mangaOriginal != null && !mangaOriginal.isBlank())
				&& (arquivoOriginal != null && !mangaOriginal.isBlank()))
			return select(base, volume, mangaOriginal, arquivoOriginal, mangaVinculado, arquivoVinculado);
		else
			return dao.select(base, volume, mangaOriginal, linguagemOriginal, arquivoOriginal, mangaVinculado,
					linguagemVinculado, arquivoVinculado);
	}

	public Vinculo select(String base, Integer volume, String mangaOriginal, String original, String mangaVinculado,
			String vinculado) throws ExcessaoBd {
		if (base == null || base.isEmpty())
			return null;

		return dao.select(base, volume, mangaOriginal, original, mangaVinculado, vinculado);
	}

	public Vinculo select(String base, Integer volume, String mangaOriginal, Language linguagemOriginal,
			String mangaVinculado, Language linguagemVinculado) throws ExcessaoBd {
		if (base == null || base.isEmpty())
			return null;

		return dao.select(base, volume, mangaOriginal, linguagemOriginal, mangaVinculado, linguagemVinculado);
	}

	public void delete(String base, Vinculo obj) throws ExcessaoBd {
		if (base == null || base.isEmpty())
			return;

		dao.delete(base, obj);
	}

	public Long insert(String base, Vinculo obj) throws ExcessaoBd {
		if (base == null || base.isEmpty())
			return null;

		return dao.insert(base, obj);
	}

	public Boolean createTabelas(String base) throws ExcessaoBd {
		if (base == null || base.isEmpty())
			return false;
		return dao.createTabelas(base);
	}

	public List<String> getTabelas() throws ExcessaoBd {
		return dao.getTabelas();
	}

	public List<String> getMangas(String base, Language linguagem) throws ExcessaoBd {
		if (base == null || linguagem == null)
			return new ArrayList<String>();
		return dao.getMangas(base, linguagem);
	}

	// ------------------------------------------------------------------------
	private VinculoServiceListener listener;

	public void setListener(VinculoServiceListener listener) {
		this.listener = listener;
	}

	public void addNaoVinculado(VinculoPagina pagina) {
		ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

		if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
			naoVinculado.add(
					new VinculoPagina(pagina.getVinculadoEsquerdaNomePagina(), pagina.getVinculadoEsquerdaPathPagina(),
							pagina.getVinculadoEsquerdaPagina(), pagina.getVinculadoEsquerdaPaginas(),
							pagina.isVinculadoEsquerdaPaginaDupla, pagina.getMangaPaginaEsquerda(),
							pagina.getImagemVinculadoEsquerda(), true, pagina.getVinculadoEsquerdaHash()));

		if (pagina.isImagemDupla) {
			naoVinculado.add(
					new VinculoPagina(pagina.getVinculadoDireitaNomePagina(), pagina.getVinculadoDireitaPathPagina(),
							pagina.getVinculadoDireitaPagina(), pagina.getVinculadoDireitaPaginas(),
							pagina.isVinculadoDireitaPaginaDupla, pagina.getMangaPaginaDireita(),
							pagina.getImagemVinculadoDireita(), true, pagina.getVinculadoDireitaHash()));
			pagina.limparVinculadoDireita();
		}
	}

	public void ordenarPaginaDupla(Boolean isUsePaginaDuplaCalculada) {
		ObservableList<VinculoPagina> vinculado = listener.getVinculados();

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

		ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

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

	private Integer qtde = 0;

	public void ordenarPaginaSimples() {
		ObservableList<VinculoPagina> vinculado = listener.getVinculados();

		if (vinculado == null || vinculado.isEmpty())
			return;

		Boolean existeImagem = vinculado.stream().anyMatch((it) -> it.isImagemDupla);

		if (existeImagem) {
			qtde = 0;
			vinculado.forEach((it) -> {
				if (it.isImagemDupla)
					qtde++;
			});

			for (var i = vinculado.size() - 1; i >= (vinculado.size() - 1 - qtde); i--)
				addNaoVinculado(vinculado.get(i));

			Integer processado = qtde;
			for (var i = vinculado.size() - 1; i >= 0; i--) {
				VinculoPagina pagina = vinculado.get(i - processado);
				if (pagina.isImagemDupla) {
					vinculado.get(i).addVinculoEsquerdaApartirDireita(pagina);
					pagina.limparVinculadoDireita();
					processado--;
				} else
					vinculado.get(i).addVinculoEsquerda(pagina);

				if (processado <= 0)
					break;
			}
		}
	}

	public void autoReordenarPaginaDupla() {
		autoReordenarPaginaDupla(false);
	}

	public void autoReordenarPaginaDupla(Boolean isLimpar) {
		ObservableList<VinculoPagina> vinculado = listener.getVinculados();

		if (vinculado == null || vinculado.isEmpty())
			return;

		Boolean temImagemDupla = false;
		if (isLimpar)
			ordenarPaginaSimples();
		else
			temImagemDupla = vinculado.stream().anyMatch((it) -> it.isImagemDupla);

		if (!temImagemDupla && isLimpar) {
			Integer lastIndex = vinculado.size() - 1;

			for (VinculoPagina pagina : vinculado) {
				int index = vinculado.indexOf(pagina);

				if (pagina.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA || index >= lastIndex)
					continue;

				if (pagina.isOriginalPaginaDupla && pagina.isVinculadoEsquerdaPaginaDupla)
					continue;

				if (pagina.isOriginalPaginaDupla) {
					VinculoPagina proximo = vinculado.get(index + 1);

					if (proximo.getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA
							|| proximo.isVinculadoEsquerdaPaginaDupla)
						continue;

					pagina.addVinculoDireitaApartirEsquerda(proximo);
					proximo.limparVinculado();

					for (var idxNext = (index + 1); idxNext < vinculado.size(); idxNext++) {
						if (idxNext < (index + 1) || idxNext >= lastIndex)
							continue;

						VinculoPagina aux = vinculado.get(idxNext + 1);
						proximo.addVinculoEsquerda(aux);
						aux.limparVinculadoEsquerda();
					}
				} else if (pagina.isVinculadoEsquerdaPaginaDupla) {
					if (vinculado.get(index + 1).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA)
						continue;

					Integer indexEmpty = lastIndex;
					for (var i = (index + 1); i <= lastIndex; i++) {
						if (vinculado.get(i).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
							indexEmpty = i;
							break;
						}
					}

					addNaoVinculado(vinculado.get(indexEmpty));
					for (var i = indexEmpty; i >= (index + 2); i--) {
						VinculoPagina aux = vinculado.get(i - 1);
						vinculado.get(i).addVinculoEsquerda(aux);
						aux.limparVinculadoEsquerda();
					}
				}
			}

		}
	}

	private Integer numeroPagina;

	public void reordenarPeloNumeroPagina() {
		ObservableList<VinculoPagina> vinculado = listener.getVinculados();

		if (vinculado == null || vinculado.isEmpty())
			return;

		ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

		List<VinculoPagina> vinculadoTemp = new ArrayList<VinculoPagina>();
		List<VinculoPagina> naoVinculadoTemp = new ArrayList<VinculoPagina>();
		Integer maxNumPag = 0;

		for (VinculoPagina pagina : vinculado) {
			if (pagina.getVinculadoEsquerdaPagina() > maxNumPag)
				maxNumPag = pagina.getVinculadoEsquerdaPagina();

			if (pagina.getVinculadoDireitaPagina() > maxNumPag)
				maxNumPag = pagina.getVinculadoDireitaPagina();
		}

		for (VinculoPagina pagina : naoVinculado) {
			if (pagina.getVinculadoEsquerdaPagina() > maxNumPag)
				maxNumPag = pagina.getVinculadoEsquerdaPagina();
		}

		for (VinculoPagina pagina : vinculado) {
			if (pagina.getOriginalPagina() == VinculoPagina.PAGINA_VAZIA)
				continue;

			if (pagina.getOriginalPagina() > maxNumPag)
				break;
			else {
				VinculoPagina novo = new VinculoPagina(pagina);
				vinculadoTemp.add(novo);

				Optional<VinculoPagina> encontrado = vinculado.stream()
						.filter((it) -> it.getVinculadoEsquerdaPagina().compareTo(pagina.getOriginalPagina()) == 0
								|| it.getVinculadoDireitaPagina().compareTo(pagina.getOriginalPagina()) == 0)
						.findFirst();

				if (encontrado.isEmpty())
					encontrado = naoVinculado.stream()
							.filter((it) -> it.getVinculadoEsquerdaPagina().compareTo(pagina.getOriginalPagina()) == 0)
							.findFirst();

				if (encontrado.isPresent()) {
					if (encontrado.get().getVinculadoDireitaPagina() == pagina.getOriginalPagina())
						novo.addVinculoEsquerdaApartirDireita(encontrado.get());
					else
						novo.addVinculoEsquerda(encontrado.get());
				}
			}
		}

		if (maxNumPag >= vinculadoTemp.size()) {
			for (numeroPagina = naoVinculado.size() - 1; numeroPagina <= maxNumPag; numeroPagina++) {
				Optional<VinculoPagina> encontrado = vinculado.stream()
						.filter((it) -> it.getVinculadoEsquerdaPagina().compareTo(numeroPagina) == 0
								|| it.getVinculadoDireitaPagina().compareTo(numeroPagina) == 0)
						.findFirst();

				if (encontrado.isEmpty())
					encontrado = naoVinculado.stream()
							.filter((it) -> it.getVinculadoEsquerdaPagina().compareTo(numeroPagina) == 0).findFirst();

				if (encontrado.isPresent()) {
					VinculoPagina item = encontrado.get();
					if (item.getVinculadoEsquerdaPagina() == numeroPagina)
						naoVinculadoTemp.add(new VinculoPagina(item.getVinculadoEsquerdaNomePagina(),
								item.getVinculadoEsquerdaPathPagina(), item.getVinculadoEsquerdaPagina(),
								item.getVinculadoEsquerdaPaginas(), item.isVinculadoEsquerdaPaginaDupla,
								item.getMangaPaginaEsquerda(), item.getImagemVinculadoEsquerda(), true,
								item.getVinculadoEsquerdaHash()));
					else
						naoVinculadoTemp.add(new VinculoPagina(item.getVinculadoDireitaNomePagina(),
								item.getVinculadoDireitaPathPagina(), item.getVinculadoDireitaPagina(),
								item.getVinculadoDireitaPaginas(), item.isVinculadoDireitaPaginaDupla,
								item.getMangaPaginaDireita(), item.getImagemVinculadoDireita(), true,
								item.getVinculadoDireitaHash()));
				}
			}
		}

		vinculado.clear();
		naoVinculado.clear();
		vinculadoTemp
				.sort((VinculoPagina a, VinculoPagina b) -> a.getOriginalPagina().compareTo(b.getOriginalPagina()));
		naoVinculadoTemp.sort((VinculoPagina a, VinculoPagina b) -> a.getVinculadoEsquerdaPagina()
				.compareTo(b.getVinculadoEsquerdaPagina()));

		vinculado.addAll(vinculadoTemp);
		naoVinculadoTemp.addAll(naoVinculadoTemp);

	}

	// ------------------------------------------------------------------------

	public void addNaoVInculado(VinculoPagina pagina, Pagina origem) {
		ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

		if (origem == Pagina.VINCULADO_DIREITA) {
			if (pagina.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
				naoVinculado.add(new VinculoPagina(pagina.getVinculadoDireitaNomePagina(),
						pagina.getVinculadoDireitaPathPagina(), pagina.getVinculadoDireitaPagina(),
						pagina.getVinculadoDireitaPaginas(), pagina.isVinculadoDireitaPaginaDupla,
						pagina.getMangaPaginaDireita(), pagina.getImagemVinculadoDireita(), true,
						pagina.getVinculadoDireitaHash()));

			pagina.limparVinculadoDireita();
		} else {
			if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
				naoVinculado.add(new VinculoPagina(pagina.getVinculadoEsquerdaNomePagina(),
						pagina.getVinculadoEsquerdaPathPagina(), pagina.getVinculadoEsquerdaPagina(),
						pagina.getVinculadoEsquerdaPaginas(), pagina.isVinculadoEsquerdaPaginaDupla,
						pagina.getMangaPaginaEsquerda(), pagina.getImagemVinculadoEsquerda(), true,
						pagina.getVinculadoEsquerdaHash()));

			if (pagina.isImagemDupla)
				pagina.moverDireitaParaEsquerda();
			else
				pagina.limparVinculadoEsquerda();
		}
	}

	public void fromNaoVinculado(VinculoPagina origem, VinculoPagina destino, Pagina local) {
		if (origem == null || destino == null)
			return;

		ObservableList<VinculoPagina> vinculado = listener.getVinculados();
		ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

		Integer destinoIndex = vinculado.indexOf(destino);
		Integer limite = vinculado.size() - 1;

		naoVinculado.remove(origem);

		if (destino.getImagemVinculadoEsquerda() == null)
			vinculado.get(destinoIndex).addVinculoEsquerda(origem);
		else {
			addNaoVinculado(vinculado.get(limite));

			for (var i = limite; i >= destinoIndex; i--) {
				if (i == destinoIndex)
					vinculado.get(i).addVinculoEsquerda(origem);
				else
					vinculado.get(i).addVinculoEsquerda(vinculado.get(i - 1));
			}
		}

	}

	public void onMovimentaEsquerda(VinculoPagina origem, VinculoPagina destino) {
		if (origem == null || destino == null || origem.equals(destino))
			return;

		ObservableList<VinculoPagina> vinculado = listener.getVinculados();

		Integer origemnIndex = vinculado.indexOf(origem);
		Integer destinoIndex = vinculado.indexOf(destino);
		Integer diferenca = destinoIndex - origemnIndex;

		if (origemnIndex > destinoIndex) {
			Integer limite = vinculado.size() - 1;

			Integer index = vinculado.indexOf(
					vinculado.stream().filter(it -> it.getImagemVinculadoEsquerda() != null).findFirst().get());
			if (index < 0)
				index = limite;

			for (var i = index; i >= origemnIndex; i--)
				if (vinculado.get(i).getVinculadoDireitaPagina() == VinculoPagina.PAGINA_VAZIA)
					limite = i;

			for (var i = destinoIndex; i >= origemnIndex; i--)
				addNaoVinculado(vinculado.get(i));

			diferenca *= -1;

			for (var i = destinoIndex; i >= limite; i--) {
				if (i == destinoIndex)
					vinculado.get(i).addVinculoEsquerda(destino);
				else if ((i + diferenca) > (limite))
					continue;
				else {
					vinculado.get(i).addVinculoEsquerda(vinculado.get(i + diferenca));
					vinculado.get(i + diferenca).limparVinculadoDireita();
				}

			}

			for (var i = destinoIndex; i >= limite; i--)
				if (vinculado.get(i).isImagemDupla
						&& vinculado.get(0).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA
						&& vinculado.get(0).getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
					vinculado.get(0).moverDireitaParaEsquerda();

		} else {
			Integer limite = vinculado.size() - 1;
			Integer espacos = 0;

			for (var i = origemnIndex; i >= limite; i--)
				if (vinculado.get(0).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA)
					espacos++;

			if (diferenca > espacos) {
				for (var i = limite; i >= (limite - diferenca); i--)
					addNaoVinculado(vinculado.get(i));

				for (var i = limite; i >= origemnIndex; i--) {
					if (i < destinoIndex)
						vinculado.get(i).limparVinculadoEsquerda();
					else
						vinculado.get(i).addVinculoEsquerda(vinculado.get(i - diferenca));
				}
			} else {
				espacos = 0;

				for (var i = origemnIndex; i >= limite; i--) {
					if (vinculado.get(0).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
						espacos++;

						if (espacos >= diferenca) {
							limite = i;
							break;
						}
					}
				}

				espacos = 0;
				Integer index;

				for (var i = limite; i >= origemnIndex; i--) {
					if (i < destinoIndex)
						vinculado.get(0).limparVinculadoEsquerda(true);
					else {
						index = i - (1 + espacos);
						if (vinculado.get(index).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
							do {
								espacos++;
								index = i - (1 + espacos);
							} while (vinculado.get(index).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA);
						}

						vinculado.get(i).addVinculoEsquerda(vinculado.get(index));
					}

				}

			}

		}

	}

	public void onMovimentaDireita(Pagina origem, VinculoPagina itemOrigem, Pagina destino, VinculoPagina itemDestino) {
		if (origem == null || destino == null)
			return;

		ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

		if (origem != Pagina.VINCULADO_DIREITA && itemDestino.isImagemDupla)
			naoVinculado.add(new VinculoPagina(itemDestino.getVinculadoDireitaNomePagina(),
					itemDestino.getVinculadoDireitaPathPagina(), itemDestino.getVinculadoDireitaPagina(),
					itemDestino.getVinculadoDireitaPaginas(), itemDestino.isVinculadoDireitaPaginaDupla,
					itemDestino.getMangaPaginaDireita(), itemDestino.getImagemVinculadoDireita(), true,
					itemDestino.getVinculadoDireitaHash()));
		else if (origem == Pagina.VINCULADO_DIREITA
				&& itemDestino.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
			naoVinculado.add(new VinculoPagina(itemDestino.getVinculadoEsquerdaNomePagina(),
					itemDestino.getVinculadoEsquerdaPathPagina(), itemDestino.getVinculadoEsquerdaPagina(),
					itemDestino.getVinculadoEsquerdaPaginas(), itemDestino.isVinculadoEsquerdaPaginaDupla,
					itemDestino.getMangaPaginaEsquerda(), itemDestino.getImagemVinculadoEsquerda(), true,
					itemDestino.getVinculadoEsquerdaHash()));

		if (origem == Pagina.VINCULADO_DIREITA && destino == Pagina.VINCULADO_DIREITA) {
			itemDestino.addVinculoDireita(itemOrigem);
			itemOrigem.limparVinculadoDireita();
		} else if (origem == Pagina.NAO_VINCULADO || destino == Pagina.NAO_VINCULADO) {
			if (origem == Pagina.NAO_VINCULADO && destino == Pagina.NAO_VINCULADO)
				return;
			else if (origem == Pagina.NAO_VINCULADO) {
				itemDestino.addVinculoDireita(itemOrigem);
				naoVinculado.remove(itemOrigem);
			} else if (destino == Pagina.NAO_VINCULADO)
				itemOrigem.limparVinculadoDireita();

		} else {

			ObservableList<VinculoPagina> vinculado = listener.getVinculados();

			Integer origemnIndex = vinculado.indexOf(itemOrigem);
			Integer destinoIndex = vinculado.indexOf(itemDestino);

			if (origem != Pagina.VINCULADO_DIREITA && destino == Pagina.VINCULADO_ESQUERDA)
				itemDestino.addVinculoEsquerda(itemOrigem);
			else if (origem != Pagina.VINCULADO_DIREITA && destino != Pagina.VINCULADO_ESQUERDA)
				itemDestino.addVinculoDireitaApartirEsquerda(itemOrigem);
			else if (origem == Pagina.VINCULADO_DIREITA && destino == Pagina.VINCULADO_ESQUERDA)
				itemDestino.addVinculoEsquerda(itemOrigem);
			else if (origem == Pagina.VINCULADO_DIREITA && destino != Pagina.VINCULADO_ESQUERDA)
				itemDestino.addVinculoDireitaApartirEsquerda(itemOrigem);

			Boolean movido = false;
			switch (origem) {
			case VINCULADO_ESQUERDA:
				movido = itemOrigem.limparVinculadoEsquerda(true);
				break;
			case VINCULADO_DIREITA:
				itemOrigem.limparVinculadoDireita();
				movido = false;
				break;
			default:
				movido = false;
			}

			if (origemnIndex > destinoIndex && origem != Pagina.VINCULADO_DIREITA && !movido)
				origemnIndex++;
			destinoIndex++;

			if (origemnIndex >= vinculado.size() || destinoIndex >= vinculado.size())
				return;

			VinculoPagina proximoOrigem = vinculado.get(origemnIndex);
			VinculoPagina proximoDestino = vinculado.get(destinoIndex);

			if (proximoDestino.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
				onMovimentaEsquerda(proximoOrigem, proximoDestino);

		}

	}

	// -------------------------------------------------------------------------------------------------
	public VinculoPagina findPagina(ObservableList<VinculoPagina> vinculado, ObservableList<VinculoPagina> naoVinculado,
			Integer numeroPagina) {
		if (numeroPagina == VinculoPagina.PAGINA_VAZIA)
			return null;

		Optional<VinculoPagina> pagina = vinculado.stream()
				.filter(it -> it.getVinculadoEsquerdaPagina().compareTo(numeroPagina) == 0
						|| it.getVinculadoDireitaPagina().compareTo(numeroPagina) == 0)
				.findFirst();

		if (pagina.isPresent())
			return pagina.get();
		else {
			pagina = naoVinculado.stream().filter(it -> it.getVinculadoEsquerdaPagina().compareTo(numeroPagina) == 0)
					.findFirst();

			return pagina.get();
		}
	}

	public MangaPagina findPagina(List<MangaPagina> paginas, List<MangaPagina> encontrados, String path,
			String nomePagina, Integer numeroPagina, String hash) {
		MangaPagina manga = null;

		if (paginas.isEmpty())
			return manga;

		Pair<Float, Boolean> capitulo = Util.getCapitulo(path);

		if (capitulo != null) {
			Optional<MangaPagina> encontrado = paginas.stream().filter(pg -> !encontrados.contains(pg))
					.filter(pg -> pg.getCapitulo() != null && pg.getCapitulo().compareTo(capitulo.getKey()) == 0)
					.filter(pg -> pg.getHash() != null && pg.getHash().equalsIgnoreCase(hash)).findFirst();

			if (encontrado.isPresent() && !encontrado.get().getHash().isEmpty())
				manga = encontrado.get();

			if (manga == null) {
				encontrado = paginas.stream().filter(pg -> !encontrados.contains(pg))
						.filter(pg -> pg.getCapitulo() != null && pg.getCapitulo().compareTo(capitulo.getKey()) == 0)
						.filter(pg -> pg.getNomePagina() != null && pg.getNomePagina().equalsIgnoreCase(nomePagina))
						.findFirst();

				if (encontrado.isPresent())
					manga = encontrado.get();
			}

			if (manga == null) {
				encontrado = paginas.stream().filter(pg -> !encontrados.contains(pg))
						.filter(pg -> pg.getCapitulo() != null && pg.getCapitulo().compareTo(capitulo.getKey()) == 0)
						.filter(pg -> pg.getNumero() != null && pg.getNumero().compareTo(numeroPagina) == 0)
						.findFirst();

				if (encontrado.isPresent())
					manga = encontrado.get();
			}
		} else {
			Optional<MangaPagina> encontrado = paginas.stream().filter(pg -> !encontrados.contains(pg))
					.filter(pg -> pg.getHash() != null && pg.getHash().equalsIgnoreCase(hash)).findFirst();

			if (encontrado.isPresent() && !encontrado.get().getHash().isEmpty())
				manga = encontrado.get();

			if (manga == null) {
				encontrado = paginas.stream().filter(pg -> !encontrados.contains(pg))
						.filter(pg -> pg.getNomePagina() != null && pg.getNomePagina().equalsIgnoreCase(nomePagina))
						.findFirst();

				if (encontrado.isPresent())
					manga = encontrado.get();
			}

			if (manga == null) {
				encontrado = paginas.stream().filter(pg -> !encontrados.contains(pg))
						.filter(pg -> pg.getNumero() != null && pg.getNumero().compareTo(numeroPagina) == 0)
						.findFirst();

				if (encontrado.isPresent())
					manga = encontrado.get();
			}
		}

		if (manga != null)
			encontrados.add(manga);

		return manga;
	}

}
