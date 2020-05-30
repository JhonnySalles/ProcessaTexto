package org.jisho.textosJapones.model.entities;

public class Estatistica {

	private String kanji;
	private String tipo;
	private String leitura;
	private Double quantidade;
	private Float percentual;
	private Double media;
	private Float percentMedia;
	private Integer corSequencial;
	private boolean gerar = false;

	public String getKanji() {
		return kanji;
	}

	public void setKanji(String kanji) {
		this.kanji = kanji;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getLeitura() {
		return leitura;
	}

	public void setLeitura(String leitura) {
		this.leitura = leitura;
	}

	public Double getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Double quantidade) {
		this.quantidade = quantidade;
	}

	public Float getPercentual() {
		return percentual;
	}

	public void setPercentual(Float percentual) {
		this.percentual = percentual;
	}

	public Double getMedia() {
		return media;
	}

	public void setMedia(Double media) {
		this.media = media;
	}

	public Float getPercentMedia() {
		return percentMedia;
	}

	public void setPercentMedia(Float percentMedia) {
		this.percentMedia = percentMedia;
	}

	public Integer getCorSequencial() {
		return corSequencial;
	}

	public void setCorSequencial(Integer corSequencial) {
		this.corSequencial = corSequencial;
	}

	public boolean isGerar() {
		return gerar;
	}

	public void setGerar(boolean gerar) {
		this.gerar = gerar;
	}
	
	public Estatistica() {
		this.kanji = "";
		this.tipo = "";
		this.leitura = "";
		this.quantidade = 0.0d;
		this.percentual = 0.0f;
		this.media = 0.0d;
		this.percentMedia = 0.0f;
		this.corSequencial = 0;
	}

	public Estatistica(String tipo) {
		this.tipo = tipo;
	}

	public Estatistica(String kanji, String tipo, String leitura, Double quantidade, Float percentual, Double media,
			Float percentMedia, Integer corSequencial) {
		this.kanji = kanji;
		this.tipo = tipo;
		this.leitura = leitura;
		this.quantidade = quantidade;
		this.percentual = percentual;
		this.media = media;
		this.percentMedia = percentMedia;
		this.corSequencial = corSequencial;
	}

}
