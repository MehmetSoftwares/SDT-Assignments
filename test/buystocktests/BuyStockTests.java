package buystocktests;

import business.dtos.BuyStockRequest;
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

public class BuyStockTests {

  private MockUnitOfWork uow;
  private StockTradingService service;

  @BeforeEach
  void setUp() {
    uow = new MockUnitOfWork();
    service = new StockTradingService(uow);

    uow.getStocks().add(new Stock("AAPL", "Apple", 100.0, "Steady"));
    uow.getStocks().add(new Stock("DEAD", "DeadCorp", 50.0, "Bankrupt"));

    Portfolio portfolio = new Portfolio(1, 10000.0);
    portfolio.setId(1);
    uow.getPortfolios().add(portfolio);
  }

  // 1. Zero & One

  @Test
  void buyOneShare_succeeds() {
    service.buyShares(new BuyStockRequest(1, "AAPL", 1));

    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNotNull(owned);
    assertEquals(1, owned.getNumberOfShares());
  }

  @Test
  void buyZeroShares_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.buyShares(new BuyStockRequest(1, "AAPL", 0)));
  }

  @Test
  void buyNewStock_createsNewOwnedStock() {
    service.buyShares(new BuyStockRequest(1, "AAPL", 5));

    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertNotNull(owned);
    assertEquals(5, owned.getNumberOfShares());
    assertEquals("AAPL", owned.getStockSymbol());
  }

  @Test
  void buyExistingStock_updatesQuantity() {
    OwnedStock existing = new OwnedStock(1, 1, "AAPL", 10);
    uow.getOwnedStocks().add(existing);

    service.buyShares(new BuyStockRequest(1, "AAPL", 5));

    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertEquals(15, owned.getNumberOfShares());
  }

  // 2. Many & Boundaries

  @Test
  void buyLargeQuantity_withSufficientBalance_succeeds() {
    Portfolio portfolio = uow.getPortfolioDao().getById(1);
    portfolio.setCurrentBalance(1_000_000.0);

    service.buyShares(new BuyStockRequest(1, "AAPL", 5000));

    OwnedStock owned = uow.getOwnedStockDao().getByStockSymbol("AAPL");
    assertEquals(5000, owned.getNumberOfShares());
  }

  @Test
  void buyShares_balanceExactlyEqualsTotalCost_succeeds() {
    double feeRate = AppConfig.getInstance().getTransactionFee();
    double pricePerShare = 100.0;
    int quantity = 10;
    double totalCost = pricePerShare * quantity * (1 + feeRate);

    Portfolio portfolio = uow.getPortfolioDao().getById(1);
    portfolio.setCurrentBalance(totalCost);

    service.buyShares(new BuyStockRequest(1, "AAPL", quantity));

    assertEquals(0.0, uow.getPortfolioDao().getById(1).getCurrentBalance(), 0.001);
  }

  @Test
  void buyShares_balanceOnePennyShort_throwsException() {
    double feeRate = AppConfig.getInstance().getTransactionFee();
    double pricePerShare = 100.0;
    int quantity = 10;
    double totalCost = pricePerShare * quantity * (1 + feeRate);

    Portfolio portfolio = uow.getPortfolioDao().getById(1);
    portfolio.setCurrentBalance(totalCost - 0.01);

    assertThrows(IllegalArgumentException.class,
        () -> service.buyShares(new BuyStockRequest(1, "AAPL", quantity)));
  }

  // 3. Interface & Exceptions

  @Test
  void buyNegativeShares_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.buyShares(new BuyStockRequest(1, "AAPL", -5)));
  }

  @Test
  void buyBankruptStock_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.buyShares(new BuyStockRequest(1, "DEAD", 1)));
  }

  @Test
  void buyShares_insufficientBalance_throwsException() {
    Portfolio portfolio = uow.getPortfolioDao().getById(1);
    portfolio.setCurrentBalance(1.0);

    assertThrows(IllegalArgumentException.class,
        () -> service.buyShares(new BuyStockRequest(1, "AAPL", 100)));
  }

  @Test
  void buyShares_nullSymbol_throwsException() {
    assertThrows(Exception.class,
        () -> service.buyShares(new BuyStockRequest(1, null, 5)));
  }

  @Test
  void buyShares_emptySymbol_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.buyShares(new BuyStockRequest(1, "", 5)));
  }

  @Test
  void buyShares_unknownSymbol_throwsException() {
    assertThrows(IllegalArgumentException.class,
        () -> service.buyShares(new BuyStockRequest(1, "UNKNOWN", 5)));
  }

  // 4. State & Behavior

  @Test
  void buyShares_portfolioBalanceUpdatedCorrectly() {
    double feeRate = AppConfig.getInstance().getTransactionFee();
    double initialBalance = 10000.0;
    double pricePerShare = 100.0;
    int quantity = 5;
    double expectedCost = pricePerShare * quantity * (1 + feeRate);

    service.buyShares(new BuyStockRequest(1, "AAPL", quantity));

    double actualBalance = uow.getPortfolioDao().getById(1).getCurrentBalance();
    assertEquals(initialBalance - expectedCost, actualBalance, 0.001);
  }

  @Test
  void buyShares_transactionRecordCreated() {
    service.buyShares(new BuyStockRequest(1, "AAPL", 5));

    assertEquals(1, uow.getTransactions().size());
    Transaction t = uow.getTransactions().get(0);
    assertEquals("BUY", t.getType());
    assertEquals("AAPL", t.getStockSymbol());
    assertEquals(5, t.getQuantity());
    assertEquals(100.0, t.getPricePerShare(), 0.001);
  }

  @Test
  void buyExistingStock_quantityIncrementedCorrectly() {
    OwnedStock existing = new OwnedStock(1, 1, "AAPL", 7);
    uow.getOwnedStocks().add(existing);

    service.buyShares(new BuyStockRequest(1, "AAPL", 3));

    assertEquals(10, uow.getOwnedStockDao().getByStockSymbol("AAPL").getNumberOfShares());
  }

  // 5. AppConfig & Fee

  @Test
  void buyShares_feeCalculatedFromAppConfig() {
    double feeRate = AppConfig.getInstance().getTransactionFee();
    int quantity = 10;
    double pricePerShare = 100.0;
    double expectedFee = pricePerShare * quantity * feeRate;

    service.buyShares(new BuyStockRequest(1, "AAPL", quantity));

    Transaction t = uow.getTransactions().get(0);
    assertEquals(expectedFee, t.getFee(), 0.001);
  }

  @Test
  void buyShares_commitCalledOnce_onSuccess() {
    service.buyShares(new BuyStockRequest(1, "AAPL", 1));

    assertEquals(1, uow.getCommitCount());
    assertEquals(0, uow.getRollbackCount());
  }

  @Test
  void buyShares_rollbackCalledOnce_onFailure() {
    assertThrows(IllegalArgumentException.class,
        () -> service.buyShares(new BuyStockRequest(1, "AAPL", -1)));

    assertEquals(0, uow.getCommitCount());
    assertEquals(1, uow.getRollbackCount());
  }
  @Test
  void buyShares_zeroFee_totalCostEqualsSubtotal() {
    AppConfig.getInstance().setTransactionFee(0.0);

    service.buyShares(new BuyStockRequest(1, "AAPL", 10));

    Transaction t = uow.getTransactions().get(0);
    assertEquals(0.0, t.getFee(), 0.001);
    assertEquals(1000.0, t.getTotalAmount(), 0.001);

    AppConfig.getInstance().setTransactionFee(0.05);
  }

  @Test
  void buyShares_negativeFee_throwsOrHandlesGracefully() {
    AppConfig.getInstance().setTransactionFee(-0.1);

    service.buyShares(new BuyStockRequest(1, "AAPL", 10));

    Transaction t = uow.getTransactions().get(0);
    assertTrue(t.getTotalAmount() < 1000.0);

    AppConfig.getInstance().setTransactionFee(0.05);
  }
}