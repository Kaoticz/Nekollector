package com.github.kaoticz.nekollector.config;

import com.github.kaoticz.nekollector.MainApplication;
import com.github.kaoticz.nekollector.common.Statics;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class ConfigManager {
    private final Path settingsPath;
    private ConfigModel settings;

    public ConfigManager() {
        var settingsDirUri = MainApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        settingsDirUri = settingsDirUri.substring(0, settingsDirUri.lastIndexOf(File.separatorChar, settingsDirUri.length() - 2)) + File.separatorChar + "data";
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
            this.settings = Statics.JSON_DESERIALIZER.readValue(Files.readString(settingsPath), ConfigModel.class);
        } catch (IOException e) {
            this.settings = new ConfigModel();
        }
    }

    public ConfigModel getSettings() {
        return settings;
    }

    public void saveSettings(Consumer<ConfigModel> action) throws IOException {
        action.accept(this.settings);
        var json = Statics.JSON_DESERIALIZER.writeValueAsString(this.settings);
        Files.writeString(settingsPath, json);
    }
}