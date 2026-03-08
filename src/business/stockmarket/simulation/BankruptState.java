package business.stockmarket.simulation;

import java.util.Random;

public class BankruptState implements LiveStockState
{
  private static final Random random = new Random();
  private int tickCount = 0;

  @Override public double calculatePriceChange(LiveStock liveStock)
  {
    tickCount++;

    if (tickCount >= 3)
      liveStock.setState(new ResetState());

    return 0;
  }

  @Override public String getStateName()
  {
    return "Bankrupt";
  }
}