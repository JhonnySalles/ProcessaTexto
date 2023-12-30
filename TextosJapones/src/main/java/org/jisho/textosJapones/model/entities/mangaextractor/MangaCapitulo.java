package org.jisho.textosJapones.model.entities.mangaextractor;

import com.google.gson.annotations.Expose;
import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.enums.Language;

import java.util.*;

public class MangaCapitulo extends Manga {

	private UUID id;
	@Expose
	private String manga;
	@Expose
	private Integer volume;
	@Expose
	private Float capitulo;
	@Expose
	private Language lingua;
	@Expose
	private String scan;
	@Expose
	private List<MangaPagina> paginas;
	@Expose
	private Boolean extra;
	@Expose
	private Boolean raw;
	@Expose
	private Set<VocabularioExterno> vocabularios;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
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

	public Boolean isExtra() {
		return extra;
	}

	public void setExtra(Boolean extra) {
		this.extra = extra;
	}

	public Boolean isRaw() {
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

	public Set<VocabularioExterno> getVocabularios() {
		return vocabularios;
	}

	public void setVocabularios(Set<VocabularioExterno> vocabularios) {
		this.vocabularios = vocabularios;
	}

	public void addVocabulario(VocabularioExterno vocabulario) {
		this.vocabularios.add(vocabulario);
	}

	public MangaCapitulo() {
		super();
		this.id = null;
		this.manga = "";
		this.volume = 0;
		this.capitulo = 0F;
		this.lingua = Language.PORTUGUESE;
		this.scan = "";
		this.vocabularios = new HashSet<>();
		this.processar = true;
		this.paginas = new ArrayList<>();
	}

	public MangaCapitulo(UUID id, String manga, Integer volume, Float capitulo, Language lingua, String scan, Boolean extra, Boolean raw) {
		super(manga, volume, capitulo);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.lingua = lingua;
		this.scan = scan;
		this.vocabularios = new HashSet<>();
		this.processar = true;
		this.paginas = new ArrayList<>();
		this.extra = extra;
		this.raw = raw;
	}

	public MangaCapitulo(UUID id, String manga, Integer volume, Float capitulo, Language lingua, String scan,
			Boolean extra, Boolean raw, List<MangaPagina> paginas) {
		super(manga, volume, capitulo);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.lingua = lingua;
		this.scan = scan;
		this.vocabularios = new HashSet<>();
		this.processar = true;
		this.paginas = paginas;
		this.extra = extra;
		this.raw = raw;
	}

	public MangaCapitulo(UUID id, String manga, Integer volume, Float capitulo, Language lingua, String scan,
			Set<VocabularioExterno> vocabularios) {
		super(manga, volume, capitulo);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.lingua = lingua;
		this.scan = scan;
		this.vocabularios = vocabularios;
		this.processar = true;
		this.paginas = new ArrayList<MangaPagina>();
	}

	public MangaCapitulo(UUID id, String manga, Integer volume, Float capitulo, Language lingua, String scan,
			Boolean extra, Boolean raw, Set<VocabularioExterno> vocabularios, List<MangaPagina> paginas) {
		super(manga, volume, capitulo);
		this.id = id;
		this.manga = manga;
		this.volume = volume;
		this.capitulo = capitulo;
		this.lingua = lingua;
		this.scan = scan;
		this.paginas = paginas;
		this.vocabularios = vocabularios;
		this.processar = true;
		this.extra = extra;
		this.raw = raw;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		MangaCapitulo that = (MangaCapitulo) o;
		return Objects.equals(id, that.id) && Objects.equals(manga, that.manga) && Objects.equals(volume, that.volume) && Objects.equals(capitulo, that.capitulo) && lingua == that.lingua && Objects.equals(scan, that.scan) && Objects.equals(extra, that.extra) && Objects.equals(raw, that.raw);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, manga, volume, capitulo, lingua, scan, extra, raw);
	}

	@Override
	public String toString() {
		return "MangaCapitulo [id=" + id + ", capitulo=" + capitulo + ", lingua=" + lingua + ", scan=" + scan
				+ ", paginas=" + paginas + ", extra=" + extra + ", raw=" + raw + "]";
	}

}
