package org.jisho.textosJapones.model.enums;

public enum Tela {
	
	TEXTO("Texto"), LEGENDA("Legenda"), MANGA("Manga");

	private String tela;

	Tela(String tela) {
		this.tela = tela;
	}

	public String getDescricao() {
		return tela;
	}
	
	// Necessário para que a escrita do combo seja Ativo e não ATIVO (nome do enum)
	@Override
	public String toString() {
		return this.tela;
	}


}
