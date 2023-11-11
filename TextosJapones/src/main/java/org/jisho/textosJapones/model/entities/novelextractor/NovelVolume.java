package org.jisho.textosJapones.model.entities.novelextractor;

import com.google.gson.annotations.Expose;
import org.jisho.textosJapones.model.enums.Language;

import java.util.*;

public class NovelVolume {

    private UUID id;
    @Expose
    private String novel;
    @Expose
    private String titulo;
    @Expose
    private String tituloAlternativo;
    @Expose
    private String descricao;
    @Expose
    private String arquivo;
    @Expose
    private String editora;
    @Expose
    private Integer volume;
    @Expose
    private Language lingua;
    @Expose
    private List<NovelCapitulo> capitulos;
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

    public void setNovel(String novel) {
        this.novel = novel;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTituloAlternativo() {
        return tituloAlternativo;
    }

    public void setTituloAlternativo(String tituloAlternativo) {
        this.tituloAlternativo = tituloAlternativo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getArquivo() {
        return arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Language getLingua() {
        return lingua;
    }

    public void setLingua(Language lingua) {
        this.lingua = lingua;
    }

    public List<NovelCapitulo> getCapitulos() {
        return capitulos;
    }

    public void setCapitulos(List<NovelCapitulo> capitulos) {
        this.capitulos = capitulos;
    }

    public void addCapitulos(NovelCapitulo capitulo) {
        this.capitulos.add(capitulo);
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

    public NovelVolume() {
        super();
        this.id = null;
        this.novel = "";
        this.titulo = "";
        this.tituloAlternativo = "";
        this.descricao = "";
        this.editora = "";
        this.volume = 0;
        this.lingua = Language.PORTUGUESE;
        this.arquivo = "";
        this.processado = false;
        this.vocabularios = new HashSet<>();
        this.capitulos = new ArrayList<>();
    }

    public NovelVolume(UUID id, String novel, String titulo, String tituloAlternativo, String descricao, String arquivo, String editora, Integer volume, Language lingua, Boolean processado) {
        this.id = id;
        this.novel = novel;
        this.titulo = titulo;
        this.tituloAlternativo = tituloAlternativo;
        this.descricao = descricao;
        this.arquivo = arquivo;
        this.editora = editora;
        this.volume = volume;
        this.lingua = lingua;
        this.processado = processado;
        this.vocabularios = new HashSet<>();
        this.capitulos = new ArrayList<>();
    }

    public NovelVolume(UUID id, String novel, String titulo, String tituloAlternativo, String descricao, String arquivo, String editora, Integer volume, Language lingua,
                       List<NovelCapitulo> capitulos, Set<NovelVocabulario> vocabularios, Boolean processado) {
        this.id = id;
        this.novel = novel;
        this.titulo = titulo;
        this.tituloAlternativo = tituloAlternativo;
        this.descricao = descricao;
        this.arquivo = arquivo;
        this.editora = editora;
        this.volume = volume;
        this.lingua = lingua;
        this.capitulos = capitulos;
        this.vocabularios = vocabularios;
        this.processado = processado;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NovelVolume that = (NovelVolume) o;
        return Objects.equals(id, that.id) && Objects.equals(novel, that.novel) && Objects.equals(volume, that.volume) && lingua == that.lingua;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, novel, volume, lingua);
    }

    @Override
    public String toString() {
        return "NovelVolume [" + "id=" + id + ", novel=" + novel + ", titulo=" + titulo + ", tituloAlternativo=" + tituloAlternativo + ", descricao=" + descricao +
                ", arquivo=" + arquivo + ", editora=" + editora + ", volume=" + volume + ", lingua=" + lingua + "]";
    }
}
