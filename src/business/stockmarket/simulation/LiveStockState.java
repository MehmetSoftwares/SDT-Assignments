package business.stockmarket.simulation;

public interface LiveStockState
{
  double calculatePriceChange(LiveStock liveStock);
  String getStateName();
}