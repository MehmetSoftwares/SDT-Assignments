package integrationtests;

import business.dtos.BuyStockRequest;
import business.dtos.SellStockRequest;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SellStockIntegrationTest {

  private static final String TEST_DB = "test-database-sell";
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
    uow.getPortfolioDao().create(new Portfolio(1, 10000.0));
    uow.commit();

    tradingService.buyShares(new BuyStockRequest(1, "AAPL", 20));
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
  void sellOneShare_persistedCorrectly() {
    tradingService.sellShares(new SellStockRequest(1, "AAPL", 1));

    uow.refresh();
    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNotNull(owned);
    assertEquals(19, owned.getNumberOfShares());
  }

  @Test
  void sellAllShares_removedFromFile() {
    tradingService.sellShares(new SellStockRequest(1, "AAPL", 20));

    uow.refresh();
    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNull(owned);
  }

  @Test
  void sellShares_balanceIncreasedInFile() {
    double balanceBefore = queryService.getBalance(1);
    double feeRate = AppConfig.getInstance().getTransactionFee();
    double expectedRevenue = 100.0 * 5 * (1 - feeRate);

    tradingService.sellShares(new SellStockRequest(1, "AAPL", 5));

    double balanceAfter = queryService.getBalance(1);
    assertEquals(balanceBefore + expectedRevenue, balanceAfter, 0.01);
  }

  @Test
  void sellShares_transactionPersistedToFile() {
    tradingService.sellShares(new SellStockRequest(1, "AAPL", 5));

    List<Transaction> history = queryService.getTransactionHistory(1);
    Transaction sellTx = history.stream()
        .filter(t -> t.getType().equals("SELL"))
        .findFirst().orElse(null);

    assertNotNull(sellTx);
    assertEquals("AAPL", sellTx.getStockSymbol());
    assertEquals(5, sellTx.getQuantity());
  }

  @Test
  void sellMoreThanOwned_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> tradingService.sellShares(new SellStockRequest(1, "AAPL", 21)));
  }

  @Test
  void sellStockNotOwned_throwsException() {
    uow.begin();
    uow.getStockDao().create(new Stock("GOOG", "Alphabet", 120.0, "Steady"));
    uow.commit();

    assertThrows(IllegalArgumentException.class,
        () -> tradingService.sellShares(new SellStockRequest(1, "GOOG", 1)));
  }

  @Test
  void sellZeroShares_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> tradingService.sellShares(new SellStockRequest(1, "AAPL", 0)));
  }

  @Test
  void sellNegativeShares_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> tradingService.sellShares(new SellStockRequest(1, "AAPL", -5)));
  }

  @Test
  void sellShares_failureDoesNotChangeData() {
    double balanceBefore = queryService.getBalance(1);

    assertThrows(IllegalArgumentException.class,
        () -> tradingService.sellShares(new SellStockRequest(1, "AAPL", 21)));

    assertEquals(balanceBefore, queryService.getBalance(1), 0.01);
    uow.refresh();
    assertEquals(20, uow.getOwnedStockDao().getByStockSymbol("AAPL").getNumberOfShares());
  }

  @Test
  void sellShares_dataPersistedAcrossNewUowInstance() {
    tradingService.sellShares(new SellStockRequest(1, "AAPL", 10));

    UnitOfWork freshUow = new FileUnitOfWork(TEST_DB);
    OwnedStock owned = freshUow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNotNull(owned);
    assertEquals(10, owned.getNumberOfShares());
  }
}