package org.jisho.textosJapones.model.entities;

public class FilaSQL {

	private Long id;
	private String select;
	private String update;
	private String delete;
	private String vocabulario;

	private Boolean isExporta;
	private Boolean isLimpeza;

	public Long getId() {
		return id;
	}

	public String getSelect() {
		return select;
	}

	public String getUpdate() {
		return update;
	}

	public String getDelete() {
		return delete;
	}

	public String getVocabulario() {
		return vocabulario;
	}

	public Boolean isExporta() {
		return isExporta;
	}

	public void setExporta(Boolean exporta) {
		isExporta = exporta;
	}

	public Boolean isLimpeza() {
		return isLimpeza;
	}

	public void setLimpeza(Boolean limpeza) {
		isLimpeza = limpeza;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}

	public FilaSQL(String select, String update, String delete, Boolean isExporta, Boolean isLimpeza) {
		this.id = 0L;
		this.select = select;
		this.update = update;
		this.delete = delete;
		this.isExporta = isExporta;
		this.isLimpeza = isLimpeza;
		this.vocabulario = "";
	}

	public FilaSQL(Long id, String select, String update, String delete, String vocabulario, Boolean isExporta, Boolean isLimpeza) {
		this.id = id;
		this.select = select;
		this.update = update;
		this.delete = delete;
		this.vocabulario = vocabulario;
		this.isExporta = isExporta;
		this.isLimpeza = isLimpeza;
	}

	@Override
	public String toString() {
		return "FilaSQL [id=" + id + ", select=" + select + ", update=" + update + ", delete=" + delete
				+ ", vocabulario=" + vocabulario + "]";
	}

}
