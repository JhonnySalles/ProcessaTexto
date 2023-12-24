package org.jisho.textosJapones.model.entities.subtitle;

import org.jisho.textosJapones.model.enums.Language;

import java.util.UUID;

public class Legenda {
    private UUID id;
    private Integer sequencia;
    private Integer episodio;
    private Language linguagem;
    private String tempo;
    private String texto;
    private String traducao;
    private String vocabulario;
    private String som;
    private String imagem;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getSequencia() {
        return sequencia;
    }

    public void setSequencia(Integer sequencia) {
        this.sequencia = sequencia;
    }

    public Integer getEpisodio() {
        return episodio;
    }

    public void setEpisodio(Integer episodio) {
        this.episodio = episodio;
    }

    public Language getLinguagem() {
        return linguagem;
    }

    public void setLinguagem(Language linguagem) {
        this.linguagem = linguagem;
    }

    public String getTempo() {
        return tempo;
    }

    public void setTempo(String tempo) {
        this.tempo = tempo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getTraducao() {
        return traducao;
    }

    public void setTraducao(String traducao) {
        this.traducao = traducao;
    }

    public String getSom() {
        return som;
    }

    public void setSom(String som) {
        this.som = som;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getVocabulario() {
        return vocabulario;
    }

    public void setVocabulario(String vocabulario) {
        this.vocabulario = vocabulario;
    }

    public Legenda(Integer sequencia, Integer episodio, Language linguagem, String tempo, String texto, String traducao, String som, String imagem, String vocabulario) {
        this.id = null;
        this.sequencia = sequencia;
        this.episodio = episodio;
        this.linguagem = linguagem;
        this.tempo = tempo;
        this.texto = texto;
        this.traducao = traducao;
        this.som = som;
        this.imagem = imagem;
        this.vocabulario = vocabulario;
    }
}
