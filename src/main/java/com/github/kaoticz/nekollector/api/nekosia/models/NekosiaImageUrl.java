package com.github.kaoticz.nekollector.api.nekosia.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents a Nekosia image URL.
 */
public class NekosiaImageUrl {
    private NekosiaOriginalImageUrl nekosiaOriginalImageUrl;

    /**
     * Gets the URL to the original image file.
     * @return The URL to the original image file.
     */
    @JsonGetter("original")
    public NekosiaOriginalImageUrl getOriginal() {
        return nekosiaOriginalImageUrl;
    }

    /**
     * Sets the URL to the original image file.
     * @param nekosiaOriginalImageUrl The URL to the original image file.
     */
    @JsonSetter("original")
    private void setOriginal(NekosiaOriginalImageUrl nekosiaOriginalImageUrl) {
        this.nekosiaOriginalImageUrl = nekosiaOriginalImageUrl;
    }
}
