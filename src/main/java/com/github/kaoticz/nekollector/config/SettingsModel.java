package com.github.kaoticz.nekollector.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the data in the settings file.
 */
public class SettingsModel {
    private String nasaKey = "DEMO_KEY";
    private String unsplashKey;
    private HashMap<String, String> favorites = new HashMap<>();

    /**
     * Gets the key for the Nasa APOD API.
     * @return The API key.
     */
    @JsonGetter("nasa_key")
    public String getNasaKey() {
        return nasaKey;
    }

    /**
     * Sets the key for the Nasa APOD API.
     * @param nasaKey The API key.
     */
    @JsonSetter("nasa_key")
    public void setNasaKey(String nasaKey) {
        this.nasaKey = nasaKey;
    }

    /**
     * Gets the key for the Unsplash API.
     * @return The API key.
     */
    @JsonGetter("unsplash_key")
    public String getUnsplashKey() {
        return unsplashKey;
    }

    /**
     * Sets the key for the Unsplash API.
     * @param unsplashKey The API key.
     */
    @JsonSetter("unsplash_key")
    public void setUnsplashKey(String unsplashKey) {
        this.unsplashKey = unsplashKey;
    }

    /**
     * Gets the favorite images.
     * @return Key-pair values of the image URL and their name.
     */
    @JsonGetter("favorites")
    public Map<String, String> getFavorites() {
        return favorites;
    }

    /**
     * Sets the favorite images.
     * @param favorites Key-pair values of the image URL and their name.
     */
    @JsonSetter("favorites")
    private void setFavorites(HashMap<String, String> favorites) {
        this.favorites = favorites;
    }
}