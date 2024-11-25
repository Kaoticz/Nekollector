package com.github.kaoticz.nekollector.api.nekosbest.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Represents an image from the nekos.best API.
 */
public class NekosBestImage {
    private String url;

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
}
