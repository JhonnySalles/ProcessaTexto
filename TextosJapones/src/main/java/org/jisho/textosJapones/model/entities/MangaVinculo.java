package org.jisho.textosJapones.model.entities;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class MangaVinculo {

	@Expose
	private MangaVolume manga;
	@Expose
	private List<MangaVolume> vinculos;

	public MangaVolume getManga() {
		return manga;
	}

	public void setManga(MangaVolume manga) {
		this.manga = manga;
	}

	public List<MangaVolume> getVinculos() {
		return vinculos;
	}

	public void setVinculos(List<MangaVolume> vinculos) {
		this.vinculos = vinculos;
	}

	public void addVinculo(MangaVolume vinculo) {
		this.vinculos.add(vinculo);
	}

	public MangaVinculo() {
		this.manga = null;
		this.vinculos = new ArrayList<MangaVolume>();
	}

	public MangaVinculo(MangaVolume manga, List<MangaVolume> vinculos) {
		this.manga = manga;
		this.manga.isVinculo = true;
		this.vinculos = vinculos;
	}
}
