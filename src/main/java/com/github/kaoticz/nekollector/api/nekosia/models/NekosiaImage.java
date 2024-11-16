package com.github.kaoticz.nekollector.api.nekosia.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class NekosiaImage {
    private Original original;

    @JsonGetter("original")
    public Original getOriginal() {
        return original;
    }

    @JsonSetter("original")
    private void setOriginal(Original original) {
        this.original = original;
    }
}
