package org.jisho.textosJapones.model.entities;

public class Kanji {
    private Long id;
    private String kanji;
    private String palavra;
    private String significado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKanji() {
        return kanji;
    }

    public void setKanji(String kanji) {
        this.kanji = kanji;
    }

    public String getPalavra() {
        return palavra;
    }

    public void setPalavra(String palavra) {
        this.palavra = palavra;
    }

    public String getSignificado() {
        return significado;
    }

    public void setSignificado(String significado) {
        this.significado = significado;
    }

    public Kanji(Long id, String kanji, String palavra, String significado) {
        this.id = id;
        this.kanji = kanji;
        this.palavra = palavra;
        this.significado = significado;
    }
}
