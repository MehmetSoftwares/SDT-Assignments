package persistence.interfaces;

public interface UnitOfWork {
  void begin();
  void commit();
  void rollback();
  void refresh();
  StockDao getStockDao();
  PortfolioDao getPortfolioDao();
  OwnedStockDao getOwnedStockDao();
  TransactionDao getTransactionDao();
}