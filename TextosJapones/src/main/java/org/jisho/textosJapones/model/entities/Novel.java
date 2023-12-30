package org.jisho.textosJapones.model.entities;

public class Novel {

	protected String base;
	protected String novel;
	protected Float volume;
	protected Float capitulo;
	protected String texto;
	protected Boolean processar;
	protected String linguagem;
	protected Boolean alterado;
	protected Boolean itemExcluido;
	protected String prefixo;
	protected String origem;

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getNovel() {
		return novel;
	}

	public void setNovel(String novel) {
		this.novel = novel;
	}

	public Float getVolume() {
		return volume;
	}

	public void setVolume(Float volume) {
		this.volume = volume;
	}

	public Float getCapitulo() {
		return capitulo;
	}

	public void setCapitulo(Float capitulo) {
		this.capitulo = capitulo;
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

	public Novel() {
		this.base = null;
		this.novel = null;
		this.volume = null;
		this.capitulo = null;
		this.processar = true;
		this.linguagem = null;
		this.alterado = false;
		this.itemExcluido = false;
		this.prefixo = null;
		this.origem = null;
	}

	public Novel(String base, String novel) {
		this.base = base;
		this.novel = novel;
		this.volume = null;
		this.capitulo = null;
		this.processar = true;
		this.linguagem = null;
		this.alterado = false;
		this.itemExcluido = false;
		this.prefixo = null;
		this.origem = null;
	}

	public Novel(String base, String novel, String linguagem) {
		this.base = base;
		this.novel = novel;
		this.volume = null;
		this.capitulo = null;
		this.linguagem = linguagem;
		this.processar = true;
		this.alterado = false;
		this.itemExcluido = false;
	}

	public Novel(String base, String novel, Float volume, Float capitulo) {
		this.base = base;
		this.novel = novel;
		this.volume = volume;
		this.capitulo = capitulo;
		this.linguagem = null;
		this.alterado = false;
		this.itemExcluido = false;
		this.prefixo = null;
		this.origem = null;
		this.processar = true;
	}

	public Novel(String novel, Float volume, Float capitulo) {
		this.base = null;
		this.novel = novel;
		this.volume = volume;
		this.capitulo = null;
		this.linguagem = null;
		this.alterado = false;
		this.processar = true;
		this.itemExcluido = false;
		this.prefixo = null;
		this.origem = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		result = prime * result + ((linguagem == null) ? 0 : linguagem.hashCode());
		result = prime * result + ((novel == null) ? 0 : novel.hashCode());
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
		Novel other = (Novel) obj;
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
		if (novel == null) {
            return other.novel == null;
		} else return novel.equals(other.novel);
    }
}
