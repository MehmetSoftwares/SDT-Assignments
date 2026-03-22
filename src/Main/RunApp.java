package Main;

import business.dtos.BuyStockRequest;
import business.dtos.SellStockRequest;
import business.services.PortfolioQueryService;
import business.services.StockBankruptService;
import business.services.StockListenerService;
import business.services.StockTradingService;
import business.stockmarket.StockMarket;
import business.stockmarket.simulation.MarketUpdateThread;
import domain.OwnedStock;
import domain.Portfolio;
import domain.Stock;
import domain.Transaction;
import persistence.fileimplementation.FileOwnedStockDao;
import persistence.fileimplementation.FilePortfolioDao;
import persistence.fileimplementation.StockFileDAO;
import persistence.fileimplementation.TransactionFileDAO;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.interfaces.OwnedStockDao;
import persistence.interfaces.PortfolioDao;
import persistence.interfaces.StockDao;
import persistence.interfaces.TransactionDao;
import shared.logging.ConsoleLogOutput;
import shared.logging.Logger;

import java.util.List;

public class RunApp
{
  public static void main(String[] args)
  {
    Logger logger = Logger.getInstance();
    logger.setLogOutput(new ConsoleLogOutput());
    logger.log("INFO", "Application started - Testing Persistence Layer");

    try
    {
      // ─── Assignment 3: Persistence Layer ───────────────────────────
      FileUnitOfWork uow = new FileUnitOfWork("database");

      uow.begin();
      uow.commit();

      StockDao stockDao = new StockFileDAO(uow);
      PortfolioDao portfolioDao = new FilePortfolioDao(uow);
      OwnedStockDao ownedStockDao = new FileOwnedStockDao(uow);
      TransactionDao transactionDao = new TransactionFileDAO(uow);

      logger.log("INFO", "Starting transaction to create data...");
      uow.begin();

      Stock apple = new Stock("AAPL", "Apple Inc.", 150.0, "Steady");
      stockDao.create(apple);

      Stock google = new Stock("GOOG", "Alphabet Inc.", 120.0, "Steady");
      stockDao.create(google);

      Portfolio myPortfolio = new Portfolio(0, 10000.0);
      portfolioDao.create(myPortfolio);

      uow.commit();
      logger.log("INFO", "Data committed successfully to files.");

      System.out.println("\n--- VERIFICERING AF DATA I SYSTEMET ---");

      List<Stock> allStocks = stockDao.getAll();
      for (Stock s : allStocks)
      {
        System.out.println(
            "Aktie i systemet: " + s.getName() + " [" + s.getSymbol() + "]");
      }

      List<Portfolio> allPortfolios = portfolioDao.getAll();
      for (Portfolio p : allPortfolios)
      {
        System.out.println(
            "Portefølje i systemet: ID " + p.getId() + " - Balance: "
                + p.getCurrentBalance());
      }

      // ─── Assignment 4: State Pattern / StockMarket ─────────────────
      System.out.println("\n--- TESTING STOCK MARKET SIMULATION ---");

      StockMarket market = StockMarket.getInstance();

      market.addExistingStock(apple);
      market.addExistingStock(google);

      // ─── Assignment 5: Observer Pattern ──────────────────────────
      System.out.println("\n--- TESTING OBSERVER PATTERN ---");

      StockListenerService stockListenerService = new StockListenerService(uow,
          stockDao);
      market.addListener(stockListenerService);

      stockListenerService.addListener(
          event -> System.out.println("UI UPDATE: " + event.symbol()
              + " → " + event.currentPrice() + " [" + event.stateName() + "]")
      );

      StockBankruptService stockBankruptService = new StockBankruptService(uow,
          ownedStockDao);
      market.addListener(stockBankruptService);

      // ─── Assignment 6: Transaction Script Pattern ────────────────
      System.out.println("\n--- TESTING BUY & SELL ---");

      StockTradingService tradingService = new StockTradingService(
          uow, stockDao, portfolioDao, ownedStockDao, transactionDao);
      PortfolioQueryService queryService = new PortfolioQueryService(
          uow, stockDao, portfolioDao, ownedStockDao, transactionDao);

      System.out.println("Balance before: " + queryService.getBalance(1));

      tradingService.buyShares(new BuyStockRequest(1, "AAPL", 5));
      System.out.println("Balance after buying 5 AAPL: "
          + queryService.getBalance(1));

      tradingService.sellShares(new SellStockRequest(1, "AAPL", 2));
      System.out.println("Balance after selling 2 AAPL: "
          + queryService.getBalance(1));

      System.out.println("Owned stocks:");
      for (OwnedStock os : queryService.getOwnedStocks(1))
      {
        System.out.println("  " + os.getStockSymbol()
            + " x" + os.getNumberOfShares());
      }

      System.out.println("Total portfolio value: "
          + queryService.getTotalPortfolioValue(1));

      System.out.println("Transaction history:");
      for (Transaction t : queryService.getTransactionHistory(1))
      {
        System.out.println("  " + t.getType() + " " + t.getQuantity()
            + " " + t.getStockSymbol() + " @ " + t.getPricePerShare()
            + " (fee: " + t.getFee() + ")");
      }

      Thread marketThread = new Thread(new MarketUpdateThread());
      marketThread.start();
    }
    catch (Exception e)
    {
      logger.log("ERROR",
          "A critical error occurred during testing: " + e.getMessage());
    }
  }
}