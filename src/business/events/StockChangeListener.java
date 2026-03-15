package business.events;

@FunctionalInterface
public interface StockChangeListener {
  void onStockChange(StockUpdateEvent event);
}