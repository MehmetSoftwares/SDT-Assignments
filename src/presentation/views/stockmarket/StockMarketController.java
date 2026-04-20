package presentation.views.stockmarket;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import presentation.core.ViewManager;

import java.net.URL;
import java.util.ResourceBundle;

public class StockMarketController implements Initializable {

  @FXML private ListView<String> availableStocksList;
  @FXML private TextField sharesInput;
  @FXML private Label priceLabel;
  @FXML private Label stateLabel;
  @FXML private Label statusLabel;
  @FXML private Label balanceLabel;
  @FXML private LineChart<String, Number> priceChart;
  @FXML private Button buyButton;
  @FXML private Button portfolioButton;

  private final StockMarketViewModel viewModel;
  private final ViewManager viewManager;

  public StockMarketController(StockMarketViewModel viewModel, ViewManager viewManager) {
    this.viewModel = viewModel;
    this.viewManager = viewManager;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    availableStocksList.setItems(viewModel.getAvailableStocks());

    priceLabel.textProperty().bind(viewModel.getCurrentPrice());
    stateLabel.textProperty().bind(viewModel.getCurrentState());
    statusLabel.textProperty().bind(viewModel.getStatusMessage());
    balanceLabel.textProperty().bind(viewModel.getBalance());
    sharesInput.textProperty().bindBidirectional(viewModel.getNumberOfShares());

    availableStocksList.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldVal, newVal) -> {
          if (newVal != null) viewModel.selectStock(newVal);
        }
    );

    buyButton.setOnAction(e -> viewModel.buyShares());
    portfolioButton.setOnAction(e -> viewManager.showView("/fxml/portfolio.fxml"));

    priceChart.getData().add(viewModel.getPriceChartData());

    viewModel.loadStocks();
  }
}