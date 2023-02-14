package org.jisho.textosJapones.processar.comicinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jisho.textosJapones.controller.mangas.MangasComicInfoController;
import org.jisho.textosJapones.fileparse.Parse;
import org.jisho.textosJapones.fileparse.ParseFactory;
import org.jisho.textosJapones.model.entities.comicinfo.ComicInfo;
import org.jisho.textosJapones.model.entities.comicinfo.Pages;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.comicinfo.ComicPageType;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.configuration.Configuracao;

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

public class ProcessaComicInfo {

	private static String WINRAR;
	private static String PATTERN = ".*\\.(zip|cbz|rar|cbr|tar)$";
	private static String COMICINFO = "ComicInfo.xml";
	private static JAXBContext JAXBC = null;
	private static Boolean CANCELAR = false;
	private static MangasComicInfoController CONTROLLER;
	
	public static void setPai(MangasComicInfoController controller) {
		CONTROLLER = controller;
	}
	
	public static void cancelar() {
		CANCELAR = true;
	}

	public static void processa(String winrar, Language linguagem, String path, Callback<Integer[], Boolean> callback) {
		WINRAR = winrar;
		CANCELAR = false;
		
		Properties secret = Configuracao.loadSecrets();
		String clientId = secret.getProperty("my_anime_list_client_id");
		MAL = MyAnimeList.withClientID(clientId);

		File arquivos = new File(path);
		Integer[] size = new Integer[2];
		
		try {
			JAXBC = JAXBContext.newInstance(ComicInfo.class);
			
			if (arquivos.isDirectory()) {
				size[0] = 0;
				size[1] = arquivos.listFiles().length;
				callback.call(size);
				
				for (File arquivo : arquivos.listFiles()) {
					processa(linguagem, arquivo, null);
					size[0]++;
					callback.call(size);
					
					if (CANCELAR)
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
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (JAXBC != null)
				JAXBC = null;
		}
	}
	
	public static Boolean processa(String winrar, Language linguagem, String arquivo, Long idMal) {
		WINRAR = winrar;
		CANCELAR = false;
		
		Properties secret = Configuracao.loadSecrets();
		String clientId = secret.getProperty("my_anime_list_client_id");
		MAL = MyAnimeList.withClientID(clientId);

		File arquivos = new File(arquivo);
		
		if (!arquivos.exists())
			return false;

		try {
			if (JAXBC == null)
				JAXBC = JAXBContext.newInstance(ComicInfo.class);
			
			processa(linguagem, arquivos, idMal);
		} catch (JAXBException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (JAXBC != null)
				JAXBC = null;
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
		if (MAL == null) {
			Properties secret = Configuracao.loadSecrets();
			String clientId = secret.getProperty("my_anime_list_client_id");
			MAL = MyAnimeList.withClientID(clientId);
		}
		
		dev.katsute.mal4j.manga.Manga manga = MAL.getManga(idMal);
		
		if (manga != null) {
			registro.setNome(manga.getTitle());
			registro.setId(manga.getID());
			registro.setImagem(new ImageView(manga.getMainPicture().getMediumURL()));	
			return true;
		} else 
			return false;
	}

	private static String API_JIKAN_CHARACTER = "https://api.jikan.moe/v4/manga/%s/characters";
	private static String TITLE_PATERN = "[^\\w\\s]";
	private static String DESCRIPTION_MAL = "Tagged with MyAnimeList on ";
	private static dev.katsute.mal4j.manga.Manga MANGA = null;
	private static Pair<Long, String> MANGA_CHARACTER;
	private static void processaMal(String arquivo, String nome, ComicInfo info, Language linguagem, Long idMal) {
		try {
			Long id = idMal;
			
			if (id == null) {
				if (info.getNotes() != null) {
					if (info.getNotes().contains(";")) {
						for (String note : info.getNotes().split(";")) 
							if (note.toLowerCase().contains(DESCRIPTION_MAL.toLowerCase()))
								id = Long.valueOf(note.substring(note.indexOf("[Issue ID")).replace("[Issue ID", "").replace("]", "").trim());
					} else if (info.getNotes().toLowerCase().contains(DESCRIPTION_MAL.toLowerCase()))
						id = Long.valueOf(info.getNotes().substring(info.getNotes().indexOf("[Issue ID")).replace("[Issue ID", "").replace("]", "").trim());
				}
			}
			
			String title = nome.replaceAll(TITLE_PATERN, "").trim();
			
			if (MANGA == null || !title.equalsIgnoreCase(MANGA.getTitle().replaceAll(TITLE_PATERN, "").trim())) {
				MANGA = null;
				if (id != null)
					MANGA = MAL.getManga(id);
				else {
					List<dev.katsute.mal4j.manga.Manga> search;
					int max = 5;
					int page = 0;
					do {  
						System.out.println("Realizando a consulta " + page);
						search = MAL.getManga().withQuery(nome).withLimit(50).withOffset(page).search();
						if (search != null && !search.isEmpty())
							for (dev.katsute.mal4j.manga.Manga item : search) {
								System.out.println(item.getTitle());
								if (item.getType() == dev.katsute.mal4j.manga.property.MangaType.Manga && title.equalsIgnoreCase(item.getTitle().replaceAll(TITLE_PATERN, "").trim())) {
									System.out.println("Encontrado o manga " + item.getTitle());
									MANGA = item;
									break;
								}
							}
												
						if (page == 0 && MANGA == null) {
							if (search != null && !search.isEmpty()) {
								org.jisho.textosJapones.model.entities.comicinfo.MAL mal = new org.jisho.textosJapones.model.entities.comicinfo.MAL(arquivo, nome);
								for (dev.katsute.mal4j.manga.Manga item : search) {
									org.jisho.textosJapones.model.entities.comicinfo.MAL.Registro registro = mal.addRegistro(item.getTitle(), item.getID(), false);
									registro.setImagem(new ImageView(item.getMainPicture().getMediumURL()));
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
				for (dev.katsute.mal4j.manga.property.Author author : MANGA.getAuthors()) {
					if (author.getRole().toLowerCase().equals("art")) {
						if (info.getPenciller() == null || info.getPenciller().isEmpty())
							info.setPenciller((author.getFirstName() + " " + author.getLastName()).trim());
	
						if (info.getInker() == null || info.getInker().isEmpty())
							info.setInker((author.getFirstName() + " " + author.getLastName()).trim());
	
						if (info.getCoverArtist() == null || info.getCoverArtist().isEmpty())
							info.setCoverArtist((author.getFirstName() + " " + author.getLastName()).trim());
					} else if (author.getRole().toLowerCase().equals("story")) {
						if (info.getWriter() == null || info.getWriter().isEmpty())
							info.setWriter((author.getFirstName() + " " + author.getLastName()).trim());
					} else  {
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
	
				if (info.getAlternateSeries() == null || info.getAlternateSeries().isEmpty()) {
					title = "";
					
					if (MANGA.getAlternativeTitles().getJapanese() != null
							|| MANGA.getAlternativeTitles().getJapanese().isEmpty()) {
						if (linguagem.equals(Language.JAPANESE))
							info.setTitle(MANGA.getAlternativeTitles().getJapanese());
						
						title += MANGA.getAlternativeTitles().getJapanese() + "; ";
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
				
				DateTimeFormatter dateTime = DateTimeFormatter.ofPattern("yyy-MM-dd HH:mm:ss");
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
					            .send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
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
					        		character = character.replace(",","");
					        	else if (character.contains(","))
					        		character = character.replace(","," ");
					        	
					        	characters += character + (obj.get("role").getAsString().toLowerCase().equals("main") ? " (" + obj.get("role").getAsString() + "), " : ", ");
					        }
					        
					        if (!characters.isEmpty()) {
					        	info.setCharacters(characters.substring(0, characters.lastIndexOf(", ")) + ".");
					        	MANGA_CHARACTER = new Pair<Long, String>(MANGA.getID(), info.getCharacters());
					        }
					    }
					}  catch (Exception e) {
						e.printStackTrace();
						System.out.println(MANGA.getID());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processa(Language linguagem, File arquivo, Long idMal) {
		if (arquivo.getName().toLowerCase().matches(PATTERN)) {
			File info = null;
			try {
				info = extraiInfo(arquivo);
			
				if (info == null || !info.exists())
					return;

				ComicInfo comic;
				try {
			        Unmarshaller unmarshaller = JAXBC.createUnmarshaller();
					comic = (ComicInfo) unmarshaller.unmarshal(info);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				
				String nome = getNome(arquivo.getName());
				System.out.println("Processando o manga " + nome);
	
				comic.setManga(org.jisho.textosJapones.model.enums.comicinfo.Manga.Yes);
				comic.setLanguageISO(linguagem.getSigla());
	
				if (comic.getTitle() == null || comic.getTitle().toLowerCase().contains("vol.") || comic.getTitle().toLowerCase().contains("volume"))
					comic.setTitle(comic.getSeries());
				else if (comic.getTitle() != null && !comic.getTitle().equalsIgnoreCase(comic.getSeries()))
					comic.setStoryArc(comic.getTitle());
	
				processaMal(arquivo.getAbsolutePath(), nome, comic, linguagem, idMal);
	
				Parse parse = null;
				try {
					parse = ParseFactory.create(arquivo);
	
					if (linguagem.equals(Language.PORTUGUESE)) {
						String tradutor = "";
						for (String pasta : parse.getPastas().keySet()) {
							String item = "";
	
							if (pasta.contains("]"))
								item = pasta.substring(0, pasta.indexOf("]")).replace("[", "");
	
							if (!item.isEmpty() && !tradutor.contains(pasta))
								tradutor += "; " + item;
						}
	
						if (!tradutor.isEmpty()) {
							comic.setTranslator(tradutor.substring(0, tradutor.length() - 2));
							comic.setScanInformation(tradutor.substring(0, tradutor.length() - 2));
						}
					}
	
					Map<String, Integer> pastas = parse.getPastas();
					int index = 0;
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
								page.setBookmark("al cover");
								page.setDoublePage(true);
								page.setType(ComicPageType.Other);
							} else if (imagem.contains("zsumário") || imagem.contains("zsumario")) {
								page.setBookmark("sumary");
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
											
											if (!capitulo.isEmpty())
												break;
										}
									}
									if (!capitulo.isEmpty())
										page.setBookmark(capitulo);
								}
								
								if (page.getImageWidth() == null || page.getImageHeight() == null) {
									try {
										Image image = new Image(parse.getPagina(i));
										page.setImageWidth(Double.valueOf(image.getWidth()).intValue());
										page.setImageHeight(Double.valueOf(image.getHeight()).intValue());
									} catch (IOException e) {
										e.printStackTrace();
									}	
								}
								
								if (page.getImageWidth() != null || page.getImageHeight() != null)
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
					e.printStackTrace();
					return;
				}
	
				insereInfo(arquivo, info);
			
			} finally {
				if (info != null)
					info.delete();
			}
		}
	}

	private static File extraiInfo(File arquivo) {
		File comicInfo = null;
		Process proc = null;
		
		String comando = "cmd.exe /C cd \"" + WINRAR + "\" &&rar e -y " + '"' + arquivo.getPath() + '"' + " " + '"' + Util.getCaminho(arquivo.getPath()) + '"' + " " + '"' + COMICINFO + '"';
		System.out.println("rar e -y " + '"' + arquivo.getPath() + '"' + " " + '"' + Util.getCaminho(arquivo.getPath()) + '"' + " " + '"' + COMICINFO + '"');
		
		try {
			Runtime rt = Runtime.getRuntime();
			proc = rt.exec(comando);
			
			System.out.println("Resultado: " + proc.waitFor());
			
			String resultado = "";

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String s = null;
			while ((s = stdInput.readLine()) != null)
				resultado += s + "\n";

			if (!resultado.isEmpty())
				System.out.println("Output comand:\n" + resultado);

			s = null;
			resultado = "";
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			while ((s = stdError.readLine()) != null)
				resultado += s + "\n";

			if (!resultado.isEmpty()) {
				System.out.println(
						"Error comand:\n" + resultado + "\nNão foi possível extrair o arquivo " + COMICINFO + ".");
			} else
				comicInfo = new File(Util.getCaminho(arquivo.getPath()) + '\\' + COMICINFO);
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			if (proc != null)
				proc.destroy();
		}

		return comicInfo;
	}

	private static void insereInfo(File arquivo, File info) {
		String comando = "cmd.exe /C cd \"" + WINRAR + "\" &&rar a -ep " + '"' + arquivo.getPath() + '"' + " " + '"'
				+ info.getPath() + '"';
		
		System.out.println("rar a -ep " + '"' + arquivo.getPath() + '"' + " " + '"' + info.getPath() + '"');
		
		Process proc = null;
		try {
			Runtime rt = Runtime.getRuntime();
			proc = rt.exec(comando);
			System.out.println("Resultado: " + proc.waitFor());

			String resultado = "";

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String s = null;
			while ((s = stdInput.readLine()) != null)
				resultado += s + "\n";

			if (!resultado.isEmpty())
				System.out.println("Output comand:\n" + resultado);

			s = null;
			resultado = "";
			BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

			while ((s = stdError.readLine()) != null)
				resultado += s + "\n";

			if (!resultado.isEmpty()) {
				info.renameTo(new File(
						arquivo.getPath() + Util.getNome(arquivo.getName()) + Util.getExtenssao(info.getName())));
				System.out.println("Error comand:\n" + resultado
						+ "\nNecessário adicionar o rar no path e reiniciar a aplicação.");
			} else
				info.delete();

		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		} finally {
			if (proc != null)
				proc.destroy();
		}
	}

}
