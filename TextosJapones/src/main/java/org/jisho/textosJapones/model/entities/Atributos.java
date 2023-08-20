package org.jisho.textosJapones.model.entities;

public class Atributos {

	private final Boolean dupla;
	private final String md5;
	private final String pHash;

	public Boolean getDupla() {
		return dupla;
	}

	public String getMd5() {
		return md5;
	}

	public String getPHash() {
		return pHash;
	}

	public Atributos(Boolean dupla, String md5, String pHash) {
		this.dupla = dupla;
		this.md5 = md5;
		this.pHash = pHash;
	}

}
