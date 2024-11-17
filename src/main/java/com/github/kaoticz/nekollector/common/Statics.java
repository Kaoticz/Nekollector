package com.github.kaoticz.nekollector.common;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

import java.net.http.HttpClient;
import java.util.Objects;

/**
 * Contains global objects relevant to the application.
 */
public class Statics {
    public static final String FAVORITE_BUTTON_TEXT = "\uD83D\uDFCA Favorite";

    public static final String NOT_FAVORITE_BUTTON_TEXT = "☆ Favorite";
    /**
     * The loading gif shown before the requested image.
     */
    public static final Image LOADING_IMAGE = new Image(Objects.requireNonNull(Statics.class.getResourceAsStream("/assets/images/loading.gif")));

    /**
     * The program's HTTP client.
     */
    public static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * Defines a rounded corner for stroke components.
     */
    public static final CornerRadii SMOOTH_CORNER = new CornerRadii(8);

    /**
     * Defines a transparent border.
     */
    public static final Border DESELECTION_BORDER = new Border(new BorderStroke(Paint.valueOf("C8C8C8"), BorderStrokeStyle.SOLID, SMOOTH_CORNER, BorderStroke.THIN));

    /**
     * Defines the border for selected favorite images.
     */
    public static final Border SELECTION_BORDER = new Border(new BorderStroke(Paint.valueOf("FF0067"), BorderStrokeStyle.SOLID, SMOOTH_CORNER, BorderStroke.MEDIUM));

    /**
     * The JSON serializer and deserializer.
     */
    public static final ObjectMapper JSON_DESERIALIZER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.INDENT_OUTPUT, true);
}
