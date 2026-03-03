package persistence.interfaces;
import domain.OwnedStock;
import java.util.List;

public interface OwnedStockDao {
  void create(OwnedStock ownedStock);
  OwnedStock getById(int id);
  List<OwnedStock> getByPortfolioId(int portfolioId);
  List<OwnedStock> getAll();
  void update(OwnedStock ownedStock);
  void delete(int id);

}