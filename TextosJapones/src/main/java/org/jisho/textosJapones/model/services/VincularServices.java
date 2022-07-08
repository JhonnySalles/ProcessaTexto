package org.jisho.textosJapones.model.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jisho.textosJapones.model.dao.DaoFactory;
import org.jisho.textosJapones.model.dao.VincularDao;
import org.jisho.textosJapones.model.entities.MangaPagina;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;

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

	public void addNaoVinculado(ObservableList<VinculoPagina> naoVinculado, VinculoPagina pagina) {
		if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
			naoVinculado.add(new VinculoPagina(pagina.getVinculadoEsquerdaNomePagina(),
					pagina.getVinculadoEsquerdaPathPagina(), pagina.getVinculadoEsquerdaPagina(),
					pagina.getVinculadoEsquerdaPaginas(), pagina.isVinculadoEsquerdaPaginaDupla,
					pagina.getMangaPaginaEsquerda(), pagina.getImagemVinculadoEsquerda(), true));

		if (pagina.isImagemDupla) {
			naoVinculado.add(new VinculoPagina(pagina.getVinculadoDireitaNomePagina(),
					pagina.getVinculadoDireitaPathPagina(), pagina.getVinculadoDireitaPagina(),
					pagina.getVinculadoDireitaPaginas(), pagina.isVinculadoDireitaPaginaDupla,
					pagina.getMangaPaginaDireita(), pagina.getImagemVinculadoDireita(), true));
			pagina.limparVinculadoDireita();
		}
	}

	public void ordenarPaginaDupla(ObservableList<VinculoPagina> vinculado, ObservableList<VinculoPagina> naoVinculado,
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

	private Integer qtde = 0;

	public void ordenarPaginaSimples(ObservableList<VinculoPagina> vinculado,
			ObservableList<VinculoPagina> naoVinculado) {

		if (vinculado == null || vinculado.isEmpty())
			return;

		Boolean existeImagem = vinculado.stream().anyMatch((it) -> it.isImagemDupla);

		if (existeImagem) {
			qtde = 0;
			vinculado.forEach((it) -> {
				if (it.isImagemDupla)
					qtde++;
			});

			for (var i = vinculado.size(); i <= (vinculado.size() - 1 - qtde); i--)
				addNaoVinculado(naoVinculado, vinculado.get(i));

			Integer processado = qtde;
			for (var i = vinculado.size(); i <= 0; i--) {
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

	public void autoReordenarPaginaDupla(ObservableList<VinculoPagina> vinculado,
			ObservableList<VinculoPagina> naoVinculado) {
		autoReordenarPaginaDupla(vinculado, naoVinculado, false);
	}

	public void autoReordenarPaginaDupla(ObservableList<VinculoPagina> vinculado,
			ObservableList<VinculoPagina> naoVinculado, Boolean isLimpar) {

		if (vinculado == null || vinculado.isEmpty())
			return;

		Boolean temImagemDupla = false;
		if (isLimpar)
			ordenarPaginaSimples(vinculado, naoVinculado);
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

					for (var idxNext = (index + 1); idxNext <= vinculado.size(); idxNext++) {
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

					addNaoVinculado(naoVinculado, vinculado.get(indexEmpty));
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

	public void reordenarPeloNumeroPagina(ObservableList<VinculoPagina> vinculado,
			ObservableList<VinculoPagina> naoVinculado, Boolean isLimpar) {

		if (vinculado == null || vinculado.isEmpty())
			return;

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
			for (numeroPagina = naoVinculado.size(); numeroPagina <= maxNumPag; numeroPagina++) {
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
								item.getMangaPaginaEsquerda(), item.getImagemVinculadoEsquerda(), true));
					else
						naoVinculadoTemp.add(new VinculoPagina(item.getVinculadoDireitaNomePagina(),
								item.getVinculadoDireitaPathPagina(), item.getVinculadoDireitaPagina(),
								item.getVinculadoDireitaPaginas(), item.isVinculadoDireitaPaginaDupla,
								item.getMangaPaginaDireita(), item.getImagemVinculadoDireita(), true));
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

}
