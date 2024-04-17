package org.jisho.textosJapones.model.enums;

public enum Database {
	INGLES("INGLES"), JAPONES("JAPONES"), EXTERNO("EXTERNO");

	private final String modo;

	Database(String modo) {
		this.modo = modo;
	}

	@Override
	public String toString() {
		return this.modo;
	}
}
