package mocks;

import domain.Portfolio;
import persistence.interfaces.PortfolioDao;

import java.util.List;

public class MockPortfolioDao implements PortfolioDao {

    private final List<Portfolio> portfolios;

    public MockPortfolioDao(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }

    @Override public void create(Portfolio portfolio) {
        portfolios.add(portfolio);
    }

    @Override public Portfolio getById(int id) {
        for (Portfolio p : portfolios) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    @Override public List<Portfolio> getAll() {
        return portfolios;
    }

    @Override public void update(Portfolio portfolio) {
        for (int i = 0; i < portfolios.size(); i++) {
            if (portfolios.get(i).getId() == portfolio.getId()) {
                portfolios.set(i, portfolio);
                return;
            }
        }
    }

    @Override public void delete(int id) {
        portfolios.removeIf(p -> p.getId() == id);
    }
}