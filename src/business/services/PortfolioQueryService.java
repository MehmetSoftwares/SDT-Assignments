package business.services;

import domain.OwnedStock;
import domain.Portfolio;
import domain.Stock;
import domain.Transaction;
import persistence.interfaces.OwnedStockDao;
import persistence.interfaces.PortfolioDao;
import persistence.interfaces.StockDao;
import persistence.interfaces.TransactionDao;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.util.List;

public class PortfolioQueryService {

    private final UnitOfWork uow;
    private final StockDao stockDao;
    private final PortfolioDao portfolioDao;
    private final OwnedStockDao ownedStockDao;
    private final TransactionDao transactionDao;
    private final Logger logger = Logger.getInstance();

  public PortfolioQueryService(UnitOfWork uow) {
    this.uow = uow;
    this.stockDao = uow.getStockDao();
    this.portfolioDao = uow.getPortfolioDao();
    this.ownedStockDao = uow.getOwnedStockDao();
    this.transactionDao = uow.getTransactionDao();
  }

  public List<Stock> getAvailableStocks() {
    uow.refresh();
    return stockDao.getAll();
  }

  public List<OwnedStock> getOwnedStocks(int portfolioId) {
    uow.refresh();
    return ownedStockDao.getByPortfolioId(portfolioId);
  }

  public double getBalance(int portfolioId) {
    uow.refresh();
    Portfolio portfolio = portfolioDao.getById(portfolioId);
    if (portfolio == null) {
      throw new IllegalArgumentException(
          "Portfolio not found: " + portfolioId);
    }
    return portfolio.getCurrentBalance();
  }

  public double getTotalPortfolioValue(int portfolioId) {
    uow.refresh();
    Portfolio portfolio = portfolioDao.getById(portfolioId);
    if (portfolio == null) {
      throw new IllegalArgumentException(
          "Portfolio not found: " + portfolioId);
    }

    double stockValue = 0;
    List<OwnedStock> ownedStocks = ownedStockDao.getByPortfolioId(
        portfolioId);
    for (OwnedStock owned : ownedStocks) {
      Stock stock = stockDao.getById(owned.getStockSymbol());
      if (stock != null) {
        stockValue += stock.getCurrentPrice()
            * owned.getNumberOfShares();
      }
    }

    return portfolio.getCurrentBalance() + stockValue;
  }

  public List<Transaction> getTransactionHistory(int portfolioId) {
    uow.refresh();
    return transactionDao.getByPortfolioId(portfolioId);
  }
}