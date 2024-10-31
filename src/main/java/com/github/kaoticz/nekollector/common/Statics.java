package com.github.kaoticz.nekollector.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import java.net.http.HttpClient;
import java.util.Objects;

public class Statics {
    public static final Image LOADING_IMAGE = new Image(Objects.requireNonNull(Statics.class.getResourceAsStream("/assets/loading.gif")), 198, 198, true, true);

    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static final ObjectMapper JSON_DESERIALIZER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
