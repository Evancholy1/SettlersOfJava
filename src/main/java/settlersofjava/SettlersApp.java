package settlersofjava;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import settlersofjava.board.BoardBuilder;
import settlersofjava.board.BoardState;
import settlersofjava.ui.BoardView;

/**
 * Entry point for Settlers of Java.
 * Bootstraps the JavaFX application and wires the game engine.
 */
public class SettlersApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // TODO: load main.fxml, inject BoardState + PlayerList into GameController
        BoardState boardState = new BoardBuilder()
                .withShuffledTiles()
                .withVerticesAndEdges()
                .build();
        BoardView boardView = new BoardView(boardState);
        Scene scene = new Scene(boardView, 800, 600);
        primaryStage.setTitle("Settlers of Java");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

