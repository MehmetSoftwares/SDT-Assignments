package presentation;

import javafx.application.Application;
import javafx.stage.Stage;
import presentation.core.ApplicationContext;
import presentation.core.ControllerFactory;
import presentation.core.ViewManager;
import shared.logging.Logger;
import shared.logging.ConsoleLogOutput;

public class StockGameApp extends Application {

  @Override
  public void start(Stage primaryStage) {
    Logger.getInstance().setLogOutput(new ConsoleLogOutput());

    ApplicationContext appContext = new ApplicationContext();
    appContext.initializeGame();

    ControllerFactory controllerFactory = new ControllerFactory(appContext);
    ViewManager viewManager = new ViewManager(primaryStage, controllerFactory);
    controllerFactory.setViewManager(viewManager);

    primaryStage.setTitle("Stock Trading Game");
    viewManager.showView("/fxml/stockmarket.fxml");
  }

    public static void main(String[] args) {
        launch(args);
    }
}
