package org.jisho.textosJapones.model.entities;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.CheckBox;

public class Revisar {

	private String vocabulario;
	private String formaBasica;
	private String leitura;
	private String traducao;
	private String ingles;
	private Boolean anime;
	private Boolean manga;

	final private CheckBox revisado = new CheckBox();

	public String getVocabulario() {
		return vocabulario;
	}

	public void setVocabulario(String vocabulario) {
		this.vocabulario = vocabulario;
	}

	public String getFormaBasica() {
		return formaBasica;
	}

	public void setFormaBasica(String formaBasica) {
		this.formaBasica = formaBasica;
	}

	public String getLeitura() {
		return leitura;
	}

	public void setLeitura(String leitura) {
		this.leitura = leitura;
	}

	public String getTraducao() {
		return traducao;
	}

	public void setTraducao(String traducao) {
		this.traducao = traducao;
	}

	public String getIngles() {
		return ingles;
	}

	public void setIngles(String ingles) {
		this.ingles = ingles;
	}

	public CheckBox getRevisado() {
		return revisado;
	}

	public Boolean isAnime() {
		return anime;
	}

	public void setAnime(Boolean anime) {
		this.anime = anime;
	}

	public Boolean isManga() {
		return manga;
	}

	public void setManga(Boolean manga) {
		this.manga = manga;
	}

	public Revisar() {
		this.vocabulario = "";
		this.formaBasica = "";
		this.leitura = "";
		this.traducao = "";
		this.ingles = "";
		this.anime = false;
		this.manga = false;
		this.revisado.setSelected(false);
	}

	public Revisar(String vocabulario) {
		this.vocabulario = vocabulario;
		this.formaBasica = "";
		this.leitura = "";
		this.traducao = "";
		this.ingles = "";
		this.anime = false;
		this.manga = false;
		this.revisado.setSelected(false);
	}

	public Revisar(String vocabulario, String formaBasica, String leitura) {
		this.vocabulario = vocabulario;
		this.leitura = leitura;
		this.formaBasica = formaBasica;
		this.traducao = "";
		this.ingles = "";
		this.anime = false;
		this.manga = false;
		this.revisado.setSelected(false);
	}

	public Revisar(String vocabulario, String formaBasica, String leitura, Boolean revisado, Boolean isAnime,
			Boolean isManga) {
		this.vocabulario = vocabulario;
		this.leitura = leitura;
		this.formaBasica = formaBasica;
		this.traducao = "";
		this.ingles = "";
		this.anime = isAnime;
		this.manga = isManga;
		this.revisado.setSelected(revisado);
	}

	public Revisar(String vocabulario, String formaBasica, String leitura, String traducao, String ingles) {
		this.vocabulario = vocabulario;
		this.formaBasica = formaBasica;
		this.leitura = leitura;
		this.traducao = traducao;
		this.ingles = ingles;
		this.anime = false;
		this.manga = false;
		this.revisado.setSelected(false);
	}

	public Revisar(String vocabulario, String formaBasica, String leitura, String traducao, String ingles,
			Boolean revisado, Boolean anime, Boolean manga) {
		this.vocabulario = vocabulario;
		this.formaBasica = formaBasica;
		this.leitura = leitura;
		this.traducao = traducao;
		this.ingles = ingles;
		this.anime = anime;
		this.manga = manga;
		this.revisado.setSelected(revisado);
	}

	@Override
	public String toString() {
		return vocabulario + ", ";
	}

	public static Vocabulario toVocabulario(Revisar revisar) {
		return new Vocabulario(revisar.getVocabulario(), revisar.getFormaBasica(), revisar.getLeitura(),
				revisar.getTraducao());
	}

	public static List<Vocabulario> toVocabulario(List<Revisar> revisar) {
		List<Vocabulario> lista = new ArrayList<>();

		for (Revisar obj : revisar)
			lista.add(toVocabulario(obj));

		return lista;
	}
}
