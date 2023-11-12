package org.jisho.textosJapones.model.entities;

import javafx.scene.control.CheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Revisar {

    private UUID id;
    private String vocabulario;
    private String formaBasica;
    private String leitura;
    private String portugues;
    private String ingles;
    private Boolean anime;
    private Boolean manga;
    private Boolean novel;

    final private CheckBox revisado = new CheckBox();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getPortugues() {
        return portugues;
    }

    public void setPortugues(String portugues) {
        this.portugues = portugues;
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

    public Boolean isNovel() {
        return novel;
    }

    public void setNovel(Boolean novel) {
        this.novel = novel;
    }


    public Revisar() {
        this.id = null;
        this.vocabulario = "";
        this.formaBasica = "";
        this.leitura = "";
        this.portugues = "";
        this.ingles = "";
        this.anime = false;
        this.manga = false;
        this.novel = false;
        this.revisado.setSelected(false);
    }

    public Revisar(String vocabulario) {
        this.id = null;
        this.vocabulario = vocabulario;
        this.formaBasica = "";
        this.leitura = "";
        this.portugues = "";
        this.ingles = "";
        this.anime = false;
        this.manga = false;
        this.novel = false;
        this.revisado.setSelected(false);
    }

    public Revisar(String vocabulario, String formaBasica, String leitura) {
        this.id = null;
        this.vocabulario = vocabulario;
        this.leitura = leitura;
        this.formaBasica = formaBasica;
        this.portugues = "";
        this.ingles = "";
        this.anime = false;
        this.manga = false;
        this.novel = false;
        this.revisado.setSelected(false);
    }

    public Revisar(String vocabulario, Boolean revisado, Boolean isAnime, Boolean isManga, Boolean isNovel) {
        this.id = null;
        this.vocabulario = vocabulario;
        this.leitura = "";
        this.formaBasica = "";
        this.portugues = "";
        this.ingles = "";
        this.anime = isAnime;
        this.manga = isManga;
        this.novel = isNovel;
        this.revisado.setSelected(revisado);
    }

    public Revisar(String vocabulario, String formaBasica, String leitura, Boolean revisado, Boolean isAnime, Boolean isManga, Boolean isNovel) {
        this.id = null;
        this.vocabulario = vocabulario;
        this.leitura = leitura;
        this.formaBasica = formaBasica;
        this.portugues = "";
        this.ingles = "";
        this.anime = isAnime;
        this.manga = isManga;
        this.novel = isNovel;
        this.revisado.setSelected(revisado);
    }

    public Revisar(String vocabulario, String formaBasica, String leitura, String portugues, String ingles) {
        this.id = null;
        this.vocabulario = vocabulario;
        this.formaBasica = formaBasica;
        this.leitura = leitura;
        this.portugues = portugues;
        this.ingles = ingles;
        this.anime = false;
        this.manga = false;
        this.novel = false;
        this.revisado.setSelected(false);
    }

    public Revisar(UUID id, String vocabulario, String formaBasica, String leitura, String portugues, String ingles,
                   Boolean revisado, Boolean anime, Boolean manga, Boolean novel) {
        this.id = id;
        this.vocabulario = vocabulario;
        this.formaBasica = formaBasica;
        this.leitura = leitura;
        this.portugues = portugues;
        this.ingles = ingles;
        this.anime = anime;
        this.manga = manga;
        this.novel = novel;
        this.revisado.setSelected(revisado);
    }

    @Override
    public String toString() {
        return vocabulario + ", ";
    }

    public static Vocabulario toVocabulario(Revisar revisar) {
        return new Vocabulario(null, revisar.getVocabulario(), revisar.getFormaBasica(), revisar.getLeitura(),
                revisar.getIngles(), revisar.getPortugues());
    }

    public static List<Vocabulario> toVocabulario(List<Revisar> revisar) {
        List<Vocabulario> lista = new ArrayList<>();

        for (Revisar obj : revisar)
            lista.add(toVocabulario(obj));

        return lista;
    }
}
