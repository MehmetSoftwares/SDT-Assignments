package business.services;

import business.events.StockUpdateEvent;
import business.events.StockUpdateListener;
import domain.OwnedStock;
import persistence.interfaces.OwnedStockDao;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.util.List;

public class StockBankruptService implements StockUpdateListener {

  private final UnitOfWork uow;
  private final OwnedStockDao ownedStockDao;
  private final Logger logger = Logger.getInstance();

  public StockBankruptService(UnitOfWork uow, OwnedStockDao ownedStockDao) {
    this.uow = uow;
    this.ownedStockDao = ownedStockDao;
  }

  @Override
  public void onStockUpdate(StockUpdateEvent event) {
    if (!event.isBankrupt()) {
      return;
    }

    try {
      uow.begin();

      List<OwnedStock> ownedStocks = ownedStockDao.getByStockSymbol(event.symbol());

      for (OwnedStock owned : ownedStocks) {
        ownedStockDao.delete(owned.getId());
        logger.log("INFO",
            "Player lost " + owned.getNumberOfShares()
                + " shares of " + event.symbol()
                + " due to bankruptcy");
      }

      uow.commit();

    } catch (Exception e) {
      uow.rollback();
      logger.log("ERROR",
          "Failed to handle bankruptcy for " + event.symbol()
              + ": " + e.getMessage());
    }
  }
}