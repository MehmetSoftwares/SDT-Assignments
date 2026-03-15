package business.events;

public record StockUpdateEvent(
    String symbol,
    double currentPrice,
    String stateName
) {
    public boolean isBankrupt() {
        return "Bankrupt".equals(stateName);
    }
}