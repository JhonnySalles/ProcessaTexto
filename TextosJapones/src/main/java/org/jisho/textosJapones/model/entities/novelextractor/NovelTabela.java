package org.jisho.textosJapones.model.entities.novelextractor;

import org.jisho.textosJapones.model.entities.Novel;

import java.util.ArrayList;
import java.util.List;

public class NovelTabela extends Novel {

    private List<NovelVolume> volumes;

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
