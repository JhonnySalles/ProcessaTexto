package org.jisho.textosJapones.model.entities;

import org.jisho.textosJapones.model.enums.Language;

public class Manga {

	protected String base;
	protected String manga;
	protected Integer volume;
	protected Float capitulo;
	protected Integer pagina;
	protected String nomePagina;
	protected Boolean processar;

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

	public Integer getPagina() {
		return pagina;
	}

	public void setPagina(Integer pagina) {
		this.pagina = pagina;
	}

	public String getNomePagina() {
		return nomePagina;
	}

	public void setNomePagina(String nomePagina) {
		this.nomePagina = nomePagina;
	}

	public Boolean isProcessar() {
		return processar;
	}

	public void setProcessar(Boolean processar) {
		this.processar = processar;
	}

	public void addOutrasInformacoes(String base, String manga, Integer volume, Float capitulo, Language lingua) {
		this.base = base;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
	}

	public Manga() {
		this.base = "";
		this.manga = "";
		this.volume = null;
		this.capitulo = null;
		this.pagina = null;
		this.nomePagina = "";
		this.processar = true;
	}

	public Manga(String base, String manga) {
		this.base = base;
		this.manga = manga;
		this.volume = null;
		this.capitulo = null;
		this.pagina = null;
		this.nomePagina = "";
		this.processar = true;
	}

	public Manga(String base, String manga, Integer volume, Float capitulo, Integer pagina, String nomePagina) {
		this.base = base;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.pagina = pagina;
		this.nomePagina = nomePagina;
	}
}
