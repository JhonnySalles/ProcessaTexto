package org.jisho.textosJapones.model.entities.comicinfo;

import com.jfoenix.controls.JFXButton;

public class BaseLista {

	private String descricao;
	private String nome;
	private Long id;
	private Boolean processar;
	private JFXButton button;

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

	public Boolean isProcessar() {
		return processar;
	}
	
	public void setProcessar(Boolean processar) {
		this.processar = processar;
	}
	
	public JFXButton getButton() {
		return button;
	}

	public void setButton(JFXButton button) {
		this.button = button;
	}

	public BaseLista(String descricao, String nome, Long id, Boolean processar) {
		this.descricao = descricao;
		this.nome = nome;
		this.id = id;
		this.processar = processar;
	}
}
