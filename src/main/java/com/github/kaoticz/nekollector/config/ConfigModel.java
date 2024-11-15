package com.github.kaoticz.nekollector.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.HashMap;
import java.util.Map;

public class ConfigModel {
    private String nasaKey = "DEMO_KEY";
    private String unsplashKey;
    private HashMap<String, String> favorites = new HashMap<>();

    @JsonGetter("nasa_key")
    public String getNasaKey() {
        return nasaKey;
    }

    @JsonSetter("nasa_key")
    public void setNasaKey(String nasaKey) {
        this.nasaKey = nasaKey;
    }

    @JsonGetter("unsplash_key")
    public String getUnsplashKey() {
        return unsplashKey;
    }

    @JsonSetter("unsplash_key")
    public void setUnsplashKey(String unsplashKey) {
        this.unsplashKey = unsplashKey;
    }

    @JsonGetter("favorites")
    public Map<String, String> getFavorites() {
        return favorites;
    }

    @JsonSetter("favorites")
    private void setFavorites(HashMap<String, String> favorites) {
        this.favorites = favorites;
    }
}