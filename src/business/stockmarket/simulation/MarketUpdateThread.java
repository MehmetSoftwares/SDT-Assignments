package business.stockmarket.simulation;

import business.stockmarket.StockMarket;
import shared.configuration.AppConfig;
import shared.logging.Logger;

public class MarketUpdateThread implements Runnable
{
  private final StockMarket stockMarket;
  private final int updateFrequency;
  private final Logger logger = Logger.getInstance();

  public MarketUpdateThread()
  {
    this.stockMarket = StockMarket.getInstance();
    this.updateFrequency = AppConfig.getInstance().getUpdateFrequencyInMs();
  }

  @Override public void run()
  {
    logger.log("INFO", "MarketUpdateThread started.");

    while (true)
    {
      try
      {
        stockMarket.updateAllStocks();
        Thread.sleep(updateFrequency);
      }
      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
        logger.log("WARNING", "MarketUpdateThread stopped.");
        break;
      }
    }
  }
}