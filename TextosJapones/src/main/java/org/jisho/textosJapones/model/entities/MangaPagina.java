package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.List;

public class MangaPagina {

	private Integer numero;
	private String hash;
	private List<MangaTexto> textos;
	private String vocabulario;

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public List<MangaTexto> getTextos() {
		return textos;
	}

	public void setTextos(List<MangaTexto> textos) {
		this.textos = textos;
	}

	public void addTexto(MangaTexto texto) {
		this.textos.add(texto);
	}

	public String getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}

	public MangaPagina() {
		this.numero = 0;
		this.hash = "";
		this.vocabulario = "";
		this.textos = new ArrayList<MangaTexto>();
	}

	public MangaPagina(Integer numero, String hash, List<MangaTexto> textos) {
		this.numero = numero;
		this.hash = hash;
		this.textos = textos;
		this.vocabulario = "";
	}

	public MangaPagina(Integer numero, String hash, List<MangaTexto> textos, String vocabulario) {
		this.numero = numero;
		this.hash = hash;
		this.textos = textos;
		this.vocabulario = vocabulario;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
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
		MangaPagina other = (MangaPagina) obj;
		if (hash == null) {
			if (other.hash != null)
				return false;
		} else if (!hash.equals(other.hash))
			return false;
		return true;
	}

}
