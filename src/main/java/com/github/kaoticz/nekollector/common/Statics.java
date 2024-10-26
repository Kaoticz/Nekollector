package com.github.kaoticz.nekollector.common;

import javafx.scene.image.Image;
import java.util.Objects;

public class Statics {
    public final static Image LOADING_IMAGE = new Image(Objects.requireNonNull(Statics.class.getResourceAsStream("/assets/loading.gif")));
}
