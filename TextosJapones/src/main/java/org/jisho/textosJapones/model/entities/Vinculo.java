package org.jisho.textosJapones.model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jisho.textosJapones.model.enums.Language;

public class Vinculo {

	private Long id;
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
	private String base;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public Vinculo() {
		this.id = null;
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
		this.base = "";
	}

	public Vinculo(String base, String nomeArquivoOriginal, Language linguagemOriginal, MangaVolume volumeOriginal) {
		this.id = null;
		this.base = base;
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

	public Vinculo(Long id, String base, String nomeArquivoOriginal, Language linguagemOriginal,
			MangaVolume volumeOriginal, String nomeArquivoVinculado, Language linguagemVinculado,
			MangaVolume volumeVinculado, LocalDateTime dataCriacao, LocalDateTime ultimaAlteracao) {
		this.id = id;
		this.base = base;
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

	public Vinculo(Long id, String base, String nomeArquivoOriginal, Language linguagemOriginal,
			MangaVolume volumeOriginal, String nomeArquivoVinculado, Language linguagemVinculado,
			MangaVolume volumeVinculado, LocalDateTime dataCriacao, LocalDateTime ultimaAlteracao,
			List<VinculoPagina> vinculados, List<VinculoPagina> naoVinculados) {
		this.id = id;
		this.base = base;
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
		return "Vinculo [id=" + id + ", nomeArquivoOriginal=" + nomeArquivoOriginal + ", nomeArquivoVinculado="
				+ nomeArquivoVinculado + ", linguagemOriginal=" + linguagemOriginal + ", linguagemVinculado="
				+ linguagemVinculado + ", dataCriacao=" + dataCriacao + ", ultimaAlteracao=" + ultimaAlteracao
				+ ", base=" + base + "]";
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
