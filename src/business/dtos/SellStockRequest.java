package business.dtos;

public record SellStockRequest(
    int portfolioId,
    String stockSymbol,
    int numberOfShares
) {}