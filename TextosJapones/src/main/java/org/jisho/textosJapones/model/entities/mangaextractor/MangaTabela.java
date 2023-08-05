package org.jisho.textosJapones.model.entities.mangaextractor;

import org.jisho.textosJapones.model.entities.Manga;

import java.util.ArrayList;
import java.util.List;

public class MangaTabela extends Manga {

	private String base;
	private List<MangaVolume> volumes;
	private List<MangaVinculo> vinculados;
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

	public List<MangaVinculo> getVinculados() {
		return vinculados;
	}

	public void setVinculados(List<MangaVinculo> vinculados) {
		this.vinculados = vinculados;
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
		this.vinculados = new ArrayList<MangaVinculo>();
	}

	public MangaTabela(String base, List<MangaVolume> volumes) {
		this.base = base;
		this.quantidade = 0;
		this.volumes = volumes;
		this.processar = true;
		this.vinculados = new ArrayList<MangaVinculo>();
	}

	public MangaTabela(String base, Integer quantidade, List<MangaVolume> volumes) {
		this.base = base;
		this.quantidade = quantidade;
		this.volumes = volumes;
		this.processar = true;
		this.vinculados = new ArrayList<MangaVinculo>();
	}

	public MangaTabela(String base, List<MangaVolume> volumes, List<MangaVinculo> vinculados) {
		this.base = base;
		this.quantidade = 0;
		this.processar = true;
		this.vinculados = vinculados;

		if (volumes == null)
			this.volumes = new ArrayList<MangaVolume>();
		else
			this.volumes = volumes;
	}

}
