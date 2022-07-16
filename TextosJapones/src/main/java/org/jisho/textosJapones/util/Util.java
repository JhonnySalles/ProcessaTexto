package org.jisho.textosJapones.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jisho.textosJapones.fileparse.Parse;
import org.jisho.textosJapones.fileparse.ParseFactory;
import org.jisho.textosJapones.fileparse.RarParse;
import org.jisho.textosJapones.model.enums.Api;

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DataFormat;
import javafx.util.Pair;

public class Util {

	final public static DataFormat VINCULO_ITEM_FORMAT = new DataFormat("custom.item.vinculo");
	final public static DataFormat NUMERO_PAGINA_ITEM_FORMAT = new DataFormat("custom.item.numero.pagina");

	public static String normalize(String texto) {
		if (texto == null || texto.isEmpty())
			return "";

		String frase = texto.substring(0, 1).toUpperCase() + texto.substring(1).replaceAll("; ", ", ").concat(".");
		if (frase.contains(".."))
			frase = frase.replaceAll("\\.{2,}", ".");
		return frase;
	}

	public static Api next(Api api) {
		Api next = api;
		switch (next) {
		case CONTA_PRINCIPAL:
			next = Api.CONTA_SECUNDARIA;
			break;
		case CONTA_SECUNDARIA:
			next = Api.CONTA_MIGRACAO_1;
			break;
		case CONTA_MIGRACAO_1:
			next = Api.CONTA_MIGRACAO_2;
			break;
		case CONTA_MIGRACAO_2:
			next = Api.CONTA_MIGRACAO_3;
			break;
		case CONTA_MIGRACAO_3:
			next = Api.CONTA_MIGRACAO_4;
			break;
		default:
			next = Api.CONTA_PRINCIPAL;
			break;
		}
		return next;
	}

	private static String PASTA_CACHE = new File(".").getAbsolutePath() + "/cache/";
	private static Random random = new Random();

	public static Parse criaParse(File arquivo) {
		Parse parse = ParseFactory.create(arquivo);
		if (parse instanceof RarParse)
			((RarParse) parse).setCacheDirectory(
					new File(PASTA_CACHE, getNomeSemExtenssao(arquivo.getName()) + random.nextInt(1000)));

		return parse;
	}

	public static void destroiParse(Parse parse) {
		if (parse == null)
			return;

		if (parse instanceof RarParse)
			try {
				((RarParse) parse).destroi();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public static Boolean isJson(String filename) {
		return filename.toLowerCase().matches(".*\\.(json)$");
	}

	public static Boolean isImage(String filename) {
		return filename.toLowerCase().matches(".*\\.(jpg|jpeg|bmp|gif|png|webp)$");
	}

	public static Boolean isZip(String filename) {
		return filename.toLowerCase().matches(".*\\.(zip|cbz)$");
	}

	public static Boolean isRar(String filename) {
		return filename.toLowerCase().matches(".*\\.(rar|cbr)$");
	}

	public static Boolean isTarball(String filename) {
		return filename.toLowerCase().matches(".*\\.(cbt)$");
	}

	public static Boolean isSevenZ(String filename) {
		return filename.toLowerCase().matches(".*\\.(cb7|7z)$");
	}

	public static String MD5(String string) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(string.getBytes(), 0, string.length());
			return new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			return string.replace("/", ".");
		}
	}

	public static String MD5(InputStream image) {
		try {
			byte[] buffer = new byte[1024];
			MessageDigest digest = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while (numRead != -1) {
				numRead = image.read(buffer);
				if (numRead > 0)
					digest.update(buffer, 0, numRead);
			}

			byte[] md5Bytes = digest.digest();
			String md5 = "";

			for (int i = 0; i < md5Bytes.length; i++) {
				md5 += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
			}

			return md5;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				image.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public static byte[] toByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			byte[] b = new byte[4096];
			int n = 0;
			while ((n = is.read(b)) != -1) {
				output.write(b, 0, n);
			}
			return output.toByteArray();
		} finally {
			output.close();
		}
	}

	public static String getNome(String path) {
		String name = path;
		if (name.contains("/"))
			name = name.substring(name.lastIndexOf("/") + 1, name.length());
		else if (name.contains("\\"))
			name = name.substring(name.lastIndexOf("\\") + 1, name.length());

		return name;
	}

	public static String getNomeSemExtenssao(String path) {
		String name = path;

		if (name.contains("/"))
			name = name.substring(name.lastIndexOf("/") + 1, name.length());
		else if (name.contains("\\"))
			name = name.substring(name.lastIndexOf("\\") + 1, name.length());

		if (name.contains("."))
			name = name.substring(0, name.lastIndexOf("."));

		return name;
	}

	public static String getExtenssao(String path) {
		if (path.contains("."))
			return path.substring(path.lastIndexOf("."), path.length());
		else
			return path;
	}

	public static Pair<Float, Boolean> getCapitulo(String path) {
		Pair<Float, Boolean> capitulo = null;
		Float numero = -1f;
		Boolean extra = false;

		String pasta = Util.getPasta(path).toLowerCase();

		if (pasta.contains("capítulo"))
			numero = Float.valueOf(pasta.substring(pasta.indexOf("capítulo")).replaceAll("[^\\d.]", ""));
		else if (pasta.contains("capitulo"))
			numero = Float.valueOf(pasta.substring(pasta.indexOf("capitulo")).replaceAll("[^\\d.]", ""));
		else if (pasta.contains("extra")) {
			numero = Float.valueOf(pasta.substring(pasta.indexOf("extra")).replaceAll("[^\\d.]", ""));
			extra = true;
		}

		if (numero > -1)
			capitulo = new Pair<Float, Boolean>(numero, extra);

		return capitulo;
	}

	public static String getPasta(String path) {
		// Two validations are needed, because the rar file only has the base values,
		// with the beginning already in the folder when it exists
		String folder = path;

		if (folder.contains("/"))
			folder = folder.substring(0, folder.lastIndexOf("/"));
		else if (folder.contains("\\"))
			folder = folder.substring(0, folder.lastIndexOf("\\"));

		if (folder.contains("/"))
			folder = folder.substring(folder.lastIndexOf("/") + 1, folder.length());
		else if (folder.contains("\\"))
			folder = folder.substring(folder.lastIndexOf("\\") + 1, folder.length());

		return folder;
	}

	public static String getCaminho(String path) {
		String folder = path;

		if (folder.contains("/"))
			folder = folder.substring(0, folder.lastIndexOf("/"));
		else if (folder.contains("\\"))
			folder = folder.substring(0, folder.lastIndexOf("\\"));

		return folder;
	}

	public static WritableImage criaSnapshot(Node node) {
		SnapshotParameters snapshotParams = new SnapshotParameters();
		WritableImage image = node.snapshot(snapshotParams, null);
		return image;
	}

	public static Integer getFirstVisibleIndex(ListView<?> t) {
		try {
			ListViewSkin<?> ts = (ListViewSkin<?>) t.getSkin();
			VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
			Integer first = vf.getFirstVisibleCell().getIndex();
			// System.out.println("##### Scrolling last " + first);
			return first;
		} catch (Exception ex) {
			System.out.println("##### Scrolling: Exception " + ex);
			return null;
		}

	}

	public static Integer getLastVisibleIndex(ListView<?> t) {
		try {
			ListViewSkin<?> ts = (ListViewSkin<?>) t.getSkin();
			VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(0);
			Integer last = vf.getLastVisibleCell().getIndex();
			// System.out.println("##### Scrolling last " + last);
			return last;
		} catch (Exception ex) {
			System.out.println("##### Scrolling: Exception " + ex);
			return null;
		}
	}

	public static Integer getFirstVisibleIndex(TableView<?> t) {
		try {
			TableViewSkin<?> ts = (TableViewSkin<?>) t.getSkin();
			VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(1);
			Integer first = vf.getFirstVisibleCell().getIndex();
			// System.out.println("##### Scrolling last " + first);
			return first;
		} catch (Exception ex) {
			System.out.println("##### Scrolling: Exception " + ex);
			return null;
		}

	}

	public static Integer getLastVisibleIndex(TableView<?> t) {
		try {
			TableViewSkin<?> ts = (TableViewSkin<?>) t.getSkin();
			VirtualFlow<?> vf = (VirtualFlow<?>) ts.getChildren().get(1);
			Integer last = vf.getLastVisibleCell().getIndex();
			// System.out.println("##### Scrolling last " + last);
			return last;
		} catch (Exception ex) {
			System.out.println("##### Scrolling: Exception " + ex);
			return null;
		}
	}

	public static void getCapitulos(Parse parse, Map<String, Integer> lista, ListView<String> tabela) {
		Pair<Map<String, Integer>, List<String>> capitulos = getCapitulos(parse);

		lista.clear();
		lista.putAll(capitulos.getKey());
		tabela.setItems(FXCollections.observableArrayList(capitulos.getValue()));
	}

	public static Pair<Map<String, Integer>, List<String>> getCapitulos(Parse parse) {
		if (parse == null)
			return new Pair<Map<String, Integer>, List<String>>(new HashMap<String, Integer>(),
					new ArrayList<String>());
		Map<String, Integer> capitulos = parse.getPastas();
		List<String> descricao = new ArrayList<String>(capitulos.keySet());
		descricao.sort((a, b) -> a.compareToIgnoreCase(b));
		Pair<Map<String, Integer>, List<String>> itens = new Pair<Map<String, Integer>, List<String>>(capitulos,
				descricao);

		return itens;
	}

	public static Timestamp convertToTimeStamp(LocalDateTime ldt) {
		return Timestamp.valueOf(ldt);
	}

	public static LocalDateTime convertToDateTime(Timestamp ts) {
		return ts.toLocalDateTime();
	}
}
