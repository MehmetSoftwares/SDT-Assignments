package presentation.core;

import javafx.application.Platform;
import javafx.util.Callback;
import presentation.views.portfolio.PortfolioController;
import presentation.views.portfolio.PortfolioViewModel;
import presentation.views.stockmarket.StockMarketController;
import presentation.views.stockmarket.StockMarketViewModel;

public class ControllerFactory implements Callback<Class<?>, Object> {

  private final ApplicationContext appContext;
  private ViewManager viewManager;

  public ControllerFactory(ApplicationContext appContext) {
    this.appContext = appContext;
  }

  public void setViewManager(ViewManager viewManager) {
    this.viewManager = viewManager;
  }

  @Override
  public Object call(Class<?> cls) {
    if (cls == StockMarketController.class) {
      StockMarketViewModel vm = new StockMarketViewModel(
          appContext.getPortfolioQueryService(),
          appContext.getStockTradingService(),
          appContext.getNotificationService()
      );
      appContext.getStockListenerService().addListener(event ->
          Platform.runLater(() -> {
            vm.updateChartData(event.symbol(), event.currentPrice());
            if (event.symbol().equals(vm.getSelectedStock().get())) {
              vm.getCurrentState().set(event.stateName());
            }
          })
      );
      return new StockMarketController(vm, viewManager);
    }
    if (cls == PortfolioController.class) {
      PortfolioViewModel vm = new PortfolioViewModel(
          appContext.getPortfolioQueryService(),
          appContext.getStockTradingService(),
          appContext.getNotificationService()
      );
      return new PortfolioController(vm, viewManager);
    }
    throw new RuntimeException("Unknown controller class: " + cls.getName());
  }
}