package org.jisho.textosJapones.model.entities.comicinfo;

import com.jfoenix.controls.JFXButton;

import javafx.scene.image.ImageView;

public class BaseLista {

	private String descricao;
	private String nome;
	private Long id;
	private Boolean selecionado;
	private JFXButton processar;
	private JFXButton site;
	private ImageView imagem;
	

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

	public Boolean isSelecionado() {
		return selecionado;
	}
	
	public void setSelecionado(Boolean selecionado) {
		this.selecionado = selecionado;
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
		this.selecionado = processar;
	}
}
