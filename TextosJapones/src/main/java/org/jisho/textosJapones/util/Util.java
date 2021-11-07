package org.jisho.textosJapones.util;

import org.jisho.textosJapones.model.enums.Api;

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
}
