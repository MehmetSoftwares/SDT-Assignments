package domain;

public class Stock {
  private final String symbol;
  private final String name;
  private double currentPrice;
  private String currentState;

  public Stock(String symbol, String name, double currentPrice, String currentState) {
    if (symbol == null || symbol.isBlank())
      throw new IllegalArgumentException("Symbol can not be empty");
    if (name == null || name.isBlank())
      throw new IllegalArgumentException("Name can not be empty");
    if (currentPrice < 0)
      throw new IllegalArgumentException("Price can not be negative");
    if (currentState == null || currentState.isBlank())
      throw new IllegalArgumentException("State can not be empty");

    this.symbol = symbol;
    this.name = name;
    this.currentPrice = currentPrice;
    this.currentState = currentState;
  }

  public String getSymbol() {
    return symbol;
  }

  public String getName() {
    return name;
  }

  public double getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(double currentPrice) {
    this.currentPrice = currentPrice;
  }

  public String getCurrentState() {
    return currentState;
  }

  public void setCurrentState(String currentState) {
    this.currentState = currentState;
  }
}