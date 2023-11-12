package org.jisho.textosJapones.model.entities.novelextractor;

import java.util.ArrayList;
import java.util.List;

public class NovelTabela {

    private String base;
    private List<NovelVolume> volumes;

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

    public void addVolume(NovelVolume volume) {
        this.volumes.add(volume);
    }

    public NovelTabela() {
        this.base = "";
        this.volumes = new ArrayList<>();
    }

    public NovelTabela(String base, List<NovelVolume> volumes) {
        this.base = base;
        this.volumes = volumes;
    }

}
