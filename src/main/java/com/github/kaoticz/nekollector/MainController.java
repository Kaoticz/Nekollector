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
            var stackPane = this.favoritesManager.createFavoriteContainer(mockApiResult);
            this.sideBarContainer.getChildren().add(stackPane);
            var innerCounter = counter++;

            CompletableFuture.runAsync(() -> {
                var apiResult = new ApiResult(favorite.getValue(), new Image(favorite.getKey()));
                this.favoritesManager.addFavorite(apiResult);

                // Set the view components in a JavaFX thread.
                Platform.runLater(() -> {
                    var updatedStackPanel = this.favoritesManager.createFavoriteContainer(apiResult);
                    setFavoriteButtonAction(updatedStackPanel, apiResult.apiImage().getUrl());
                    this.sideBarContainer.getChildren().set(innerCounter, updatedStackPanel);
                });
            });
        }
    }

    /**
     * This method is run when the window is loaded for the first time.
     */
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            var window = this.imageView.getScene().getWindow();

            window.widthProperty().addListener((_, _, _) -> {
                if (!isLoading) {
                    this.imageView.setFitWidth(imageContainer.getWidth() - 10);
                }
            });
            window.heightProperty().addListener((_, _, _) -> {
                if (!isLoading) {
                    this.imageView.setFitHeight(imageContainer.getHeight() - 10);
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
    public void handleFavoriteButton(ActionEvent ignoredEvent) {
        var imageUrl = imageView.getImage().getUrl();

        if (favoritesManager.isFavorite(imageUrl)) {
            // Remove favorite button
            this.sideBarContainer.getChildren()
                    .stream()
                    .filter(node -> node instanceof StackPane)
                    .map(node -> (StackPane)node)
                    .filter(stackPane -> stackPane.getChildren().stream().anyMatch(node -> node instanceof ImageView image && image.getImage().getUrl().equals(imageUrl)))
                    .findFirst()
                    .ifPresent(stackPane -> this.sideBarContainer.getChildren().remove(stackPane));

            this.favoritesManager.removeFavorite(imageUrl);
        } else {
            // Add favorite button
            var apiResult = new ApiResult(this.titleBar.getText(), this.imageView.getImage());
            this.favoritesManager.addFavorite(apiResult);

            var stackPane = this.favoritesManager.createFavoriteContainer(apiResult);
            setFavoriteButtonAction(stackPane, apiResult.apiImage().getUrl());

            this.sideBarContainer.getChildren().add(stackPane);
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
        deselectFavoriteButton(this.sideBarContainer);
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
        deselectFavoriteButton(this.sideBarContainer);

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

    private void setFavoriteButtonAction(StackPane stackPane, String imageUrl) {
        var button = (Button)stackPane.getChildren()
                .filtered(node -> node instanceof Button)
                .getFirst();

        button.setDisable(false);
        button.setOnMouseClicked(_ -> {
            deselectFavoriteButton(this.sideBarContainer);

            // Select the current button
            button.setBorder(Statics.SELECTION_BORDER);

            // Display the selected image
            var favorite = favoritesManager.getCachedFavorite(imageUrl);
            titleBar.setText(favorite.serviceName());
            setResizedImage(favorite.apiImage());
        });
    }

    private void deselectFavoriteButton(VBox sideBarContainer) {
        // Find the selected button and deselect it
        var selectedButtons = sideBarContainer.getChildren()
                .stream()
                .filter(node -> node instanceof StackPane)
                .map(node -> (StackPane)node)
                .flatMap(node -> node.getChildren().stream())
                .filter(node -> node instanceof Button)
                .map(node -> (Button)node)
                .filter(btn -> btn.getBorder() != Statics.TRANSPARENT_BORDER)
                .toList();

        // If the user selects favorites too quickly, there might be a possibility
        // of more than one button being selected at a time, so we iterate through
        // all of them
        for (var selectedButton : selectedButtons) {
            selectedButton.setBorder(Statics.TRANSPARENT_BORDER);
        }
    }
}