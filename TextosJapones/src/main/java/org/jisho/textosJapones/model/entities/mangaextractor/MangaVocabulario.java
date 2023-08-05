package org.jisho.textosJapones.model.entities.mangaextractor;

import com.google.gson.annotations.Expose;

import java.util.UUID;

public class MangaVocabulario {

	UUID id;
	@Expose
	String palavra;
	@Expose
	String portugues;
	@Expose
	String ingles;
	@Expose
	String leitura;
	@Expose
	Boolean revisado;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getPalavra() {
		return palavra;
	}

	public void setPalavra(String palavra) {
		this.palavra = palavra;
	}

	public String getPortugues() {
		return portugues;
	}

	public void setPortugues(String portugues) {
		this.portugues = portugues;
	}
	
	public String getIngles() {
		return ingles;
	}

	public void setIngles(String ingles) {
		this.ingles = ingles;
	}

	public String getLeitura() {
		return leitura;
	}

	public void setLeitura(String leitura) {
		this.leitura = leitura;
	}

	public Boolean getRevisado() {
		return revisado;
	}

	public void setRevisado(Boolean revisado) {
		this.revisado = revisado;
	}

	public MangaVocabulario() {
		this.id = null;
		this.palavra = "";
		this.portugues = "";
		this.ingles = "";
		this.leitura = "";
		this.revisado = true;
	}
	
	public MangaVocabulario(String palavra, String portugues) {
		this.id = null;
		this.palavra = palavra;
		this.portugues = portugues;
		this.ingles = "";
		this.leitura = "";
		this.revisado = true;
	}
	
	public MangaVocabulario(String palavra, String portugues, String ingles, String leitura) {
		this.id = null;
		this.palavra = palavra;
		this.portugues = portugues;
		this.ingles = ingles;
		this.leitura = leitura;
		this.revisado = true;
	}
	
	public MangaVocabulario(String palavra, String portugues, String ingles, String leitura, Boolean revisado) {
		this.id = null;
		this.palavra = palavra;
		this.portugues = portugues;
		this.ingles = ingles;
		this.leitura = leitura;
		this.revisado = revisado;
	}

	public MangaVocabulario(UUID id, String palavra, String portugues, String ingles, String leitura, Boolean revisado) {
		this.id = id;
		this.palavra = palavra;
		this.portugues = portugues;
		this.ingles = ingles;
		this.leitura = leitura;
		this.revisado = revisado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ingles == null) ? 0 : ingles.hashCode());
		result = prime * result + ((palavra == null) ? 0 : palavra.hashCode());
		result = prime * result + ((portugues == null) ? 0 : portugues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MangaVocabulario other = (MangaVocabulario) obj;
		if (ingles == null) {
			if (other.ingles != null)
				return false;
		} else if (!ingles.equals(other.ingles))
			return false;
		if (palavra == null) {
			if (other.palavra != null)
				return false;
		} else if (!palavra.equals(other.palavra))
			return false;
		if (portugues == null) {
			if (other.portugues != null)
				return false;
		} else if (!portugues.equals(other.portugues))
			return false;
		return true;
	}
}
