package org.jisho.textosJapones.model.entities.novelextractor;

import javafx.scene.image.Image;
import org.jisho.textosJapones.model.enums.Language;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.UUID;

public class NovelCapa {

    private UUID id;
    private String novel;
    private Float volume;
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

    public NovelCapa() {
        this.id = null;
        this.novel = "";
        this.volume = 0f;
        this.lingua = null;
        this.imagem = null;
        this.arquivo = "";
        this.extenssao = "";
    }

    public NovelCapa(UUID id, String novel, Float volume, Language lingua, String arquivo, String extenssao, BufferedImage imagem) {
        this.id = id;
        this.novel = novel;
        this.volume = volume;
        this.lingua = lingua;
        this.imagem = imagem;
        this.arquivo = arquivo;
        this.extenssao = extenssao;
    }
}
