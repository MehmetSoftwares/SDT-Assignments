package integrationtests;

import business.dtos.BuyStockRequest;
import business.services.PortfolioQueryService;
import business.services.StockTradingService;
import domain.OwnedStock;
import domain.Portfolio;
import domain.Stock;
import domain.Transaction;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.fileimplementation.FileUnitOfWork;
import persistence.interfaces.UnitOfWork;
import shared.configuration.AppConfig;
import shared.logging.ConsoleLogOutput;
import shared.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BuyStockIntegrationTest {

  private static final String TEST_DB = "test-database";
  private UnitOfWork uow;
  private StockTradingService tradingService;
  private PortfolioQueryService queryService;

  @BeforeAll
  static void initJFX() {
    try {
      Platform.startup(() -> {});
    } catch (IllegalStateException e) {
    }
    Logger.getInstance().setLogOutput(new ConsoleLogOutput());
    AppConfig.getInstance().setTransactionFee(0.05);
  }

  @BeforeEach
  void setUp() throws IOException {
    clearTestDirectory();
    uow = new FileUnitOfWork(TEST_DB);
    tradingService = new StockTradingService(uow);
    queryService = new PortfolioQueryService(uow);

    uow.begin();
    uow.getStockDao().create(new Stock("AAPL", "Apple", 100.0, "Steady"));
    uow.getStockDao().create(new Stock("DEAD", "DeadCorp", 50.0, "Bankrupt"));
    uow.getPortfolioDao().create(new Portfolio(1, 10000.0));
    uow.commit();
  }

  private void clearTestDirectory() throws IOException {
    File dir = new File(TEST_DB);
    if (dir.exists()) {
      for (File file : dir.listFiles()) {
        file.delete();
      }
    }
  }

  @Test
  void buyOneShare_persistedCorrectly() {
    tradingService.buyShares(new BuyStockRequest(1, "AAPL", 1));

    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNotNull(owned);
    assertEquals(1, owned.getNumberOfShares());
  }

  @Test
  void buyShares_balanceUpdatedInFile() {
    double feeRate = AppConfig.getInstance().getTransactionFee();
    double expectedCost = 100.0 * 5 * (1 + feeRate);

    tradingService.buyShares(new BuyStockRequest(1, "AAPL", 5));

    double balance = queryService.getBalance(1);
    assertEquals(10000.0 - expectedCost, balance, 0.01);
  }

  @Test
  void buyShares_transactionPersistedToFile() {
    tradingService.buyShares(new BuyStockRequest(1, "AAPL", 3));

    List<Transaction> history = queryService.getTransactionHistory(1);
    assertEquals(1, history.size());
    Transaction t = history.get(0);
    assertEquals("BUY", t.getType());
    assertEquals("AAPL", t.getStockSymbol());
    assertEquals(3, t.getQuantity());
  }

  @Test
  void buyExistingStock_quantityAccumulatesInFile() {
    tradingService.buyShares(new BuyStockRequest(1, "AAPL", 5));
    tradingService.buyShares(new BuyStockRequest(1, "AAPL", 3));

    uow.refresh();
    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertEquals(8, owned.getNumberOfShares());
  }

  @Test
  void buyShares_exactBalance_succeeds() {
    double feeRate = AppConfig.getInstance().getTransactionFee();
    double totalCost = 100.0 * 10 * (1 + feeRate);

    uow.begin();
    Portfolio p = uow.getPortfolioDao().getById(1);
    p.setCurrentBalance(totalCost);
    uow.getPortfolioDao().update(p);
    uow.commit();

    tradingService.buyShares(new BuyStockRequest(1, "AAPL", 10));

    assertEquals(0.0, queryService.getBalance(1), 0.01);
  }

  @Test
  void buyShares_insufficientBalance_noDataChanged() {
    uow.begin();
    Portfolio p = uow.getPortfolioDao().getById(1);
    p.setCurrentBalance(1.0);
    uow.getPortfolioDao().update(p);
    uow.commit();

    assertThrows(IllegalArgumentException.class,
        () -> tradingService.buyShares(new BuyStockRequest(1, "AAPL", 100)));

    assertEquals(1.0, queryService.getBalance(1), 0.01);
    assertNull(uow.getOwnedStockDao().getByStockSymbol("AAPL"));
  }

  @Test
  void buyBankruptStock_noDataChanged() {
    assertThrows(IllegalArgumentException.class,
        () -> tradingService.buyShares(new BuyStockRequest(1, "DEAD", 1)));

    assertEquals(10000.0, queryService.getBalance(1), 0.01);
  }

  @Test
  void buyZeroShares_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> tradingService.buyShares(new BuyStockRequest(1, "AAPL", 0)));
  }

  @Test
  void buyNegativeShares_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> tradingService.buyShares(new BuyStockRequest(1, "AAPL", -5)));
  }

  @Test
  void buyUnknownStock_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> tradingService.buyShares(new BuyStockRequest(1, "UNKNOWN", 5)));
  }

  @Test
  void buyShares_dataPersistedAcrossNewUowInstance() {
    tradingService.buyShares(new BuyStockRequest(1, "AAPL", 5));

    UnitOfWork freshUow = new FileUnitOfWork(TEST_DB);
    OwnedStock owned = freshUow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNotNull(owned);
    assertEquals(5, owned.getNumberOfShares());
  }
}