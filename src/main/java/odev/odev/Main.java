package odev.odev;
import controller.  TrafficController;
import javafx.application.Application;
import javafx.stage.Stage;
import model.VehicleManager;
import view.TrafficView;


public class Main extends Application {
    @Override
    public void start(Stage stage) {
        VehicleManager manager = new VehicleManager();
        TrafficController controller = new TrafficController(manager);
        TrafficView view = new TrafficView(controller);
        view.start(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}
