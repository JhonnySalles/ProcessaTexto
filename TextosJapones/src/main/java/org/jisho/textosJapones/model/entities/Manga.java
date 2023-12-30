package org.jisho.textosJapones.model.entities;

import org.jisho.textosJapones.model.enums.Language;

public class Manga {

	protected String base;
	protected String manga;
	protected Integer volume;
	protected Float capitulo;
	protected Integer pagina;
	protected String nomePagina;
	protected String texto;
	protected Boolean processar;
	protected String linguagem;
	protected Integer volumeDestino;
	protected Float capituloDestino;
	protected Boolean alterado;
	protected Boolean itemExcluido;
	protected String prefixo;
	protected String origem;
	public Boolean isVinculo = false;

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

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public String getLinguagem() {
		return linguagem;
	}

	public void setLinguagem(String linguagem) {
		this.linguagem = linguagem;
	}

	public Boolean isProcessar() {
		return processar;
	}

	public void setProcessar(Boolean processar) {
		this.processar = processar;
	}

	public Integer getVolumeDestino() {
		return volumeDestino;
	}

	public void setVolumeDestino(Integer volumeDestino) {
		this.volumeDestino = volumeDestino;
	}

	public Float getCapituloDestino() {
		return capituloDestino;
	}

	public void setCapituloDestino(Float capituloDestino) {
		this.capituloDestino = capituloDestino;
	}

	public Boolean isAlterado() {
		return alterado;
	}

	public void setAlterado(Boolean alterado) {
		this.alterado = alterado;
	}
	
	public Boolean isItemExcluido() {
		return itemExcluido;
	}

	public void setItemExcluido(Boolean itemExcluido) {
		this.itemExcluido = itemExcluido;
	}

	public String getPrefixo() {
		return prefixo;
	}

	public void setPrefixo(String prefixo) {
		this.prefixo = prefixo;
	}

	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}

	public void addOutrasInformacoes(String origem, String prefixo, Float capitulo) {
		this.prefixo = prefixo;
		this.origem = origem;
		this.capitulo = capitulo;
	}
	
	public void addOutrasInformacoes(String base, String manga, Integer volume, Float capitulo, Language lingua) {
		this.base = base;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.pagina = null;
		this.nomePagina = null;
		this.texto = null;
		this.processar = true;
		this.linguagem = lingua.getSigla().toUpperCase();
		this.capituloDestino = capitulo;
		this.volumeDestino = volume;
		this.alterado = false;
		this.itemExcluido = false;
		this.prefixo = null;
		this.origem = null;
	}

	public void addOutrasInformacoes(String base, String manga, Integer volume, Float capitulo, Language lingua,
			Integer pagina, String nomePagina, String texto) {
		this.base = base;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.pagina = pagina;
		this.nomePagina = nomePagina;
		this.texto = texto;
		this.processar = true;
		this.linguagem = lingua.getSigla().toUpperCase();
		this.capituloDestino = capitulo;
		this.volumeDestino = volume;
		this.alterado = false;
		this.itemExcluido = false;
		this.prefixo = null;
		this.origem =  null;
	}

	public Manga() {
		this.base = null;
		this.manga = null;
		this.volume = null;
		this.capitulo = null;
		this.pagina = null;
		this.nomePagina = null;
		this.processar = true;
		this.linguagem = null;
		this.volumeDestino = null;
		this.capituloDestino = null;
		this.alterado = false;
		this.itemExcluido = false;
		this.isVinculo = false;
		this.prefixo = null;
		this.origem = null;
	}

	public Manga(String base, String manga) {
		this.base = base;
		this.manga = manga;
		this.volume = null;
		this.capitulo = null;
		this.pagina = null;
		this.nomePagina = null;
		this.processar = true;
		this.linguagem = null;
		this.volumeDestino = null;
		this.capituloDestino = null;
		this.alterado = false;
		this.itemExcluido = false;
		this.isVinculo = false;
		this.prefixo = null;
		this.origem = null;
	}

	public Manga(String base, String manga, String linguagem) {
		this.base = base;
		this.manga = manga;
		this.volume = null;
		this.capitulo = null;
		this.pagina = null;
		this.nomePagina = null;
		this.linguagem = linguagem;
		this.processar = true;
		this.volumeDestino = null;
		this.capituloDestino = null;
		this.alterado = false;
		this.itemExcluido = false;
		this.isVinculo = false;
	}
	
	public Manga(String base, String manga, String linguagem, Boolean isVinculo) {
		this.base = base;
		this.manga = manga;
		this.volume = null;
		this.capitulo = null;
		this.pagina = null;
		this.nomePagina = null;
		this.linguagem = linguagem;
		this.processar = true;
		this.volumeDestino = null;
		this.capituloDestino = null;
		this.alterado = false;
		this.itemExcluido = false;
		this.isVinculo = isVinculo;
		this.prefixo = null;
		this.origem = null;
	}

	public Manga(String base, String manga, Integer volume, Float capitulo, Integer pagina, String nomePagina) {
		this.base = base;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.pagina = pagina;
		this.nomePagina = nomePagina;
		this.linguagem = null;
		this.volumeDestino = volume;
		this.capituloDestino = capitulo;
		this.alterado = false;
		this.itemExcluido = false;
		this.isVinculo = false;
		this.prefixo = null;
		this.origem = null;
		this.processar = true;
	}
	
	public Manga(String manga, Integer volume, Float capitulo, Integer pagina, String nomePagina) {
		this.base = null;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.pagina = pagina;
		this.nomePagina = nomePagina;
		this.linguagem = null;
		this.volumeDestino = volume;
		this.capituloDestino = capitulo;
		this.alterado = false;
		this.itemExcluido = false;
		this.isVinculo = false;
		this.prefixo = null;
		this.origem = null;
		this.processar = true;
	}
	
	public Manga(String manga, Integer volume, Float capitulo) {
		this.base = null;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = null;
		this.pagina = null;
		this.nomePagina = null;
		this.linguagem = null;
		this.volumeDestino = volume;
		this.capituloDestino = capitulo;
		this.alterado = false;
		this.itemExcluido = false;
		this.isVinculo = false;
		this.prefixo = null;
		this.origem = null;
		this.processar = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		result = prime * result + ((linguagem == null) ? 0 : linguagem.hashCode());
		result = prime * result + ((manga == null) ? 0 : manga.hashCode());
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
		Manga other = (Manga) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		if (linguagem == null) {
			if (other.linguagem != null)
				return false;
		} else if (!linguagem.equals(other.linguagem))
			return false;
		if (manga == null) {
            return other.manga == null;
		} else return manga.equals(other.manga);
    }
}
