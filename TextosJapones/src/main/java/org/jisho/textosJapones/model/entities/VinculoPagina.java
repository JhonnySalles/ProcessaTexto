package org.jisho.textosJapones.model.entities;

import javafx.scene.image.Image;
import org.jisho.textosJapones.model.entities.mangaextractor.MangaPagina;
import org.jisho.textosJapones.model.enums.Pagina;

import java.io.Serializable;
import java.util.Objects;

public class VinculoPagina implements Serializable {

	private static final long serialVersionUID = 1595061547917015448L;

	final public static Integer PAGINA_VAZIA = -1;

	private Long id;
	private String originalHash;
	private String originalNomePagina;
	private String originalPathPagina;
	private Integer originalPagina;
	private Integer originalPaginas;
	public Boolean isOriginalPaginaDupla;
	private String originalPHash;
	private float[] originalHistogram;

	private String vinculadoDireitaHash;
	private String vinculadoDireitaNomePagina;
	private String vinculadoDireitaPathPagina;
	private Integer vinculadoDireitaPagina;
	private Integer vinculadoDireitaPaginas;
	public Boolean isVinculadoDireitaPaginaDupla;
	private String vinculadoDireitaPHash;
	private float[] vinculadoDireitaHistogram;

	private String vinculadoEsquerdaHash;
	private String vinculadoEsquerdaNomePagina;
	private String vinculadoEsquerdaPathPagina;
	private Integer vinculadoEsquerdaPagina;
	private Integer vinculadoEsquerdaPaginas;
	public Boolean isVinculadoEsquerdaPaginaDupla;
	private String vinculadoEsquerdaPHash;
	private float[] vinculadoEsquerdaHistogram;

	private transient MangaPagina mangaPaginaOriginal;
	private transient MangaPagina mangaPaginaDireita;
	private transient MangaPagina mangaPaginaEsquerda;

	private transient Image imagemOriginal;
	private transient Image imagemVinculadoDireita;
	private transient Image imagemVinculadoEsquerda;

	public Boolean isImagemDupla;
	public Boolean isNaoVinculado;
	public Pagina onDragOrigem;

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

	public String getOriginalHash() {
		return originalHash;
	}

	public void setOriginalHash(String originalHash) {
		this.originalHash = originalHash;
	}

	public String getVinculadoDireitaHash() {
		return vinculadoDireitaHash;
	}

	public void setVinculadoDireitaHash(String vinculadoDireitaHash) {
		this.vinculadoDireitaHash = vinculadoDireitaHash;
	}

	public String getVinculadoEsquerdaHash() {
		return vinculadoEsquerdaHash;
	}

	public void setVinculadoEsquerdaHash(String vinculadoEsquerdaHash) {
		this.vinculadoEsquerdaHash = vinculadoEsquerdaHash;
	}

	public String getOriginalPHash() {
		return originalPHash;
	}

	public String getVinculadoDireitaPHash() {
		return vinculadoDireitaPHash;
	}

	public String getVinculadoEsquerdaPHash() {
		return vinculadoEsquerdaPHash;
	}

	public void setOriginalPHash(String originalPHash) {
		this.originalPHash = originalPHash;
	}

	public void setVinculadoDireitaPHash(String vinculadoDireitaPHash) {
		this.vinculadoDireitaPHash = vinculadoDireitaPHash;
	}

	public void setVinculadoEsquerdaPHash(String vinculadoEsquerdaPHash) {
		this.vinculadoEsquerdaPHash = vinculadoEsquerdaPHash;
	}

	public float[] getOriginalHistogram() {
		return originalHistogram;
	}

	public void setOriginalHistogram(float[] originalHistogram) {
		this.originalHistogram = originalHistogram;
	}

	public float[] getVinculadoDireitaHistogram() {
		return vinculadoDireitaHistogram;
	}

	public void setVinculadoDireitaHistogram(float[] vinculadoDireitaHistogram) {
		this.vinculadoDireitaHistogram = vinculadoDireitaHistogram;
	}

	public float[] getVinculadoEsquerdaHistogram() {
		return vinculadoEsquerdaHistogram;
	}

	public void setVinculadoEsquerdaHistogram(float[] vinculadoEsquerdaHistogram) {
		this.vinculadoEsquerdaHistogram = vinculadoEsquerdaHistogram;
	}

	public void mesclar(VinculoPagina outro) {
		this.vinculadoEsquerdaNomePagina = outro.vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = outro.vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = outro.vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = outro.vinculadoEsquerdaPaginas;
		this.vinculadoEsquerdaHash = outro.vinculadoEsquerdaHash;
		this.vinculadoEsquerdaPHash = outro.vinculadoEsquerdaPHash;
		this.vinculadoEsquerdaHistogram = outro.vinculadoEsquerdaHistogram;	
		this.isVinculadoEsquerdaPaginaDupla = outro.isVinculadoEsquerdaPaginaDupla;
		this.mangaPaginaEsquerda = outro.mangaPaginaEsquerda;
		this.imagemVinculadoEsquerda = outro.imagemVinculadoEsquerda;

		this.vinculadoDireitaNomePagina = outro.vinculadoDireitaNomePagina;
		this.vinculadoDireitaPathPagina = outro.vinculadoDireitaPathPagina;
		this.vinculadoDireitaPagina = outro.vinculadoDireitaPagina;
		this.vinculadoDireitaPaginas = outro.vinculadoDireitaPaginas;
		this.vinculadoDireitaHash = outro.vinculadoDireitaHash;
		this.vinculadoDireitaPHash = outro.vinculadoDireitaPHash;
		this.vinculadoDireitaHistogram = outro.vinculadoDireitaHistogram;
		this.isVinculadoDireitaPaginaDupla = outro.isVinculadoDireitaPaginaDupla;
		this.mangaPaginaDireita = outro.mangaPaginaDireita;
		this.imagemVinculadoDireita = outro.imagemVinculadoDireita;

		this.isImagemDupla = outro.isImagemDupla;
	}

	public void addOriginalSemId(VinculoPagina original) {
		this.id = null;
		this.originalNomePagina = original.originalNomePagina;
		this.originalPathPagina = original.originalPathPagina;
		this.originalPagina = original.originalPagina;
		this.originalPaginas = original.originalPaginas;
		this.originalHash = original.originalHash;
		this.originalPHash = original.originalPHash;
		this.originalHistogram = original.originalHistogram;
		this.isOriginalPaginaDupla = original.isOriginalPaginaDupla;
		this.mangaPaginaOriginal = original.mangaPaginaOriginal;
		this.imagemOriginal = original.imagemOriginal;
	}

	public void addOriginal(VinculoPagina original) {
		addOriginal(original.id, original.originalNomePagina, original.originalPathPagina, original.originalPagina,
				original.originalPaginas, original.isOriginalPaginaDupla, original.mangaPaginaOriginal,
				original.imagemOriginal, original.originalHash, original.originalPHash, original.originalHistogram);
	}

	public void addOriginal(Long id, String originalNomePagina, String originalPathPagina, Integer originalPagina,
			Integer originalPaginas, Boolean isOriginalPaginaDupla, MangaPagina mangaPaginaOriginal,
			Image imagemOriginal, String originalHash, String originalPHash, float[] originalHistogram) {
		this.id = id;
		this.originalNomePagina = originalNomePagina;
		this.originalPathPagina = originalPathPagina;
		this.originalPagina = originalPagina;
		this.originalPaginas = originalPaginas;
		this.originalHash = originalHash;
		this.originalPHash = originalPHash;
		this.originalHistogram = originalHistogram;

		this.isOriginalPaginaDupla = isOriginalPaginaDupla;
		this.mangaPaginaOriginal = mangaPaginaOriginal;
		this.imagemOriginal = imagemOriginal;
	}

	public void addVinculoEsquerdaApartirDireita(VinculoPagina vinculo) {
		addVinculoEsquerda(vinculo.vinculadoDireitaNomePagina, vinculo.vinculadoDireitaPathPagina,
				vinculo.vinculadoDireitaPagina, vinculo.vinculadoDireitaPaginas, vinculo.isVinculadoDireitaPaginaDupla,
				vinculo.mangaPaginaDireita, vinculo.imagemVinculadoDireita, vinculo.vinculadoDireitaHash,
				vinculo.vinculadoDireitaPHash, vinculo.vinculadoDireitaHistogram);
	}

	public void addVinculoEsquerda(VinculoPagina vinculo) {
		addVinculoEsquerda(vinculo.vinculadoEsquerdaNomePagina, vinculo.vinculadoEsquerdaPathPagina,
				vinculo.vinculadoEsquerdaPagina, vinculo.vinculadoEsquerdaPaginas,
				vinculo.isVinculadoEsquerdaPaginaDupla, vinculo.mangaPaginaEsquerda, vinculo.imagemVinculadoEsquerda,
				vinculo.vinculadoEsquerdaHash, vinculo.vinculadoEsquerdaPHash, vinculo.vinculadoEsquerdaHistogram);
	}

	public void addVinculoEsquerda(String vinculadoEsquerdaNomePagina, String vinculadoEsquerdaPathPagina,
                                   Integer vinculadoEsquerdaPagina, Integer vinculadoEsquerdaPaginas, Boolean isVinculadoEsquerdaPaginaDupla,
                                   MangaPagina mangaPaginaEsquerda, Image imagemVinculadoEsquerda, String vinculadoEsquerdaHash,
                                   String vinculadoEsquerdaPHash, float[] vinculadoEsquerdaHistogram) {
		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
		this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla;
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;
		this.imagemVinculadoEsquerda = imagemVinculadoEsquerda;
		this.vinculadoEsquerdaHash = vinculadoEsquerdaHash;
		this.vinculadoEsquerdaPHash = vinculadoEsquerdaPHash;
		this.vinculadoEsquerdaHistogram = vinculadoEsquerdaHistogram;	
	}

	public void addVinculoDireitaApartirEsquerda(VinculoPagina vinculo) {
		addVinculoDireita(vinculo.vinculadoEsquerdaNomePagina, vinculo.vinculadoEsquerdaPathPagina,
				vinculo.vinculadoEsquerdaPagina, vinculo.vinculadoEsquerdaPaginas,
				vinculo.isVinculadoEsquerdaPaginaDupla, vinculo.mangaPaginaEsquerda, vinculo.imagemVinculadoEsquerda,
				vinculo.vinculadoEsquerdaHash, vinculo.vinculadoEsquerdaPHash, vinculo.vinculadoEsquerdaHistogram);
	}

	public void addVinculoDireita(VinculoPagina vinculo) {
		addVinculoDireita(vinculo.vinculadoDireitaNomePagina, vinculo.vinculadoDireitaPathPagina,
				vinculo.vinculadoDireitaPagina, vinculo.vinculadoDireitaPaginas, vinculo.isVinculadoDireitaPaginaDupla,
				vinculo.mangaPaginaDireita, vinculo.imagemVinculadoDireita, vinculo.vinculadoDireitaHash,
				vinculo.vinculadoDireitaPHash, vinculo.vinculadoDireitaHistogram);
	}

	public void addVinculoDireita(String vinculadoDireitaNomePagina, String vinculadoDireitaPathPagina,
                                  Integer vinculadoDireitaPagina, Integer vinculadoDireitaPaginas, Boolean isVinculadoDireitaPaginaDupla,
                                  MangaPagina mangaPaginaDireita, Image imagemVinculadoDireita, String vinculadoDireitaHash,
                                  String vinculadoDireitaPHash, float[] vinculadoDireitaHistogram) {
		if (this.vinculadoEsquerdaPagina == PAGINA_VAZIA) {
			this.vinculadoEsquerdaNomePagina = vinculadoDireitaNomePagina;
			this.vinculadoEsquerdaPathPagina = vinculadoDireitaPathPagina;
			this.vinculadoEsquerdaPagina = vinculadoDireitaPagina;
			this.vinculadoEsquerdaPaginas = vinculadoDireitaPaginas;
			this.vinculadoEsquerdaHash = vinculadoDireitaHash;
			this.vinculadoEsquerdaPHash = vinculadoDireitaPHash;
			this.vinculadoEsquerdaHistogram = vinculadoDireitaHistogram;
			this.isVinculadoEsquerdaPaginaDupla = isVinculadoDireitaPaginaDupla;
			this.mangaPaginaEsquerda = mangaPaginaDireita;
			this.imagemVinculadoEsquerda = imagemVinculadoDireita;
			this.isImagemDupla = this.vinculadoDireitaPagina != PAGINA_VAZIA;
		} else {
			this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina;
			this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina;
			this.vinculadoDireitaPagina = vinculadoDireitaPagina;
			this.vinculadoDireitaPaginas = vinculadoDireitaPaginas;
			this.vinculadoDireitaHash = vinculadoDireitaHash;
			this.vinculadoDireitaPHash = vinculadoDireitaPHash;
			this.vinculadoDireitaHistogram = vinculadoDireitaHistogram;
			this.isVinculadoDireitaPaginaDupla = isVinculadoDireitaPaginaDupla;
			this.mangaPaginaDireita = mangaPaginaDireita;
			this.imagemVinculadoDireita = imagemVinculadoDireita;
			this.isImagemDupla = true;
		}
	}

	public void moverDireitaParaEsquerda() {
		this.vinculadoEsquerdaNomePagina = this.vinculadoDireitaNomePagina;
		this.vinculadoEsquerdaPathPagina = this.vinculadoDireitaPathPagina;
		this.vinculadoEsquerdaPagina = this.vinculadoDireitaPagina;
		this.vinculadoEsquerdaPaginas = this.vinculadoDireitaPaginas;
		this.vinculadoEsquerdaHash = this.vinculadoDireitaHash;
		this.vinculadoEsquerdaPHash = this.vinculadoDireitaPHash;
		this.vinculadoEsquerdaHistogram = this.vinculadoDireitaHistogram;
		this.mangaPaginaEsquerda = this.mangaPaginaDireita;
		this.imagemVinculadoEsquerda = this.imagemVinculadoDireita;
		this.isVinculadoEsquerdaPaginaDupla = this.isVinculadoDireitaPaginaDupla;
		this.limparVinculadoDireita();
	}

	public void limparVinculado() {
		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;

		this.vinculadoEsquerdaNomePagina = "";
		this.vinculadoEsquerdaPathPagina = "";
		this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
		this.vinculadoEsquerdaPaginas = 0;
		this.vinculadoEsquerdaHash = "";
		this.vinculadoEsquerdaPHash = "";
		this.vinculadoEsquerdaHistogram = null;

		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = null;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;

		this.isVinculadoDireitaPaginaDupla = false;
		this.isVinculadoEsquerdaPaginaDupla = false;

		this.isImagemDupla = false;
		this.isNaoVinculado = false;
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
			this.vinculadoEsquerdaHash = this.vinculadoDireitaHash;
			this.vinculadoEsquerdaPHash = this.vinculadoDireitaPHash;
			this.vinculadoEsquerdaHistogram = this.vinculadoDireitaHistogram;
			this.mangaPaginaEsquerda = this.mangaPaginaDireita;
			this.imagemVinculadoEsquerda = this.imagemVinculadoDireita;
			this.limparVinculadoDireita();
			movido = true;
		} else {
			this.vinculadoEsquerdaNomePagina = "";
			this.vinculadoEsquerdaPathPagina = "";
			this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
			this.vinculadoEsquerdaPaginas = 0;
			this.vinculadoEsquerdaHash = "";
			this.vinculadoEsquerdaPHash = "";
			this.vinculadoEsquerdaHistogram = null;
			this.isVinculadoEsquerdaPaginaDupla = false;
			this.imagemVinculadoEsquerda = null;
			this.mangaPaginaEsquerda = null;
			this.isImagemDupla = false;
		}

		return movido;
	}

	public void limparVinculadoDireita() {
		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;
		this.isVinculadoDireitaPaginaDupla = false;
		this.isImagemDupla = false;
		this.imagemVinculadoDireita = null;
		this.mangaPaginaDireita = null;
	}

	public VinculoPagina() {
		this.id = null;
		this.originalNomePagina = "";
		this.originalPathPagina = "";
		this.originalPagina = PAGINA_VAZIA;
		this.originalPaginas = 0;
		this.originalHash = "";
		this.originalPHash = "";
		this.originalHistogram = null;

		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;

		this.vinculadoEsquerdaNomePagina = "";
		this.vinculadoEsquerdaPathPagina = "";
		this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
		this.vinculadoEsquerdaPaginas = 0;
		this.vinculadoEsquerdaHash = "";
		this.vinculadoEsquerdaPHash = "";
		this.vinculadoEsquerdaHistogram = null;

		this.mangaPaginaOriginal = null;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = null;
		this.imagemOriginal = null;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;

		this.isOriginalPaginaDupla = false;
		this.isVinculadoDireitaPaginaDupla = false;
		this.isVinculadoEsquerdaPaginaDupla = false;

		this.isImagemDupla = false;
		this.isNaoVinculado = false;
	}

	public VinculoPagina(String originalNomePagina, String originalPathPagina, Integer originalPagina,
			Integer originalPaginas, Boolean isOriginalPaginaDupla, MangaPagina mangaPaginaOriginal,
			Image imagemOriginal, String originalHash, String originalPHash, float[] originalHistogram) {
		this.id = null;
		this.originalNomePagina = originalNomePagina;
		this.originalPathPagina = originalPathPagina;
		this.originalPagina = originalPagina;
		this.originalPaginas = originalPaginas;
		this.originalHash = originalHash;
		this.originalPHash = originalPHash;
		this.originalHistogram = originalHistogram;

		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;

		this.vinculadoEsquerdaNomePagina = "";
		this.vinculadoEsquerdaPathPagina = "";
		this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
		this.vinculadoEsquerdaPaginas = 0;
		this.vinculadoEsquerdaHash = "";
		this.vinculadoEsquerdaPHash = "";
		this.vinculadoEsquerdaHistogram = null;

		this.mangaPaginaOriginal = mangaPaginaOriginal;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = null;
		this.imagemOriginal = imagemOriginal;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;

		this.isOriginalPaginaDupla = isOriginalPaginaDupla;
		this.isVinculadoDireitaPaginaDupla = false;
		this.isVinculadoEsquerdaPaginaDupla = false;

		this.isImagemDupla = false;
		this.isNaoVinculado = false;
	}

	public VinculoPagina(String vinculadoEsquerdaNomePagina, String vinculadoEsquerdaPathPagina,
                         Integer vinculadoEsquerdaPagina, Integer vinculadoEsquerdaPaginas, Boolean isVinculadoEsquerdaPaginaDupla,
                         MangaPagina mangaPaginaEsquerda, Image imagemVinculadoEsquerda, Boolean naoVinculado,
                         String vinculadoEsquerdaHash, String vinculadoEsquerdaPHash, float[] vinculadoEsquerdaHistogram) {
		this.id = null;

		this.originalNomePagina = "";
		this.originalPathPagina = "";
		this.originalPagina = PAGINA_VAZIA;
		this.originalPaginas = 0;
		this.originalHash = "";
		this.originalPHash = "";
		this.originalHistogram = null;

		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;

		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
		this.vinculadoEsquerdaHash = vinculadoEsquerdaHash;
		this.vinculadoEsquerdaPHash = vinculadoEsquerdaPHash;
		this.vinculadoEsquerdaHistogram = vinculadoEsquerdaHistogram;

		this.mangaPaginaOriginal = null;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;
		this.imagemOriginal = null;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = imagemVinculadoEsquerda;

		this.isOriginalPaginaDupla = false;
		this.isVinculadoDireitaPaginaDupla = false;
		this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla;

		this.isImagemDupla = false;
		this.isNaoVinculado = naoVinculado;
	}

	public VinculoPagina(Long id, String vinculadoEsquerdaNomePagina, String vinculadoEsquerdaPathPagina,
                         Integer vinculadoEsquerdaPagina, Integer vinculadoEsquerdaPaginas, Boolean isVinculadoEsquerdaPaginaDupla,
                         MangaPagina mangaPaginaEsquerda, Boolean naoVinculado) {
		this.id = id;

		this.originalNomePagina = "";
		this.originalPathPagina = "";
		this.originalPagina = PAGINA_VAZIA;
		this.originalPaginas = 0;
		this.originalHash = "";
		this.originalPHash = "";
		this.originalHistogram = null;

		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;

		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
		this.vinculadoEsquerdaHash = "";
		this.vinculadoEsquerdaPHash = "";
		this.vinculadoEsquerdaHistogram = null;

		this.mangaPaginaOriginal = null;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;
		this.imagemOriginal = null;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;

		this.isOriginalPaginaDupla = false;
		this.isVinculadoDireitaPaginaDupla = false;
		this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla;

		this.isImagemDupla = false;
		this.isNaoVinculado = naoVinculado;
	}

	public VinculoPagina(Long id, String originalNomePagina, String originalPathPagina, Integer originalPagina,
                         Integer originalPaginas, Boolean isOriginalPaginaDupla, String vinculadoDireitaNomePagina,
                         String vinculadoDireitaPathPagina, Integer vinculadoDireitaPagina, Integer vinculadoDireitaPaginas,
                         Boolean isVinculadoDireitaPaginaDupla, String vinculadoEsquerdaNomePagina,
                         String vinculadoEsquerdaPathPagina, Integer vinculadoEsquerdaPagina, Integer vinculadoEsquerdaPaginas,
                         Boolean isVinculadoEsquerdaPaginaDupla, MangaPagina mangaPaginaOriginal, MangaPagina mangaPaginaDireita,
                         MangaPagina mangaPaginaEsquerda, Boolean imagemDupla, Boolean naoVinculado) {
		this.id = id;

		this.originalNomePagina = originalNomePagina;
		this.originalPathPagina = originalPathPagina;
		this.originalPagina = originalPagina;
		this.originalPaginas = originalPaginas;
		this.originalHash = "";
		this.originalPHash = "";
		this.originalHistogram = null;

		this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina;
		this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina;
		this.vinculadoDireitaPagina = vinculadoDireitaPagina;
		this.vinculadoDireitaPaginas = vinculadoDireitaPaginas;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;

		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
		this.vinculadoEsquerdaHash = "";
		this.vinculadoEsquerdaPHash = "";
		this.vinculadoEsquerdaHistogram = null;

		this.mangaPaginaOriginal = mangaPaginaOriginal;
		this.mangaPaginaDireita = mangaPaginaDireita;
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;

		this.imagemOriginal = null;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;

		this.isOriginalPaginaDupla = isOriginalPaginaDupla;
		this.isVinculadoDireitaPaginaDupla = isVinculadoDireitaPaginaDupla;
		this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla;

		this.isImagemDupla = imagemDupla;
		this.isNaoVinculado = naoVinculado;
	}

	public VinculoPagina(Long id, String originalNomePagina, String originalPathPagina, Integer originalPagina,
                         Integer originalPaginas, Boolean isOriginalPaginaDupla, String vinculadoDireitaNomePagina,
                         String vinculadoDireitaPathPagina, Integer vinculadoDireitaPagina, Integer vinculadoDireitaPaginas,
                         Boolean isVinculadoDireitaPaginaDupla, String vinculadoEsquerdaNomePagina,
                         String vinculadoEsquerdaPathPagina, Integer vinculadoEsquerdaPagina, Integer vinculadoEsquerdaPaginas,
                         Boolean isVinculadoEsquerdaPaginaDupla, MangaPagina mangaPaginaOriginal, MangaPagina mangaPaginaDireita,
                         MangaPagina mangaPaginaEsquerda, Image imagemOriginal, Image imagemVinculadoDireita,
                         Image imagemVinculadoEsquerda, Boolean imagemDupla, Boolean naoVinculado, String originalHash,
                         String vinculadoDireitaHash, String vinculadoEsquerdaHash) {
		this.id = id;

		this.originalNomePagina = originalNomePagina;
		this.originalPathPagina = originalPathPagina;
		this.originalPagina = originalPagina;
		this.originalPaginas = originalPaginas;
		this.originalHash = originalHash;
		this.originalPHash = "";
		this.originalHistogram = null;

		this.vinculadoDireitaNomePagina = vinculadoDireitaNomePagina;
		this.vinculadoDireitaPathPagina = vinculadoDireitaPathPagina;
		this.vinculadoDireitaPagina = vinculadoDireitaPagina;
		this.vinculadoDireitaPaginas = vinculadoDireitaPaginas;
		this.vinculadoDireitaHash = vinculadoDireitaHash;
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;

		this.vinculadoEsquerdaNomePagina = vinculadoEsquerdaNomePagina;
		this.vinculadoEsquerdaPathPagina = vinculadoEsquerdaPathPagina;
		this.vinculadoEsquerdaPagina = vinculadoEsquerdaPagina;
		this.vinculadoEsquerdaPaginas = vinculadoEsquerdaPaginas;
		this.vinculadoEsquerdaHash = vinculadoEsquerdaHash;
		this.vinculadoEsquerdaPHash = "";
		this.vinculadoEsquerdaHistogram = null;

		this.mangaPaginaOriginal = mangaPaginaOriginal;
		this.mangaPaginaDireita = mangaPaginaDireita;
		this.mangaPaginaEsquerda = mangaPaginaEsquerda;
		this.imagemOriginal = imagemOriginal;
		this.imagemVinculadoDireita = imagemVinculadoDireita;
		this.imagemVinculadoEsquerda = imagemVinculadoEsquerda;

		this.isOriginalPaginaDupla = isOriginalPaginaDupla;
		this.isVinculadoDireitaPaginaDupla = isVinculadoDireitaPaginaDupla;
		this.isVinculadoEsquerdaPaginaDupla = isVinculadoEsquerdaPaginaDupla;

		this.isImagemDupla = imagemDupla;
		this.isNaoVinculado = naoVinculado;

	}

	public VinculoPagina(VinculoPagina manga) {
		this.id = null;
		this.originalNomePagina = manga.originalNomePagina;
		this.originalPathPagina = manga.originalPathPagina;
		this.originalPagina = manga.originalPagina;
		this.originalPaginas = manga.originalPaginas;
		this.originalHash = manga.originalHash;
		this.originalPHash = manga.originalPHash;
		this.originalHistogram = manga.originalHistogram;

		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;

		this.vinculadoEsquerdaNomePagina = "";
		this.vinculadoEsquerdaPathPagina = "";
		this.vinculadoEsquerdaPagina = PAGINA_VAZIA;
		this.vinculadoEsquerdaPaginas = 0;
		this.vinculadoEsquerdaHash = "";
		this.vinculadoEsquerdaPHash = "";
		this.vinculadoEsquerdaHistogram = null;

		this.mangaPaginaOriginal = manga.mangaPaginaOriginal;
		this.mangaPaginaDireita = null;
		this.mangaPaginaEsquerda = null;

		this.imagemOriginal = manga.imagemOriginal;
		this.imagemVinculadoDireita = null;
		this.imagemVinculadoEsquerda = null;

		this.isOriginalPaginaDupla = manga.isOriginalPaginaDupla;
		this.isVinculadoDireitaPaginaDupla = false;
		this.isVinculadoEsquerdaPaginaDupla = false;

		this.isImagemDupla = false;
		this.isNaoVinculado = false;
	}

	public VinculoPagina(VinculoPagina manga, Boolean isEsquerda, Boolean isNaoVinculado) {
		this.id = null;

		this.originalNomePagina = "";
		this.originalPathPagina = "";
		this.originalPagina = PAGINA_VAZIA;
		this.originalPaginas = 0;
		this.originalHash = "";
		this.originalPHash = "";
		this.originalHistogram = null;
		this.mangaPaginaOriginal = null;
		this.imagemOriginal = null;
		this.isOriginalPaginaDupla = false;

		this.vinculadoDireitaNomePagina = "";
		this.vinculadoDireitaPathPagina = "";
		this.vinculadoDireitaPagina = PAGINA_VAZIA;
		this.vinculadoDireitaPaginas = 0;
		this.vinculadoDireitaHash = "";
		this.vinculadoDireitaPHash = "";
		this.vinculadoDireitaHistogram = null;
		this.mangaPaginaDireita = null;
		this.imagemVinculadoDireita = null;
		this.isVinculadoDireitaPaginaDupla = false;

		if (isEsquerda) {
			this.vinculadoEsquerdaNomePagina = manga.vinculadoEsquerdaNomePagina;
			this.vinculadoEsquerdaPathPagina = manga.vinculadoEsquerdaPathPagina;
			this.vinculadoEsquerdaPagina = manga.vinculadoEsquerdaPagina;
			this.vinculadoEsquerdaPaginas = manga.vinculadoEsquerdaPaginas;
			this.vinculadoEsquerdaHash = manga.vinculadoEsquerdaHash;
			this.vinculadoEsquerdaPHash = manga.vinculadoEsquerdaPHash;
			this.mangaPaginaEsquerda = manga.mangaPaginaEsquerda;
			this.imagemVinculadoEsquerda = manga.imagemVinculadoEsquerda;
			this.isVinculadoEsquerdaPaginaDupla = manga.isVinculadoEsquerdaPaginaDupla;
			this.vinculadoEsquerdaHistogram = manga.vinculadoEsquerdaHistogram;
		} else {
			this.vinculadoEsquerdaNomePagina = manga.vinculadoDireitaNomePagina;
			this.vinculadoEsquerdaPathPagina = manga.vinculadoDireitaPathPagina;
			this.vinculadoEsquerdaPagina = manga.vinculadoDireitaPagina;
			this.vinculadoEsquerdaPaginas = manga.vinculadoDireitaPaginas;
			this.vinculadoEsquerdaHash = manga.vinculadoDireitaHash;
			this.vinculadoEsquerdaPHash = manga.vinculadoDireitaPHash;
			this.mangaPaginaEsquerda = manga.mangaPaginaDireita;
			this.imagemVinculadoEsquerda = manga.imagemVinculadoDireita;
			this.isVinculadoEsquerdaPaginaDupla = manga.isVinculadoDireitaPaginaDupla;
			this.vinculadoEsquerdaHistogram = manga.vinculadoDireitaHistogram;
		}

		this.isImagemDupla = false;
		this.isNaoVinculado = isNaoVinculado;
	}

	@Override
	public String toString() {
		return "VinculoPagina [id=" + id + ", originalNomePagina=" + originalNomePagina + ", originalPathPagina="
				+ originalPathPagina + ", originalPagina=" + originalPagina + ", originalPaginas=" + originalPaginas
				+ ", isOriginalPaginaDupla=" + isOriginalPaginaDupla + ", vinculadoDireitaNomePagina="
				+ vinculadoDireitaNomePagina + ", vinculadoDireitaPathPagina=" + vinculadoDireitaPathPagina
				+ ", vinculadoDireitaPagina=" + vinculadoDireitaPagina + ", vinculadoDireitaPaginas="
				+ vinculadoDireitaPaginas + ", isVinculadoDireitaPaginaDupla=" + isVinculadoDireitaPaginaDupla
				+ ", vinculadoEsquerdaNomePagina=" + vinculadoEsquerdaNomePagina + ", vinculadoEsquerdaPathPagina="
				+ vinculadoEsquerdaPathPagina + ", vinculadoEsquerdaPagina=" + vinculadoEsquerdaPagina
				+ ", vinculadoEsquerdaPaginas=" + vinculadoEsquerdaPaginas + ", isVinculadoEsquerdaPaginaDupla="
				+ isVinculadoEsquerdaPaginaDupla + ", imagemDupla=" + isImagemDupla + ", naoVinculado=" + isNaoVinculado
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
