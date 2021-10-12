package org.jisho.textosJapones.model.entities;

import com.google.gson.annotations.Expose;

public class MangaVocabulario {

	Long id;
	@Expose
	String palavra;
	@Expose
	String significado;
	@Expose
	String leitura;
	@Expose
	Boolean revisado;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPalavra() {
		return palavra;
	}

	public void setPalavra(String palavra) {
		this.palavra = palavra;
	}

	public String getSignificado() {
		return significado;
	}

	public void setSignificado(String significado) {
		this.significado = significado;
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
		this.id = 0L;
		this.palavra = "";
		this.significado = "";
		this.leitura = "";
		this.revisado = true;
	}
	
	public MangaVocabulario(String palavra, String significado, String leitura) {
		this.id = 0L;
		this.palavra = palavra;
		this.significado = significado;
		this.leitura = leitura;
		this.revisado = true;
	}
	
	public MangaVocabulario(String palavra, String significado,  String leitura, Boolean revisado) {
		this.id = 0L;
		this.palavra = palavra;
		this.significado = significado;
		this.leitura = leitura;
		this.revisado = revisado;
	}

	public MangaVocabulario(Long id, String palavra, String significado, String leitura, Boolean revisado) {
		this.id = id;
		this.palavra = palavra;
		this.significado = significado;
		this.leitura = leitura;
		this.revisado = revisado;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((palavra == null) ? 0 : palavra.hashCode());
		result = prime * result + ((significado == null) ? 0 : significado.hashCode());
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
		if (palavra == null) {
			if (other.palavra != null)
				return false;
		} else if (!palavra.equals(other.palavra))
			return false;
		if (significado == null) {
			if (other.significado != null)
				return false;
		} else if (!significado.equals(other.significado))
			return false;
		return true;
	}
}
