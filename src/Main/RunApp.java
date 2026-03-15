package Main;

import business.services.StockBankruptService;
import business.services.StockListenerService;
import business.stockmarket.StockMarket;
import business.stockmarket.simulation.MarketUpdateThread;
import domain.OwnedStock;
import domain.Portfolio;
import domain.Stock;
import persistence.fileimplementation.FileOwnedStockDao;
import persistence.fileimplementation.FilePortfolioDao;
import persistence.fileimplementation.StockFileDAO;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.interfaces.OwnedStockDao;
import persistence.interfaces.PortfolioDao;
import persistence.interfaces.StockDao;
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

      // Ryd gamle testdata
      uow.begin();
      uow.commit();

      StockDao stockDao = new StockFileDAO(uow);
      PortfolioDao portfolioDao = new FilePortfolioDao(uow);
      OwnedStockDao ownedStockDao = new FileOwnedStockDao(uow);

      logger.log("INFO", "Starting transaction to create data...");
      uow.begin();

      Stock apple = new Stock("AAPL", "Apple Inc.", 150.0, "Active");
      stockDao.create(apple);

      Portfolio myPortfolio = new Portfolio(0, 10000.0);
      portfolioDao.create(myPortfolio);

      OwnedStock purchase = new OwnedStock(0, 1, "AAPL", 10);
      ownedStockDao.create(purchase);

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

            uow.begin();
      Stock google = new Stock("GOOG", "Alphabet Inc.", 120.0, "Steady");
      stockDao.create(google);
      uow.commit();

      // Brug addExistingStock så priserne matcher databasen
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

      StockBankruptService stockBankruptService = new StockBankruptService(uow, ownedStockDao);
      market.addListener(stockBankruptService);

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