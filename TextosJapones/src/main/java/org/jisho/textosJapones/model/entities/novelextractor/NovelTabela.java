package org.jisho.textosJapones.model.entities.novelextractor;

import java.util.ArrayList;
import java.util.List;

public class NovelTabela {

    private String base;
    private List<NovelVolume> volumes;
    private Integer quantidade;

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public List<NovelVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<NovelVolume> volumes) {
        this.volumes = volumes;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public NovelTabela() {
        this.base = "";
        this.quantidade = 0;
        this.volumes = new ArrayList<>();
    }

    public NovelTabela(String base, Integer quantidade, List<NovelVolume> volumes) {
        this.base = base;
        this.quantidade = quantidade;
        this.volumes = volumes;
    }

}
