package org.jisho.textosJapones.model.entities.comicinfo;

import com.jfoenix.controls.JFXButton;

import javafx.scene.image.ImageView;

public class BaseLista {

	protected String descricao;
	protected String nome;
	protected Long id;
	protected String idVisual;
	protected JFXButton processar;
	protected JFXButton site;
	protected ImageView imagem;
	protected Boolean selecionado = false;
	protected Boolean marcado = false;
	

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getIdVisual() {
		return idVisual;
	}

	public Boolean isSelecionado() {
		return selecionado;
	}
	
	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
	}
	
	public Boolean isMarcado() {
		return marcado;
	}
	
	public void setMarcado(Boolean marcado) {
		this.marcado = marcado;
	}
	
	public JFXButton getProcessar() {
		return processar;
	}
	
	public JFXButton getSite() {
		return site;
	}
	
	public ImageView getImagem() {
		return imagem;
	}

	public void setImagem(ImageView imagem) {
		this.imagem = imagem;
	}

	public void setButton(JFXButton processar, JFXButton site) {
		this.processar = processar;
		this.site = site;
	}
	
	public BaseLista(String descricao, String nome, Long id, Boolean processar) {
		this.descricao = descricao;
		this.nome = nome;
		this.id = id;
		this.idVisual = id != null ? id.toString() : "";
		this.marcado = processar;
	}
}
