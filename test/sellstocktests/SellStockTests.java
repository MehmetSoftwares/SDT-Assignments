package sellstocktests;

import business.dtos.SellStockRequest;
import business.services.StockTradingService;
import domain.OwnedStock;
import domain.Portfolio;
import domain.Stock;
import domain.Transaction;
import mocks.MockUnitOfWork;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shared.configuration.AppConfig;

import static org.junit.jupiter.api.Assertions.*;

public class SellStockTests {

  private MockUnitOfWork uow;
  private StockTradingService service;

  @BeforeEach
  void setUp() {
    uow = new MockUnitOfWork();
    service = new StockTradingService(uow);

    uow.getStocks().add(new Stock("AAPL", "Apple", 100.0, "Steady"));

    Portfolio portfolio = new Portfolio(1, 10000.0);
    portfolio.setId(1);
    uow.getPortfolios().add(portfolio);

    OwnedStock owned = new OwnedStock(1, 1, "AAPL", 20);
    uow.getOwnedStocks().add(owned);

    AppConfig.getInstance().setTransactionFee(0.05);
  }

  // 1. Zero & One

  @Test
  void sellOneShare_succeeds() {
    service.sellShares(new SellStockRequest(1, "AAPL", 1));

    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNotNull(owned);
    assertEquals(19, owned.getNumberOfShares());
  }

  @Test
  void sellZeroShares_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.sellShares(new SellStockRequest(1, "AAPL", 0)));
  }

  @Test
  void sellAllShares_removesOwnedStock() {
    service.sellShares(new SellStockRequest(1, "AAPL", 20));

    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNull(owned);
  }

  // 2. Many & Boundaries

  @Test
  void sellMoreThanOwned_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.sellShares(new SellStockRequest(1, "AAPL", 21)));
  }

  @Test
  void sellExactlyOwned_removesOwnedStock() {
    service.sellShares(new SellStockRequest(1, "AAPL", 20));

    assertNull(uow.getOwnedStockDao().getByStockSymbol("AAPL"));
    assertEquals(0, uow.getOwnedStocks().size());
  }

  @Test
  void sellOneShareLessThanOwned_updatesQuantity() {
    service.sellShares(new SellStockRequest(1, "AAPL", 19));

    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertEquals(1, owned.getNumberOfShares());
  }

  // 3. Interface & Exceptions

  @Test
  void sellNegativeShares_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.sellShares(new SellStockRequest(1, "AAPL", -5)));
  }

  @Test
  void sellStockNotOwned_throwsException() {
    uow.getStocks().add(new Stock("GOOG", "Alphabet", 120.0, "Steady"));

    assertThrows(IllegalArgumentException.class,
        () -> service.sellShares(new SellStockRequest(1, "GOOG", 1)));
  }

  @Test
  void sellShares_nullSymbol_throwsException() {
    assertThrows(Exception.class,
        () -> service.sellShares(new SellStockRequest(1, null, 5)));
  }

  @Test
  void sellShares_emptySymbol_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.sellShares(new SellStockRequest(1, "", 5)));
  }

  @Test
  void sellShares_unknownSymbol_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.sellShares(new SellStockRequest(1, "UNKNOWN", 5)));
  }

  @Test
  void sellShares_portfolioNotFound_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.sellShares(new SellStockRequest(999, "AAPL", 1)));
  }

  // 4. State & Behavior

  @Test
  void sellShares_portfolioBalanceUpdatedCorrectly() {
    double feeRate = AppConfig.getInstance().getTransactionFee();
    double initialBalance = 10000.0;
    double pricePerShare = 100.0;
    int quantity = 5;
    double expectedRevenue = pricePerShare * quantity * (1 - feeRate);

    service.sellShares(new SellStockRequest(1, "AAPL", quantity));

    double actualBalance = uow.getPortfolioDao().getById(1).getCurrentBalance();
    assertEquals(initialBalance + expectedRevenue, actualBalance, 0.001);
  }

  @Test
  void sellShares_transactionRecordCreated() {
    service.sellShares(new SellStockRequest(1, "AAPL", 5));

    assertEquals(1, uow.getTransactions().size());
    Transaction t = uow.getTransactions().get(0);
    assertEquals("SELL", t.getType());
    assertEquals("AAPL", t.getStockSymbol());
    assertEquals(5, t.getQuantity());
    assertEquals(100.0, t.getPricePerShare(), 0.001);
  }

  @Test
  void sellShares_quantityDecrementedCorrectly() {
    service.sellShares(new SellStockRequest(1, "AAPL", 7));

    assertEquals(13, uow.getOwnedStockDao().getByStockSymbol("AAPL").getNumberOfShares());
  }

  // 5. Fee & AppConfig

  @Test
  void sellShares_feeCalculatedFromAppConfig() {
    double feeRate = AppConfig.getInstance().getTransactionFee();
    int quantity = 10;
    double pricePerShare = 100.0;
    double expectedFee = pricePerShare * quantity * feeRate;

    service.sellShares(new SellStockRequest(1, "AAPL", quantity));

    Transaction t = uow.getTransactions().get(0);
    assertEquals(expectedFee, t.getFee(), 0.001);
  }

  @Test
  void sellShares_zeroFee_revenueEqualsSubtotal() {
    AppConfig.getInstance().setTransactionFee(0.0);

    service.sellShares(new SellStockRequest(1, "AAPL", 10));

    Transaction t = uow.getTransactions().get(0);
    assertEquals(0.0, t.getFee(), 0.001);
    assertEquals(1000.0, t.getTotalAmount(), 0.001);

    AppConfig.getInstance().setTransactionFee(0.05);
  }

  // 6. Lifecycle

  @Test
  void sellShares_commitCalledOnce_onSuccess() {
    service.sellShares(new SellStockRequest(1, "AAPL", 1));

    assertEquals(1, uow.getCommitCount());
    assertEquals(0, uow.getRollbackCount());
  }

  @Test
  void sellShares_rollbackCalledOnce_onFailure() {
    assertThrows(IllegalArgumentException.class,
        () -> service.sellShares(new SellStockRequest(1, "AAPL", -1)));

    assertEquals(0, uow.getCommitCount());
    assertEquals(1, uow.getRollbackCount());
  }
}