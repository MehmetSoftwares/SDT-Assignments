package persistence.fileimplementation;

import domain.Transaction;
import persistence.interfaces.TransactionDao;
import shared.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class TransactionFileDAO implements TransactionDao
{

  private FileUnitOfWork uow;
  private int nextId = 1;
  private Logger logger = Logger.getInstance();

  public TransactionFileDAO(FileUnitOfWork uow)
  {
    this.uow = uow;
    findNextId();
  }

  private void findNextId()
  {
    List<Transaction> transactions = uow.getTransactions();
    if (!transactions.isEmpty())
    {
      Transaction last = transactions.get(transactions.size() - 1);
      nextId = last.getId() + 1;
    }
  }

  @Override public void create(Transaction transaction)
  {
    Transaction withId = new Transaction(
        nextId++, transaction.getPortfolioId(), transaction.getStockSymbol(),
        transaction.getType(), transaction.getQuantity(),
        transaction.getPricePerShare(), transaction.getTotalAmount(),
        transaction.getFee(), transaction.getTimestamp());
    uow.getTransactions().add(withId);
  }

  @Override public List<Transaction> getByPortfolioId(int portfolioId)
  {
    List<Transaction> result = new ArrayList<>();
    for (Transaction t : uow.getTransactions())
    {
      if (t.getPortfolioId() == portfolioId)
      {
        result.add(t);
      }
    }
    return result;
  }

  @Override public List<Transaction> getAll()
  {
    return uow.getTransactions();
  }
}