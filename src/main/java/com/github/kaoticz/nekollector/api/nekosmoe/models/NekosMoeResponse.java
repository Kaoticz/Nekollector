package com.github.kaoticz.nekollector.api.nekosmoe.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.List;

/**
 * Represents a response from the nekos.moe API.
 */
public class NekosMoeResponse {
    private List<NekosMoeImage> images;

    /**
     * Gets the API images.
     * @return The API images.
     */
    @JsonGetter("images")
    public List<NekosMoeImage> getImages() {
        return images;
    }

    /**
     * Sets the API images.
     * @param images The API images.
     */
    @JsonSetter("images")
    private void setImages(NekosMoeImage[] images) {
        this.images = List.of(images);
    }
}