package com.github.kaoticz.nekollector.api.nekosia.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class Original {
    private String url;

    @JsonGetter("url")
    public String getUrl() {
        return url;
    }

    @JsonSetter("url")
    private void setUrl(String url) {
        this.url = url;
    }
}