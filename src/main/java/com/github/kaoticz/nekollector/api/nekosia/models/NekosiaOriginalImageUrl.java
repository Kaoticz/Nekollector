package com.github.kaoticz.nekollector.api.nekosia.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents an original image from the Nekosia API.
 */
public class NekosiaOriginalImageUrl {
    private String url;
    private String extension;

    /**
     * Gets the URL to the image.
     * @return The URL to the image.
     */
    @JsonGetter("url")
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL to the image.
     * @param url The URL to the image.
     */
    @JsonSetter("url")
    private void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets the file extension of the image.
     * @return The file extension of the image.
     */
    @JsonGetter("extension")
    public String getExtension() {
        return extension;
    }

    /**
     * Sets the file extension of the image.
     * @param extension The file extension of the image.
     */
    @JsonSetter("extension")
    public void setExtension(String extension) {
        this.extension = extension;
    }
}