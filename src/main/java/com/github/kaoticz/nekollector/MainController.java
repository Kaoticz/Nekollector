package com.github.kaoticz.nekollector;

import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.api.nekosia.services.NekosiaService;
import com.github.kaoticz.nekollector.common.Statics;
import com.github.kaoticz.nekollector.config.ConfigManager;
import com.github.kaoticz.nekollector.services.ApiCoordinator;
import com.github.kaoticz.nekollector.services.FavoritesManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainController {
    private final ConfigManager configManager = new ConfigManager();

    private final FavoritesManager favoritesManager = new FavoritesManager(configManager);

    private final ApiCoordinator apiCoordinator = new ApiCoordinator(
            new NekosiaService()
    );

    private boolean isLoading;

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

    private void populateFavoriteButtons() {
        // Create a copy of the favorites to avoid concurrency issues
        var favorites = Map.copyOf(this.configManager.getSettings().getFavorites());
        var counter = 0;

        for (var favorite : favorites.entrySet()) {
            var mockApiResult = new ApiResult(favorite.getValue(), Statics.LOADING_IMAGE);
            this.favoritesManager.addFavorite(mockApiResult);
            this.sideBarContainer.getChildren().add(favoritesManager.createFavoriteButton(mockApiResult));
            var innerCounter = counter++;

            CompletableFuture.runAsync(() -> {
                try {
                    var apiResult = new ApiResult(favorite.getValue(), new Image(favorite.getKey()));
                    this.favoritesManager.updateFavorite(favorite.getValue(), apiResult);

                    // Set the view components in a JavaFX thread.
                    Platform.runLater(() -> this.sideBarContainer.getChildren().set(innerCounter, this.favoritesManager.createFavoriteButton(apiResult)));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            });
        }
    }

    /**
     * This method is run when the window is loaded for the first time.
     */
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            var window = imageView.getScene().getWindow();

            window.widthProperty().addListener((_, _, _) -> {
                if (!isLoading) {
                    imageView.setFitWidth(imageContainer.getWidth() - 10);
                }
            });
            window.heightProperty().addListener((_, _, _) -> {
                if (!isLoading) {
                    imageView.setFitHeight(imageContainer.getHeight() - 10);
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
    public void addFavoriteButton(ActionEvent ignoredEvent) {
        var imageUrl = imageView.getImage().getUrl();

        if (favoritesManager.isFavorite(imageUrl)) {
            // TODO: remove button
            favoritesManager.removeFavorite(imageUrl);
        } else {
            var apiResult = new ApiResult(titleBar.getText(), imageView.getImage());
            favoritesManager.addFavorite(apiResult);
            this.sideBarContainer.getChildren().add(favoritesManager.createFavoriteButton(apiResult));
        }
    }

    /**
     * Loads the previous image in the view.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void moveToPreviousImage(ActionEvent ignoredEvent) {
        System.out.println("moveToPreviousImage press!");

        if (this.apiCoordinator.currentIndex() <= 1) {
            this.previousButton.setDisable(true);
        }

        var apiResult = this.apiCoordinator.getPreviousImage();

        this.titleBar.setText(apiResult.serviceName());
        setResizedImage(apiResult.apiImage());
    }

    /**
     * Loads the next image in the view.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void moveToNextImage(ActionEvent ignoredEvent) {
        System.out.println("moveToNextImage press!");
        loadNextImage();
    }

    /**
     * Saves the current image to the file system.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void downloadImage(ActionEvent ignoredEvent) {
        System.out.println("downloadImage press!");
    }

    /**
     * Prepares the view for an image change and loads the next image.
     */
    private void loadNextImage() {
        isLoading = true;
        this.imageView.setImage(Statics.LOADING_IMAGE);
        this.imageView.setFitHeight(Statics.LOADING_IMAGE.getHeight());
        this.titleBar.setText("...");
        toggleAllButtons(true, false);

        this.apiCoordinator.getNextImageAsync()
                .handle((apiResult, ex) -> {
                    if (ex == null) {
                        this.titleBar.setText(apiResult.serviceName());
                        setResizedImage(apiResult.apiImage());
                        toggleAllButtons(false, true);
                    } else {
                        var errorCause = ex.fillInStackTrace().getCause();
                        var errorReason = (errorCause.getMessage() == null)
                                ? "Operation has timed out"
                                : errorCause.getMessage();

                        System.out.println(errorReason);
                        this.titleBar.setText("Request has failed: " + errorReason);
                        this.nextButton.setDisable(false);

                        if (this.apiCoordinator.currentIndex() > 1) {
                            this.previousButton.setDisable(false);
                        }

                        // TODO: set error image here
                    }

                    isLoading = false;

                    return apiResult;
                });
    }

    /**
     * Resizes the specified image to the size of the container.
     * @param image The image to be resized.
     */
    private void setResizedImage(Image image) {
        this.imageView.setImage(image);
        this.imageView.setFitWidth(this.imageContainer.getWidth());
        this.imageView.setFitHeight(this.imageContainer.getHeight());
    }

    /**
     * Toggles all buttons in the view to the specified value.
     * @param disable Whether the buttons should be disabled or not.
     * @param protectPreviousButton Whether the "Previous" button should be toggled so it can be safely used.
     */
    private void toggleAllButtons(boolean disable, boolean protectPreviousButton) {
        this.favoriteButton.setDisable(disable);
        this.previousButton.setDisable(disable);
        this.downloadButton.setDisable(disable);
        this.nextButton.setDisable(disable);

        if (protectPreviousButton && !disable && apiCoordinator.currentIndex() > 1) {
            this.previousButton.setDisable(false);
        } else if (protectPreviousButton && apiCoordinator.currentIndex() < 1) {
            this.previousButton.setDisable(true);
        }
    }
}