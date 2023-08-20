package org.jisho.textosJapones.model.entities.comicinfo;

import java.util.ArrayList;
import java.util.List;

public class MAL extends BaseLista {

	private String arquivo;
	
	private List<Registro> myanimelist;
		
	public class Registro extends BaseLista {
		private String nome;
		private Long id;
		private final MAL parent;
		
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
			this.idVisual = id != null ? id.toString() : "";
		}
		
		public MAL getParent() {
			return parent;
		}
		
		public Registro(MAL parent, String nome, Long id, Boolean processar) {
			super("", nome, id, processar);
			this.parent = parent;
			this.nome = nome;
			this.id = id;
		}
	}

	public String getArquivo() {
		return arquivo;
	}

	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
	}

	public List<Registro> getMyanimelist() {
		return myanimelist;
	}

	public void setMyanimelist(List<Registro> myanimelist) {
		this.myanimelist = myanimelist;
	}
	
	public Registro addRegistro(String nome, Long id, Boolean processar) {
		Registro item = new Registro(this, nome, id, processar);
		myanimelist.add(item);
		return item;
	}

	public MAL(String arquivo, String nome, List<Registro> myanimelist) {
		super(arquivo, nome, null, false);
		this.arquivo = arquivo;
		this.myanimelist = myanimelist;
	}
	
	public MAL(String arquivo, String nome) {
		super(arquivo, nome, null, false);
		this.arquivo = arquivo;
		this.myanimelist = new ArrayList<Registro>();
	}
}
