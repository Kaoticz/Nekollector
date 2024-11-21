package com.github.kaoticz.nekollector.config;

import com.github.kaoticz.nekollector.common.Statics;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Handles the configuration file used by the application to store API credentials and program data.
 */
public class SettingsManager {
    private final Path settingsPath;
    private SettingsModel settings;

    /**
     * Initializes an object that handles the configuration file used by the application.
     */
    public SettingsManager() {
        var settingsDirUri = System.getProperty("user.dir") + File.separatorChar + "data";
        var settingsDirPath = Path.of(settingsDirUri);
        this.settingsPath = Path.of(settingsDirUri + File.separatorChar + "settings.json");

        try {
            if (!Files.exists(settingsDirPath)) {
                Files.createDirectory(settingsDirPath);
            }

            if (!Files.exists(settingsPath)) {
                Files.createFile(settingsPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.settings = Statics.JSON_DESERIALIZER.readValue(Files.readString(settingsPath), SettingsModel.class);
        } catch (IOException e) {
            this.settings = new SettingsModel();
        }

        if (this.settings.getDefaultDownloadDirectory() == null || this.settings.getDefaultDownloadDirectory().isBlank()) {
            this.settings.setDefaultDownloadDirectory(settingsDirUri);
        }
    }

    /**
     * Gets the settings used by the application.
     * @return The settings.
     */
    public @NotNull SettingsModel getSettings() {
        return settings;
    }

    /**
     * Saves the current settings to the settings file.
     * @param action The changes to be performed to the current settings.
     * @throws IOException Occurs when writing to the file fails.
     */
    public void saveSettings(@NotNull Consumer<SettingsModel> action) throws IOException {
        action.accept(this.settings);
        var json = Statics.JSON_DESERIALIZER.writeValueAsString(this.settings);
        Files.writeString(settingsPath, json);
    }
}