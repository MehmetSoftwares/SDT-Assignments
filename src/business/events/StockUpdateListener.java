package business.events;

@FunctionalInterface
public interface StockUpdateListener {
    void onStockUpdate(StockUpdateEvent event);
}