package business.services;

import business.dtos.BuyStockRequest;
import business.dtos.SellStockRequest;
import domain.OwnedStock;
import domain.Portfolio;
import domain.Stock;
import domain.Transaction;
import persistence.interfaces.OwnedStockDao;
import persistence.interfaces.PortfolioDao;
import persistence.interfaces.StockDao;
import persistence.interfaces.TransactionDao;
import persistence.interfaces.UnitOfWork;
import shared.configuration.AppConfig;
import shared.logging.Logger;

import java.time.LocalDateTime;

public class StockTradingService {

    private final UnitOfWork uow;
    private final StockDao stockDao;
    private final PortfolioDao portfolioDao;
    private final OwnedStockDao ownedStockDao;
    private final TransactionDao transactionDao;
    private final Logger logger = Logger.getInstance();
    private final AppConfig config = AppConfig.getInstance();

  public StockTradingService(UnitOfWork uow) {
    this.uow = uow;
    this.stockDao = uow.getStockDao();
    this.portfolioDao = uow.getPortfolioDao();
    this.ownedStockDao = uow.getOwnedStockDao();
    this.transactionDao = uow.getTransactionDao();
  }

    public void buyShares(BuyStockRequest request) {
        try {
            uow.begin();

            if (request.numberOfShares() <= 0) {
                throw new IllegalArgumentException(
                    "Number of shares must be positive");
            }

            Stock stock = stockDao.getById(request.stockSymbol());
            if (stock == null) {
                throw new IllegalArgumentException(
                    "Stock not found: " + request.stockSymbol());
            }

            if ("Bankrupt".equals(stock.getCurrentState())) {
                throw new IllegalArgumentException(
                    "Cannot buy bankrupt stock: " + request.stockSymbol());
            }

            Portfolio portfolio = portfolioDao.getById(request.portfolioId());
            if (portfolio == null) {
                throw new IllegalArgumentException(
                    "Portfolio not found: " + request.portfolioId());
            }

            double pricePerShare = stock.getCurrentPrice();
            double subtotal = pricePerShare * request.numberOfShares();
            double feeRate = config.getTransactionFee();
            double feeAmount = subtotal * feeRate;
            double totalCost = subtotal + feeAmount;

            if (portfolio.getCurrentBalance() < totalCost) {
                throw new IllegalArgumentException(
                    "Insufficient balance. Need: " + totalCost
                        + ", have: " + portfolio.getCurrentBalance());
            }

            OwnedStock owned = ownedStockDao.getByStockSymbol(
                request.stockSymbol());
            if (owned != null) {
                owned.setNumberOfShares(
                    owned.getNumberOfShares() + request.numberOfShares());
                ownedStockDao.update(owned);
            } else {
                OwnedStock newOwned = new OwnedStock(
                    0, request.portfolioId(),
                    request.stockSymbol(), request.numberOfShares());
                ownedStockDao.create(newOwned);
            }

            portfolio.setCurrentBalance(
                portfolio.getCurrentBalance() - totalCost);
            portfolioDao.update(portfolio);

            Transaction transaction = new Transaction(
                0, request.portfolioId(), request.stockSymbol(),
                "BUY", request.numberOfShares(), pricePerShare,
                totalCost, feeAmount, LocalDateTime.now());
            transactionDao.create(transaction);

            uow.commit();
            logger.log("INFO",
                "Bought " + request.numberOfShares() + " shares of "
                    + request.stockSymbol() + " for " + totalCost);

        } catch (Exception e) {
            uow.rollback();
            logger.log("ERROR",
                "Failed to buy shares: " + e.getMessage());
            throw e;
        }
    }

    public void sellShares(SellStockRequest request) {
        try {
            uow.begin();

            if (request.numberOfShares() <= 0) {
                throw new IllegalArgumentException(
                    "Number of shares must be positive");
            }

            Stock stock = stockDao.getById(request.stockSymbol());
            if (stock == null) {
                throw new IllegalArgumentException(
                    "Stock not found: " + request.stockSymbol());
            }

            OwnedStock owned = ownedStockDao.getByStockSymbol(
                request.stockSymbol());
            if (owned == null) {
                throw new IllegalArgumentException(
                    "You don't own shares of: " + request.stockSymbol());
            }

            if (request.numberOfShares() > owned.getNumberOfShares()) {
                throw new IllegalArgumentException(
                    "Cannot sell " + request.numberOfShares()
                        + " shares, you only own "
                        + owned.getNumberOfShares());
            }

            Portfolio portfolio = portfolioDao.getById(request.portfolioId());
            if (portfolio == null) {
                throw new IllegalArgumentException(
                    "Portfolio not found: " + request.portfolioId());
            }

            double pricePerShare = stock.getCurrentPrice();
            double subtotal = pricePerShare * request.numberOfShares();
            double feeRate = config.getTransactionFee();
            double feeAmount = subtotal * feeRate;
            double totalRevenue = subtotal - feeAmount;

            int remainingShares = owned.getNumberOfShares()
                - request.numberOfShares();
            if (remainingShares == 0) {
                ownedStockDao.delete(owned.getId());
            } else {
                owned.setNumberOfShares(remainingShares);
                ownedStockDao.update(owned);
            }

            portfolio.setCurrentBalance(
                portfolio.getCurrentBalance() + totalRevenue);
            portfolioDao.update(portfolio);

            Transaction transaction = new Transaction(
                0, request.portfolioId(), request.stockSymbol(),
                "SELL", request.numberOfShares(), pricePerShare,
                totalRevenue, feeAmount, LocalDateTime.now());
            transactionDao.create(transaction);

            uow.commit();
            logger.log("INFO",
                "Sold " + request.numberOfShares() + " shares of "
                    + request.stockSymbol() + " for " + totalRevenue);

        } catch (Exception e) {
            uow.rollback();
            logger.log("ERROR",
                "Failed to sell shares: " + e.getMessage());
            throw e;
        }
    }
}