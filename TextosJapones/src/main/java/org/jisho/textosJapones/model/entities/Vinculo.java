package org.jisho.textosJapones.model.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vinculo {

	private String id;
	private String nomeArquivoOriginal;
	private String nomeArquivoVinculado;
	private MangaVolume volumeOriginal;
	private MangaVolume volumeVinculado;
	private List<VinculoPagina> vinculados;
	private List<VinculoPagina> naoVinculados;
	private LocalDateTime dataCriacao;
	private LocalDateTime ultimaAlteracao;

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
		this.nomeArquivoOriginal = "";
		this.nomeArquivoVinculado = "";
		this.volumeOriginal = null;
		this.volumeVinculado = null;
		this.vinculados = new ArrayList<VinculoPagina>();
		this.naoVinculados = new ArrayList<VinculoPagina>();
		this.dataCriacao = LocalDateTime.now();
		this.ultimaAlteracao = LocalDateTime.now();
	}

	public Vinculo(String nomeArquivoOriginal, MangaVolume volumeOriginal) {
		this.id = null;
		this.nomeArquivoOriginal = nomeArquivoOriginal;
		this.nomeArquivoVinculado = null;
		this.volumeOriginal = volumeOriginal;
		this.volumeVinculado = null;
		this.vinculados = new ArrayList<VinculoPagina>();
		this.naoVinculados = new ArrayList<VinculoPagina>();
		this.dataCriacao = LocalDateTime.now();
		this.ultimaAlteracao = LocalDateTime.now();
	}

	public Vinculo(String id, String nomeArquivoOriginal, String nomeArquivoVinculado, MangaVolume volumeOriginal, 
			MangaVolume volumeVinculado, LocalDateTime dataCriacao,	LocalDateTime ultimaAlteracao) {
		super();
		this.id = id;
		this.nomeArquivoOriginal = nomeArquivoOriginal;
		this.nomeArquivoVinculado = nomeArquivoVinculado;
		this.volumeOriginal = volumeOriginal;
		this.volumeVinculado = volumeVinculado;
		this.vinculados = new ArrayList<VinculoPagina>();
		this.naoVinculados = new ArrayList<VinculoPagina>();
		this.dataCriacao = dataCriacao;
		this.ultimaAlteracao = ultimaAlteracao;
	}

	public Vinculo(String id, String nomeArquivoOriginal, String nomeArquivoVinculado, MangaVolume volumeOriginal, 
			MangaVolume volumeVinculado, LocalDateTime dataCriacao,	LocalDateTime ultimaAlteracao,
			List<VinculoPagina> vinculados, List<VinculoPagina> naoVinculados) {
		super();
		this.id = id;
		this.nomeArquivoOriginal = nomeArquivoOriginal;
		this.nomeArquivoVinculado = nomeArquivoVinculado;	
		this.volumeOriginal = volumeOriginal;
		this.volumeVinculado = volumeVinculado;
		this.vinculados = vinculados;
		this.naoVinculados = naoVinculados;
		this.dataCriacao = dataCriacao;
		this.ultimaAlteracao = ultimaAlteracao;
	}

	@Override
	public String toString() {
		return "Vinculo [id=" + id + ", nomeArquivoOriginal=" + nomeArquivoOriginal + ", nomeArquivoVinculado="
				+ nomeArquivoVinculado + ", dataCriacao=" + dataCriacao + ", ultimaAlteracao=" + ultimaAlteracao + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, nomeArquivoOriginal, nomeArquivoVinculado);
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
		return Objects.equals(id, other.id) && Objects.equals(nomeArquivoOriginal, other.nomeArquivoOriginal)
				&& Objects.equals(nomeArquivoVinculado, other.nomeArquivoVinculado);
	}

}
