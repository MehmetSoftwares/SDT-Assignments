package presentation.views.portfolio;

import domain.OwnedStock;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import presentation.core.ViewManager;

import java.net.URL;
import java.util.ResourceBundle;

public class PortfolioController implements Initializable {

  @FXML private TableView<OwnedStock> ownedStocksTable;
  @FXML private TableColumn<OwnedStock, String> symbolColumn;
  @FXML private TableColumn<OwnedStock, Integer> sharesColumn;
  @FXML private Label balanceLabel;
  @FXML private Label totalValueLabel;
  @FXML private TextField sellSymbolInput;
  @FXML private TextField sellQuantityInput;
  @FXML private Button sellButton;
  @FXML private Button marketButton;

  private final PortfolioViewModel viewModel;
  private final ViewManager viewManager;

  public PortfolioController(PortfolioViewModel viewModel, ViewManager viewManager) {
    this.viewModel = viewModel;
    this.viewManager = viewManager;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    ownedStocksTable.setItems(viewModel.getOwnedStocks());

    symbolColumn.setCellValueFactory(data ->
        new SimpleStringProperty(data.getValue().getStockSymbol()));
    sharesColumn.setCellValueFactory(data ->
        new SimpleObjectProperty<>(data.getValue().getNumberOfShares()));

    balanceLabel.textProperty().bind(viewModel.getBalance());
    totalValueLabel.textProperty().bind(viewModel.getTotalValue());

    sellSymbolInput.textProperty().bindBidirectional(viewModel.getSellSymbol());
    sellQuantityInput.textProperty().bindBidirectional(viewModel.getSellQuantity());

    sellButton.setOnAction(e -> viewModel.sellShares());
    marketButton.setOnAction(e -> viewManager.showView("/fxml/stockmarket.fxml"));

    viewModel.loadPortfolio();
  }
}