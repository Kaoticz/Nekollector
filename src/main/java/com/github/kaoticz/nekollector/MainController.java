package com.github.kaoticz.nekollector;

import com.github.kaoticz.nekollector.api.models.ApiResult;
import com.github.kaoticz.nekollector.api.nekosia.services.NekosiaService;
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
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MainController {
    private final SettingsManager settingsManager = new SettingsManager();

    private final FavoritesManager favoritesManager = new FavoritesManager(settingsManager);

    private final ApiCoordinator apiCoordinator = new ApiCoordinator(
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
            this.downloadButton.setDisable(true);

            this.imageContainer.widthProperty().addListener((_, _, newValue) -> {
                if (!this.imageView.getImage().equals(Statics.LOADING_IMAGE)) {
                    this.imageView.setFitWidth(newValue.doubleValue() - 10);
                }
            });

            this.imageContainer.heightProperty().addListener((_, _, newValue) -> {
                if (!this.imageView.getImage().equals(Statics.LOADING_IMAGE)) {
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

        Utilities.resizeImage(this.imageContainer, this.imageView, apiResult.apiImage());
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
        // Verifique se há uma imagem válida antes de prosseguir
        if (this.imageView.getImage() == null || this.imageView.getImage().equals(Statics.LOADING_IMAGE)) {
            System.out.println("Nenhuma imagem disponível para download.");
            return;
        }

        // Use FileChooser para permitir que o usuário escolha onde salvar a imagem
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Imagem");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg"));

        // Obtenha a imagem atual e prepare para salvar
        var file = fileChooser.showSaveDialog(this.imageView.getScene().getWindow());
        if (file != null) {
            try (var outputStream = new FileOutputStream(file)) {
                var bufferedImage = Utilities.convertToBufferedImage(this.imageView.getImage());
                var formatName = file.getName().endsWith(".png") ? "png" : "jpg"; // Suporte para PNG e JPG
                ImageIO.write(bufferedImage, formatName, outputStream);
                System.out.println("Imagem salva em: " + file.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Erro ao salvar a imagem: " + e.getMessage());
            }
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
        Platform.runLater(() -> this.downloadButton.setDisable(true));

        this.imageView.setImage(Statics.LOADING_IMAGE);
        this.imageView.setFitHeight(Statics.LOADING_IMAGE.getHeight());
        this.titleBar.setDisable(true);
        this.titleBar.setText("...");
        toggleAllButtons(true, false, true);
        Utilities.deselectFavoriteButton(this.sideBarContainer);

        this.apiCoordinator.getNextImageAsync()
                .thenAccept(apiResult -> {
                    Utilities.setTitleBarText(apiResult, this.favoritesManager, this.titleBar);
                    Utilities.resizeImage(this.imageContainer, this.imageView, apiResult.apiImage());
                    toggleAllButtons(false, true, false);
                    Utilities.deselectFavoriteButton(this.sideBarContainer);

                    Platform.runLater(() -> this.downloadButton.setDisable(false));

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

                    System.out.println("\u001B[31m" + errorReason + "\u001B[0m");
                    this.titleBar.setText("Request has failed: " + errorReason);
                    this.titleBar.setDisable(true);
                    this.nextButton.setDisable(false);

                    if (this.apiCoordinator.currentIndex() >= 1) {
                        this.previousButton.setDisable(false);
                    }

                    // TODO: set error image here

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