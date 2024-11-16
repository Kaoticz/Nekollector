package com.github.kaoticz.nekollector.api.models;

import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the result of an API request.
 * @param serviceName The name of the API service that handled the request.
 * @param apiImage The image returned by the API.
 */
public record ApiResult(@NotNull String serviceName, @NotNull Image apiImage) {
}