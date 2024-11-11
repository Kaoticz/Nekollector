package com.github.kaoticz.nekollector.api.abstractions;

import javafx.scene.image.Image;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an API service that fetches images
 */
public interface ApiService {
    /**
     * Gets the name of the service.
     * @return The name of the service.
     */
    String getServiceName();
    /**
     * Gets a random image from the API.
     * @return A promise of the request with an image.
     */
    CompletableFuture<Image> getImageAsync();
}
