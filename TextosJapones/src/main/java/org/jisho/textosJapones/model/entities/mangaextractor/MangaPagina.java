package org.jisho.textosJapones.model.entities.mangaextractor;

import com.google.gson.annotations.Expose;
import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.entities.VocabularioExterno;

import java.util.*;

public class MangaPagina extends Manga {

	private UUID id;
	@Expose
	private String nomePagina;
	@Expose
	private Integer numero;
	@Expose
	private String hash;
	@Expose
	private List<MangaTexto> textos;
	@Expose
	private Set<VocabularioExterno> vocabularios;
	@Expose
	private Integer sequencia;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
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

	public Integer getSequencia() {
		return sequencia;
	}

	public void setSequencia(Integer sequencia) {
		this.sequencia = sequencia;
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

	public Set<VocabularioExterno> getVocabularios() {
		return vocabularios;
	}

	public void setVocabularios(Set<VocabularioExterno> vocabularios) {
		this.vocabularios = vocabularios;
	}

	public String getDescricao() {
		return prefixo + " || " + String.format("%03f", capitulo) + " - (" + String.format("%03d", numero) + ") " + nomePagina;
	}

	public MangaPagina() {
		this.id = null;
		this.nomePagina = "";
		this.numero = 0;
		this.hash = "";
		this.vocabularios = new HashSet<>();
		this.textos = new ArrayList<>();
	}

	public MangaPagina(UUID id, String nomePagina, Integer numero, String hash) {
		super(null, null, null, numero, nomePagina);
		this.id = id;
		this.nomePagina = nomePagina;
		this.numero = numero;
		this.hash = hash;
		this.vocabularios = new HashSet<>();
		this.textos = new ArrayList<>();
	}

	public MangaPagina(UUID id, String nomePagina, Integer numero, String hash, List<MangaTexto> textos) {
		super(null, null, null, numero, nomePagina);
		this.id = id;
		this.nomePagina = nomePagina;
		this.numero = numero;
		this.hash = hash;
		this.vocabularios = new HashSet<>();
		this.textos = textos;
	}

	public MangaPagina(UUID id, String nomePagina, Integer numero, String hash, List<MangaTexto> textos, Set<VocabularioExterno> vocabularios) {
		super(null, null, null, numero, nomePagina);
		this.id = id;
		this.nomePagina = nomePagina;
		this.numero = numero;
		this.hash = hash;
		this.vocabularios = vocabularios;
		this.textos = textos;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		MangaPagina that = (MangaPagina) o;
		return Objects.equals(id, that.id) && Objects.equals(nomePagina, that.nomePagina) && Objects.equals(numero, that.numero) && Objects.equals(hash, that.hash);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, nomePagina, numero, hash);
	}

	@Override
	public String toString() {
		return "MangaPagina [id=" + id + ", nomePagina=" + nomePagina + ", numero=" + numero + ", hash=" + hash
				+ ", textos=" + textos + ", sequencia=" + sequencia + "]";
	}
}
