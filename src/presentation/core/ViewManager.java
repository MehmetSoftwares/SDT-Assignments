package presentation.core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewManager {

    private final Stage stage;
    private final ControllerFactory controllerFactory;

    public ViewManager(Stage stage, ControllerFactory controllerFactory) {
        this.stage = stage;
        this.controllerFactory = controllerFactory;
    }

    public void showView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }

    public void showView(String fxmlPath, String argument) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(controllerFactory);
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AcceptsStringArgument) {
                ((AcceptsStringArgument) controller).setArgument(argument);
            }
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load view: " + fxmlPath, e);
        }
    }
}
