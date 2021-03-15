package org.jisho.textosJapones.model.entities;

public class FilaSQL {

	private Long id;
	private String select;
	private String update;
	private String vocabulario;

	public Long getId() {
		return id;
	}

	public String getSelect() {
		return select;
	}

	public String getUpdate() {
		return update;
	}

	public String getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}
	
	public FilaSQL(String select, String update) {
		this.id = 0L;
		this.select = select;
		this.update = update;
		this.vocabulario = "";
	}

	public FilaSQL(Long id, String select, String update, String vocabulario) {
		this.id = id;
		this.select = select;
		this.update = update;
		this.vocabulario = vocabulario;
	}

	@Override
	public String toString() {
		return "FilaSQL [id=" + id + ", select=" + select + ", update=" + update + ", vocabulario=" + vocabulario + "]";
	}

}
