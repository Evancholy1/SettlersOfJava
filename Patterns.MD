# Design Patterns — Settlers of Java

## 1. Factory (`TileFactory`)

The `TileFactory` class is responsible for creating all `HexTile` instances on the board.
Rather than calling `new ResourceTile(...)` or `new DesertTile(...)` directly throughout the
codebase, all tile creation is centralized in `TileFactory.createResourceTile()` and
`TileFactory.createDesertTile()`. This ensures the correct subclass is always returned based
on the terrain type, and makes it easy to extend the tile hierarchy (e.g. sea tiles, port tiles)
without changing any calling code.

## 2. Singleton (`CatanGame` and `EventBus`)

Both `CatanGame` and `EventBus` are implemented as Singletons — only one instance of each
exists for the entire application lifetime. `CatanGame.getInstance()` provides a single
global access point to the game state (board, players, current phase), preventing multiple
conflicting game states from being created. `EventBus.getInstance()` ensures all publishers
and listeners are connected to the same event channel, so no game event is ever missed by a
subscriber.

## 3. Observer (`EventBus` + JavaFX Properties)

The Observer pattern is used in two layers. At the game level, `EventBus` maintains a list of
`GameEventListener` subscribers (such as `GameController`) and notifies them whenever a
`GameEvent` is published. For example, when dice are rolled or a building is placed. At the
UI level, `Player` exposes its resource counts and victory points as JavaFX `ObservableMap`
and `IntegerProperty` fields. The `ResourcePanelView` binds its labels directly to these
properties, so the UI updates automatically whenever a player's state changes — no manual
refresh calls required.

## 4. Strategy (`TradeStrategy`)

The `TradeStrategy` interface defines a single method: `getRate(ResourceType)` — that returns
how many resources a player must trade in for one of another type. `BankTradeStrategy` always
returns 4 (the default 4:1 bank trade), while `PortTradeStrategy` returns 3 or 2 depending on
the port type the player is adjacent to. By coding to the `TradeStrategy` interface rather than
hardcoding trade rates, the game can swap in the correct strategy per player per turn without
any conditional logic in the core game engine.

## 5. Dependency Injection (`GameController`)

`GameController` does not create or look up `BoardState` or `PlayerList` itself — they are
injected via its constructor at application startup. This decouples the UI controller from the
game engine: `GameController` only knows it has a board and a list of players, not how they
were built. This makes the controller independently testable (you can inject a minimal
`BoardState` in a unit test without spinning up a full game) and keeps construction logic
centralized in `SettlersApp`.