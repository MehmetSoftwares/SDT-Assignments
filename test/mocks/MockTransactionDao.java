package mocks;

import domain.Transaction;
import persistence.interfaces.TransactionDao;

import java.util.ArrayList;
import java.util.List;

public class MockTransactionDao implements TransactionDao {

    private final List<Transaction> transactions;
    private int nextId = 1;

    public MockTransactionDao(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override public void create(Transaction transaction) {
        Transaction withId = new Transaction(
            nextId++, transaction.getPortfolioId(),
            transaction.getStockSymbol(), transaction.getType(),
            transaction.getQuantity(), transaction.getPricePerShare(),
            transaction.getTotalAmount(), transaction.getFee(),
            transaction.getTimestamp());
        transactions.add(withId);
    }

    @Override public List<Transaction> getByPortfolioId(int portfolioId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getPortfolioId() == portfolioId) result.add(t);
        }
        return result;
    }

    @Override public List<Transaction> getAll() {
        return transactions;
    }
}