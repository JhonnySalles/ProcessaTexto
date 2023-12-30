package org.jisho.textosJapones.model.entities;

import com.google.gson.annotations.Expose;

import java.util.UUID;

public class VocabularioExterno extends Vocabulario {

	@Expose
	protected String palavra;
	@Expose
	protected Boolean revisado;

	public String getPalavra() {
		return palavra;
	}

	public void setPalavra(String palavra) {
		this.palavra = palavra;
		this.vocabulario = palavra;
	}

	public Boolean getRevisado() {
		return revisado;
	}

	public void setRevisado(Boolean revisado) {
		this.revisado = revisado;
	}

	public VocabularioExterno() {
		super();
		this.palavra = "";
	}

	public VocabularioExterno(String palavra, String portugues, String ingles, String leitura) {
		super(null, palavra, "", leitura, "", ingles, portugues);
		this.palavra = palavra;
		this.revisado = true;
	}

	public VocabularioExterno(UUID id, String palavra, String portugues, Boolean revisado) {
		super(id, palavra, portugues);
		this.palavra = palavra;
		this.revisado = revisado;
	}

	public VocabularioExterno(UUID id, String palavra, String portugues, String ingles, String leitura, Boolean revisado) {
		super(id, palavra, "", leitura, "", ingles, portugues);
		this.palavra = palavra;
		this.revisado = revisado;
	}

	//Japones
	public VocabularioExterno(UUID id, String palavra, String portugues, String ingles, String leitura, String leituraNovel, Boolean revisado) {
		super(id, palavra, "", leitura, leituraNovel, ingles, portugues);
		this.palavra = palavra;
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
		VocabularioExterno other = (VocabularioExterno) obj;
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
			return other.portugues == null;
		} else return portugues.equals(other.portugues);
	}
}
