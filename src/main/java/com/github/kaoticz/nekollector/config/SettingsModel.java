package com.github.kaoticz.nekollector.config;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains the data in the settings file.
 */
public class SettingsModel {
    private String defaultDownloadDirectory = "";
    private HashMap<String, String> favorites = new HashMap<>();

    /**
     * Gets the default directory images are saved to.
     * @return The absolute path to the directory.
     */
    @JsonGetter("default_download_directory")
    public String getDefaultDownloadDirectory() {
        return defaultDownloadDirectory;
    }

    /**
     * Sets the default directory images are saved to.
     * @param defaultDownloadDirectory The directory to save to.
     */
    @JsonSetter("default_download_directory")
    public void setDefaultDownloadDirectory(@NotNull String defaultDownloadDirectory) {
        if (!Files.isDirectory(Path.of(defaultDownloadDirectory))) {
            throw new IllegalArgumentException("The provided path must point to a directory: " + defaultDownloadDirectory);
        }

        this.defaultDownloadDirectory = defaultDownloadDirectory;
    }

    /**
     * Gets the favorite images.
     * @return Key-pair values of the image URL and their name.
     */
    @JsonGetter("favorites")
    public @NotNull Map<String, String> getFavorites() {
        return favorites;
    }

    /**
     * Sets the favorite images.
     * @param favorites Key-pair values of the image URL and their name.
     */
    @JsonSetter("favorites")
    private void setFavorites(@NotNull HashMap<String, String> favorites) {
        this.favorites = favorites;
    }
}
