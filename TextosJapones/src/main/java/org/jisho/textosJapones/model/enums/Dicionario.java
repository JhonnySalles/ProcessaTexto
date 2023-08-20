package org.jisho.textosJapones.model.enums;

public enum Dicionario {
	SAMLL("Sudachi - small"), CORE("Sudachi - core"), FULL("Sudachi - full");

	private final String dicionario;

	Dicionario(String dicionario) {
		this.dicionario = dicionario;
	}

	public String getDescricao() {
		return dicionario;
	}

	@Override
	public String toString() {
		return this.dicionario;
	}
}
