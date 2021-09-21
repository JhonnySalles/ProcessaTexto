package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.annotations.Expose;

public class MangaPagina extends Manga {

	private Long id;
	@Expose
	private String nomePagina;
	@Expose
	private Integer numero;
	@Expose
	private String hash;
	@Expose
	private List<MangaTexto> textos;
	@Expose
	private Set<MangaVocabulario> vocabulario;
	private Boolean processado;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomePagina() {
		return nomePagina;
	}

	public void setNomePagina(String nomePagina) {
		this.nomePagina = nomePagina;
	}

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

	public Set<MangaVocabulario> getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(Set<MangaVocabulario> vocabulario) {
		this.vocabulario = vocabulario;
	}
	
	public void addVocabulario(MangaVocabulario vocabulario) {
		this.vocabulario.add(vocabulario);
	}

	public Boolean getProcessado() {
		return processado;
	}

	public void setProcessado(Boolean processado) {
		this.processado = processado;
	}

	public MangaPagina() {
		this.id = 0L;
		this.nomePagina = "";
		this.numero = 0;
		this.hash = "";
		this.processado = false;
		this.processar = true;
		this.vocabulario = new HashSet<MangaVocabulario>();
		this.textos = new ArrayList<MangaTexto>();
	}

	public MangaPagina(Long id, String nomePagina, Integer numero, String hash, Boolean processado) {
		this.id = id;
		this.nomePagina = nomePagina;
		this.numero = numero;
		this.hash = hash;
		this.vocabulario = new HashSet<MangaVocabulario>();;
		this.processado = processado;
		this.processar = true;
		this.textos = new ArrayList<MangaTexto>();
	}

	public MangaPagina(Long id, String nomePagina, Integer numero, String hash, Boolean processado, List<MangaTexto> textos) {
		this.id = id;
		this.nomePagina = nomePagina;
		this.numero = numero;
		this.hash = hash;
		this.vocabulario = new HashSet<MangaVocabulario>();;
		this.processado = processado;
		this.textos = textos;
		this.processar = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hash == null) ? 0 : hash.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((nomePagina == null) ? 0 : nomePagina.hashCode());
		result = prime * result + ((numero == null) ? 0 : numero.hashCode());
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
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (nomePagina == null) {
			if (other.nomePagina != null)
				return false;
		} else if (!nomePagina.equals(other.nomePagina))
			return false;
		if (numero == null) {
			if (other.numero != null)
				return false;
		} else if (!numero.equals(other.numero))
			return false;
		return true;
	}

}
