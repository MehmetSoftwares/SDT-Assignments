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

    public PortfolioQueryService(UnitOfWork uow, StockDao stockDao,
            PortfolioDao portfolioDao, OwnedStockDao ownedStockDao,
            TransactionDao transactionDao) {
        this.uow = uow;
        this.stockDao = stockDao;
        this.portfolioDao = portfolioDao;
        this.ownedStockDao = ownedStockDao;
        this.transactionDao = transactionDao;
    }

    public List<Stock> getAvailableStocks() {
        uow.begin();
        List<Stock> stocks = stockDao.getAll();
        uow.commit();
        return stocks;
    }

    public List<OwnedStock> getOwnedStocks(int portfolioId) {
        uow.begin();
        List<OwnedStock> owned = ownedStockDao.getByPortfolioId(portfolioId);
        uow.commit();
        return owned;
    }

    public double getBalance(int portfolioId) {
        uow.begin();
        Portfolio portfolio = portfolioDao.getById(portfolioId);
        uow.commit();
        if (portfolio == null) {
            throw new IllegalArgumentException(
                "Portfolio not found: " + portfolioId);
        }
        return portfolio.getCurrentBalance();
    }

    public double getTotalPortfolioValue(int portfolioId) {
        uow.begin();
        Portfolio portfolio = portfolioDao.getById(portfolioId);
        if (portfolio == null) {
            uow.commit();
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

        uow.commit();
        return portfolio.getCurrentBalance() + stockValue;
    }

    public List<Transaction> getTransactionHistory(int portfolioId) {
        uow.begin();
        List<Transaction> transactions = transactionDao.getByPortfolioId(
            portfolioId);
        uow.commit();
        return transactions;
    }
}