package org.jisho.textosJapones.model.entities.novelextractor;

import com.google.gson.annotations.Expose;

import java.util.Objects;
import java.util.UUID;

public class NovelTexto {

    private UUID id;
    @Expose
    private String texto;
    @Expose
    private Integer sequencia;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Integer getSequencia() {
        return sequencia;
    }

    public void setSequencia(Integer sequencia) {
        this.sequencia = sequencia;
    }

    public NovelTexto() {
        super();
        this.id = null;
        this.texto = "";
        this.sequencia = 0;
    }

    public NovelTexto(UUID id, String texto, Integer sequencia) {
        super();
        this.id = id;
        this.texto = texto;
        this.sequencia = sequencia;
    }

    @Override
    public String toString() {
        return "NovelTexto [id=" + id + ", texto=" + texto + ", sequencia=" + sequencia + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NovelTexto that = (NovelTexto) o;
        return Objects.equals(id, that.id) && Objects.equals(texto, that.texto) && Objects.equals(sequencia, that.sequencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, texto, sequencia);
    }
}
