package org.jisho.textosJapones.model.enums;

public enum Tipo {
	TEXTO("Texto"), MUSICA("Música"), VOCABULARIO("Vocabulário");

	private String modo;

	Tipo(String modo) {
		this.modo = modo;
	}

	public String getDescricao() {
		return modo;
	}

	@Override
	public String toString() {
		return this.modo;
	}
}
