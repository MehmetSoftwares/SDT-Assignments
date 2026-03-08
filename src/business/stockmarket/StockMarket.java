package business.stockmarket;

import business.stockmarket.simulation.LiveStock;
import domain.Stock;
import shared.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class StockMarket
{
  private static volatile StockMarket instance;
  private final List<LiveStock> liveStocks = new ArrayList<>();
  private final Logger logger = Logger.getInstance();

  private StockMarket()
  {
  }

  public static StockMarket getInstance()
  {
    if (instance == null)
    {
      synchronized (StockMarket.class)
      {
        if (instance == null)
          instance = new StockMarket();
      }
    }
    return instance;
  }

  public void addNewStock(String symbol)
  {
    LiveStock ls = new LiveStock(symbol);
    liveStocks.add(ls);
    logger.log("INFO", "New LiveStock added: " + symbol);
  }

  public void addExistingStock(Stock stock)
  {
    LiveStock ls = new LiveStock(stock.getSymbol(), stock.getCurrentPrice(),
        stock.getCurrentState());
    liveStocks.add(ls);
    logger.log("INFO", "Existing LiveStock added: " + stock.getSymbol());
  }

  public void updateAllStocks()
  {
    for (LiveStock ls : liveStocks)
    {
      ls.updatePrice();
      logger.log("INFO",
          ls.getSymbol() + " | " + ls.getStateName() + " | price: "
              + ls.getCurrentPrice());
    }
  }

  public List<LiveStock> getLiveStocks()
  {
    return liveStocks;
  }
}