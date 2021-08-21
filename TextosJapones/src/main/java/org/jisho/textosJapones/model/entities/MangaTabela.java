package org.jisho.textosJapones.model.entities;

public class MangaTabela {

	private String base;
	private String manga;
	private Integer volume;
	private Float capitulo;
	private boolean processar = false;

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getManga() {
		return manga;
	}

	public void setManga(String manga) {
		this.manga = manga;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public Float getCapitulo() {
		return capitulo;
	}

	public void setCapitulo(Float capitulo) {
		this.capitulo = capitulo;
	}

	public boolean isProcessar() {
		return processar;
	}

	public void setProcessar(boolean processar) {
		this.processar = processar;
	}
	
	public MangaTabela() {
		this.base = "";
		this.manga = "";
		this.volume = 0;
		this.capitulo = 0F;
	}

	public MangaTabela(String base, String manga, Integer volume, Float capitulo, boolean processar) {
		this.base = base;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.processar = processar;
	}
}
