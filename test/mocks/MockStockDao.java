package mocks;

import domain.Stock;
import persistence.interfaces.StockDao;

import java.util.List;

public class MockStockDao implements StockDao {

    private final List<Stock> stocks;

    public MockStockDao(List<Stock> stocks) {
        this.stocks = stocks;
    }

    @Override public void create(Stock stock) {
        stocks.add(stock);
    }

    @Override public Stock getById(String symbol) {
        for (Stock s : stocks) {
            if (s.getSymbol().equals(symbol)) return s;
        }
        return null;
    }

    @Override public List<Stock> getAll() {
        return stocks;
    }

    @Override public void update(Stock stock) {
        for (int i = 0; i < stocks.size(); i++) {
            if (stocks.get(i).getSymbol().equals(stock.getSymbol())) {
                stocks.set(i, stock);
                return;
            }
        }
    }

    @Override public void delete(String symbol) {
        stocks.removeIf(s -> s.getSymbol().equals(symbol));
    }
}