package br.com.fenix.processatexto.model.entities.firebase;

import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class Vocabulario {
    protected UUID id;
    @Expose
    protected String vocabulario;
    protected String formaBasica;
    @Expose
    protected String leitura;
    protected String leituraNovel;
    @Expose
    protected String ingles;
    @Expose
    protected String portugues;
    public String sincronizacao;

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

    public String getLeituraNovel() {
        return leituraNovel;
    }

    public void setLeituraNovel(String leituraNovel) {
        this.leituraNovel = leituraNovel;
    }

    public String getIngles() {
        return ingles;
    }

    public void setIngles(String ingles) {
        this.ingles = ingles;
    }

    public String getPortugues() {
        return portugues;
    }

    public void setPortugues(String portugues) {
        this.portugues = portugues;
    }


    public Vocabulario(br.com.fenix.processatexto.model.entities.processatexto.Vocabulario vocabulario) {
        this.id = vocabulario.getId();
        this.vocabulario = vocabulario.getVocabulario();
        this.formaBasica = vocabulario.getFormaBasica();
        this.leitura = vocabulario.getLeitura();
        this.leituraNovel = vocabulario.getLeituraNovel();
        this.portugues = vocabulario.getPortugues();
        this.ingles = vocabulario.getIngles();
        this.sincronizacao = vocabulario.getSincronizacao();
    }

    public Vocabulario(UUID id, String vocabulario, String formaBasica, String leitura, String leituraNovel, String ingles, String portugues) {
        this.id = id;
        this.vocabulario = vocabulario;
        this.formaBasica = formaBasica;
        this.leitura = leitura;
        this.leituraNovel = leituraNovel;
        this.portugues = portugues;
        this.ingles = ingles;
    }

    public static br.com.fenix.processatexto.model.entities.processatexto.Vocabulario toVocabulario(String id, HashMap<String, ?> obj) {
        return new br.com.fenix.processatexto.model.entities.processatexto.Vocabulario(UUID.fromString(id), (String) obj.get("vocabulario"), (String) obj.get("formaBasica"),
                (String) obj.get("leitura"), (String) obj.get("leituraNovel"), (String) obj.get("ingles"), (String) obj.get("portugues"));
    }

    @Override
    public String toString() {
        return vocabulario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vocabulario that = (Vocabulario) o;
        return Objects.equals(id, that.id) && Objects.equals(vocabulario, that.vocabulario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vocabulario);
    }
}
