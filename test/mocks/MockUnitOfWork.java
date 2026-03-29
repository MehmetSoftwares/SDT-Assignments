package mocks;

import domain.OwnedStock;
import domain.Portfolio;
import domain.Stock;
import domain.Transaction;
import persistence.interfaces.OwnedStockDao;
import persistence.interfaces.PortfolioDao;
import persistence.interfaces.StockDao;
import persistence.interfaces.TransactionDao;
import persistence.interfaces.UnitOfWork;

import java.util.ArrayList;
import java.util.List;

public class MockUnitOfWork implements UnitOfWork {

    private final List<Stock> stocks = new ArrayList<>();
    private final List<Portfolio> portfolios = new ArrayList<>();
    private final List<OwnedStock> ownedStocks = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    private final StockDao stockDao;
    private final PortfolioDao portfolioDao;
    private final OwnedStockDao ownedStockDao;
    private final TransactionDao transactionDao;

    private int commitCount = 0;
    private int rollbackCount = 0;
    private int beginCount = 0;

    public MockUnitOfWork() {
        this.stockDao = new MockStockDao(stocks);
        this.portfolioDao = new MockPortfolioDao(portfolios);
        this.ownedStockDao = new MockOwnedStockDao(ownedStocks);
        this.transactionDao = new MockTransactionDao(transactions);
    }

    @Override public void begin() { beginCount++; }
    @Override public void commit() { commitCount++; }
    @Override public void rollback() { rollbackCount++; }
    @Override public void refresh() {}

    @Override public StockDao getStockDao() { return stockDao; }
    @Override public PortfolioDao getPortfolioDao() { return portfolioDao; }
    @Override public OwnedStockDao getOwnedStockDao() { return ownedStockDao; }
    @Override public TransactionDao getTransactionDao() { return transactionDao; }

    public int getCommitCount() { return commitCount; }
    public int getRollbackCount() { return rollbackCount; }
    public int getBeginCount() { return beginCount; }

    public List<Stock> getStocks() { return stocks; }
    public List<Portfolio> getPortfolios() { return portfolios; }
    public List<OwnedStock> getOwnedStocks() { return ownedStocks; }
    public List<Transaction> getTransactions() { return transactions; }
}