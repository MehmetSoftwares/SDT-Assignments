package presentation.views.portfolio;

import business.dtos.SellStockRequest;
import business.services.PortfolioQueryService;
import business.services.StockTradingService;
import domain.OwnedStock;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import presentation.core.NotificationService;

public class PortfolioViewModel {

    private final PortfolioQueryService portfolioQueryService;
    private final StockTradingService stockTradingService;
    private final NotificationService notificationService;

    private final ObservableList<OwnedStock> ownedStocks = FXCollections.observableArrayList();
    private final StringProperty balance = new SimpleStringProperty("0.00");
    private final StringProperty totalValue = new SimpleStringProperty("0.00");
    private final StringProperty sellSymbol = new SimpleStringProperty("");
    private final StringProperty sellQuantity = new SimpleStringProperty("");

    public PortfolioViewModel(PortfolioQueryService portfolioQueryService,
                              StockTradingService stockTradingService,
                              NotificationService notificationService) {
        this.portfolioQueryService = portfolioQueryService;
        this.stockTradingService = stockTradingService;
        this.notificationService = notificationService;
    }

    public void loadPortfolio() {
        ownedStocks.setAll(portfolioQueryService.getOwnedStocks(1));
        balance.set(String.format("%.2f", portfolioQueryService.getBalance(1)));
        totalValue.set(String.format("%.2f", portfolioQueryService.getTotalPortfolioValue(1)));
    }

    public void sellShares() {
        String symbol = sellSymbol.get();
        String quantityStr = sellQuantity.get();

        if (symbol == null || symbol.isEmpty()) {
            notificationService.showNotification("Please enter a stock symbol", "warning");
            return;
        }
        if (quantityStr == null || quantityStr.isEmpty()) {
            notificationService.showNotification("Please enter a quantity", "warning");
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            notificationService.showNotification("Invalid quantity", "error");
            return;
        }

        try {
            stockTradingService.sellShares(new SellStockRequest(1, symbol, quantity));
            notificationService.showNotification("Sold " + quantity + " shares of " + symbol, "info");
            loadPortfolio();
        } catch (Exception e) {
            notificationService.showNotification(e.getMessage(), "error");
        }
    }

    public ObservableList<OwnedStock> getOwnedStocks() {
        return ownedStocks;
    }

    public StringProperty getBalance() {
        return balance;
    }

    public StringProperty getTotalValue() {
        return totalValue;
    }

    public StringProperty getSellSymbol() {
        return sellSymbol;
    }

    public StringProperty getSellQuantity() {
        return sellQuantity;
    }
}
