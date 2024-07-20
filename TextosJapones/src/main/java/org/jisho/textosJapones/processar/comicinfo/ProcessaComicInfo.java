package org.jisho.textosJapones.processar.comicinfo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.katsute.mal4j.MyAnimeList;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import javafx.util.Pair;
import org.jisho.textosJapones.controller.mangas.MangasComicInfoController;
import org.jisho.textosJapones.fileparse.Parse;
import org.jisho.textosJapones.fileparse.ParseFactory;
import org.jisho.textosJapones.model.entities.comicinfo.ComicInfo;
import org.jisho.textosJapones.model.entities.comicinfo.Pages;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.comicinfo.ComicPageType;
import org.jisho.textosJapones.model.services.ComicInfoServices;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.configuration.Configuracao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class ProcessaComicInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessaComicInfo.class);

    private static String WINRAR;
    private static final String PATTERN = ".*\\.(zip|cbz|rar|cbr|tar)$";
    private static final String COMICINFO = "ComicInfo.xml";
    private static final Long IMAGE_WIDTH = 170L;
    private static final Long IMAGE_HEIGHT = 300L;
    private static JAXBContext JAXBC = null;
    private static Boolean CANCELAR_PROCESSAMENTO = false;
    private static Boolean CANCELAR_VALIDACAO = false;
    private static MangasComicInfoController CONTROLLER;
    private static String MARCACAPITULO;
    private static Boolean IGNORAR_VINCULO_SALVO;

    private static final Boolean CONSULTA_MAL = true;
    private static final Boolean CONSULTA_JIKAN = true;

    private static ComicInfoServices SERVICE = null;

    public static void setPai(MangasComicInfoController controller) {
        CONTROLLER = controller;
    }

    public static void cancelar() {
        CANCELAR_PROCESSAMENTO = true;
        CANCELAR_VALIDACAO = true;
    }

    public static void validar(String winrar, Language linguagem, String path, Callback<Integer[], Boolean> callback) {
        WINRAR = winrar;
        CANCELAR_VALIDACAO = false;

        File arquivos = new File(path);
        Integer[] size = new Integer[2];

        try {
            JAXBC = JAXBContext.newInstance(ComicInfo.class);
            SERVICE = new ComicInfoServices();

            if (arquivos.isDirectory()) {
                size[0] = 0;
                size[1] = arquivos.listFiles().length;
                callback.call(size);

                for (File arquivo : arquivos.listFiles()) {
                    String nome = arquivo.getName();
                    System.out.print("Validando o manga " + nome);
                    String valido = valida(linguagem, arquivo);

                    if (valido.isEmpty()) {
                        LOGGER.info(" - OK. ");
                        gravalog(path, "Validando o manga " + nome + " - OK.\n");
                    } else {
                        LOGGER.info(" - Arquivo possui pendências: ");
                        LOGGER.info(valido);
                        gravalog(path, "Validando o manga " + nome + " - Arquivo possui pendências: \n");
                        gravalog(path, valido + "\n");
                    }

                    LOGGER.info("-".repeat(100));
                    gravalog(path, "-".repeat(100) + "\n");

                    size[0]++;
                    callback.call(size);

                    if (CANCELAR_VALIDACAO)
                        break;
                }
            } else if (arquivos.isFile()) {
                size[0] = 0;
                size[1] = 1;
                callback.call(size);

                String nome = arquivos.getName();
                System.out.print("Validando o manga " + nome);
                String valido = valida(linguagem, arquivos);

                if (valido.isEmpty()) {
                    LOGGER.info(" - OK. ");
                    gravalog(path, "Validando o manga " + nome + " - OK.\n");
                } else {
                    LOGGER.info(" - Arquivo possui pendências: ");
                    LOGGER.info(valido);
                    gravalog(path, "Validando o manga " + nome + " - Arquivo possui pendências: \n");
                    gravalog(path, valido + "\n");
                }

                LOGGER.info("-".repeat(100));
                gravalog(path, "-".repeat(100) + "\n");

                size[0]++;
                callback.call(size);
            }
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (JAXBC != null)
                JAXBC = null;

            if (SERVICE != null)
                SERVICE = null;
        }
    }

    public static void processa(String winrar, Language linguagem, String path, String marcaCapitulo, Boolean ignorarVinculoSalvo, Callback<Integer[], Boolean> callback) {
        WINRAR = winrar;
        CANCELAR_PROCESSAMENTO = false;
        MARCACAPITULO = marcaCapitulo == null ? "" : marcaCapitulo;
        IGNORAR_VINCULO_SALVO = ignorarVinculoSalvo;

        MAL = MyAnimeList.withClientID(Configuracao.getMyAnimeListClient());

        File arquivos = new File(path);
        Integer[] size = new Integer[2];

        try {
            JAXBC = JAXBContext.newInstance(ComicInfo.class);
            SERVICE = new ComicInfoServices();

            if (arquivos.isDirectory()) {
                size[0] = 0;
                size[1] = arquivos.listFiles().length;
                callback.call(size);

                for (File arquivo : arquivos.listFiles()) {
                    processa(linguagem, arquivo, null);
                    size[0]++;
                    callback.call(size);

                    if (CANCELAR_PROCESSAMENTO)
                        break;
                }
            } else if (arquivos.isFile()) {
                size[0] = 0;
                size[1] = 1;
                callback.call(size);

                processa(linguagem, arquivos, null);

                size[0]++;
                callback.call(size);
            }
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (JAXBC != null)
                JAXBC = null;

            if (SERVICE != null)
                SERVICE = null;
        }
    }

    public static Boolean processa(String winrar, Language linguagem, String arquivo, Long idMal) {
        WINRAR = winrar;
        CANCELAR_PROCESSAMENTO = false;

        MAL = MyAnimeList.withClientID(Configuracao.getMyAnimeListClient());

        File arquivos = new File(arquivo);

        if (!arquivos.exists())
            return false;

        try {
            if (JAXBC == null)
                JAXBC = JAXBContext.newInstance(ComicInfo.class);
            SERVICE = new ComicInfoServices();

            processa(linguagem, arquivos, idMal);
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        } finally {
            if (JAXBC != null)
                JAXBC = null;

            if (SERVICE != null)
                SERVICE = null;
        }
        return true;
    }

    private static MyAnimeList MAL;

    private static String getNome(String nome) {
        String arquivo = Util.getNome(nome);

        if (arquivo.toLowerCase().contains("volume"))
            arquivo = arquivo.substring(0, arquivo.toLowerCase().indexOf("volume"));
        else if (arquivo.toLowerCase().contains("capitulo"))
            arquivo = arquivo.substring(0, arquivo.toLowerCase().indexOf("capitulo"));
        else if (arquivo.toLowerCase().contains("capítulo"))
            arquivo = arquivo.substring(0, arquivo.toLowerCase().indexOf("capítulo"));
        else if (arquivo.contains("-"))
            arquivo = arquivo.substring(0, arquivo.lastIndexOf("-"));

        if (arquivo.endsWith(" - "))
            arquivo = arquivo.substring(0, arquivo.lastIndexOf(" - "));

        return arquivo;
    }

    public static boolean getById(Long idMal, org.jisho.textosJapones.model.entities.comicinfo.MAL.Registro registro) {
        if (MAL == null)
            MAL = MyAnimeList.withClientID(Configuracao.getMyAnimeListClient());

        dev.katsute.mal4j.manga.Manga manga = MAL.getManga(idMal);

        if (manga != null) {
            registro.setNome(manga.getTitle());
            registro.setId(manga.getID());
            registro.setImagem(new ImageView(manga.getMainPicture().getMediumURL()));
            if (registro.getImagem() != null) {
                registro.getImagem().setFitWidth(IMAGE_WIDTH);
                registro.getImagem().setFitHeight(IMAGE_HEIGHT);
            }
            return true;
        } else
            return false;
    }

    private static final String API_JIKAN_CHARACTER = "https://api.jikan.moe/v4/manga/%s/characters";
    private static final String TITLE_PATERN = "[^\\w\\s]";
    private static final String MANGA_PATERN = " - Volume[\\w\\W]*";
    private static final String DESCRIPTION_MAL = "Tagged with MyAnimeList on ";
    private static dev.katsute.mal4j.manga.Manga MANGA = null;
    private static Pair<Long, String> MANGA_CHARACTER;

    private static Long getIdMal(String notas) {
        Long id = null;
        if (notas != null) {
            if (notas.contains(";")) {
                for (String note : notas.split(";"))
                    if (note.toLowerCase().contains(DESCRIPTION_MAL.toLowerCase()))
                        id = Long.valueOf(note.substring(note.indexOf("[Issue ID")).replace("[Issue ID", "").replace("]", "").trim());
            } else if (notas.toLowerCase().contains(DESCRIPTION_MAL.toLowerCase()))
                id = Long.valueOf(notas.substring(notas.indexOf("[Issue ID")).replace("[Issue ID", "").replace("]", "").trim());
        }
        return id;
    }

    private static void processaMal(String arquivo, String nome, ComicInfo info, Language linguagem, Long idMal) {
        try {
            Long id = idMal;
            ComicInfo saved = null;

            if (IGNORAR_VINCULO_SALVO)
                saved = SERVICE.select(info.getComic(), info.getLanguageISO());

            if (id == null)
                id = getIdMal(info.getNotes());

            if (id == null) {
                if (saved != null && saved.getIdMal() > 0) {
                    id = saved.getIdMal();
                    info.setId(saved.getId());
                }
            }

            String title = nome.replaceAll(MANGA_PATERN, "").trim();

            if (MANGA == null || !title.equalsIgnoreCase(MANGA.getTitle().replaceAll(MANGA_PATERN, "").trim())) {
                MANGA = null;
                if (id != null)
                    MANGA = MAL.getManga(id);
                else {
                    List<dev.katsute.mal4j.manga.Manga> search;
                    int max = 2;
                    int page = 0;
                    do {
                        LOGGER.info("Realizando a consulta " + page);
                        search = MAL.getManga().withQuery(nome).withLimit(50).withOffset(page).search();
                        if (search != null && !search.isEmpty())
                            for (dev.katsute.mal4j.manga.Manga item : search) {
                                LOGGER.info(item.getTitle());
                                if (item.getType() == dev.katsute.mal4j.manga.property.MangaType.Manga && title.equalsIgnoreCase(item.getTitle().replaceAll(TITLE_PATERN, "").trim())) {
                                    LOGGER.info("Encontrado o manga " + item.getTitle());
                                    MANGA = item;
                                    break;
                                }
                            }

                        if (page == 0 && MANGA == null) {
                            if (search != null && !search.isEmpty()) {
                                org.jisho.textosJapones.model.entities.comicinfo.MAL mal = new org.jisho.textosJapones.model.entities.comicinfo.MAL(arquivo, nome);
                                for (dev.katsute.mal4j.manga.Manga item : search) {
                                    org.jisho.textosJapones.model.entities.comicinfo.MAL.Registro registro = mal.addRegistro(item.getTitle(), item.getID(), false);
                                    if (item.getMainPicture().getMediumURL() != null)
                                        registro.setImagem(new ImageView(item.getMainPicture().getMediumURL()));
                                    else if (item.getPictures().length > 0 && item.getPictures()[0].getMediumURL() != null)
                                        registro.setImagem(new ImageView(item.getPictures()[0].getMediumURL()));

                                    if (registro.getImagem() != null) {
                                        registro.getImagem().setFitWidth(IMAGE_WIDTH);
                                        registro.getImagem().setFitHeight(IMAGE_HEIGHT);
                                        registro.getImagem().setPreserveRatio(true);
                                    }
                                }

                                try {
                                    Parse parse = ParseFactory.create(arquivo);
                                    mal.setImagem(new ImageView(new Image(parse.getPagina(0))));
                                    mal.getImagem().setFitWidth(IMAGE_WIDTH);
                                    mal.getImagem().setFitHeight(IMAGE_HEIGHT);
                                    mal.getImagem().setPreserveRatio(true);
                                } catch (Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                }

                                mal.getMyanimelist().get(0).setMarcado(true);
                                Platform.runLater(() -> {
                                    CONTROLLER.addItem(mal);
                                });
                            }
                        }

                        page++;
                        if (page > max)
                            break;
                    } while (MANGA == null && search != null && !search.isEmpty());
                }
            }

            if (MANGA != null) {
                if (info.getId() == null) {
                    if (saved != null)
                        info.setId(saved.getId());

                    info.setIdMal(id);
                    SERVICE.save(info);
                }

                for (dev.katsute.mal4j.manga.property.Author author : MANGA.getAuthors()) {
                    if (author.getRole().equalsIgnoreCase("art")) {
                        if (info.getPenciller() == null || info.getPenciller().isEmpty())
                            info.setPenciller((author.getFirstName() + " " + author.getLastName()).trim());

                        if (info.getInker() == null || info.getInker().isEmpty())
                            info.setInker((author.getFirstName() + " " + author.getLastName()).trim());

                        if (info.getCoverArtist() == null || info.getCoverArtist().isEmpty())
                            info.setCoverArtist((author.getFirstName() + " " + author.getLastName()).trim());
                    } else if (author.getRole().equalsIgnoreCase("story")) {
                        if (info.getWriter() == null || info.getWriter().isEmpty())
                            info.setWriter((author.getFirstName() + " " + author.getLastName()).trim());
                    } else {
                        if (author.getRole().toLowerCase().contains("story")) {
                            if (info.getWriter() == null || info.getWriter().isEmpty())
                                info.setWriter((author.getFirstName() + " " + author.getLastName()).trim());
                        }

                        if (author.getRole().toLowerCase().contains("art")) {
                            if (info.getPenciller() == null || info.getPenciller().isEmpty())
                                info.setPenciller((author.getFirstName() + " " + author.getLastName()).trim());

                            if (info.getInker() == null || info.getInker().isEmpty())
                                info.setInker((author.getFirstName() + " " + author.getLastName()).trim());

                            if (info.getCoverArtist() == null || info.getCoverArtist().isEmpty())
                                info.setCoverArtist((author.getFirstName() + " " + author.getLastName()).trim());
                        }
                    }
                }

                if (info.getGenre() == null || info.getGenre().isEmpty()) {
                    String genero = "";
                    for (dev.katsute.mal4j.property.Genre genre : MANGA.getGenres())
                        genero += genre.getName() + "; ";

                    info.setGenre(genero.substring(0, genero.lastIndexOf("; ")));
                }

                if (linguagem.equals(Language.PORTUGUESE)) {
                    if (MANGA.getAlternativeTitles().getEnglish() != null && !MANGA.getAlternativeTitles().getEnglish().isEmpty()) {
                        info.setTitle(MANGA.getTitle());
                        info.setSeries(MANGA.getAlternativeTitles().getEnglish());
                    }
                } else if (linguagem.equals(Language.JAPANESE)) {
                    if (MANGA.getAlternativeTitles().getJapanese() != null
                            && !MANGA.getAlternativeTitles().getJapanese().isEmpty())
                        info.setTitle(MANGA.getAlternativeTitles().getJapanese());
                }

                if (info.getAlternateSeries() == null || info.getAlternateSeries().isEmpty()) {
                    title = "";

                    if (MANGA.getAlternativeTitles().getJapanese() != null
                            && !MANGA.getAlternativeTitles().getJapanese().isEmpty()) {
                        title += MANGA.getAlternativeTitles().getJapanese() + "; ";
                    }

                    if (MANGA.getAlternativeTitles().getEnglish() != null
                            && !MANGA.getAlternativeTitles().getEnglish().isEmpty()) {
                        title += MANGA.getAlternativeTitles().getEnglish() + "; ";
                    }

                    if (MANGA.getAlternativeTitles().getSynonyms() != null)
                        for (String synonym : MANGA.getAlternativeTitles().getSynonyms())
                            title += synonym + "; ";

                    if (!title.isEmpty())
                        info.setAlternateSeries(title.substring(0, title.lastIndexOf("; ")));
                }

                if (info.getPublisher() == null || info.getPublisher().isEmpty()) {
                    String publisher = "";
                    for (dev.katsute.mal4j.manga.property.Publisher pub : MANGA.getSerialization())
                        publisher += pub.getName() + "; ";

                    if (!publisher.isEmpty())
                        info.setPublisher(publisher.substring(0, publisher.lastIndexOf("; ")));
                }

                DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String notes = "";
                if (info.getNotes() != null) {
                    if (info.getNotes().contains(";")) {
                        for (String note : info.getNotes().split(";"))
                            if (note.toLowerCase().contains(DESCRIPTION_MAL.toLowerCase()))
                                notes += DESCRIPTION_MAL + dateTime.format(LocalDateTime.now()) + ". [Issue ID " + MANGA.getID() + "]; ";
                            else
                                notes += note.trim() + "; ";
                    } else
                        notes += info.getNotes() + "; " + DESCRIPTION_MAL + dateTime.format(LocalDateTime.now()) + ". [Issue ID " + MANGA.getID() + "]; ";
                } else
                    notes += DESCRIPTION_MAL + dateTime.format(LocalDateTime.now()) + ". [Issue ID " + MANGA.getID() + "]; ";

                info.setNotes(notes.substring(0, notes.lastIndexOf("; ")));

                if (CONSULTA_JIKAN) {
                    if (MANGA_CHARACTER != null && MANGA_CHARACTER.getKey().compareTo(MANGA.getID()) == 0)
                        info.setCharacters(MANGA_CHARACTER.getValue());
                    else {
                        MANGA_CHARACTER = null;
                        try {
                            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder();
                            HttpRequest request = reqBuilder
                                    .uri(new URI(String.format(API_JIKAN_CHARACTER, MANGA.getID())))
                                    .GET()
                                    .build();

                            HttpResponse<String> response = HttpClient.newBuilder()
                                    .build()
                                    .send(request, HttpResponse.BodyHandlers.ofString());
                            String responseBody = response.body();

                            if (responseBody.contains("character")) {
                                Gson gson = new Gson();
                                JsonElement element = gson.fromJson(responseBody, JsonElement.class);
                                JsonObject jsonObject = element.getAsJsonObject();
                                JsonArray list = jsonObject.getAsJsonArray("data");
                                String characters = "";

                                for (JsonElement item : list) {
                                    JsonObject obj = item.getAsJsonObject();
                                    String character = obj.getAsJsonObject("character").get("name").getAsString();

                                    if (character.contains(", "))
                                        character = character.replace(",", "");
                                    else if (character.contains(","))
                                        character = character.replace(",", " ");

                                    characters += character + (obj.get("role").getAsString().equalsIgnoreCase("main") ? " (" + obj.get("role").getAsString() + "), " : ", ");
                                }

                                if (!characters.isEmpty()) {
                                    info.setCharacters(characters.substring(0, characters.lastIndexOf(", ")) + ".");
                                    MANGA_CHARACTER = new Pair<>(MANGA.getID(), info.getCharacters());
                                }
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                            LOGGER.info(MANGA.getID().toString());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private static void processa(Language linguagem, File arquivo, Long idMal) {
        if (arquivo.getName().toLowerCase().matches(PATTERN)) {
            File info = null;
            try {
                info = extraiInfo(arquivo, false);

                if (info == null || !info.exists())
                    return;

                ComicInfo comic;
                try {
                    Unmarshaller unmarshaller = JAXBC.createUnmarshaller();
                    comic = (ComicInfo) unmarshaller.unmarshal(info);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return;
                }

                String nome = getNome(arquivo.getName());
                LOGGER.info("Processando o manga " + nome);

                if (nome.contains("-"))
                    comic.setComic(nome.substring(0, nome.lastIndexOf("-")).trim());
                else if (nome.contains("."))
                    comic.setComic(nome.substring(0, nome.lastIndexOf(".")).trim());
                else
                    comic.setComic(nome);

                comic.setManga(org.jisho.textosJapones.model.enums.comicinfo.Manga.Yes);
                comic.setLanguageISO(linguagem.getSigla());

                if (comic.getTitle() == null || comic.getTitle().toLowerCase().contains("vol.") || comic.getTitle().toLowerCase().contains("volume"))
                    comic.setTitle(comic.getSeries());
                else if (comic.getTitle() != null && !comic.getTitle().equalsIgnoreCase(comic.getSeries()))
                    comic.setStoryArc(comic.getTitle());

                if (CONSULTA_MAL)
                    processaMal(arquivo.getAbsolutePath(), nome, comic, linguagem, idMal);

                List<Pair<Float, String>> titulosCapitulo = new ArrayList<>();
                if (comic.getSummary() != null && !comic.getSummary().isEmpty()) {
                    String sumary = comic.getSummary().toLowerCase();
                    if (sumary.contains("chapter titles") || sumary.contains("chapter list") || sumary.contains("contents")) {
                        String[] linhas = comic.getSummary().split("\n");
                        for (String linha : linhas) {
                            Float number = 0f;
                            String chapter = "";
                            if (linha.matches("([\\w. ]+[\\d][:|.][\\w\\W]++)|([\\d][:|.][\\w\\W]++)")) {
                                String[] aux = null;
                                if (linha.contains(":"))
                                    aux = linha.split(":");
                                else if (linha.contains(". ")) {
                                    aux = linha.replace(". ", ":").split(":");
                                }

                                if (aux != null) {
                                    try {
                                        if (aux[0].matches("[a-zA-Z ]+[.][\\d]")) // Ex: Act.1: Spring of the Dead
                                            number = Float.valueOf(aux[0].replaceAll("[^\\d]", ""));
                                        else if (aux[0].toLowerCase().contains("extra") || aux[0].toLowerCase().contains("special"))
                                            number = -1f;
                                        else
                                            number = Float.valueOf(aux[0].replaceAll("[^\\d.]", ""));

                                        chapter = aux[1].trim();
                                    } catch (Exception e) {
                                        LOGGER.error(e.getMessage(), e);
                                    }
                                }
                            }

                            if (number.compareTo(0f) > 0)
                                titulosCapitulo.add(new Pair<>(number, chapter));
                        }
                    }
                }

                Parse parse = null;
                try {
                    parse = ParseFactory.create(arquivo);

                    if (linguagem.equals(Language.PORTUGUESE)) {
                        String tradutor = "";
                        for (String pasta : parse.getPastas().keySet()) {
                            String item = "";

                            if (pasta.contains("]"))
                                item = pasta.substring(0, pasta.indexOf("]")).replace("[", "");

                            if (!item.isEmpty()) {
                                if (item.contains("&")) {
                                    String[] itens = item.split("&");
                                    for (String itm : itens)
                                        if (!tradutor.contains(itm.trim()))
                                            tradutor += itm.trim() + "; ";
                                } else if (!tradutor.contains(item))
                                        tradutor += item + "; ";
                            }
                        }

                        if (!tradutor.isEmpty()) {
                            comic.setTranslator(tradutor.substring(0, tradutor.length() - 2));
                            comic.setScanInformation(tradutor.substring(0, tradutor.length() - 2));
                        }
                    } else if (linguagem.equals(Language.JAPANESE)) {
                        comic.setTranslator("");
                        comic.setScanInformation("");
                    }

                    Map<String, Integer> pastas = parse.getPastas();
                    int index = 0;

                    comic.getPages().forEach( it -> {
                        it.setBookmark(null);
                        it.setType(null);
                    });

                    for (int i = 0; i < parse.getSize(); i++) {
                        if (index >= comic.getPages().size())
                            continue;

                        if (Util.isImage(parse.getPaginaPasta(i))) {
                            String imagem = parse.getPaginaPasta(i).toLowerCase();
                            Pages page = comic.getPages().get(index);

                            if (imagem.contains("frente")) {
                                page.setBookmark("Cover");
                                page.setType(ComicPageType.FrontCover);
                            } else if (imagem.contains("tras")) {
                                page.setBookmark("Back");
                                page.setType(ComicPageType.BackCover);
                            } else if (imagem.contains("tudo")) {
                                page.setBookmark("All cover");
                                page.setDoublePage(true);
                                page.setType(ComicPageType.Other);
                            } else if (imagem.contains("zsumário") || imagem.contains("zsumario")) {
                                page.setBookmark("Sumary");
                                page.setType(ComicPageType.InnerCover);
                            } else {
                                if (pastas.containsValue(i)) {
                                    String capitulo = "";
                                    for (Entry<String, Integer> entry : pastas.entrySet()) {
                                        if (entry.getValue().equals(i)) {
                                            if (entry.getKey().toLowerCase().contains("capitulo"))
                                                capitulo = entry.getKey()
                                                        .substring(entry.getKey().toLowerCase().indexOf("capitulo"));
                                            else if (entry.getKey().toLowerCase().contains("capítulo"))
                                                capitulo = entry.getKey()
                                                        .substring(entry.getKey().toLowerCase().indexOf("capítulo"));

                                            if (!capitulo.isEmpty()) {
                                                if (!MARCACAPITULO.isEmpty()) {
                                                    if (capitulo.toLowerCase().contains("capítulo"))
                                                        capitulo = capitulo.substring(capitulo.toLowerCase().indexOf("capítulo") + 8);
                                                    else
                                                        capitulo = capitulo.substring(capitulo.toLowerCase().indexOf("capitulo") + 8);

                                                    if (MARCACAPITULO.toLowerCase().contains("%s")) // Japanese
                                                        capitulo = MARCACAPITULO.toLowerCase().replace("%s", capitulo.trim());
                                                    else
                                                        capitulo = MARCACAPITULO + capitulo;
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    if (!capitulo.isEmpty()) {
                                        if (!titulosCapitulo.isEmpty()) {
                                            try {
                                                Float number = Float.valueOf(capitulo.replaceAll("[^\\d.]", ""));
                                                Optional<Pair<Float, String>> titulo = titulosCapitulo.stream().filter(it -> it.getKey().compareTo(number) == 0).findFirst();
                                                if (titulo.isPresent()) {
                                                    capitulo += " - " + titulo.get().getValue();
                                                    titulosCapitulo.remove(titulo.get());
                                                }
                                            } catch (Exception e) {
                                                LOGGER.error(e.getMessage(), e);
                                            }
                                        }
                                        page.setBookmark(capitulo);
                                    }
                                }

                                if (page.getImageWidth() == null || page.getImageHeight() == null) {
                                    try {
                                        Image image = new Image(parse.getPagina(i));
                                        page.setImageWidth(Double.valueOf(image.getWidth()).intValue());
                                        page.setImageHeight(Double.valueOf(image.getHeight()).intValue());
                                    } catch (IOException e) {
                                        LOGGER.error(e.getMessage(), e);
                                    }
                                }

                                if (page.getImageWidth() != null && page.getImageHeight() != null && page.getImageHeight() > 0)
                                    if ((page.getImageWidth() / page.getImageHeight()) > 0.9)
                                        page.setDoublePage(true);
                            }
                            index++;
                        }
                    }

                } finally {
                    Util.destroiParse(parse);
                }

                try {
                    Marshaller marshaller = JAXBC.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    FileOutputStream out = new FileOutputStream(info);
                    marshaller.marshal(comic, out);
                    out.close();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return;
                }

                insereInfo(arquivo, info);

            } finally {
                if (info != null)
                    info.delete();
            }
        }
    }

    private static File extraiInfo(File arquivo, Boolean silent) {
        File comicInfo = null;
        Process proc = null;

        String comando = "cmd.exe /C cd \"" + WINRAR + "\" &&rar e -y " + '"' + arquivo.getPath() + '"' + " " + '"' + Util.getCaminho(arquivo.getPath()) + '"' + " " + '"' + COMICINFO + '"';

        if (!silent)
            LOGGER.info("rar e -y " + '"' + arquivo.getPath() + '"' + " " + '"' + Util.getCaminho(arquivo.getPath()) + '"' + " " + '"' + COMICINFO + '"');

        try {
            Runtime rt = Runtime.getRuntime();
            proc = rt.exec(comando);

            if (!silent)
                LOGGER.info("Resultado: " + proc.waitFor());

            String resultado = "";

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null)
                resultado += s + "\n";

            if (!silent && !resultado.isEmpty())
                LOGGER.info("Output comand:\n" + resultado);

            s = null;
            String error = "";
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            while ((s = stdError.readLine()) != null)
                error += s + "\n";

            if (resultado.isEmpty() && !error.isEmpty()) {
                LOGGER.info( "Error comand:\n" + resultado + "\nNão foi possível extrair o arquivo " + COMICINFO + ".");
            } else
                comicInfo = new File(Util.getCaminho(arquivo.getPath()) + '\\' + COMICINFO);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (proc != null)
                proc.destroy();
        }

        return comicInfo;
    }

    private static void insereInfo(File arquivo, File info) {
        String comando = "cmd.exe /C cd \"" + WINRAR + "\" &&rar a -ep " + '"' + arquivo.getPath() + '"' + " " + '"'
                + info.getPath() + '"';

        LOGGER.info("rar a -ep " + '"' + arquivo.getPath() + '"' + " " + '"' + info.getPath() + '"');

        Process proc = null;
        try {
            Runtime rt = Runtime.getRuntime();
            proc = rt.exec(comando);
            LOGGER.info("Resultado: " + proc.waitFor());

            String resultado = "";

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null)
                resultado += s + "\n";

            if (!resultado.isEmpty())
                LOGGER.info("Output comand:\n" + resultado);

            s = null;
            String error = "";
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            while ((s = stdError.readLine()) != null)
                error += s + "\n";

            if (resultado.isEmpty() && !error.isEmpty()) {
                info.renameTo(new File(arquivo.getPath() + Util.getNome(arquivo.getName()) + Util.getExtenssao(info.getName())));
                LOGGER.info("Error comand:\n" + resultado + "\nNecessário adicionar o rar no path e reiniciar a aplicação.");
            } else
                info.delete();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (proc != null)
                proc.destroy();
        }
    }

    private static String valida(Language linguagem, File arquivo) {
        String valida = "";
        if (arquivo.getName().toLowerCase().matches(PATTERN)) {
            File info = null;
            try {
                info = extraiInfo(arquivo, true);

                if (info == null || !info.exists())
                    return "Comic info não encontrado";

                ComicInfo comic;
                try {
                    Unmarshaller unmarshaller = JAXBC.createUnmarshaller();
                    comic = (ComicInfo) unmarshaller.unmarshal(info);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    return "Não foi possível realizar a extração do Comic info.";
                }

                if (arquivo.getName().contains("-"))
                    comic.setComic(arquivo.getName().substring(0, arquivo.getName().lastIndexOf("-")).trim());
                else if (arquivo.getName().contains("."))
                    comic.setComic(arquivo.getName().substring(0, arquivo.getName().lastIndexOf(".")).trim());
                else
                    comic.setComic(arquivo.getName());

                try {
                    ComicInfo saved = SERVICE.select(comic.getComic(), comic.getLanguageISO());
                    if (saved == null || comic.getIdMal() == null) {
                        if (saved != null)
                            comic.setId(saved.getId());

                        comic.setIdMal(getIdMal(comic.getNotes()));
                        SERVICE.save(comic);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }

                if (comic.getManga() == null)
                    valida += "Tipo de manga ausente. \n";

                if (comic.getLanguageISO() == null || comic.getLanguageISO().isEmpty())
                    valida += "Linguagem ausente. \n";

                if (comic.getTitle() == null || comic.getTitle().toLowerCase().contains("vol.") || comic.getTitle().toLowerCase().contains("volume"))
                    valida += "Título ausente. \n";

                if (comic.getTitle() == null || comic.getTitle().toLowerCase().contains("vol.") || comic.getTitle().toLowerCase().contains("volume"))
                    valida += "Título ausente. \n";

                if (linguagem.equals(Language.PORTUGUESE)) {
                    if (comic.getTranslator() == null || comic.getTranslator().isEmpty())
                        valida += "Tradutor ausente. \n";

                    if (comic.getScanInformation() == null || comic.getScanInformation().isEmpty())
                        valida += "Informação da scan ausente. \n";
                }

                Boolean bookmarks = false;
                for (Pages page : comic.getPages()) {
                    if (page.getBookmark() != null && !page.getBookmark().isEmpty()) {
                        bookmarks = true;
                        break;
                    }
                }

                if (!bookmarks)
                    valida += "Bookmars ausentes. \n";

                Boolean images = true;
                for (Pages page : comic.getPages()) {
                    if (page.getImageWidth() == null || page.getImageHeight() == null) {
                        images = false;
                        break;
                    }
                }

                if (!images)
                    valida += "Tamanho de imagens ausentes. \n";

                if (comic.getYear() == null || comic.getYear() == 0)
                    valida += "Publicação: Ano ausente. \n";

                if (comic.getMonth() == null || comic.getMonth() == 0)
                    valida += "Publicação: Mês ausente. \n";

                if (comic.getDay() == null || comic.getDay() == 0)
                    valida += "Publicação: Dia ausente. \n";

                try {
                    LocalDate.of(comic.getYear(), comic.getMonth(), comic.getDay());
                } catch (Exception e) {
                    if (comic.getYear() != null && comic.getMonth() != null && comic.getDay() != null)
                        valida += "Publicação: Data inválida. (" + comic.getDay() + "/" + comic.getMonth() + "/" + comic.getYear() + "). \n";
                    else
                        valida += "Publicação: Data inválida. \n";
                }

                if (!valida.isEmpty())
                    valida = valida.substring(0, valida.length() - 2);
            } finally {
                if (info != null)
                    info.delete();
            }
        } else
            valida = "Nome inválido";

        return valida;
    }

    final private static String ARQUIVO_LOG = "Log.txt";

    private static void gravalog(String diretorio, String texto) {
        try {
            String arquivo = diretorio + '\\' + ARQUIVO_LOG;
            if (arquivo.contains("\\" + "\\"))
                arquivo = arquivo.replace("\\" + "\\", "\\");

            File log = new File(arquivo);
            if (!log.exists())
                log.createNewFile();

            FileWriter fw = new FileWriter(arquivo, true);
            fw.write(texto);
            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

}
