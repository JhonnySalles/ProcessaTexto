package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.List;

import org.jisho.textosJapones.model.enums.Language;

public class Manga {

	private Long id;
	private String nome;
	private Integer volume;
	private Integer capitulo;
	private Language lingua;
	private String scan;
	private List<MangaPagina> paginas;
	private String vocabulario;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public Integer getCapitulo() {
		return capitulo;
	}

	public void setCapitulo(Integer capitulo) {
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

	public Manga() {
		this.id = null;
		this.nome = "";
		this.volume = 0;
		this.capitulo = 0;
		this.lingua = Language.PORTUGUESE;
		this.scan = "";
		this.vocabulario = "";
		this.paginas = new ArrayList<MangaPagina>();
	}

	public Manga(Long id, String nome, Integer volume, Integer capitulo, Language lingua, String scan,
			String vocabulario, List<MangaPagina> paginas) {
		this.id = id;
		this.nome = nome;
		this.volume = volume;
		this.capitulo = capitulo;
		this.lingua = lingua;
		this.scan = scan;
		this.paginas = paginas;
		this.vocabulario = vocabulario;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((capitulo == null) ? 0 : capitulo.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lingua == null) ? 0 : lingua.hashCode());
		result = prime * result + ((nome == null) ? 0 : nome.hashCode());
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
		Manga other = (Manga) obj;
		if (capitulo == null) {
			if (other.capitulo != null)
				return false;
		} else if (!capitulo.equals(other.capitulo))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lingua != other.lingua)
			return false;
		if (nome == null) {
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
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
