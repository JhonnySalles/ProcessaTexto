package org.jisho.textosJapones.model.entities.mangaextractor;

import org.jisho.textosJapones.model.entities.Manga;
import org.jisho.textosJapones.model.enums.Language;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class MangaCapa extends Manga {

    private UUID id;
    private String manga;
    private Integer volume;
    private Language lingua;

    private String arquivo;
    private String extenssao;
    private BufferedImage imagem;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getManga() {
        return manga;
    }

    public void setManga(String manga) {
        this.manga = manga;
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

    public String getArquivo() {
        return arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public String getExtenssao() {
        return extenssao;
    }

    public void setExtenssao(String extenssao) {
        this.extenssao = extenssao;
    }

    public BufferedImage getImagem() {
        return imagem;
    }

    public void setImagem(BufferedImage imagem) {
        this.imagem = imagem;
    }

    public MangaCapa() {
        this.id = null;
        this.manga = "";
        this.volume = 0;
        this.lingua = null;
        this.imagem = null;
        this.arquivo = "";
        this.extenssao = "";
    }

    public MangaCapa(UUID id, String manga, Integer volume, Language lingua, String arquivo, String extenssao, BufferedImage imagem) {
        this.id = id;
        this.manga = manga;
        this.volume = volume;
        this.lingua = lingua;
        this.imagem = imagem;
        this.arquivo = arquivo;
        this.extenssao = extenssao;
    }
}
