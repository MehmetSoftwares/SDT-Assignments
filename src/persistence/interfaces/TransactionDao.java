package persistence.interfaces;

import domain.Transaction;
import java.util.List;

public interface TransactionDao {
    void create(Transaction transaction);
    List<Transaction> getByPortfolioId(int portfolioId);
    List<Transaction> getAll();
}