package org.jisho.textosJapones.model.entities;

public class Vocabulario {

	private String vocabulario;
	private String formaBasica;
	private String leitura;
	private String ingles;
	private String portugues;

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
	
	public String getIngles() {
		return ingles;
	}

	public void setIngles(String ingles) {
		this.ingles = ingles;
	}

	public String getPortugues() {
		return portugues;
	}

	public void setPortugues(String portugues) {
		this.portugues = portugues;
	}

	public Vocabulario() {
		this.vocabulario = "";
		this.formaBasica = "";
		this.leitura = "";
		this.portugues = "";
		this.ingles = "";
	}

	public Vocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
		this.formaBasica = "";
		this.leitura = "";
		this.portugues = "";
		this.ingles = "";
	}

	public Vocabulario(String vocabulario, String portugues) {
		this.vocabulario = vocabulario;
		this.portugues = portugues;
		this.leitura = "";
		this.formaBasica = "";
		this.ingles = "";
	}
	
	public Vocabulario(String vocabulario, String formaBasica, String leitura) {
		this.vocabulario = vocabulario;
		this.formaBasica = formaBasica;
		this.leitura = leitura;
		this.portugues = "";
		this.ingles = "";
	}

	public Vocabulario(String vocabulario, String formaBasica, String leitura, String ingles, String portugues) {
		this.vocabulario = vocabulario;
		this.formaBasica = formaBasica;
		this.leitura = leitura;
		this.portugues = portugues;
		this.ingles = ingles;
	}

	@Override
	public String toString() {
		return vocabulario + ", ";
	}
}
