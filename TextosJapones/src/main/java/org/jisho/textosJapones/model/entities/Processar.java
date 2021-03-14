package org.jisho.textosJapones.model.entities;

public class Processar {

	private String id;
	private String original;
	private String vocabulario;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}

	public Processar() {
		this.id = "";
		this.original = "";
		this.vocabulario = "";
	}

	public Processar(String id, String original) {
		this.id = id;
		this.original = original;
		this.vocabulario = "";
	}

	public Processar(String id, String original, String vocabulario) {
		this.id = id;
		this.original = original;
		this.vocabulario = vocabulario;
	}

	@Override
	public String toString() {
		return "Processar [id=" + id + ", original=" + original + ", vocabulario=" + vocabulario + "]";
	}

}
