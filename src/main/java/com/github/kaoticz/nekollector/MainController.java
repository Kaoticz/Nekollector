package com.github.kaoticz.nekollector;

import com.github.kaoticz.nekollector.api.nekosia.services.NekosiaService;
import com.github.kaoticz.nekollector.common.Statics;
import com.github.kaoticz.nekollector.services.ApiCoordinator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainController {

    private static int favoriteCounter = 1;

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
        });
    }

    /**
     * Adds the current image as a favorite.
     * @param ignoredEvent The event arguments.
     */
    @FXML
    public void addFavoriteButton(ActionEvent ignoredEvent) {
        var button = new Button("Favorite " + favoriteCounter++);
        this.sideBarContainer.getChildren().add(button);
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
                .thenApply(apiResult -> {
                    setResizedImage(apiResult.apiImage());
                    return apiResult;
                })
                .handle((apiResult, ex) -> {
                    if (ex == null) {
                        this.titleBar.setText(apiResult.serviceName());
                        toggleAllButtons(false, true);
                    } else {
                        var errorCause = ex.fillInStackTrace().getCause();
                        var errorReason = (errorCause.getMessage() == null)
                                ? errorCause
                                : errorCause.getMessage();

                        System.out.println(errorCause);
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

        if (image.getWidth() > image.getHeight() && image.getWidth() > this.imageContainer.getWidth()) {
            this.imageView.setFitWidth(this.imageContainer.getWidth());
        } else {
            this.imageView.setFitHeight(this.imageContainer.getHeight());
        }
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