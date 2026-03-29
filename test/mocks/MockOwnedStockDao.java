package mocks;

import domain.OwnedStock;
import persistence.interfaces.OwnedStockDao;

import java.util.ArrayList;
import java.util.List;

public class MockOwnedStockDao implements OwnedStockDao {

    private final List<OwnedStock> ownedStocks;
    private int nextId = 1;
    private int createCount = 0;
    private int updateCount = 0;

    public MockOwnedStockDao(List<OwnedStock> ownedStocks) {
        this.ownedStocks = ownedStocks;
    }

    @Override public void create(OwnedStock ownedStock) {
        ownedStock.setId(nextId++);
        ownedStocks.add(ownedStock);
        createCount++;
    }

    @Override public OwnedStock getById(int id) {
        for (OwnedStock os : ownedStocks) {
            if (os.getId() == id) return os;
        }
        return null;
    }

    @Override public List<OwnedStock> getByPortfolioId(int portfolioId) {
        List<OwnedStock> result = new ArrayList<>();
        for (OwnedStock os : ownedStocks) {
            if (os.getPortfolioId() == portfolioId) result.add(os);
        }
        return result;
    }

    @Override public OwnedStock getByStockSymbol(String stockSymbol) {
        for (OwnedStock os : ownedStocks) {
            if (os.getStockSymbol().equals(stockSymbol)) return os;
        }
        return null;
    }

    @Override public List<OwnedStock> getAll() {
        return ownedStocks;
    }

    @Override public void update(OwnedStock ownedStock) {
        for (int i = 0; i < ownedStocks.size(); i++) {
            if (ownedStocks.get(i).getId() == ownedStock.getId()) {
                ownedStocks.set(i, ownedStock);
                updateCount++;
                return;
            }
        }
    }

    @Override public void delete(int id) {
        ownedStocks.removeIf(os -> os.getId() == id);
    }

    public int getCreateCount() { return createCount; }
    public int getUpdateCount() { return updateCount; }
}