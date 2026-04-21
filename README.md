# Stock Trading Game 📈
Dette repository indeholder et "Stock Trading Game" udviklet som semesterprojekt i faget **Software Design & Test**.

## 🚀 Projektstatus
**Nuværende status:** Assignment 8 (MVVM Presentation Layer).

### Færdige assignments
- **Assignment 1** — Setup & Domain Modeling
- **Assignment 3** — Persistence Layer (Unit of Work pattern, fil-baserede DAOs)
- **Assignment 4** — State Pattern (LiveStock states, MarketUpdateThread)
- **Assignment 5** — Observer Pattern (StockUpdateEvent, StockListenerService, StockBankruptService)
- **Assignment 6** — Transaction Script Pattern (StockTradingService, PortfolioQueryService)
- **Assignment 7** — Unit Testing (BuyStockTests, SellStockTests, mock-infrastruktur)
- **Assignment 8** — MVVM Presentation Layer (JavaFX GUI, ViewManager, ControllerFactory, ApplicationContext)

## 🛠 Teknologier & Arkitektur
Projektet er bygget i **Java** med lagdelt arkitektur (domain, business, persistence, presentation, shared) og **JavaFX** til GUI.

## 📂 Mappestruktur
- `src/domain/` — Domæne-klasser (Stock, Portfolio, OwnedStock, Transaction, StockPriceHistory)
- `src/business/stockmarket/` — StockMarket (Singleton), simulation (State Pattern)
- `src/business/events/` — Observer events og listener-interfaces
- `src/business/services/` — Service-klasser (StockTradingService, PortfolioQueryService, StockListenerService, StockBankruptService)
- `src/business/dtos/` — DTOs/records (BuyStockRequest, SellStockRequest)
- `src/persistence/` — DAO-interfaces og fil-implementeringer (Unit of Work ejer DAOs)
- `src/presentation/core/` — ApplicationContext, ViewManager, ControllerFactory, NotificationService
- `src/presentation/views/` — MVVM views (StockMarket, Portfolio) med Controllers og ViewModels
- `src/shared/` — Logger (Singleton), AppConfig (Singleton)
- `test/` — Unit tests og mock-infrastruktur
- `resources/` — FXML-filer og CSS
- `documentation/` — Klassediagrammer

---
*Udviklet af: [Mehmet]*
