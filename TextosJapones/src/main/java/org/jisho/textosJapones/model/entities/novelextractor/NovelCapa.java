package org.jisho.textosJapones.model.entities.novelextractor;

import javafx.scene.image.Image;
import org.jisho.textosJapones.model.enums.Language;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.UUID;

public class NovelCapa {

    private UUID id;
    private String novel;
    private Integer volume;
    private Language lingua;
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

    public BufferedImage getImagem() {
        return imagem;
    }

    public void setImagem(BufferedImage imagem) {
        this.imagem = imagem;
    }

    public NovelCapa() {
        this.id = null;
        this.novel = null;
        this.volume = null;
        this.lingua = null;
        this.imagem = null;
    }

    public NovelCapa(UUID id, String novel, Integer volume, Language lingua, BufferedImage imagem) {
        this.id = id;
        this.novel = novel;
        this.volume = volume;
        this.lingua = lingua;
        this.imagem = imagem;
    }
}
