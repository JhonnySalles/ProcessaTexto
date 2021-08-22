package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.List;

public class MangaPagina {

	private Long id;
	private String nome;
	private Integer numero;
	private String hash;
	private List<MangaTexto> textos;
	private String vocabulario;
	private Boolean processado;
	private Boolean processar;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
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

	public String getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}

	public Boolean getProcessado() {
		return processado;
	}

	public void setProcessado(Boolean processado) {
		this.processado = processado;
	}

	public Boolean isProcessar() {
		return processar;
	}

	public void setProcessar(Boolean processar) {
		this.processar = processar;
	}

	public MangaPagina() {
		this.id = 0L;
		this.nome = "";
		this.numero = 0;
		this.hash = "";
		this.vocabulario = "";
		this.processado = false;
		this.processar = true;
		this.textos = new ArrayList<MangaTexto>();
	}

	public MangaPagina(Long id, String nome, Integer numero, String hash, Boolean processado) {
		this.id = id;
		this.nome = nome;
		this.numero = numero;
		this.hash = hash;
		this.vocabulario = "";
		this.processado = processado;
		this.processar = true;
		this.textos = new ArrayList<MangaTexto>();
	}

	public MangaPagina(Long id, String nome, Integer numero, String hash, Boolean processado, List<MangaTexto> textos) {
		this.id = id;
		this.nome = nome;
		this.numero = numero;
		this.hash = hash;
		this.vocabulario = "";
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
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
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
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		if (numero == null) {
			if (other.numero != null)
				return false;
		} else if (!numero.equals(other.numero))
			return false;
		return true;
	}

}
