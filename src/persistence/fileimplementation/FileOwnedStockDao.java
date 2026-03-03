package persistence.fileimplementation;

import domain.OwnedStock;
import persistence.interfaces.OwnedStockDao;
import shared.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class FileOwnedStockDao implements OwnedStockDao
{

  private FileUnitOfWork uow;
  private int nextId = 1;
  private Logger logger = Logger.getInstance();

  public FileOwnedStockDao(FileUnitOfWork uow)
  {
    this.uow = uow;
    findNextId();
  }

  private void findNextId()
  {
    List<OwnedStock> ownedStocks = uow.getOwnedStocks();
    if (!ownedStocks.isEmpty())
    {
      OwnedStock lastStock = ownedStocks.get(ownedStocks.size() - 1);
      nextId = lastStock.getId() + 1;
    }
  }

  @Override public void create(OwnedStock ownedStock)
  {
    ownedStock.setId(nextId++);
    uow.getOwnedStocks().add(ownedStock);
  }

  @Override public OwnedStock getById(int id)
  {
    for (OwnedStock os : uow.getOwnedStocks())
    {
      if (os.getId() == id)
      {
        return os;
      }
    }
    logger.log("WARNING", "OwnedStock with ID " + id + " not found.");
    return null;
  }

  @Override public List<OwnedStock> getAll()
  {
    return uow.getOwnedStocks();
  }

  @Override public void update(OwnedStock ownedStock)
  {
    List<OwnedStock> ownedStocks = uow.getOwnedStocks();
    for (int i = 0; i < ownedStocks.size(); i++)
    {
      if (ownedStocks.get(i).getId() == ownedStock.getId())
      {
        ownedStocks.set(i, ownedStock);
        return;
      }
    }

    String errorMessage =
        "Cannot update. OwnedStock with ID " + ownedStock.getId()
            + " not found.";
    logger.log("WARNING", errorMessage);
    throw new IllegalArgumentException(errorMessage);
  }

  @Override public void delete(int id)
  {
    List<OwnedStock> ownedStocks = uow.getOwnedStocks();
    for (int i = 0; i < ownedStocks.size(); i++)
    {
      if (ownedStocks.get(i).getId() == id)
      {
        ownedStocks.remove(i);
        return;
      }
    }

    String errorMessage =
        "Cannot delete. OwnedStock with ID " + id + " not found.";
    logger.log("WARNING", errorMessage);
    throw new IllegalArgumentException(errorMessage);
  }

  @Override public List<OwnedStock> getByPortfolioId(int portfolioId)
  {
    List<OwnedStock> result = new ArrayList<>();
    for (OwnedStock os : uow.getOwnedStocks())
    {
      if (os.getPortfolioId() == portfolioId)
      {
        result.add(os);
      }
    }
    return result;
  }
}