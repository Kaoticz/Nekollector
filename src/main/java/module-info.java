module com.github.kaoticz.nekollector {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;
    requires org.jetbrains.annotations;
    requires java.desktop;
    requires javafx.swing;

    opens com.github.kaoticz.nekollector.api.nekosia.models to com.fasterxml.jackson.databind;
    opens com.github.kaoticz.nekollector.config to com.fasterxml.jackson.databind;
    opens com.github.kaoticz.nekollector to javafx.fxml;

    exports com.github.kaoticz.nekollector;
}