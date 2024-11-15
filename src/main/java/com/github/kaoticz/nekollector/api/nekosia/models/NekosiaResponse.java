package com.github.kaoticz.nekollector.api.nekosia.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class NekosiaResponse {
    private String name;
    private NekosiaImage image;

    @JsonGetter("id")
    public String getName() {
        return name;
    }

    @JsonSetter("id")
    private void setName(String name){
        this.name = name;
    }

    public String getUrl() {
        return image.getOriginal().getUrl();
    }

    @JsonSetter("image")
    private void setImage(NekosiaImage image){
        this.image = image;
    }
}