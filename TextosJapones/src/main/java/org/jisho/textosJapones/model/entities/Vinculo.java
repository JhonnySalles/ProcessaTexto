package org.jisho.textosJapones.model.entities;

import org.jisho.textosJapones.model.entities.mangaextractor.MangaVolume;
import org.jisho.textosJapones.model.enums.Language;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Vinculo {

	private UUID id;
	private String base;
	private Integer volume;
	private String nomeArquivoOriginal;
	private String nomeArquivoVinculado;
	private Language linguagemOriginal;
	private Language linguagemVinculado;
	private MangaVolume volumeOriginal;
	private MangaVolume volumeVinculado;
	private List<VinculoPagina> vinculados;
	private List<VinculoPagina> naoVinculados;
	private LocalDateTime dataCriacao;
	private LocalDateTime ultimaAlteracao;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public Integer getVolume() {
		return volume;
	}

	public void setVolume(Integer volume) {
		this.volume = volume;
	}

	public String getNomeArquivoOriginal() {
		return nomeArquivoOriginal;
	}

	public void setNomeArquivoOriginal(String nomeArquivoOriginal) {
		this.nomeArquivoOriginal = nomeArquivoOriginal;
	}

	public String getNomeArquivoVinculado() {
		return nomeArquivoVinculado;
	}

	public void setNomeArquivoVinculado(String nomeArquivoVinculado) {
		this.nomeArquivoVinculado = nomeArquivoVinculado;
	}

	public MangaVolume getVolumeOriginal() {
		return volumeOriginal;
	}

	public void setVolumeOriginal(MangaVolume volumeOriginal) {
		this.volumeOriginal = volumeOriginal;
	}

	public MangaVolume getVolumeVinculado() {
		return volumeVinculado;
	}

	public void setVolumeVinculado(MangaVolume volumeVinculado) {
		this.volumeVinculado = volumeVinculado;
	}

	public Language getLinguagemOriginal() {
		return linguagemOriginal;
	}

	public void setLinguagemOriginal(Language linguagemOriginal) {
		this.linguagemOriginal = linguagemOriginal;
	}

	public Language getLinguagemVinculado() {
		return linguagemVinculado;
	}

	public void setLinguagemVinculado(Language linguagemVinculado) {
		this.linguagemVinculado = linguagemVinculado;
	}

	public List<VinculoPagina> getVinculados() {
		return vinculados;
	}

	public void setVinculados(List<VinculoPagina> vinculados) {
		this.vinculados = vinculados;
	}

	public List<VinculoPagina> getNaoVinculados() {
		return naoVinculados;
	}

	public void setNaoVinculados(List<VinculoPagina> naoVinculados) {
		this.naoVinculados = naoVinculados;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public LocalDateTime getUltimaAlteracao() {
		return ultimaAlteracao;
	}

	public void setUltimaAlteracao(LocalDateTime ultimaAlteracao) {
		this.ultimaAlteracao = ultimaAlteracao;
	}

	public Vinculo() {
		this.id = null;
		this.base = "";
		this.volume = 0;
		this.nomeArquivoOriginal = "";
		this.nomeArquivoVinculado = "";
		this.volumeOriginal = null;
		this.volumeVinculado = null;
		this.linguagemOriginal = null;
		this.linguagemVinculado = null;
		this.vinculados = new ArrayList<VinculoPagina>();
		this.naoVinculados = new ArrayList<VinculoPagina>();
		this.dataCriacao = LocalDateTime.now();
		this.ultimaAlteracao = LocalDateTime.now();
	}

	public Vinculo(String base, Integer volume, String nomeArquivoOriginal, Language linguagemOriginal,
			MangaVolume volumeOriginal) {
		this.id = null;
		this.base = base;
		this.volume = volume;
		this.nomeArquivoOriginal = nomeArquivoOriginal;
		this.nomeArquivoVinculado = null;
		this.volumeOriginal = volumeOriginal;
		this.volumeVinculado = null;
		this.linguagemOriginal = linguagemOriginal;
		this.linguagemVinculado = null;
		this.vinculados = new ArrayList<VinculoPagina>();
		this.naoVinculados = new ArrayList<VinculoPagina>();
		this.dataCriacao = LocalDateTime.now();
		this.ultimaAlteracao = LocalDateTime.now();
	}

	public Vinculo(UUID id, String base, Integer volume, String nomeArquivoOriginal, Language linguagemOriginal,
                   MangaVolume volumeOriginal, String nomeArquivoVinculado, Language linguagemVinculado,
                   MangaVolume volumeVinculado, LocalDateTime dataCriacao, LocalDateTime ultimaAlteracao) {
		this.id = id;
		this.base = base;
		this.volume = volume;
		this.nomeArquivoOriginal = nomeArquivoOriginal;
		this.nomeArquivoVinculado = nomeArquivoVinculado;
		this.volumeOriginal = volumeOriginal;
		this.volumeVinculado = volumeVinculado;
		this.linguagemOriginal = linguagemOriginal;
		this.linguagemVinculado = linguagemVinculado;
		this.vinculados = new ArrayList<VinculoPagina>();
		this.naoVinculados = new ArrayList<VinculoPagina>();
		this.dataCriacao = dataCriacao;
		this.ultimaAlteracao = ultimaAlteracao;
	}

	public Vinculo(UUID id, String base, Integer volume, String nomeArquivoOriginal, Language linguagemOriginal,
                   MangaVolume volumeOriginal, String nomeArquivoVinculado, Language linguagemVinculado,
                   MangaVolume volumeVinculado, LocalDateTime dataCriacao, LocalDateTime ultimaAlteracao,
                   List<VinculoPagina> vinculados, List<VinculoPagina> naoVinculados) {
		this.id = id;
		this.base = base;
		this.volume = volume;
		this.nomeArquivoOriginal = nomeArquivoOriginal;
		this.nomeArquivoVinculado = nomeArquivoVinculado;
		this.volumeOriginal = volumeOriginal;
		this.volumeVinculado = volumeVinculado;
		this.linguagemOriginal = linguagemOriginal;
		this.linguagemVinculado = linguagemVinculado;
		this.vinculados = vinculados;
		this.naoVinculados = naoVinculados;
		this.dataCriacao = dataCriacao;
		this.ultimaAlteracao = ultimaAlteracao;
	}

	@Override
	public String toString() {
		return "Vinculo [id=" + id + ", base=" + base + ", volume=" + volume + ", nomeArquivoOriginal="
				+ nomeArquivoOriginal + ", nomeArquivoVinculado=" + nomeArquivoVinculado + ", linguagemOriginal="
				+ linguagemOriginal + ", linguagemVinculado=" + linguagemVinculado + ", dataCriacao=" + dataCriacao
				+ ", ultimaAlteracao=" + ultimaAlteracao + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(base, linguagemOriginal, linguagemVinculado, nomeArquivoOriginal, nomeArquivoVinculado);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vinculo other = (Vinculo) obj;
		return Objects.equals(base, other.base) && linguagemOriginal == other.linguagemOriginal
				&& linguagemVinculado == other.linguagemVinculado
				&& Objects.equals(nomeArquivoOriginal, other.nomeArquivoOriginal)
				&& Objects.equals(nomeArquivoVinculado, other.nomeArquivoVinculado);
	}

}
