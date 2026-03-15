# Stock Trading Game 📈
Dette repository indeholder et "Stock Trading Game" udviklet som semesterprojekt i faget **Software Design & Test**.

## 🚀 Projektstatus
**Nuværende status:** Assignment 5 (Observer Pattern).

### Færdige assignments
- **Assignment 1** — Setup & Domain Modeling
- **Assignment 3** — Persistence Layer (Unit of Work pattern, fil-baserede DAOs)
- **Assignment 4** — State Pattern (LiveStock states, MarketUpdateThread)
- **Assignment 5** — Observer Pattern (StockUpdateEvent, StockListenerService, StockBankruptService)

## 🛠 Teknologier & Arkitektur
Projektet er bygget i **Java** med lagdelt arkitektur (domain, business, persistence, shared).

## 📂 Mappestruktur
- `src/domain/` — Domæne-klasser (Stock, Portfolio, OwnedStock, Transaction, StockPriceHistory)
- `src/business/stockmarket/` — StockMarket (Singleton), simulation (State Pattern)
- `src/business/events/` — Observer events og listener-interfaces
- `src/business/services/` — Service-klasser (StockListenerService, StockBankruptService)
- `src/persistence/` — DAO-interfaces og fil-implementeringer (Unit of Work)
- `src/shared/` — Logger (Singleton), AppConfig (Singleton)
- `documentation/` — Klassediagrammer (draw.io)

---
*Udviklet af: [Mehmet]*
