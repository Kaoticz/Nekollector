module com.github.kaoticz.nekollector {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens com.github.kaoticz.nekollector to javafx.fxml;
    exports com.github.kaoticz.nekollector;
}