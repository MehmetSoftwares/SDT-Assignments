package business.stockmarket;

import business.stockmarket.simulation.LiveStock;
import business.events.StockUpdateEvent;
import business.events.StockUpdateListener;
import domain.Stock;
import shared.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class StockMarket {
    private static volatile StockMarket instance;
    private final List<LiveStock> liveStocks = new ArrayList<>();
    private final List<StockUpdateListener> listeners = new ArrayList<>();
    private final Logger logger = Logger.getInstance();

    private StockMarket() {}

    public static StockMarket getInstance() {
        if (instance == null) {
            synchronized (StockMarket.class) {
                if (instance == null)
                    instance = new StockMarket();
            }
        }
        return instance;
    }

    public void addListener(StockUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StockUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(StockUpdateEvent event) {
        for (StockUpdateListener listener : listeners) {
            listener.onStockUpdate(event);
        }
    }

    public void addNewStock(String symbol) {
        LiveStock ls = new LiveStock(symbol);
        liveStocks.add(ls);
        logger.log("INFO", "New LiveStock added: " + symbol);
    }

    public void addExistingStock(Stock stock) {
        LiveStock ls = new LiveStock(stock.getSymbol(), stock.getCurrentPrice(),
            stock.getCurrentState());
        liveStocks.add(ls);
        logger.log("INFO", "Existing LiveStock added: " + stock.getSymbol());
    }

    public void updateAllStocks() {
        for (LiveStock ls : liveStocks) {
            ls.updatePrice();
            logger.log("INFO",
                ls.getSymbol() + " | " + ls.getStateName() + " | price: "
                    + ls.getCurrentPrice());

            StockUpdateEvent event = new StockUpdateEvent(
                ls.getSymbol(),
                ls.getCurrentPrice(),
                ls.getStateName()
            );
            notifyListeners(event);
        }
    }

    public List<LiveStock> getLiveStocks() {
        return liveStocks;
    }
}