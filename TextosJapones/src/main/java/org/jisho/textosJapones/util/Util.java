package org.jisho.textosJapones.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.jisho.textosJapones.model.enums.Api;
import org.jisho.textosJapones.parse.Parse;
import org.jisho.textosJapones.parse.ParseFactory;
import org.jisho.textosJapones.parse.RarParse;

public class Util {

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
			((RarParse) parse).setCacheDirectory(new File(PASTA_CACHE, arquivo.getName() + random.nextInt(1000)));

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
		InputStream input = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			image.transferTo(baos);
			input = new ByteArrayInputStream(baos.toByteArray());

			byte[] buffer = new byte[1024];
			MessageDigest digest = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while (numRead != -1) {
				numRead = input.read(buffer);
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
				input.close();
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
		if (path.contains("/"))
			name = path.substring(path.lastIndexOf("/"), path.length());
		else if (path.contains("\\"))
			name = path.substring(path.lastIndexOf("\\"), path.length());

		return name;
	}

	public static String getNomeSemExtenssao(String path) {
		String name = "";

		if (path.contains("/"))
			name = path.substring(path.lastIndexOf("/"), path.length());
		else if (path.contains("\\"))
			name = path.substring(path.lastIndexOf("\\"), path.length());

		if (name.contains("."))
			name.substring(0, path.lastIndexOf("."));

		return name;
	}

	public static String getExtenssao(String path) {
		if (path.contains("."))
			return path.substring(path.lastIndexOf("."), path.length());
		else
			return path;
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
			folder = folder.substring(folder.lastIndexOf("/"), folder.length());
		else if (folder.contains("\\"))
			folder = folder.substring(folder.lastIndexOf("\\"), folder.length());

		return folder;
	}
}
