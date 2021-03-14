package org.jisho.textosJapones.model.enums;

public enum Api {
	
	CONTA_PRINCIPAL("Google - Conta principal"), CONTA_SECUNDARIA("Google - Conta secundária"), CONTA_MIGRACAO_1("Google - Migracao 1"),
	CONTA_MIGRACAO_2("Google - Migracao 2"), CONTA_MIGRACAO_3("Google - Migracao 3"), CONTA_MIGRACAO_4("Google - Migracao 4"),
	API_GOOGLE("Api Google");

	private String api;

	Api(String api) {
		this.api = api;
	}

	public String getDescricao() {
		return api;
	}
	
	// Necessário para que a escrita do combo seja Ativo e não ATIVO (nome do enum)
	@Override
	public String toString() {
		return this.api;
	}


}
