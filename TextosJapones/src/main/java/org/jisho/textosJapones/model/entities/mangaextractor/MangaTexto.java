package org.jisho.textosJapones.model.entities.mangaextractor;

import com.google.gson.annotations.Expose;
import org.jisho.textosJapones.model.entities.Manga;

import java.util.Objects;
import java.util.UUID;

public class MangaTexto extends Manga {

    private UUID id;
    @Expose
    private String texto;
    @Expose
    private Integer sequencia;
    @Expose
    private Integer x1;
    @Expose
    private Integer y1;
    @Expose
    private Integer x2;
    @Expose
    private Integer y2;

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

    public Integer getX1() {
        return x1;
    }

    public void setX1(Integer x1) {
        this.x1 = x1;
    }

    public Integer getY1() {
        return y1;
    }

    public void setY1(Integer y1) {
        this.y1 = y1;
    }

    public Integer getX2() {
        return x2;
    }

    public void setX2(Integer x2) {
        this.x2 = x2;
    }

    public Integer getY2() {
        return y2;
    }

    public void setY2(Integer y2) {
        this.y2 = y2;
    }

    public MangaTexto() {
        super();
        this.id = null;
        this.texto = "";
        this.sequencia = 0;
        this.x1 = 0;
        this.y1 = 0;
        this.x2 = 0;
        this.y2 = 0;
    }

    public MangaTexto(UUID id, String texto, Integer sequencia, Integer x1, Integer y1, Integer x2, Integer y2) {
        super();
        this.id = id;
        this.texto = texto;
        this.sequencia = sequencia;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    @Override
    public String toString() {
        return "MangaTexto [id=" + id + ", texto=" + texto + ", sequencia=" + sequencia + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MangaTexto that = (MangaTexto) o;
        return Objects.equals(id, that.id) && Objects.equals(texto, that.texto) && Objects.equals(sequencia, that.sequencia);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, texto, sequencia);
    }
}
