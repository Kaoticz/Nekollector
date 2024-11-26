package com.github.kaoticz.nekollector;

import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.api.nekosbest.services.NekosBestService;
import com.github.kaoticz.nekollector.api.nekosia.services.NekosiaService;
import com.github.kaoticz.nekollector.api.nekosmoe.services.NekosMoeService;
import com.github.kaoticz.nekollector.common.Statics;
import com.github.kaoticz.nekollector.common.Utilities;
import com.github.kaoticz.nekollector.config.SettingsManager;
import com.github.kaoticz.nekollector.services.ApiCoordinator;
import com.github.kaoticz.nekollector.services.FavoritesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * The controller for the main view.
 */
public class MainController {
    private final SettingsManager settingsManager = new SettingsManager();

    private final FavoritesManager favoritesManager = new FavoritesManager(settingsManager);

    private final ApiCoordinator apiCoordinator = new ApiCoordinator(
            new NekosBestService(),
            new NekosMoeService(),
            new NekosiaService()
    );

    @FXML
    private TextField titleBar;

    @FXML
    private VBox sideBarContainer;

    @FXML
    private StackPane imageContainer;

    @FXML
    private ImageView imageView;

    @FXML
    private Button favoriteButton;

    @FXML
    private Button previousButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button nextButton;

    /**
     * This method is run when the window is loaded for the first time.
     */
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            this.imageContainer.widthProperty().addListener((_, _, newValue) -> {
                if (!this.imageView.getImage().equals(Statics.LOADING_IMAGE) && !this.imageView.getImage().equals(Statics.ERROR_IMAGE)) {
                    this.imageView.setFitWidth(newValue.doubleValue() - 10);
                }
            });

            this.imageContainer.heightProperty().addListener((_, _, newValue) -> {
                if (!this.imageView.getImage().equals(Statics.LOADING_IMAGE) && !this.imageView.getImage().equals(Statics.ERROR_IMAGE)) {
                    this.imageView.setFitHeight(newValue.doubleValue() - 10);
                }
            });

            loadNextImage();
            populateFavoriteButtons();
        });
    }

    /**
     * Adds the current image as a favorite.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void handleFavoriteButton(@NotNull ActionEvent ignoredEvent) {
        var imageUrl = this.imageView.getImage().getUrl();

        if (this.favoritesManager.isFavorite(imageUrl)) {
            this.sideBarContainer.getChildren()
                    .stream()
                    .filter(node -> node instanceof StackPane)
                    .map(node -> (StackPane)node)
                    .filter(stackPane -> stackPane.getChildren().stream().anyMatch(node -> node instanceof ImageView image && image.getImage().getUrl().equals(imageUrl)))
                    .findFirst()
                    .ifPresent(stackPane -> this.sideBarContainer.getChildren().remove(stackPane));

            this.favoritesManager.removeFavorite(imageUrl);
            System.out.println("Removed favorite " + titleBar.getText());
        } else {
            var apiResult = new ApiResult(this.titleBar.getText(), this.imageView.getImage());
            this.favoritesManager.addFavorite(apiResult);

            var stackPane = this.favoritesManager.createFavoriteContainer(
                    apiResult,
                    this.sideBarContainer,
                    this.imageContainer,
                    this.imageView,
                    this.titleBar,
                    this.favoriteButton,
                    this.downloadButton
            );
            this.sideBarContainer.getChildren().add(stackPane);
            System.out.println("Added [" + this.apiCoordinator.currentIndex() + "] as a favorite ");
        }

        this.favoriteButton.setText(getFavoriteButtonText(imageUrl));
        this.titleBar.setDisable(!this.favoritesManager.isFavorite(imageUrl));
    }

    /**
     * Loads the previous image in the view.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void moveToPreviousImage(@NotNull ActionEvent ignoredEvent) {
        System.out.println("Moving to previous image: [" + (this.apiCoordinator.currentIndex() - 1) + "]");

        toggleAllButtons(false, true, true);
        var apiResult = this.apiCoordinator.getPreviousImage();

        Utilities.setTitleBarText(apiResult, this.favoritesManager, this.titleBar);
        this.favoriteButton.setText(getFavoriteButtonText(apiResult.apiImage().getUrl()));

        Utilities.resizeImageToContainer(this.imageContainer, this.imageView, apiResult.apiImage());
        Utilities.deselectFavoriteButton(this.sideBarContainer);
    }

    /**
     * Loads the next image in the view.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void moveToNextImage(@NotNull ActionEvent ignoredEvent) {
        System.out.println("Moving to next image: [" + (this.apiCoordinator.currentIndex() + 1) + "]");
        loadNextImage();
    }

    /**
     * Saves the current image to the file system.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void downloadImage(@NotNull ActionEvent ignoredEvent) {
        if (this.imageView.getImage().equals(Statics.LOADING_IMAGE) || this.imageView.getImage().equals(Statics.ERROR_IMAGE)) {
            System.out.println("There is no image to download. If you're seeing this message, that means this is a bug.");
            return;
        }

        // Assign the image locally to avoid concurrency issues
        var imageToSave = this.imageView.getImage();

        // Get the file extension
        var fileExtension = Utilities.getExtension(this.imageView.getImage().getUrl());

        // OpenJDK does not provide a jpg encoder for ImageIO, so save the image as png instead
        if (fileExtension.isBlank() || fileExtension.equalsIgnoreCase("jpg") || fileExtension.equalsIgnoreCase("jpeg")) {
            fileExtension = "png";
        }

        // Invoke the file picker
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save image to...");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Image (." + fileExtension + ")", "*." + fileExtension));
        fileChooser.setInitialDirectory(new File(this.settingsManager.getSettings().getDefaultDownloadDirectory()));

        // Get the file path chosen by the user
        var file = fileChooser.showSaveDialog(this.imageView.getScene().getWindow());

        if (file == null) {
            return;
        }

        var fileWithExtension = (file.getAbsolutePath().endsWith('.' + fileExtension))
                ? file
                : new File(file.getAbsolutePath() + "." + fileExtension);

        try (var outputStream = new FileOutputStream(fileWithExtension)) {
            var bufferedImage = Utilities.convertToBufferedImage(imageToSave);
            if (ImageIO.write(bufferedImage, fileExtension, outputStream)) {
                System.out.println("Image saved to: " + fileWithExtension.getAbsolutePath());
                var directoryUri = fileWithExtension.getAbsolutePath().substring(0, fileWithExtension.getAbsolutePath().lastIndexOf(File.separatorChar) + 1);

                if (!directoryUri.equals(this.settingsManager.getSettings().getDefaultDownloadDirectory())) {
                    this.settingsManager.saveSettings(settings -> settings.setDefaultDownloadDirectory(directoryUri));
                }
            } else {
                System.out.println(Utilities.createErrorString("ERROR: Failed to encode the image as " + fileExtension));
            }
        } catch (Exception e) {
            System.err.println(Utilities.createErrorString("ERROR: Failed to save image to: " + e.getMessage()));
        }
    }

    /**
     * Updates the title bar with a user-provided name.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void updateFavoriteName(@NotNull KeyEvent ignoredEvent) {
        // Yes, there is no event for when the text field is selected or deselected, so
        // this will result in the settings file being serialized for every key stroke.
        // Too bad!
        var apiResult = new ApiResult(titleBar.getText(), imageView.getImage());
        this.favoritesManager.updateFavorite(imageView.getImage().getUrl(), apiResult);
    }

    /**
     * Populates the sidebar with buttons for the favorites saved in the settings file.
     */
    private void populateFavoriteButtons() {
        // Create a copy of the favorites to avoid concurrency issues
        var favorites = Map.copyOf(this.settingsManager.getSettings().getFavorites());
        var counter = 0;

        for (var favorite : favorites.entrySet()) {
            var mockApiResult = new ApiResult(favorite.getValue(), Statics.LOADING_IMAGE);
            var stackPane = this.favoritesManager.createFavoriteContainer(
                    mockApiResult,
                    this.sideBarContainer,
                    this.imageContainer,
                    this.imageView,
                    this.titleBar,
                    this.favoriteButton,
                    this.downloadButton
            );
            Utilities.getFavoriteButton(stackPane).setDisable(true);
            this.sideBarContainer.getChildren().add(stackPane);
            var innerCounter = counter++;

            CompletableFuture.runAsync(() -> {
                var apiResult = new ApiResult(favorite.getValue(), new Image(favorite.getKey()));
                this.favoritesManager.addFavorite(apiResult);

                // Set the view components in a JavaFX thread.
                Platform.runLater(() -> {
                    var updatedStackPanel = this.favoritesManager.createFavoriteContainer(
                            apiResult,
                            this.sideBarContainer,
                            this.imageContainer,
                            this.imageView,
                            this.titleBar,
                            this.favoriteButton,
                            this.downloadButton
                    );
                    Utilities.getFavoriteButton(updatedStackPanel).setDisable(false);
                    this.sideBarContainer.getChildren().set(innerCounter, updatedStackPanel);
                });
            });
        }
    }

    /**
     * Prepares the view for an image change and loads the next image.
     */
    private void loadNextImage() {
        this.imageView.setImage(Statics.LOADING_IMAGE);
        this.imageView.setFitHeight(Statics.LOADING_IMAGE.getHeight());
        this.titleBar.setDisable(true);
        this.titleBar.setText("...");
        toggleAllButtons(true, false, true);
        Utilities.deselectFavoriteButton(this.sideBarContainer);

        this.apiCoordinator.getNextImageAsync()
                .thenAccept(apiResult -> {
                    Utilities.setTitleBarText(apiResult, this.favoritesManager, this.titleBar);
                    Utilities.resizeImageToContainer(this.imageContainer, this.imageView, apiResult.apiImage());
                    toggleAllButtons(false, true, false);
                    Utilities.deselectFavoriteButton(this.sideBarContainer);

                    // The text for buttons can only be set by a JavaFX thread
                    Platform.runLater(() -> this.favoriteButton.setText(getFavoriteButtonText(apiResult.apiImage().getUrl())));
                })
                .exceptionally(ex -> {
                    var errorCause = ex.fillInStackTrace().getCause();
                    var errorReason = "ERROR: " + (
                            (errorCause.getMessage() == null)
                                    ? "Operation has timed out"
                                    : errorCause.getMessage()
                    );

                    System.out.println(Utilities.createErrorString(errorReason));
                    this.titleBar.setText("Request has failed: " + errorReason);
                    this.titleBar.setDisable(true);
                    toggleAllButtons(true, false, false);
                    Utilities.deselectFavoriteButton(this.sideBarContainer);
                    this.nextButton.setDisable(false);

                    if (this.apiCoordinator.currentIndex() >= 1) {
                        this.previousButton.setDisable(false);
                    }

                    // Set the error image
                    this.imageView.setImage(Statics.ERROR_IMAGE);
                    imageView.setFitWidth(Statics.ERROR_IMAGE.getWidth() / 2);
                    imageView.setFitHeight(Statics.ERROR_IMAGE.getHeight() / 2);

                    return null;
                });
    }

    /**
     * Toggles all buttons in the view to the specified value.
     * @param disable Whether the buttons should be disabled or not.
     * @param protectPreviousButton Whether the "Previous" button should be toggled so it can be safely used.
     * @param isBefore Whether this method was called before the image got loaded into the view.
     */
    private void toggleAllButtons(boolean disable, boolean protectPreviousButton, boolean isBefore) {
        this.favoriteButton.setDisable(disable);
        this.previousButton.setDisable(disable);
        this.downloadButton.setDisable(disable);
        this.nextButton.setDisable(disable);

        if (protectPreviousButton && !disable && this.apiCoordinator.currentIndex() > 1) {
            this.previousButton.setDisable(false);
        } else if (protectPreviousButton && (isBefore && this.apiCoordinator.currentIndex() <= 1) || this.apiCoordinator.currentIndex() < 1) {
            this.previousButton.setDisable(true);
        }
    }

    /**
     * Gets the appropriate text for the favorite button.
     * @param imageUrl The URL to the currently set image.
     * @return The string to display on the favorite button.
     */
    private @NotNull String getFavoriteButtonText(String imageUrl) {
        return (this.favoritesManager.isFavorite(imageUrl))
                ? Statics.FAVORITE_BUTTON_TEXT
                : Statics.NOT_FAVORITE_BUTTON_TEXT;
    }
}