package org.jisho.textosJapones.model.entities;

import java.util.UUID;

public class Kanji {
    private UUID id;
    private String kanji;
    private String palavra;
    private String significado;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public Kanji(UUID id, String kanji, String palavra, String significado) {
        this.id = id;
        this.kanji = kanji;
        this.palavra = palavra;
        this.significado = significado;
    }
}
