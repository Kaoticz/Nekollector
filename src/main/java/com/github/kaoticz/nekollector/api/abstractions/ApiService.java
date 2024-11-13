package com.github.kaoticz.nekollector.api.abstractions;

import com.github.kaoticz.nekollector.api.models.ApiResult;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an API service that fetches images
 */
public interface ApiService {
    /**
     * Gets a random image from the API.
     * @return A promise of the request with an image.
     */
    CompletableFuture<ApiResult> getImageAsync();
}
