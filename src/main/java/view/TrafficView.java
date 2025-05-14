package view;

import controller.TrafficController;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

public class TrafficView {
    private TrafficController controller;

    public TrafficView(TrafficController controller) {
        this.controller = controller;
    }

    public void start(Stage stage) {
        Pane root = new Pane();
        root.setPrefSize(800, 800);
        root.setStyle("-fx-background-color: white;");

        // YOLLAR (Çift Şerit)
        Rectangle northIn = new Rectangle(370, 0, 30, 370);
        Rectangle northOut = new Rectangle(400, 0, 30, 400);

        Rectangle southIn = new Rectangle(370, 370, 30, 370);
        Rectangle southOut = new Rectangle(400, 430, 30, 400);

        Rectangle westIn = new Rectangle(0, 370, 370, 20);
        Rectangle westOut = new Rectangle(0, 410, 370, 20);

        Rectangle eastIn = new Rectangle(430, 410, 370, 20);
        Rectangle eastOut = new Rectangle(430, 370, 370, 20);

        Color roadColor = Color.LIGHTGRAY;
        northIn.setFill(roadColor); northOut.setFill(roadColor);
        southIn.setFill(roadColor); southOut.setFill(roadColor);
        westIn.setFill(roadColor); westOut.setFill(roadColor);
        eastIn.setFill(roadColor); eastOut.setFill(roadColor);

        root.getChildren().addAll(northIn, northOut, southIn, southOut, westIn, westOut, eastIn, eastOut);

        // KAVŞAK (Merkez Kare)
        Rectangle center = new Rectangle(370, 370, 60, 60);
        center.setFill(Color.DARKGRAY);
        root.getChildren().add(center);

        // TRAFİK IŞIKLARI (Köşelerde, 3 Işık)
        root.getChildren().addAll(
                createTrafficLight(355, 355, "vertical"),   // NW
                createTrafficLight(430, 355, "vertical"),   // NE
                createTrafficLight(355, 430, "vertical"),   // SW
                createTrafficLight(430, 430, "vertical")    // SE
        );

        // PENCERE
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Kavşak Çizimi (Statik)");
        stage.show();
    }

    private Group createTrafficLight(double x, double y, String orientation) {
        Rectangle body = new Rectangle(x, y, 15, 45);
        body.setArcWidth(5);
        body.setArcHeight(5);
        body.setFill(Color.BLACK);

        Circle red = new Circle(x + 7.5, y + 7.5, 4, Color.RED);
        Circle yellow = new Circle(x + 7.5, y + 22.5, 4, Color.DARKGOLDENROD);
        Circle green = new Circle(x + 7.5, y + 37.5, 4, Color.DARKGREEN);

        return new Group(body, red, yellow, green);
    }
}
