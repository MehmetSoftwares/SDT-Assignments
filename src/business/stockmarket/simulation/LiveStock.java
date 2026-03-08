package business.stockmarket.simulation;

import shared.configuration.AppConfig;
import shared.logging.Logger;

public class LiveStock
{
  private final String symbol;
  private LiveStockState currentState;
  private double currentPrice;

  private Logger logger = Logger.getInstance();

  public LiveStock(String symbol)
  {
    this.symbol = symbol;
    this.currentPrice = AppConfig.getInstance().getStockResetValue();
    this.currentState = new SteadyState();
    logger.log("INFO", "LiveStock created: " + symbol + " @ " + currentPrice);
  }

  public LiveStock(String symbol, double currentPrice, String stateName)
  {
    this.symbol = symbol;
    this.currentPrice = currentPrice;
    this.currentState = mapState(stateName);
    logger.log("INFO",
        "LiveStock started again: " + symbol + " state=" + stateName);
  }

  public void updatePrice()
  {
    double priceChange = currentState.calculatePriceChange(this);
    currentPrice += priceChange;

    if (currentPrice <= 0)
    {
      currentPrice = 0;
      setState(new BankruptState());
      logger.log("INFO", symbol + " is bankrupt!");
    }
  }

  //  kun states må kalde den her
  void setState(LiveStockState newState)
  {
    logger.log("INFO",
        symbol + " changing state: " + currentState.getStateName() + " → "
            + newState.getStateName());
    this.currentState = newState;
  }

  private LiveStockState mapState(String stateName)
  {
    return switch (stateName)
    {
      case "Growing" -> new GrowingState();
      case "Declining" -> new DecliningState();
      case "Bankrupt" -> new BankruptState();
      case "Reset" -> new ResetState();
      default -> new SteadyState();
    };
  }

  public String getSymbol()
  {
    return symbol;
  }

  public double getCurrentPrice()
  {
    return currentPrice;
  }

  public String getStateName()
  {
    return currentState.getStateName();
  }
}