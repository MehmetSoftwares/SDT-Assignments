package presentation.views.stockmarket;

import business.dtos.BuyStockRequest;
import business.services.PortfolioQueryService;
import business.services.StockTradingService;
import domain.Stock;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import presentation.core.NotificationService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class StockMarketViewModel {

  private static final int MAX_CHART_POINTS = 30;

  private final PortfolioQueryService portfolioQueryService;
  private final StockTradingService stockTradingService;
  private final NotificationService notificationService;

  private final ObservableList<String> availableStocks = FXCollections.observableArrayList();
  private final StringProperty selectedStock = new SimpleStringProperty("");
  private final StringProperty numberOfShares = new SimpleStringProperty("");
  private final StringProperty currentPrice = new SimpleStringProperty("-");
  private final StringProperty currentState = new SimpleStringProperty("-");
  private final StringProperty statusMessage = new SimpleStringProperty("");
  private final StringProperty balance = new SimpleStringProperty("-");
  private final XYChart.Series<String, Number> priceChartData = new XYChart.Series<>();
  private final Map<String, List<XYChart.Data<String, Number>>> chartDataPerStock = new HashMap<>();

  private List<Stock> stocks = new ArrayList<>();

  public StockMarketViewModel(PortfolioQueryService portfolioQueryService,
      StockTradingService stockTradingService,
      NotificationService notificationService) {
    this.portfolioQueryService = portfolioQueryService;
    this.stockTradingService = stockTradingService;
    this.notificationService = notificationService;
  }

  public void loadStocks() {
    stocks = portfolioQueryService.getAvailableStocks();
    availableStocks.clear();
    Set<String> seen = new HashSet<>();
    for (Stock stock : stocks) {
      if (seen.add(stock.getSymbol())) {
        availableStocks.add(stock.getSymbol());
      }
    }
    refreshBalance();
  }

  public void selectStock(String symbol) {
    selectedStock.set(symbol);
    priceChartData.getData().clear();
    if (chartDataPerStock.containsKey(symbol)) {
      List<XYChart.Data<String, Number>> saved = chartDataPerStock.get(symbol);
      for (XYChart.Data<String, Number> point : saved) {
        priceChartData.getData().add(new XYChart.Data<>(point.getXValue(), point.getYValue()));
      }
    }
    for (Stock stock : stocks) {
      if (stock.getSymbol().equals(symbol)) {
        currentPrice.set(String.format("%.2f", stock.getCurrentPrice()));
        currentState.set(stock.getCurrentState());
        return;
      }
    }
  }

  public void buyShares() {
    String symbol = selectedStock.get();
    String sharesStr = numberOfShares.get();

    if (symbol == null || symbol.isEmpty()) {
      notificationService.showNotification("Please select a stock", "warning");
      return;
    }
    if (sharesStr == null || sharesStr.isEmpty()) {
      notificationService.showNotification("Please enter number of shares", "warning");
      return;
    }

    int shares;
    try {
      shares = Integer.parseInt(sharesStr);
    } catch (NumberFormatException e) {
      notificationService.showNotification("Invalid number of shares", "error");
      return;
    }

    try {
      stockTradingService.buyShares(new BuyStockRequest(1, symbol, shares));
      String msg = "Bought " + shares + " shares of " + symbol;
      statusMessage.set(msg);
      notificationService.showNotification(msg, "info");
      refreshBalance();
    } catch (Exception e) {
      statusMessage.set("Error: " + e.getMessage());
      notificationService.showNotification(e.getMessage(), "error");
    }
  }

  public void updateChartData(String symbol, double price) {
    chartDataPerStock.putIfAbsent(symbol, new ArrayList<>());
    List<XYChart.Data<String, Number>> history = chartDataPerStock.get(symbol);
    String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    history.add(new XYChart.Data<>(time, price));
    if (history.size() > MAX_CHART_POINTS) {
      history.remove(0);
    }

    if (symbol.equals(selectedStock.get())) {
      priceChartData.getData().clear();
      for (XYChart.Data<String, Number> point : history) {
        priceChartData.getData().add(new XYChart.Data<>(point.getXValue(), point.getYValue()));
      }
      currentPrice.set(String.format("%.2f", price));
    }

    for (Stock stock : stocks) {
      if (stock.getSymbol().equals(symbol)) {
        stock.setCurrentPrice(price);
        break;
      }
    }
  }

  private void refreshBalance() {
    try {
      double bal = portfolioQueryService.getBalance(1);
      balance.set(String.format("%.2f", bal));
    } catch (Exception e) {
      balance.set("N/A");
    }
  }

  public ObservableList<String> getAvailableStocks() { return availableStocks; }
  public StringProperty getSelectedStock() { return selectedStock; }
  public StringProperty getNumberOfShares() { return numberOfShares; }
  public StringProperty getCurrentPrice() { return currentPrice; }
  public StringProperty getCurrentState() { return currentState; }
  public StringProperty getStatusMessage() { return statusMessage; }
  public StringProperty getBalance() { return balance; }
  public XYChart.Series<String, Number> getPriceChartData() { return priceChartData; }
}