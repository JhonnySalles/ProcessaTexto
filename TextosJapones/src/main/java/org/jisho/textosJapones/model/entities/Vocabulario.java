package org.jisho.textosJapones.model.entities;

public class Vocabulario {

	private String vocabulario;
	private String formaBasica;
	private String leitura;
	private String traducao;

	public String getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}

	public String getFormaBasica() {
		return formaBasica;
	}

	public void setFormaBasica(String formaBasica) {
		this.formaBasica = formaBasica;
	}

	public String getLeitura() {
		return leitura;
	}

	public void setLeitura(String leitura) {
		this.leitura = leitura;
	}

	public String getTraducao() {
		return traducao;
	}

	public void setTraducao(String traducao) {
		this.traducao = traducao;
	}

	public Vocabulario() {
		this.vocabulario = "";
		this.formaBasica = "";
		this.leitura = "";
		this.traducao = "";
	}

	public Vocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
		this.formaBasica = "";
		this.leitura = "";
		this.traducao = "";
	}

	public Vocabulario(String vocabulario, String traducao) {
		this.vocabulario = vocabulario;
		this.traducao = traducao;
		this.leitura = "";
		this.formaBasica = "";
	}

	public Vocabulario(String vocabulario, String formaBasica, String leitura, String traducao) {
		this.vocabulario = vocabulario;
		this.formaBasica = formaBasica;
		this.leitura = leitura;
		this.traducao = traducao;
	}

}
