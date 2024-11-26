package com.github.kaoticz.nekollector.api.nekosmoe.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.List;

/**
 * Represents an image from the nekos.moe API.
 */
public class NekosMoeImage {
    private String url;
    private List<String> tags;

    /**
     * Gets the URL to the image.
     * @return The URL to the image.
     */
    @JsonGetter("id")
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL to the image.
     * @param id The ID of the image.
     */
    @JsonSetter("id")
    private void setUrl(String id) {
        this.url = "https://nekos.moe/image/" + id;
    }

    /**
     * Gets the image tags.
     * @return The image tags.
     */
    @JsonGetter("tags")
    public List<String> getTags() {
        return tags;
    }

    /**
     * Sets the image tags.
     * @param tags The image tags.
     */
    @JsonSetter("tags")
    private void setTags(String[] tags) {
        this.tags = List.of(tags);
    }
}