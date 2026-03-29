package business.services;

import business.events.StockUpdateEvent;
import business.events.StockUpdateListener;
import domain.OwnedStock;
import persistence.interfaces.OwnedStockDao;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

public class StockBankruptService implements StockUpdateListener
{

  private final UnitOfWork uow;
  private final OwnedStockDao ownedStockDao;
  private final Logger logger = Logger.getInstance();

  public StockBankruptService(UnitOfWork uow) {
    this.uow = uow;
    this.ownedStockDao = uow.getOwnedStockDao();
  }

  @Override public void onStockUpdate(StockUpdateEvent event)
  {
    if (!event.isBankrupt())
    {
      return;
    }

    try
    {
      uow.begin();

      OwnedStock owned = ownedStockDao.getByStockSymbol(event.symbol());
      if (owned != null)
      {
        ownedStockDao.delete(owned.getId());
        logger.log("INFO",
            "Player lost " + owned.getNumberOfShares() + " shares of "
                + event.symbol() + " due to bankruptcy");
      }

      uow.commit();

    }
    catch (Exception e)
    {
      uow.rollback();
      logger.log("ERROR",
          "Failed to handle bankruptcy for " + event.symbol() + ": "
              + e.getMessage());
    }
  }
}