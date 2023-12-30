package org.jisho.textosJapones.model.entities.novelextractor;

import com.google.gson.annotations.Expose;
import org.jisho.textosJapones.model.entities.Novel;
import org.jisho.textosJapones.model.entities.VocabularioExterno;
import org.jisho.textosJapones.model.enums.Language;

import java.util.*;

public class NovelCapitulo extends Novel {

    private UUID id;
    @Expose
    private String novel;
    @Expose
    private Float volume;
    @Expose
    private Float capitulo;
    @Expose
    private String descricao;
    @Expose
    private Integer sequencia;
    @Expose
    private Language lingua;
    @Expose
    private List<NovelTexto> textos;
    @Expose
    private Set<VocabularioExterno> vocabularios;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNovel() {
        return novel;
    }

    public void setNovel(String novel) {
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public Set<VocabularioExterno> getVocabularios() {
        return vocabularios;
    }

    public void setVocabularios(Set<VocabularioExterno> vocabularios) {
        this.vocabularios = vocabularios;
    }

    public void addVocabulario(VocabularioExterno vocabulario) {
        this.vocabularios.add(vocabulario);
    }

    public NovelCapitulo() {
        super();
        this.id = null;
        this.novel = "";
        this.volume = 0f;
        this.capitulo = 0F;
        this.descricao = "";
        this.sequencia = 0;
        this.lingua = Language.PORTUGUESE;
        this.vocabularios = new HashSet<>();
        this.textos = new ArrayList<>();
    }

    public NovelCapitulo(UUID id, String novel, Float volume, Float capitulo, String descricao, Integer sequencia, Language lingua) {
        super(novel, volume, capitulo);
        this.id = id;
        this.novel = novel;
        this.volume = volume;
        this.capitulo = capitulo;
        this.descricao = descricao;
        this.sequencia = sequencia;
        this.lingua = lingua;
        this.vocabularios = new HashSet<>();
        this.textos = new ArrayList<>();
    }

    public NovelCapitulo(UUID id, String novel, Float volume, Float capitulo, String descricao, Integer sequencia, Language lingua,
                         List<NovelTexto> textos, Set<VocabularioExterno> vocabularios) {
        super(novel, volume, capitulo);
        this.id = id;
        this.novel = novel;
        this.volume = volume;
        this.capitulo = capitulo;
        this.descricao = descricao;
        this.sequencia = sequencia;
        this.lingua = lingua;
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
