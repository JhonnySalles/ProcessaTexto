package org.jisho.textosJapones.model.entities;

import java.util.Objects;

import javafx.scene.image.Image;

public class VinculoPagina {

	final public static Integer PAGINA_VAZIA = -1;

	private Long id;
	private String originalNomePagina;
	private String originalPathPagina;
	private Integer originalPagina;
	private Integer originalPaginas;
	private Boolean originalPaginaDupla;

	private String vinculadoDireitaNomePagina;
	private String vinculadoDireitaPathPagina;
	private Integer vinculadoDireitaPagina;
	private Integer vinculadoDireitaPaginas;
	private Boolean vinculadoDireitaPaginaDupla;

	private String vinculadoEsquerdaNomePagina;
	private String vinculadoEsquerdaPathPagina;
	private Integer vinculadoEsquerdaPagina;
	private Integer vinculadoEsquerdaPaginas;
	private Boolean vinculadoEsquerdaPaginaDupla;

	private MangaPagina mangaPaginaOriginal;
	private MangaPagina mangaPaginaDireita;
	private MangaPagina mangaPaginaEsquerda;

	private Image imagemOriginal;
	private Image imagemVinculadoDireita;
	private Image imagemVinculadoEsquerda;

	private Boolean imagemDupla;
	private Boolean naoVinculado;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOriginalNomePagina() {
		return originalNomePagina;
	}

	public void setOriginalNomePagina(String originalNomePagina) {
		this.originalNomePagina = originalNomePagina;
	}

	public String getOriginalPathPagina() {
		return originalPathPagina;
	}

	public void setOriginalPathPagina(String originalPathPagina) {
		this.originalPathPagina = originalPathPagina;
	}

	public Integer getOriginalPagina() {
		return originalPagina;
	}

	public void setOriginalPagina(Integer originalPagina) {
		this.originalPagina = originalPagina;
	}

	public Integer getOriginalPaginas() {
		return originalPaginas;
	}

	public void setOriginalPaginas(Integer originalPaginas) {
		this.originalPaginas = originalPaginas;
	}

	public Boolean getOriginalPaginaDupla() {
		return originalPaginaDupla;
	}

	public void setOriginalPaginaDupla(Boolean originalPaginaDupla) {
		this.originalPaginaDupla = originalPaginaDupla;
	}

	public String getVinculadoDireitaNomePagina() {
		return vinculadoDireitaNomePagina;
	}

	public void setVinculadoDireitaNomePagina(String vinculadoDireitaNomePagina) {
		this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina;
	}

	public String getVinculadoDireitaPathPagina() {
		return vinculadoDireitaPathPagina;
	}

	public void setVinculadoDireitaPathPagina(String vinculadoDireitaPathPagina) {
		this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina;
	}

	public Integer getVinculadoDireitaPagina() {
		return vinculadoDireitaPagina;
	}

	public void setVinculadoDireitaPagina(Integer vinculadoDireitaPagina) {
		this.vinculadoDireitaPagina = vinculadoDireitaPagina;
	}

	public Integer getVinculadoDireitaPaginas() {
		return vinculadoDireitaPaginas;
	}

	public void setVinculadoDireitaPaginas(Integer vinculadoDireitaPaginas) {
		this.vinculadoDireitaPaginas = vinculadoDireitaPaginas;
	}

	public Boolean getVinculadoDireitaPaginaDupla() {
		return vinculadoDireitaPaginaDupla;
	}

	public void setVinculadoDireitaPaginaDupla(Boolean vinculadoDireitaPaginaDupla) {
		this.vinculadoDireitaPaginaDupla = vinculadoDireitaPaginaDupla;
	}

	public String getVinculadoEsquerdaNomePagina() {
		return vinculadoEsquerdaNomePagina;
	}

	public void setVinculadoEsquerdaNomePagina(String vinculadoEsquerdaNomePagina) {
		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
	}

	public String getVinculadoEsquerdaPathPagina() {
		return vinculadoEsquerdaPathPagina;
	}

	public void setVinculadoEsquerdaPathPagina(String vinculadoEsquerdaPathPagina) {
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
	}

	public Integer getVinculadoEsquerdaPagina() {
		return vinculadoEsquerdaPagina;
	}

	public void setVinculadoEsquerdaPagina(Integer vinculadoEsquerdaPagina) {
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
	}

	public Integer getVinculadoEsquerdaPaginas() {
		return vinculadoEsquerdaPaginas;
	}

	public void setVinculadoEsquerdaPaginas(Integer vinculadoEsquerdaPaginas) {
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
	}

	public Boolean getVinculadoEsquerdaPaginaDupla() {
		return vinculadoEsquerdaPaginaDupla;
	}

	public void setVinculadoEsquerdaPaginaDupla(Boolean vinculadoEsquerdaPaginaDupla) {
		this.vinculadoEsquerdaPaginaDupla = vinculadoEsquerdaPaginaDupla;
	}

	public MangaPagina getMangaPaginaOriginal() {
		return mangaPaginaOriginal;
	}

	public void setMangaPaginaOriginal(MangaPagina mangaPaginaOriginal) {
		this.mangaPaginaOriginal = mangaPaginaOriginal;
	}

	public MangaPagina getMangaPaginaDireita() {
		return mangaPaginaDireita;
	}

	public void setMangaPaginaDireita(MangaPagina mangaPaginaDireita) {
		this.mangaPaginaDireita = mangaPaginaDireita;
	}

	public MangaPagina getMangaPaginaEsquerda() {
		return mangaPaginaEsquerda;
	}

	public void setMangaPaginaEsquerda(MangaPagina mangaPaginaEsquerda) {
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;
	}

	public Image getImagemOriginal() {
		return imagemOriginal;
	}

	public void setImagemOriginal(Image imagemOriginal) {
		this.imagemOriginal = imagemOriginal;
	}

	public Image getImagemVinculadoDireita() {
		return imagemVinculadoDireita;
	}

	public void setImagemVinculadoDireita(Image imagemVinculadoDireita) {
		this.imagemVinculadoDireita = imagemVinculadoDireita;
	}

	public Image getImagemVinculadoEsquerda() {
		return imagemVinculadoEsquerda;
	}

	public void setImagemVinculadoEsquerda(Image imagemVinculadoEsquerda) {
		this.imagemVinculadoEsquerda = imagemVinculadoEsquerda;
	}

	public Boolean getImagemDupla() {
		return imagemDupla;
	}

	public void setImagemDupla(Boolean imagemDupla) {
		this.imagemDupla = imagemDupla;
	}

	public Boolean getNaoVinculado() {
		return naoVinculado;
	}

	public void setNaoVinculado(Boolean naoVinculado) {
		this.naoVinculado = naoVinculado;
	}

	public void addOriginal(VinculoPagina original) {
		addOriginal(original.id, original.originalNomePagina, original.originalPathPagina, original.originalPagina,
				original.originalPaginas, original.vinculadoEsquerdaPaginaDupla, original.mangaPaginaOriginal,
				original.imagemOriginal);
	}

	public void addOriginal(Long id, String originalNomePagina, String originalPathPagina, Integer originalPagina,
			Integer originalPaginas, Boolean vinculadoEsquerdaPaginaDupla, MangaPagina mangaPaginaOriginal,
			Image imagemOriginal) {
		this.id = id;
		this.originalNomePagina = originalNomePagina;
		this.originalPathPagina = originalPathPagina;
		this.originalPagina = originalPagina;
		this.originalPaginas = originalPaginas;
		this.vinculadoEsquerdaPaginaDupla = vinculadoEsquerdaPaginaDupla;
		this.mangaPaginaOriginal = mangaPaginaOriginal;
		this.imagemOriginal = imagemOriginal;
	}

	public void addVinculoEsquerda(VinculoPagina vinculo) {
		addVinculoEsquerda(vinculo.vinculadoDireitaNomePagina, vinculo.vinculadoDireitaPathPagina,
				vinculo.vinculadoDireitaPagina, vinculo.vinculadoDireitaPaginas, vinculo.vinculadoEsquerdaPaginaDupla,
				vinculo.mangaPaginaDireita, vinculo.imagemVinculadoDireita);
	}

	public void addVinculoEsquerda(String vinculadoEsquerdaNomePagina, String vinculadoEsquerdaPathPagina,
			Integer vinculadoEsquerdaPagina, Integer vinculadoEsquerdaPaginas, Boolean vinculadoEsquerdaPaginaDupla,
			MangaPagina mangaPaginaEsquerda, Image imagemVinculadoEsquerda) {
		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
		this.vinculadoEsquerdaPaginaDupla = vinculadoEsquerdaPaginaDupla;
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;
		this.imagemVinculadoEsquerda = imagemVinculadoEsquerda;
	}

	public void addVinculoDireita(VinculoPagina vinculo) {
		addVinculoDireita(vinculo.vinculadoDireitaNomePagina, vinculo.vinculadoDireitaPathPagina,
				vinculo.vinculadoDireitaPagina, vinculo.vinculadoDireitaPaginas, vinculo.vinculadoDireitaPaginaDupla,
				vinculo.mangaPaginaDireita, vinculo.imagemVinculadoDireita);
	}

	public void addVinculoDireita(String vinculadoDireitaNomePagina, String vinculadoDireitaPathPagina,
			Integer vinculadoDireitaPagina, Integer vinculadoDireitaPaginas, Boolean vinculadoDireitaPaginaDupla,
			MangaPagina mangaPaginaDireita, Image imagemVinculadoDireita) {
		if (this.vinculadoEsquerdaPagina == PAGINA_VAZIA) {
			this.vinculadoEsquerdaNomePagina = vinculadoDireitaNomePagina;
			this.vinculadoEsquerdaPathPagina = vinculadoDireitaPathPagina;
			this.vinculadoEsquerdaPagina = vinculadoDireitaPagina;
			this.vinculadoEsquerdaPaginas = vinculadoDireitaPaginas;
			this.vinculadoEsquerdaPaginaDupla = vinculadoDireitaPaginaDupla;
			this.mangaPaginaEsquerda = mangaPaginaDireita;
			this.imagemVinculadoEsquerda = imagemVinculadoDireita;
		} else {
			this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina;
			this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina;
			this.vinculadoDireitaPagina = vinculadoDireitaPagina;
			this.vinculadoDireitaPaginas = vinculadoDireitaPaginas;
			this.vinculadoDireitaPaginaDupla = vinculadoDireitaPaginaDupla;
			this.mangaPaginaDireita = mangaPaginaDireita;
			this.imagemVinculadoDireita = imagemVinculadoDireita;
		}
	}

	public void moverDireitaParaEsquerda() {
		this.vinculadoEsquerdaNomePagina = this.vinculadoDireitaNomePagina;
		this.vinculadoEsquerdaPathPagina = this.vinculadoDireitaPathPagina;
		this.vinculadoEsquerdaPagina = this.vinculadoDireitaPagina;
		this.vinculadoEsquerdaPaginas = this.vinculadoDireitaPaginas;
		this.mangaPaginaEsquerda = this.mangaPaginaDireita;
		this.imagemVinculadoEsquerda = this.imagemVinculadoDireita;
		this.vinculadoEsquerdaPaginaDupla = this.vinculadoDireitaPaginaDupla;
		this.limparVinculadoDireita();
	}

	public void limparVinculado() {
		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoEsquerdaNomePagina = "";
		this.vinculadoEsquerdaPathPagina = "";
		this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
		this.vinculadoEsquerdaPaginas = 0;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = null;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;
		this.vinculadoDireitaPaginaDupla = false;
		this.vinculadoEsquerdaPaginaDupla = false;
		this.imagemDupla = false;
		this.naoVinculado = false;
	}

	public Boolean limparVinculadoEsquerda() {
		return limparVinculadoEsquerda(false);
	}

	public Boolean limparVinculadoEsquerda(Boolean isMover) {
		Boolean movido = false;

		if (isMover && this.vinculadoDireitaPagina != PAGINA_VAZIA) {
			this.vinculadoEsquerdaNomePagina = this.vinculadoDireitaNomePagina;
			this.vinculadoEsquerdaPathPagina = this.vinculadoDireitaPathPagina;
			this.vinculadoEsquerdaPagina = this.vinculadoDireitaPagina;
			this.vinculadoEsquerdaPaginas = this.vinculadoDireitaPaginas;
			this.mangaPaginaEsquerda = this.mangaPaginaDireita;
			this.imagemVinculadoEsquerda = this.imagemVinculadoDireita;
			this.limparVinculadoDireita();
			movido = true;
		} else {
			this.vinculadoEsquerdaNomePagina = "";
			this.vinculadoEsquerdaPathPagina = "";
			this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
			this.vinculadoEsquerdaPaginas = 0;
			this.vinculadoEsquerdaPaginaDupla = false;
		}

		return movido;
	}

	public void limparVinculadoDireita() {
		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaPaginaDupla = false;
		this.imagemDupla = false;
	}

	public VinculoPagina() {
		this.id = null;
		this.originalNomePagina = "";
		this.originalPathPagina = "";
		this.originalPagina = PAGINA_VAZIA;
		this.originalPaginas = 0;
		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoEsquerdaNomePagina = "";
		this.vinculadoEsquerdaPathPagina = "";
		this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
		this.vinculadoEsquerdaPaginas = 0;
		this.mangaPaginaOriginal = null;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = null;
		this.imagemOriginal = null;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;
		this.originalPaginaDupla = false;
		this.vinculadoDireitaPaginaDupla = false;
		this.vinculadoEsquerdaPaginaDupla = false;
		this.imagemDupla = false;
		this.naoVinculado = false;
	}

	public VinculoPagina(String originalNomePagina, String originalPathPagina, Integer originalPagina,
			Integer originalPaginas, Boolean originalPaginaDupla, MangaPagina mangaPaginaOriginal,
			Image imagemOriginal) {
		this.id = null;
		this.originalNomePagina = originalNomePagina;
		this.originalPathPagina = originalPathPagina;
		this.originalPagina = originalPagina;
		this.originalPaginas = originalPaginas;
		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoEsquerdaNomePagina = "";
		this.vinculadoEsquerdaPathPagina = "";
		this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
		this.vinculadoEsquerdaPaginas = 0;
		this.mangaPaginaOriginal = mangaPaginaOriginal;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = null;
		this.imagemOriginal = imagemOriginal;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;
		this.originalPaginaDupla = originalPaginaDupla;
		this.vinculadoDireitaPaginaDupla = false;
		this.vinculadoEsquerdaPaginaDupla = false;
		this.imagemDupla = false;
		this.naoVinculado = false;
	}

	public VinculoPagina(String vinculadoEsquerdaNomePagina, String vinculadoEsquerdaPathPagina,
			Integer vinculadoEsquerdaPagina, Integer vinculadoEsquerdaPaginas, Boolean vinculadoEsquerdaPaginaDupla,
			MangaPagina mangaPaginaEsquerda, Image imagemVinculadoEsquerda, Boolean naoVinculado) {
		this.id = null;
		this.originalNomePagina = "";
		this.originalPathPagina = "";
		this.originalPagina = PAGINA_VAZIA;
		this.originalPaginas = 0;
		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
		this.mangaPaginaOriginal = null;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;
		this.imagemOriginal = null;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = imagemVinculadoEsquerda;
		this.originalPaginaDupla = false;
		this.vinculadoDireitaPaginaDupla = false;
		this.vinculadoEsquerdaPaginaDupla = vinculadoEsquerdaPaginaDupla;
		this.imagemDupla = false;
		this.naoVinculado = naoVinculado;
	}

	public VinculoPagina(Long id, String originalNomePagina, String originalPathPagina, Integer originalPagina,
			Integer originalPaginas, Boolean originalPaginaDupla, String vinculadoDireitaNomePagina,
			String vinculadoDireitaPathPagina, Integer vinculadoDireitaPagina, Integer vinculadoDireitaPaginas,
			Boolean vinculadoDireitaPaginaDupla, String vinculadoEsquerdaNomePagina, String vinculadoEsquerdaPathPagina,
			Integer vinculadoEsquerdaPagina, Integer vinculadoEsquerdaPaginas, Boolean vinculadoEsquerdaPaginaDupla,
			MangaPagina mangaPaginaOriginal, MangaPagina mangaPaginaDireita, MangaPagina mangaPaginaEsquerda,
			Image imagemOriginal, Image imagemVinculadoDireita, Image imagemVinculadoEsquerda, Boolean imagemDupla,
			Boolean naoVinculado) {
		this.id = id;
		this.originalNomePagina = originalNomePagina;
		this.originalPathPagina = originalPathPagina;
		this.originalPagina = originalPagina;
		this.originalPaginas = originalPaginas;
		this.originalPaginaDupla = originalPaginaDupla;
		this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina;
		this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina;
		this.vinculadoDireitaPagina = vinculadoDireitaPagina;
		this.vinculadoDireitaPaginas = vinculadoDireitaPaginas;
		this.vinculadoDireitaPaginaDupla = vinculadoDireitaPaginaDupla;
		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
		this.vinculadoEsquerdaPaginaDupla = vinculadoEsquerdaPaginaDupla;
		this.mangaPaginaOriginal = mangaPaginaOriginal;
		this.mangaPaginaDireita = mangaPaginaDireita;
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;
		this.imagemOriginal = imagemOriginal;
		this.imagemVinculadoDireita = imagemVinculadoDireita;
		this.imagemVinculadoEsquerda = imagemVinculadoEsquerda;
		this.imagemDupla = imagemDupla;
		this.naoVinculado = naoVinculado;
	}

	@Override
	public String toString() {
		return "VinculoPagina [id=" + id + ", originalNomePagina=" + originalNomePagina + ", originalPathPagina="
				+ originalPathPagina + ", originalPagina=" + originalPagina + ", originalPaginas=" + originalPaginas
				+ ", originalPaginaDupla=" + originalPaginaDupla + ", vinculadoDireitaNomePagina="
				+ vinculadoDireitaNomePagina + ", vinculadoDireitaPathPagina=" + vinculadoDireitaPathPagina
				+ ", vinculadoDireitaPagina=" + vinculadoDireitaPagina + ", vinculadoDireitaPaginas="
				+ vinculadoDireitaPaginas + ", vinculadoDireitaPaginaDupla=" + vinculadoDireitaPaginaDupla
				+ ", vinculadoEsquerdaNomePagina=" + vinculadoEsquerdaNomePagina + ", vinculadoEsquerdaPathPagina="
				+ vinculadoEsquerdaPathPagina + ", vinculadoEsquerdaPagina=" + vinculadoEsquerdaPagina
				+ ", vinculadoEsquerdaPaginas=" + vinculadoEsquerdaPaginas + ", vinculadoEsquerdaPaginaDupla="
				+ vinculadoEsquerdaPaginaDupla + ", imagemDupla=" + imagemDupla + ", naoVinculado=" + naoVinculado
				+ "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(originalNomePagina, originalPagina, originalPaginas, originalPathPagina,
				vinculadoDireitaNomePagina, vinculadoDireitaPagina, vinculadoDireitaPaginas, vinculadoDireitaPathPagina,
				vinculadoEsquerdaNomePagina, vinculadoEsquerdaPagina, vinculadoEsquerdaPaginas,
				vinculadoEsquerdaPathPagina);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VinculoPagina other = (VinculoPagina) obj;
		return Objects.equals(originalNomePagina, other.originalNomePagina)
				&& Objects.equals(originalPagina, other.originalPagina)
				&& Objects.equals(originalPaginas, other.originalPaginas)
				&& Objects.equals(originalPathPagina, other.originalPathPagina)
				&& Objects.equals(vinculadoDireitaNomePagina, other.vinculadoDireitaNomePagina)
				&& Objects.equals(vinculadoDireitaPagina, other.vinculadoDireitaPagina)
				&& Objects.equals(vinculadoDireitaPaginas, other.vinculadoDireitaPaginas)
				&& Objects.equals(vinculadoDireitaPathPagina, other.vinculadoDireitaPathPagina)
				&& Objects.equals(vinculadoEsquerdaNomePagina, other.vinculadoEsquerdaNomePagina)
				&& Objects.equals(vinculadoEsquerdaPagina, other.vinculadoEsquerdaPagina)
				&& Objects.equals(vinculadoEsquerdaPaginas, other.vinculadoEsquerdaPaginas)
				&& Objects.equals(vinculadoEsquerdaPathPagina, other.vinculadoEsquerdaPathPagina);
	}

}
