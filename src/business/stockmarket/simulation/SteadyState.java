package business.stockmarket.simulation;

import java.util.Random;

public class SteadyState implements LiveStockState
{
  private static final Random random = new Random();

  @Override public double calculatePriceChange(LiveStock liveStock)
  {

    double change = (random.nextDouble() * 4) - 2;

    int roll = random.nextInt(100);
    if (roll < 5)
      liveStock.setState(new GrowingState());
    else if (roll < 10)
      liveStock.setState(new DecliningState());

    return change;
  }

  @Override public String getStateName()
  {
    return "Steady";
  }
}