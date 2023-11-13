package org.jisho.textosJapones.model.entities.novelextractor;

import com.google.gson.annotations.Expose;
import org.jisho.textosJapones.model.enums.Language;

import java.util.*;

public class NovelCapitulo {

    private UUID id;
    @Expose
    private String novel;
    @Expose
    private Float volume;
    @Expose
    private Float capitulo;
    @Expose
    private Integer sequencia;
    @Expose
    private Language lingua;
    @Expose
    private List<NovelTexto> textos;
    @Expose
    private Set<NovelVocabulario> vocabularios;
    private Boolean processado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNovel() {
        return novel;
    }

    public void setNovel(String manga) {
        this.novel = novel;
    }

    public Float getVolume() {
        return volume;
    }

    public void setVolume(Float volume) {
        this.volume = volume;
    }

    public Float getCapitulo() {
        return capitulo;
    }

    public void setCapitulo(Float capitulo) {
        this.capitulo = capitulo;
    }

    public Integer getSequencia() {
        return sequencia;
    }

    public void setSequencia(Integer sequencia) {
        this.sequencia = sequencia;
    }

    public Language getLingua() {
        return lingua;
    }

    public void setLingua(Language lingua) {
        this.lingua = lingua;
    }

    public List<NovelTexto> getTextos() {
        return textos;
    }

    public void setTextos(List<NovelTexto> textos) {
        this.textos = textos;
    }

    public void addTexto(NovelTexto texto) {
        this.textos.add(texto);
    }

    public Set<NovelVocabulario> getVocabularios() {
        return vocabularios;
    }

    public void setVocabularios(Set<NovelVocabulario> vocabularios) {
        this.vocabularios = vocabularios;
    }

    public void addVocabulario(NovelVocabulario vocabulario) {
        this.vocabularios.add(vocabulario);
    }

    public Boolean getProcessado() {
        return processado;
    }

    public void setProcessado(Boolean processado) {
        this.processado = processado;
    }

    public NovelCapitulo() {
        this.id = null;
        this.novel = "";
        this.volume = 0f;
        this.capitulo = 0F;
        this.sequencia = 0;
        this.lingua = Language.PORTUGUESE;
        this.vocabularios = new HashSet<>();
        this.textos = new ArrayList<>();
        this.processado = false;
    }

    public NovelCapitulo(UUID id, String novel, Float volume, Float capitulo, Integer sequencia, Language lingua, Boolean processado) {
        this.id = id;
        this.novel = novel;
        this.volume = volume;
        this.capitulo = capitulo;
        this.sequencia = sequencia;
        this.lingua = lingua;
        this.processado = processado;
        this.vocabularios = new HashSet<>();
        this.textos = new ArrayList<>();
    }

    public NovelCapitulo(UUID id, String novel, Float volume, Float capitulo, Integer sequencia, Language lingua, Boolean processado,
                         List<NovelTexto> textos, Set<NovelVocabulario> vocabularios) {
        this.id = id;
        this.novel = novel;
        this.volume = volume;
        this.capitulo = capitulo;
        this.sequencia = sequencia;
        this.lingua = lingua;
        this.processado = processado;
        this.textos = textos;
        this.vocabularios = vocabularios;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NovelCapitulo that = (NovelCapitulo) o;
        return Objects.equals(id, that.id) && Objects.equals(novel, that.novel) &&
                Objects.equals(volume, that.volume) && Objects.equals(capitulo, that.capitulo) &&
                lingua == that.lingua;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, novel, volume, capitulo, lingua);
    }

    @Override
    public String toString() {
        return "NovelCapitulo [id=" + id + ", capitulo=" + capitulo + ", lingua=" + lingua + ", textos=" + textos + "]";
    }

}
