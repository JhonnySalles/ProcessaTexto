package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.List;

import org.jisho.textosJapones.model.enums.Language;

public class MangaCapitulo {

	private Long id;
	private String manga;
	private Integer volume;
	private Float capitulo;
	private Language lingua;
	private String scan;
	private List<MangaPagina> paginas;
	private Boolean extra;
	private Boolean raw;
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

	public Float getCapitulo() {
		return capitulo;
	}

	public void setCapitulo(Float capitulo) {
		this.capitulo = capitulo;
	}

	public Language getLingua() {
		return lingua;
	}

	public void setLingua(Language lingua) {
		this.lingua = lingua;
	}

	public String getScan() {
		return scan;
	}

	public void setScan(String scan) {
		this.scan = scan;
	}

	public Boolean getExtra() {
		return extra;
	}

	public void setExtra(Boolean extra) {
		this.extra = extra;
	}

	public Boolean getRaw() {
		return raw;
	}

	public void setRaw(Boolean raw) {
		this.raw = raw;
	}

	public List<MangaPagina> getPaginas() {
		return paginas;
	}

	public void setPaginas(List<MangaPagina> paginas) {
		this.paginas = paginas;
	}

	public void addPaginas(MangaPagina pagina) {
		this.paginas.add(pagina);
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

	public MangaCapitulo() {
		this.id = 0L;
		this.manga = "";
		this.volume = 0;
		this.capitulo = 0F;
		this.lingua = Language.PORTUGUESE;
		this.scan = "";
		this.vocabulario = "";
		this.processado = false;
		this.processar = true;
		this.paginas = new ArrayList<MangaPagina>();
	}

	public MangaCapitulo(Long id, String manga, Integer volume, Float capitulo, Language lingua, String scan,
			String vocabulario, Boolean processado) {
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.lingua = lingua;
		this.scan = scan;
		this.vocabulario = vocabulario;
		this.processado = processado;
		this.processar = true;
		this.paginas = new ArrayList<MangaPagina>();
	}

	public MangaCapitulo(Long id, String manga, Integer volume, Float capitulo, Language lingua, String scan,
			String vocabulario, Boolean processado, List<MangaPagina> paginas) {
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.lingua = lingua;
		this.scan = scan;
		this.paginas = paginas;
		this.vocabulario = vocabulario;
		this.processado = processado;
		this.processar = true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((capitulo == null) ? 0 : capitulo.hashCode());
		result = prime * result + ((extra == null) ? 0 : extra.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lingua == null) ? 0 : lingua.hashCode());
		result = prime * result + ((manga == null) ? 0 : manga.hashCode());
		result = prime * result + ((raw == null) ? 0 : raw.hashCode());
		result = prime * result + ((scan == null) ? 0 : scan.hashCode());
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
		MangaCapitulo other = (MangaCapitulo) obj;
		if (capitulo == null) {
			if (other.capitulo != null)
				return false;
		} else if (!capitulo.equals(other.capitulo))
			return false;
		if (extra == null) {
			if (other.extra != null)
				return false;
		} else if (!extra.equals(other.extra))
			return false;
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
		if (raw == null) {
			if (other.raw != null)
				return false;
		} else if (!raw.equals(other.raw))
			return false;
		if (scan == null) {
			if (other.scan != null)
				return false;
		} else if (!scan.equals(other.scan))
			return false;
		if (volume == null) {
			if (other.volume != null)
				return false;
		} else if (!volume.equals(other.volume))
			return false;
		return true;
	}

}