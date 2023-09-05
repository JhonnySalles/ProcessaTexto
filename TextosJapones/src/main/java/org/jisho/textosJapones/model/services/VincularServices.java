package org.jisho.textosJapones.model.services;

import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.util.Pair;
import org.jisho.textosJapones.components.listener.VinculoServiceListener;
import org.jisho.textosJapones.controller.GrupoBarraProgressoController;
import org.jisho.textosJapones.controller.MenuPrincipalController;
import org.jisho.textosJapones.database.dao.DaoFactory;
import org.jisho.textosJapones.database.dao.MangaDao;
import org.jisho.textosJapones.database.dao.VincularDao;
import org.jisho.textosJapones.fileparse.Parse;
import org.jisho.textosJapones.model.entities.Vinculo;
import org.jisho.textosJapones.model.entities.VinculoPagina;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaPagina;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaTabela;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaVinculo;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaVolume;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.Pagina;
import org.jisho.textosJapones.model.exceptions.ExcessaoBd;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.similarity.ImageHistogram;
import org.jisho.textosJapones.util.similarity.ImagePHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class VincularServices {

    private static final Logger LOGGER = LoggerFactory.getLogger(VincularServices.class);

    private final VincularDao dao = DaoFactory.createVincularDao();
    private final MangaDao mangaDao = DaoFactory.createMangaDao();

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
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    public Vinculo select(String base, UUID id) throws ExcessaoBd {
        if (base == null || base.isEmpty())
            return null;

        return dao.select(base, id);
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

    public UUID insert(String base, Vinculo obj) throws ExcessaoBd {
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

    public List<MangaTabela> selectTabelasJson(String base, String manga, Integer volume, Float capitulo,
                                               Language linguagem) throws ExcessaoBd {
        return dao.selectTabelasJson(base, manga, volume, capitulo, linguagem);
    }

    public List<MangaVinculo> getMangaVinculo(String base, String manga, Integer volume, Float capitulo,
                                              Language linguagem) throws ExcessaoBd {
        return dao.selectVinculo(base, manga, volume, capitulo, linguagem);
    }

    // -------------------------------------------------------------------------------------------------
    public void gerarAtributos(Parse parse, Boolean isManga) {
        GrupoBarraProgressoController progress = MenuPrincipalController.getController().criaBarraProgresso();
        progress.getTitulo()
                .setText("Processando atributos da imagem do arquivo " + (isManga ? "original" : "vinculado"));

        Task<Void> processar = new Task<Void>() {
            Integer I, Max;

            @Override
            protected Void call() throws Exception {
                try {
                    List<VinculoPagina> lista = listener.getVinculados().parallelStream().collect(Collectors.toList());
                    if (!isManga)
                        lista.addAll(listener.getNaoVinculados());

                    I = 0;
                    Max = lista.size() * 2;

                    ImagePHash imgPHash = new ImagePHash();
                    ImageHistogram imgHistogram = new ImageHistogram();

                    updateMessage("Gerando pHash....");
                    for (VinculoPagina pagina : lista) {
                        if (isManga)
                            pagina.setOriginalPHash(imgPHash.getHash(parse.getPagina(pagina.getOriginalPagina())));
                        else {
                            if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                                pagina.setVinculadoEsquerdaPHash(
                                        imgPHash.getHash(parse.getPagina(pagina.getVinculadoEsquerdaPagina())));

                            if (pagina.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                                pagina.setVinculadoDireitaPHash(
                                        imgPHash.getHash(parse.getPagina(pagina.getVinculadoDireitaPagina())));
                        }

                        I++;
                        updateProgress(I, Max);
                    }

                    updateMessage("Gerando Histogram....");
                    for (VinculoPagina pagina : lista) {
                        if (isManga)
                            pagina.setOriginalHistogram(
                                    imgHistogram.generate(parse.getPagina(pagina.getOriginalPagina())));
                        else {
                            if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                                pagina.setVinculadoEsquerdaHistogram(
                                        imgHistogram.generate(parse.getPagina(pagina.getVinculadoEsquerdaPagina())));

                            if (pagina.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                                pagina.setVinculadoDireitaHistogram(
                                        imgHistogram.generate(parse.getPagina(pagina.getVinculadoDireitaPagina())));
                        }

                        I++;
                        updateProgress(I, Max);
                    }
                } catch (Exception e) {
                    
                    LOGGER.error(e.getMessage(), e);
                }

                return null;
            }

            @Override
            protected void succeeded() {
                progress.getBarraProgresso().progressProperty().unbind();
                progress.getLog().textProperty().unbind();
                MenuPrincipalController.getController().destroiBarraProgresso(progress, "");
            }
        };

        progress.getLog().textProperty().bind(processar.messageProperty());
        progress.getBarraProgresso().progressProperty().bind(processar.progressProperty());
        Thread t = new Thread(processar);
        t.start();
    }

    // -------------------------------------------------------------------------------------------------
    private VinculoServiceListener listener;

    public void setListener(VinculoServiceListener listener) {
        this.listener = listener;
    }

    public void addNaoVinculado(VinculoPagina pagina) {
        ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

        if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
            naoVinculado.add(new VinculoPagina(pagina, true, true));

        if (pagina.isImagemDupla) {
            naoVinculado.add(new VinculoPagina(pagina, false, true));
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
                    for (var i = (index + 1); i < lastIndex; i++) {
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

    public void autoReordenarPHash(double precisao) {
        ObservableList<VinculoPagina> vinculado = listener.getVinculados();

        if (vinculado == null || vinculado.isEmpty())
            return;

        ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

        Boolean temImagemDupla = vinculado.stream().anyMatch((it) -> it.isImagemDupla);

        if (temImagemDupla)
            ordenarPaginaSimples();

        List<VinculoPagina> processar = vinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, false)).collect(Collectors.toList());
        processar.addAll(
                vinculado.parallelStream().filter(vp -> vp.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                        .map(vp -> new VinculoPagina(vp, false, false)).collect(Collectors.toList()));

        processar.addAll(naoVinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, false)).collect(Collectors.toList()));

        List<VinculoPagina> vinculadoTemp = vinculado.parallelStream().map(vp -> new VinculoPagina(vp))
                .collect(Collectors.toList());

        ImagePHash pHash = new ImagePHash();
        double limiar = precisao / 10;

        for (VinculoPagina pagina : vinculadoTemp) {
            if (!pagina.getOriginalPHash().isEmpty()) {
                // Filtra apenas paginas que não foram encontradas, então pega os itens que são
                // parecidos e realizam uma validação do mais parecido dentre eles para retornar
                // o que mais se encaixa
                Optional<Pair<Integer, VinculoPagina>> vinculo = processar.parallelStream()
                        .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                        .filter(vp -> pHash.match(pagina.getOriginalPHash(), vp.getVinculadoEsquerdaPHash(), limiar))
                        .map(vp -> new Pair<Integer, VinculoPagina>(
                                pHash.matchLimiar(pagina.getOriginalPHash(), vp.getVinculadoEsquerdaPHash(), limiar),
                                vp))
                        .filter(vp -> vp.getKey() <= ImagePHash.SIMILAR) // Somente imagens semelhantes
                        .sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey())).findFirst();

                if (vinculo.isPresent()) {
                    pagina.addVinculoEsquerda(vinculo.get().getValue());
                    vinculo.get().getValue().limparVinculadoEsquerda();
                }
            }
        }

        List<VinculoPagina> naoLocalizado = processar.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, true)).collect(Collectors.toList());
        processar.addAll(
                vinculado.parallelStream().filter(vp -> vp.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                        .map(vp -> new VinculoPagina(vp, false, true)).collect(Collectors.toList()));

        processar.addAll(naoVinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, true)).collect(Collectors.toList()));

        vinculado.clear();
        naoVinculado.clear();
        vinculado.addAll(vinculadoTemp);
        naoVinculado.addAll(naoLocalizado);
    }

    public void autoReordenarHistogram(double precisao) {
        ObservableList<VinculoPagina> vinculado = listener.getVinculados();

        if (vinculado == null || vinculado.isEmpty())
            return;

        ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

        Boolean temImagemDupla = vinculado.stream().anyMatch((it) -> it.isImagemDupla);

        if (temImagemDupla)
            ordenarPaginaSimples();

        List<VinculoPagina> processar = vinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, false)).collect(Collectors.toList());
        processar.addAll(
                vinculado.parallelStream().filter(vp -> vp.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                        .map(vp -> new VinculoPagina(vp, false, false)).collect(Collectors.toList()));

        processar.addAll(naoVinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, false)).collect(Collectors.toList()));

        List<VinculoPagina> vinculadoTemp = vinculado.parallelStream().map(vp -> new VinculoPagina(vp))
                .collect(Collectors.toList());

        ImageHistogram histogram = new ImageHistogram();
        double limiar = precisao / 100;

        for (VinculoPagina pagina : vinculadoTemp) {
            if (!pagina.getOriginalPHash().isEmpty()) {
                // Filtra apenas paginas que não foram encontradas, então pega os itens que são
                // parecidos e realizam uma validação do mais parecido dentre eles para retornar
                // o que mais se encaixa
                Optional<Pair<Double, VinculoPagina>> vinculo = processar.parallelStream()
                        .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                        .filter(vp -> histogram.match(pagina.getOriginalHistogram(), vp.getVinculadoEsquerdaHistogram(),
                                limiar))
                        .map(vp -> new Pair<Double, VinculoPagina>(histogram.matchLimiar(pagina.getOriginalHistogram(),
                                vp.getVinculadoEsquerdaHistogram(), limiar), vp))
                        .sorted((o1, o2) -> o2.getKey().compareTo(o1.getKey())).findFirst();

                if (vinculo.isPresent()) {
                    pagina.addVinculoEsquerda(vinculo.get().getValue());
                    vinculo.get().getValue().limparVinculadoEsquerda();
                }
            }
        }

        List<VinculoPagina> naoLocalizado = processar.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, true)).collect(Collectors.toList());
        processar.addAll(
                vinculado.parallelStream().filter(vp -> vp.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                        .map(vp -> new VinculoPagina(vp, false, true)).collect(Collectors.toList()));

        processar.addAll(naoVinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, true)).collect(Collectors.toList()));

        vinculado.clear();
        naoVinculado.clear();
        vinculado.addAll(vinculadoTemp);
        naoVinculado.addAll(naoLocalizado);
    }

    public void autoReordenarInteligente(double precisao) {
        ObservableList<VinculoPagina> vinculado = listener.getVinculados();

        if (vinculado == null || vinculado.isEmpty())
            return;

        ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

        Boolean temImagemDupla = vinculado.stream().anyMatch((it) -> it.isImagemDupla);

        if (temImagemDupla)
            ordenarPaginaSimples();

        List<VinculoPagina> processar = vinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, false)).collect(Collectors.toList());
        processar.addAll(
                vinculado.parallelStream().filter(vp -> vp.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                        .map(vp -> new VinculoPagina(vp, false, false)).collect(Collectors.toList()));

        processar.addAll(naoVinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, false)).collect(Collectors.toList()));

        List<VinculoPagina> vinculadoTemp = vinculado.parallelStream().map(vp -> new VinculoPagina(vp))
                .collect(Collectors.toList());

        ImagePHash pHash = new ImagePHash();
        ImageHistogram histogram = new ImageHistogram();
        double limiar = precisao / 100;

        for (VinculoPagina pagina : vinculadoTemp) {
            if (!pagina.getOriginalPHash().isEmpty()) {
                final Pair<Float, Boolean> capitulo = Util.getCapitulo(pagina.getOriginalPathPagina());

                List<VinculoPagina> busca = new ArrayList<VinculoPagina>();
                if (capitulo != null)
                    busca.addAll(processar.parallelStream()
                            .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                            .filter(vp -> {
                                Pair<Float, Boolean> cap = Util.getCapitulo(vp.getVinculadoEsquerdaPathPagina());
                                return capitulo.getKey().equals(cap.getKey()) && capitulo.getValue().equals(cap.getValue());
                            }).collect(Collectors.toList()));
                else
                    busca.addAll(processar.parallelStream()
                            .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                            .collect(Collectors.toList()));

                Optional<Pair<Double, VinculoPagina>> vinculo = busca.parallelStream()
                        .filter(vp -> histogram.match(pagina.getOriginalHistogram(), vp.getVinculadoEsquerdaHistogram(),
                                limiar))
                        .map(vp -> new Pair<Double, VinculoPagina>(histogram.matchLimiar(pagina.getOriginalHistogram(),
                                vp.getVinculadoEsquerdaHistogram(), limiar), vp))
                        .map(vp -> new Pair<Double, VinculoPagina>(
                                Double.valueOf(pHash.matchLimiar(pagina.getOriginalPHash(),
                                        vp.getValue().getVinculadoEsquerdaPHash(), 1000)),
                                vp.getValue()))
                        .sorted((o1, o2) -> o1.getKey().compareTo(o2.getKey())).findFirst();

                if (vinculo.isPresent()) {
                    pagina.addVinculoEsquerda(vinculo.get().getValue());
                    vinculo.get().getValue().limparVinculadoEsquerda();
                }
            }
        }

        List<VinculoPagina> naoLocalizado = processar.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, true)).collect(Collectors.toList());
        processar.addAll(
                vinculado.parallelStream().filter(vp -> vp.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                        .map(vp -> new VinculoPagina(vp, false, true)).collect(Collectors.toList()));

        processar.addAll(naoVinculado.parallelStream()
                .filter(vp -> vp.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                .map(vp -> new VinculoPagina(vp, true, true)).collect(Collectors.toList()));

        vinculado.clear();
        naoVinculado.clear();
        vinculado.addAll(vinculadoTemp);
        naoVinculado.addAll(naoLocalizado);
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
                vinculadoTemp.add(new VinculoPagina(pagina));
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
                        naoVinculadoTemp.add(new VinculoPagina(item, true, true));
                    else
                        naoVinculadoTemp.add(new VinculoPagina(item, false, true));
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

    // -------------------------------------------------------------------------------------------------

    public void addNaoVInculado(VinculoPagina pagina, Pagina origem) {
        ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

        if (origem == Pagina.VINCULADO_DIREITA) {
            if (pagina.getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                naoVinculado.add(new VinculoPagina(pagina, false, true));

            pagina.limparVinculadoDireita();
        } else {
            if (pagina.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
                naoVinculado.add(new VinculoPagina(pagina, true, true));

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

        Integer origemIndex = vinculado.indexOf(origem);
        Integer destinoIndex = vinculado.indexOf(destino);
        Integer diferenca = destinoIndex - origemIndex;

        if (origemIndex > destinoIndex) {
            Integer limite = vinculado.size() - 1;

            Integer index = vinculado.indexOf(
                    vinculado.stream().filter(it -> it.getImagemVinculadoEsquerda() == null).findFirst().get());
            if (index < 0)
                index = limite;

            for (var i = index; i >= origemIndex; i--)
                if (vinculado.get(i).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA)
                    limite = i;

            for (var i = destinoIndex; i < origemIndex; i++)
                addNaoVinculado(vinculado.get(i));

            diferenca *= -1;

            for (var i = destinoIndex; i < limite; i++) {
                if (i == destinoIndex)
                    vinculado.get(i).addVinculoEsquerda(origem);
                else if ((i + diferenca) > (limite))
                    continue;
                else {
                    vinculado.get(i).addVinculoEsquerda(vinculado.get(i + diferenca));
                    vinculado.get(i + diferenca).limparVinculadoEsquerda();
                }
            }

            for (var i = destinoIndex; i < limite; i++)
                if (vinculado.get(i).isImagemDupla
                        && vinculado.get(i).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA
                        && vinculado.get(i).getVinculadoDireitaPagina() != VinculoPagina.PAGINA_VAZIA)
                    vinculado.get(i).moverDireitaParaEsquerda();

        } else {
            Integer limite = vinculado.size() - 1;
            Integer espacos = 0;

            for (var i = origemIndex; i < limite; i++)
                if (vinculado.get(i).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA)
                    espacos++;

            if (diferenca > espacos) {
                for (var i = limite; i >= (limite - diferenca); i--)
                    addNaoVinculado(vinculado.get(i));

                for (var i = limite; i >= origemIndex; i--) {
                    if (i < destinoIndex)
                        vinculado.get(i).limparVinculadoEsquerda();
                    else
                        vinculado.get(i).addVinculoEsquerda(vinculado.get(i - diferenca));
                }
            } else {
                espacos = 0;

                for (var i = origemIndex; i < limite; i++) {
                    if (vinculado.get(i).getVinculadoEsquerdaPagina() == VinculoPagina.PAGINA_VAZIA) {
                        espacos++;

                        if (espacos >= diferenca) {
                            limite = i;
                            break;
                        }
                    }
                }

                espacos = 0;
                Integer index;

                for (var i = limite; i >= origemIndex; i--) {
                    if (i < destinoIndex)
                        vinculado.get(i).limparVinculadoEsquerda(true);
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
        if (itemOrigem == null || itemDestino == null
                || (itemOrigem == itemDestino && destino == Pagina.VINCULADO_DIREITA))
            return;

        ObservableList<VinculoPagina> naoVinculado = listener.getNaoVinculados();

        if (destino != Pagina.VINCULADO_ESQUERDA && itemDestino.isImagemDupla)
            naoVinculado.add(new VinculoPagina(itemDestino, false, true));
        else if (destino == Pagina.VINCULADO_ESQUERDA
                && itemDestino.getVinculadoEsquerdaPagina() != VinculoPagina.PAGINA_VAZIA)
            naoVinculado.add(new VinculoPagina(itemDestino, true, true));

        if (origem == Pagina.VINCULADO_DIREITA && destino == Pagina.VINCULADO_DIREITA) {
            itemDestino.addVinculoDireita(itemOrigem);
            itemOrigem.limparVinculadoDireita();
        } else if (origem == Pagina.NAO_VINCULADO || destino == Pagina.NAO_VINCULADO) {
            if (origem == Pagina.NAO_VINCULADO && destino == Pagina.NAO_VINCULADO)
                return;
            else if (origem == Pagina.NAO_VINCULADO) {
                itemDestino.addVinculoDireitaApartirEsquerda(itemOrigem);
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
