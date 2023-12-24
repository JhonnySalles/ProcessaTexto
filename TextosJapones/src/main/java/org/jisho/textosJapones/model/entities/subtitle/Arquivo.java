package org.jisho.textosJapones.model.entities.subtitle;

import java.io.File;

public class Arquivo {
    private String pasta;
    private String nome;
    private Integer episodio;
    private File arquivo;
    private Boolean processar;

    public String getPasta() {
        return pasta;
    }

    public void setPasta(String pasta) {
        this.pasta = pasta;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public File getArquivo() {
        return arquivo;
    }

    public void setArquivo(File arquivo) {
        this.arquivo = arquivo;
    }

    public Integer getEpisodio() {
        return episodio;
    }

    public void setEpisodio(Integer episodio) {
        this.episodio = episodio;
    }

    public Boolean isProcessar() {
        return processar;
    }

    public void setProcessar(Boolean processar) {
        this.processar = processar;
    }

    public Arquivo(String pasta, String nome, File arquivo, Integer episodio) {
        this.pasta = pasta;
        this.nome = nome;
        this.arquivo = arquivo;
        this.episodio = episodio;
        this.processar = true;
    }
}
