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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import settlersofjava.engine.CatanGame;

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
            statusLabel.setPadding(new Insets(10, 20, 10, 20));

            PlayerDashboard dashboard = new PlayerDashboard();

            BorderPane gameLayout = new BorderPane();
            gameLayout.setTop(statusLabel);
            gameLayout.setCenter(boardView);
            gameLayout.setBottom(dashboard);

            GameController controller = new GameController(
                    boardState,
                    game.getPlayerList(),
                    game.getTurnManager(),
                    statusLabel::setText
            );
            controller.setBoardView(boardView);
            controller.setPlayerDashboard(dashboard);
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

