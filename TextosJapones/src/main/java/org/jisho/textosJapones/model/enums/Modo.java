package org.jisho.textosJapones.model.enums;

public enum Modo {
	A("A - curto"), B("B - médio"), C("C - longo");

	private final String modo;

	Modo(String modo) {
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
