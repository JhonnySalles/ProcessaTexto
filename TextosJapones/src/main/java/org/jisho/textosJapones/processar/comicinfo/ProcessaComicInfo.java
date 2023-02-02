package org.jisho.textosJapones.processar.comicinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jisho.textosJapones.fileparse.Parse;
import org.jisho.textosJapones.fileparse.ParseFactory;
import org.jisho.textosJapones.model.entities.comicinfo.ComicInfo;
import org.jisho.textosJapones.model.entities.comicinfo.Pages;
import org.jisho.textosJapones.model.enums.Language;
import org.jisho.textosJapones.model.enums.comicinfo.ComicPageType;
import org.jisho.textosJapones.util.Util;
import org.jisho.textosJapones.util.configuration.Configuracao;

import dev.katsute.mal4j.MyAnimeList;
import javafx.util.Callback;

public class ProcessaComicInfo {

	private static String WINRAR;
	private static String PATTERN = ".*\\.(zip|cbz|rar|cbr|tar)$";
	private static String COMICINFO = "ComicInfo.xml";
	private static JAXBContext JAXBC = null;

	public static void processa(String winrar, Language linguagem, String path, Callback<Integer[], Boolean> callback) {
		WINRAR = winrar;
		
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
					processa(linguagem, arquivo);
					size[0]++;
					callback.call(size);
				}
			} else if (arquivos.isFile()) {
				size[0] = 0;
				size[1] = 1;
				callback.call(size);
				
				processa(linguagem, arquivos);
				
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

	private static dev.katsute.mal4j.manga.Manga MANGA = null;
	private static void processaMal(String nome, ComicInfo info) {
		try {
			if (MANGA == null || !nome.equalsIgnoreCase(MANGA.getTitle())) {
				MANGA = null;
				List<dev.katsute.mal4j.manga.Manga> search;
				int page = 0;
				do {  
					search = MAL.getManga().withQuery(nome).withLimit(250).withOffset(page).search();
					if (search != null && !search.isEmpty())
						for (dev.katsute.mal4j.manga.Manga item : search) {
							if (item.getType() == dev.katsute.mal4j.manga.property.MangaType.Manga && nome.equalsIgnoreCase(item.getTitle())) {
								MANGA = item;
								break;
							}
						}
					page++;
					
					if (page > 1)
						break;
				} while (MANGA == null && search != null && !search.isEmpty());  
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
					String title = "";
	
					if (MANGA.getAlternativeTitles().getJapanese() != null
							|| MANGA.getAlternativeTitles().getJapanese().isEmpty())
						title += MANGA.getAlternativeTitles().getJapanese() + "; ";
	
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
	
					info.setPublisher(publisher.substring(0, publisher.lastIndexOf("; ")));
				}
	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void processa(Language linguagem, File arquivo) {
		if (arquivo.getName().toLowerCase().matches(PATTERN)) {
			File info = null;
			try {
				info = extraiInfo(arquivo);
			
				if (info == null)
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
	
				comic.setManga(org.jisho.textosJapones.model.enums.comicinfo.Manga.Yes);
				comic.setLanguageISO(linguagem.getSigla());
	
				if (comic.getTitle().toLowerCase().contains("vol.") || comic.getTitle().toLowerCase().contains("volume"))
					comic.setTitle(comic.getSeries());
	
				processaMal(nome, comic);
	
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
	
								if (page.getImageWidth() > page.getImageHeight())
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
