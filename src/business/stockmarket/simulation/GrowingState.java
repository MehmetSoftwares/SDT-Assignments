package business.stockmarket.simulation;

import java.util.Random;

public class GrowingState implements LiveStockState
{
  private static final Random random = new Random();
  private int tickCount = 0;

  @Override public double calculatePriceChange(LiveStock liveStock)
  {

    double change = (random.nextDouble() * 10) - 2;
    tickCount++;

    if (tickCount >= 5 && random.nextBoolean())
      liveStock.setState(new SteadyState());

    return change;
  }

  @Override public String getStateName()
  {
    return "Growing";
  }
}