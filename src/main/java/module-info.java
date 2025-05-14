module odev.odev {
    requires javafx.controls;
    requires javafx.fxml;


    opens odev.odev to javafx.fxml;
    exports odev.odev;
    exports controller;
    opens controller to javafx.fxml;
    exports model;
    opens model to javafx.fxml;
    exports view;
    opens view to javafx.fxml;

}