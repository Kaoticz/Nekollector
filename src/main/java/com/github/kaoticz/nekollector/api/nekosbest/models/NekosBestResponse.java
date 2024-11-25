package com.github.kaoticz.nekollector.api.nekosbest.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents a response from the nekos.best API.
 */
public class NekosBestResponse {
    private NekosBestImage[] images;

    /**
     * Gets the API images.
     * @return The API images.
     */
    @JsonGetter("results")
    public NekosBestImage[] getImages() {
        return images;
    }

    /**
     * Sets the API images.
     * @param images The API images.
     */
    @JsonSetter("results")
    private void setImages(NekosBestImage[] images) {
        this.images = images;
    }
}
