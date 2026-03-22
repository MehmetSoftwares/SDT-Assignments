package business.dtos;

public record BuyStockRequest(
    int portfolioId,
    String stockSymbol,
    int numberOfShares
) {}