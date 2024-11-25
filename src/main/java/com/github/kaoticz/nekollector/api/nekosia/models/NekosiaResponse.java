package com.github.kaoticz.nekollector.api.nekosia.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a response from the Nekosia API.
 */
public class NekosiaResponse {
    private boolean success;
    private int httpStatus;
    private int count;
    private NekosiaImage[] images;

    /**
     * Gets whether the request was successful or not.
     * @return The success of the operation.
     */
    @JsonGetter("success")
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets whether the operation was successful or not.
     * @param success The success of the operation.
     */
    @JsonSetter("success")
    private void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets the HTTP status of the operation.
     * @return The HTTP status of the operation.
     */
    @JsonGetter("status")
    public int getHttpStatus() {
        return httpStatus;
    }

    /**
     * Sets the HTTP status of the operation.
     * @param httpStatus The HTTP status of the operation.
     */
    @JsonSetter("status")
    private void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    /**
     * Gets how many images are available in the current user session.
     * @return The amount of images still available in the current user session.
     */
    @JsonGetter("count")
    public int getCount() {
        return count;
    }

    /**
     * Sets how many images are available in the current user session.
     * @param count The amount of images still available in the current user session.
     */
    @JsonSetter("count")
    private void setCount(int count) {
        this.count = count;
    }

    /**
     * Gets the API images.
     * @return The API images.
     */
    @JsonGetter("images")
    public @NotNull NekosiaImage[] getImages() {
        return images;
    }

    /**
     * Sets the API images.
     * @param images The API images.
     */
    @JsonSetter("images")
    private void setImages(@NotNull NekosiaImage[] images) {
        this.images = images;
    }
}