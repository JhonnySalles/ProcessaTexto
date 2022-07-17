package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jisho.textosJapones.model.enums.Language;

import com.google.gson.annotations.Expose;

public class MangaVolume extends Manga {

	private Long id;
	@Expose
	private String manga;
	@Expose
	private Integer volume;
	@Expose
	private Language lingua;
	@Expose
	private List<MangaCapitulo> capitulos;
	@Expose
	private Set<MangaVocabulario> vocabularios;
	@Expose
	private String arquivo;
	private Boolean processado;

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

	public String getArquivo() {
		return arquivo;
	}

	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
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

	public Set<MangaVocabulario> getVocabularios() {
		return vocabularios;
	}

	public void setVocabularios(Set<MangaVocabulario> vocabularios) {
		this.vocabularios = vocabularios;
	}

	public void addVocabulario(MangaVocabulario vocabulario) {
		this.vocabularios.add(vocabulario);
	}

	public Boolean getProcessado() {
		return processado;
	}

	public void setProcessado(Boolean processado) {
		this.processado = processado;
	}

	public MangaVolume() {
		super();
		this.id = 0L;
		this.manga = "";
		this.volume = 0;
		this.lingua = Language.PORTUGUESE;
		this.vocabularios = new HashSet<MangaVocabulario>();
		this.processado = false;
		this.processar = true;
		this.capitulos = new ArrayList<MangaCapitulo>();
		this.arquivo = "";
	}

	public MangaVolume(Long id, String manga, Integer volume, Language lingua, String arquivo) {
		super(manga, volume, null);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.arquivo = arquivo == null ? "" : arquivo;
		this.capitulos = new ArrayList<MangaCapitulo>();
		this.vocabularios = new HashSet<MangaVocabulario>();
		this.processado = false;
		this.processar = true;
	}

	public MangaVolume(Long id, String manga, Integer volume, Language lingua, String arquivo,
			List<MangaCapitulo> capitulos) {
		super(manga, volume, null);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.arquivo = arquivo == null ? "" : arquivo;
		this.capitulos = capitulos;
		this.vocabularios = new HashSet<MangaVocabulario>();
		this.processado = false;
		this.processar = true;
	}

	public MangaVolume(Long id, String manga, Integer volume, Language lingua, String arquivo,
			Set<MangaVocabulario> vocabularios) {
		super(manga, volume, null);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.vocabularios = vocabularios;
		this.arquivo = arquivo == null ? "" : arquivo;
		this.processado = false;
		this.processar = true;
		this.capitulos = new ArrayList<MangaCapitulo>();
	}

	public MangaVolume(Long id, String manga, Integer volume, Language lingua, String arquivo,
			Set<MangaVocabulario> vocabularios, List<MangaCapitulo> capitulos) {
		super(manga, volume, null);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.lingua = lingua;
		this.capitulos = capitulos;
		this.vocabularios = vocabularios;
		this.arquivo = arquivo == null ? "" : arquivo;
		this.processado = false;
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

	@Override
	public String toString() {
		return "MangaVolume [id=" + id + ", manga=" + manga + ", volume=" + volume + ", lingua=" + lingua
				+ ", vocabularios=" + vocabularios + ", arquivo=" + arquivo + "]";
	}
}
