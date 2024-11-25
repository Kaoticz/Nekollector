package com.github.kaoticz.nekollector.api.nekosia.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents an image from the Nekosia API response.
 */
public class NekosiaImage {
    private String id;
    private NekosiaImageUrl image;

    /**
     * Gets the ID of the image.
     * @return The ID of the image.
     */
    @JsonGetter("id")
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the image.
     * @param id The ID of the image.
     */
    @JsonSetter("id")
    private void setId(String id){
        this.id = id;
    }

    /**
     * Gets the URL of the image.
     * @return The URL to the image.
     */
    public String getUrl() {
        return image.getOriginal().getUrl();
    }

    /**
     * Sets the URL of the image.
     * @param image The URL of the image.
     */
    @JsonSetter("image")
    private void setImage(NekosiaImageUrl image){
        this.image = image;
    }
}