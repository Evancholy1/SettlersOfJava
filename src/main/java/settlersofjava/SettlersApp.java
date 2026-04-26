package settlersofjava;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import settlersofjava.board.BoardBuilder;
import settlersofjava.board.BoardState;
import settlersofjava.ui.BoardView;
import settlersofjava.ui.GameController;
import settlersofjava.ui.PlayerDashboard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import settlersofjava.engine.CatanGame;
import settlersofjava.ui.DiceView;
import settlersofjava.ui.GameLogPanel;
import settlersofjava.ui.PlayerTradeOfferPanel;
import settlersofjava.ui.PlayerTradeResponsePanel;
import settlersofjava.ui.PlayerTradeSelectPanel;
import settlersofjava.ui.TradingPanel;

import java.util.List;

/**
 * Entry point for Settlers of Java.
 * Bootstraps the JavaFX application and wires the game engine.
 */
public class SettlersApp extends Application {

    private static final int windowSizeX = 1280;
    private static final int windowSizeY = 720;

    @Override
    public void start(Stage primaryStage) {
        // TODO: load main.fxml, inject BoardState + PlayerList into GameController
        Scene setupScene = createSetupScene(primaryStage);

        // Show Setup Screen
        primaryStage.setTitle("Settlers of Java");
        primaryStage.setScene(setupScene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private Scene createSetupScene(Stage primaryStage) {
        // Setup Screen UI
        VBox setupLayout = new VBox(15);
        setupLayout.setAlignment(Pos.CENTER);
        setupLayout.setPadding(new Insets(50));

        Label title = new Label("Welcome to Settlers Of Java");
        title.setFont(new Font("System Bold", 24));
        TextField p1Input = new TextField("Player 1");
        TextField p2Input = new TextField("Player 2");
        TextField p3Input = new TextField("Player 3");
        TextField p4Input = new TextField("Player 4");
        p1Input.setMaxWidth(200);
        p2Input.setMaxWidth(200);
        p3Input.setMaxWidth(200);
        p4Input.setMaxWidth(200);
        Button startButton = new Button("Start Game");
        startButton.setFont(new Font(16));
        setupLayout.getChildren().addAll(
                title,
                new Label("Enter Player Names:"),
                p1Input, p2Input, p3Input, p4Input,
                startButton
        );
        Scene setupScene = new Scene(setupLayout, windowSizeX, windowSizeY);

        // Start Button Logic
        startButton.setOnAction(e -> {
            List<String> names = List.of(
                    p1Input.getText(),
                    p2Input.getText(),
                    p3Input.getText(),
                    p4Input.getText()
            );

            CatanGame game = CatanGame.getInstance(names);

            BoardState boardState = new BoardBuilder()
                    .withShuffledTilesAndNumberTokens()
                    .withVerticesAndEdges()
                    .withPorts()
                    .build();
            game.setBoardState(boardState);

            BoardView boardView = new BoardView(boardState);

            Label statusLabel = new Label();
            statusLabel.setFont(new Font("System Bold", 16));

            Button rollButton           = new Button("Roll Dice");
            Button endTurnButton        = new Button("End Turn");
            Button buildRoadButton      = new Button("Build Road");
            Button buildSettlementButton = new Button("Build Settlement");
            Button buildCityButton      = new Button("Upgrade to City");
            rollButton.setFont(new Font(14));
            endTurnButton.setFont(new Font(14));
            buildRoadButton.setFont(new Font(14));
            buildSettlementButton.setFont(new Font(14));
            buildCityButton.setFont(new Font(14));

            HBox topBar = new HBox(20, statusLabel, rollButton, endTurnButton);
            topBar.setAlignment(Pos.CENTER_LEFT);
            topBar.setPadding(new Insets(8, 20, 4, 20));

            Button testButton = new Button("TEST");
            testButton.setFont(new Font(14));
            testButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold;");

            Button tradeButton = new Button("Trade w/ Players");
            tradeButton.setFont(new Font(14));
            tradeButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold;");

            HBox buildBar = new HBox(12, buildRoadButton, buildSettlementButton, buildCityButton, tradeButton, testButton);
            buildBar.setAlignment(Pos.CENTER_LEFT);
            buildBar.setPadding(new Insets(4, 20, 8, 20));

            VBox topArea = new VBox(topBar, buildBar);

            PlayerDashboard dashboard = new PlayerDashboard();
            TradingPanel tradingPanel = new TradingPanel();
            PlayerTradeOfferPanel tradeOfferPanel       = new PlayerTradeOfferPanel();
            PlayerTradeResponsePanel tradeResponsePanel = new PlayerTradeResponsePanel();
            PlayerTradeSelectPanel tradeSelectPanel     = new PlayerTradeSelectPanel();

            DiceView diceView = new DiceView();
            diceView.setMouseTransparent(true); // dice are display-only; don't block board clicks
            StackPane boardStack = new StackPane(boardView, diceView);
            StackPane.setAlignment(diceView, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(diceView, new Insets(0, 20, 20, 0));

            BorderPane gameLayout = new BorderPane();
            gameLayout.setStyle("-fx-background-color: #1565C0;");
            gameLayout.setTop(topArea);
            gameLayout.setCenter(boardStack);
            gameLayout.setBottom(new VBox(tradingPanel, tradeOfferPanel, tradeResponsePanel, tradeSelectPanel, dashboard));
            gameLayout.setRight(new GameLogPanel());

            GameController controller = new GameController(
                    boardState,
                    game.getPlayerList(),
                    game.getTurnManager(),
                    statusLabel::setText
            );
            controller.setBoardView(boardView);
            controller.setPlayerDashboard(dashboard);
            controller.setTradingPanel(tradingPanel);
            controller.setPlayerTradePanels(tradeOfferPanel, tradeResponsePanel, tradeSelectPanel);

            testButton.setOnAction(ev -> {
                settlersofjava.player.Player current = game.getTurnManager().getCurrentPlayer();
                for (settlersofjava.resources.ResourceType t : settlersofjava.resources.ResourceType.values())
                    current.addResource(t, 10);
            });

            tradeButton.setOnAction(ev           -> controller.openPlayerTrade());
            rollButton.setOnAction(ev            -> controller.rollDice());
            endTurnButton.setOnAction(ev        -> controller.endTurn());
            buildRoadButton.setOnAction(ev      -> controller.toggleBuildRoad());
            buildSettlementButton.setOnAction(ev -> controller.toggleBuildSettlement());
            buildCityButton.setOnAction(ev      -> controller.toggleBuildCity());

            controller.setActionButtons(rollButton, endTurnButton);
            controller.setBuildButtons(buildRoadButton, buildSettlementButton, buildCityButton);

            controller.startSetup();

            Scene gameScene = new Scene(gameLayout, windowSizeX, windowSizeY);
            primaryStage.setScene(gameScene);
        });

        return setupScene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

