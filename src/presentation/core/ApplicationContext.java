package presentation.core;

import business.services.PortfolioQueryService;
import business.services.StockBankruptService;
import business.services.StockListenerService;
import business.services.StockTradingService;
import business.stockmarket.StockMarket;
import business.stockmarket.simulation.MarketUpdateThread;
import domain.Portfolio;
import domain.Stock;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.interfaces.UnitOfWork;
import shared.configuration.AppConfig;

import java.util.List;

public class ApplicationContext {

    private final UnitOfWork uow;
    private final StockTradingService stockTradingService;
    private final PortfolioQueryService portfolioQueryService;
    private final StockListenerService stockListenerService;
    private final StockBankruptService stockBankruptService;
    private final StockMarket stockMarket;
    private final NotificationService notificationService;

    public ApplicationContext() {
        this.uow = new FileUnitOfWork("database");
        this.stockTradingService = new StockTradingService(uow);
        this.portfolioQueryService = new PortfolioQueryService(uow);
        this.stockListenerService = new StockListenerService(uow);
        this.stockBankruptService = new StockBankruptService(uow);
        this.stockMarket = StockMarket.getInstance();
        this.notificationService = new AlertNotificationService();
    }

    public void initializeGame() {
        uow.refresh();
        List<Stock> existingStocks = uow.getStockDao().getAll();
        if (existingStocks.isEmpty()) {
            uow.begin();
            uow.getStockDao().create(new Stock("AAPL", "Apple", 100, "Steady"));
            uow.getStockDao().create(new Stock("GOOG", "Alphabet", 120, "Steady"));
            uow.getStockDao().create(new Stock("MSFT", "Microsoft", 90, "Steady"));
            uow.commit();
        }

        uow.refresh();
        List<Portfolio> portfolios = uow.getPortfolioDao().getAll();
        if (portfolios.isEmpty()) {
            uow.begin();
            uow.getPortfolioDao().create(new Portfolio(1, AppConfig.getInstance().getStartingBalance()));
            uow.commit();
        }

        uow.refresh();
        List<Stock> stocks = uow.getStockDao().getAll();
        for (Stock stock : stocks) {
            stockMarket.addExistingStock(stock);
        }

        stockMarket.addListener(stockListenerService);
        stockMarket.addListener(stockBankruptService);

        Thread marketThread = new Thread(new MarketUpdateThread());
        marketThread.setDaemon(true);
        marketThread.start();
    }

    public StockTradingService getStockTradingService() {
        return stockTradingService;
    }

    public PortfolioQueryService getPortfolioQueryService() {
        return portfolioQueryService;
    }

    public StockListenerService getStockListenerService() {
        return stockListenerService;
    }

    public StockBankruptService getStockBankruptService() {
        return stockBankruptService;
    }

    public StockMarket getStockMarket() {
        return stockMarket;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
}
