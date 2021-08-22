package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.List;

import org.jisho.textosJapones.model.enums.Language;

public class MangaVolume {

	private Long id;
	private String manga;
	private Integer volume;
	private Language lingua;
	private List<MangaCapitulo> capitulos;
	private String vocabulario;
	private Boolean processado;
	private Boolean processar;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getManga() {
		return manga;
	}

	public void setManga(String manga) {
		this.manga = manga;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public Language getLingua() {
		return lingua;
	}

	public void setLingua(Language lingua) {
		this.lingua = lingua;
	}

	public List<MangaCapitulo> getCapitulos() {
		return capitulos;
	}

	public void setCapitulos(List<MangaCapitulo> capitulos) {
		this.capitulos = capitulos;
	}

	public void addCapitulos(MangaCapitulo capitulo) {
		this.capitulos.add(capitulo);
	}

	public String getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}

	public Boolean getProcessado() {
		return processado;
	}

	public void setProcessado(Boolean processado) {
		this.processado = processado;
	}
	
	public Boolean isProcessar() {
		return processar;
	}

	public void setProcessar(Boolean processar) {
		this.processar = processar;
	}

	public MangaVolume() {
		this.id = 0L;
		this.manga = "";
		this.volume = 0;
		this.lingua = Language.PORTUGUESE;
		this.vocabulario = "";
		this.processado = false;
		this.processar = true;
		this.capitulos = new ArrayList<MangaCapitulo>();
	}

	public MangaVolume(Long id, String manga, Integer volume, Language lingua, String vocabulario, Boolean processado) {
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.vocabulario = vocabulario;
		this.processado = processado;
		this.processar = true;
		this.capitulos = new ArrayList<MangaCapitulo>();
	}

	public MangaVolume(Long id, String manga, Integer volume, Language lingua, String vocabulario, Boolean processado,
			List<MangaCapitulo> capitulos) {
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.capitulos = capitulos;
		this.vocabulario = vocabulario;
		this.processado = processado;
		this.processar = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lingua == null) ? 0 : lingua.hashCode());
		result = prime * result + ((manga == null) ? 0 : manga.hashCode());
		result = prime * result + ((volume == null) ? 0 : volume.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MangaVolume other = (MangaVolume) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lingua != other.lingua)
			return false;
		if (manga == null) {
			if (other.manga != null)
				return false;
		} else if (!manga.equals(other.manga))
			return false;
		if (volume == null) {
			if (other.volume != null)
				return false;
		} else if (!volume.equals(other.volume))
			return false;
		return true;
	}

}
