package business.services;

import business.events.StockChangeListener;
import business.events.StockUpdateEvent;
import business.events.StockUpdateListener;
import domain.Stock;
import persistence.interfaces.StockDao;
import persistence.interfaces.UnitOfWork;
import shared.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class StockListenerService implements StockUpdateListener {

  private final UnitOfWork uow;
  private final StockDao stockDao;
  private final Logger logger = Logger.getInstance();
  private final List<StockChangeListener> listeners = new ArrayList<>();

  public StockListenerService(UnitOfWork uow) {
    this.uow = uow;
    this.stockDao = uow.getStockDao();
  }

  public void addListener(StockChangeListener listener) {
    listeners.add(listener);
  }

  public void removeListener(StockChangeListener listener) {
    listeners.remove(listener);
  }

  private void notifyListeners(StockUpdateEvent event) {
    for (StockChangeListener listener : listeners) {
      listener.onStockChange(event);
    }
  }

  @Override
  public void onStockUpdate(StockUpdateEvent event) {
    try {
      uow.begin();

      Stock stock = stockDao.getById(event.symbol());
      if (stock == null) {
        logger.log("WARNING",
            "Stock not found in database: " + event.symbol());
        uow.rollback();
        return;
      }

      stock.setCurrentPrice(event.currentPrice());
      stock.setCurrentState(event.stateName());
      stockDao.update(stock);

      uow.commit();
      logger.log("INFO",
          "Persisted update for " + event.symbol()
              + " | price: " + event.currentPrice()
              + " | state: " + event.stateName());

      notifyListeners(event);

    } catch (Exception e) {
      uow.rollback();
      logger.log("ERROR",
          "Failed to persist update for " + event.symbol()
              + ": " + e.getMessage());
    }
  }
}