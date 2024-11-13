package com.github.kaoticz.nekollector.services;

import com.github.kaoticz.nekollector.api.abstractions.ApiService;
import com.github.kaoticz.nekollector.api.models.ApiResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles API requests and manages their results.
 */
public class ApiCoordinator {
    private static final Random RANDOM = new Random();

    private final List<ApiService> apiServices;

    private final ArrayList<ApiResult> cachedResults = new ArrayList<>();

    private int currentIndex = -1;

    /**
     * Handles API requests and manages their results.
     * @param apiServices The API services to be handled.
     */
    public ApiCoordinator(ApiService... apiServices) {
        if (apiServices.length == 0) {
            throw new IllegalArgumentException("At least one API service must be provided.");
        }

        this.apiServices = List.of(apiServices);
    }

    /**
     * Gets the current position in the list of images.
     * @return The current index in the list.
     */
    public int currentIndex() {
        return this.currentIndex;
    }

    /**
     * Gets the previous image in the list.
     * @return The previous image.
     * @throws IllegalStateException Occurs when the current index is at the beginning of the list.
     */
    public ApiResult getPreviousImage() throws IllegalStateException {
        if (this.currentIndex <= 0) {
            throw new IllegalStateException("Cannot get previous image as the current index is at the beginning of the list.");
        }

        return this.cachedResults.get(--currentIndex);
    }

    /**
     * Gets the next image in the list. If there are none, it requests one and adds it to the list.
     * @return A future with the next image.
     */
    public CompletableFuture<ApiResult> getNextImageAsync() {
        if (this.currentIndex < this.cachedResults.size() - 1) {
            return CompletableFuture.completedFuture(this.cachedResults.get(++this.currentIndex));
        }

        var randomApiService = apiServices.get(RANDOM.nextInt(apiServices.size()));
        return randomApiService.getImageAsync()
                .orTimeout(17, TimeUnit.SECONDS)
                .thenApply(apiResult -> {
                    cachedResults.add(apiResult);
                    this.currentIndex++;

                    return apiResult;
                });
    }
}