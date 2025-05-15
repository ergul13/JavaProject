package odev.odev;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // FXML dosyasını doğru konumdan yükle
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/odev/odev/hello-view.fxml"));

        // Sahneyi oluştur (1280x720 önerilen pencere boyutu)
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);

        // Başlık ve sahne ayarları
        stage.setTitle("Trafik Kontrol Sistemi");
        stage.setScene(scene);

        // Kapatma düğmeleri ve pencere çerçevesi aktif
        stage.setResizable(true);
        stage.centerOnScreen();  // Ortala

        // Göster
        stage.show();
        //Fatih
    }

    public static void main(String[] args) {
        launch();
    }
}