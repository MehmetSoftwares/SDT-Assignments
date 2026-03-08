package business.stockmarket.simulation;

import shared.configuration.AppConfig;

public class ResetState implements LiveStockState
{
  @Override public double calculatePriceChange(LiveStock liveStock)
  {

    double resetPrice = AppConfig.getInstance().getStockResetValue();
    double change = resetPrice - liveStock.getCurrentPrice();

    liveStock.setState(new SteadyState());

    return change;
  }

  @Override public String getStateName()
  {
    return "Reset";
  }
}