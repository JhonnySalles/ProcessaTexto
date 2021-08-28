package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.List;

public class MangaTabela extends Manga {

	private String base;
	private List<MangaVolume> volumes;
	private Integer quantidade;

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public List<MangaVolume> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<MangaVolume> volumes) {
		this.volumes = volumes;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public MangaTabela() {
		this.base = "";
		this.quantidade = 0;
		this.processar = true;
		this.volumes = new ArrayList<MangaVolume>();

	}

	public MangaTabela(String base, List<MangaVolume> volumes) {
		this.base = base;
		this.quantidade = 0;
		this.volumes = volumes;
		this.processar = true;
	}

	public MangaTabela(String base, Integer quantidade, List<MangaVolume> volumes) {
		this.base = base;
		this.quantidade = quantidade;
		this.volumes = volumes;
		this.processar = true;
	}
}
