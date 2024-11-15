package com.github.kaoticz.nekollector.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.image.Image;
import java.net.http.HttpClient;
import java.util.Objects;

/**
 * Contains global objects relevant to the application.
 */
public class Statics {
    /**
     * The loading gif shown before the requested image.
     */
    public static final Image LOADING_IMAGE = new Image(Objects.requireNonNull(Statics.class.getResourceAsStream("/assets/loading.gif")));

    /**
     * The program's HTTP client.
     */
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * The JSON serializer and deserializer.
     */
    public static final ObjectMapper JSON_DESERIALIZER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);
}
