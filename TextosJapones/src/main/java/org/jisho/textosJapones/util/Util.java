package org.jisho.textosJapones.util;

public class Util {

	public static String normalize(String texto) {
		String frase = texto.substring(0, 1).toUpperCase() + texto.substring(1).replaceAll("; ", ", ").concat(".");
		if (frase.contains(".."))
			frase = frase.replaceAll("\\.{2,}", ".");
		return texto;
	}
	
}
