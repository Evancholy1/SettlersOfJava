package settlersofjava.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import settlersofjava.player.Player;

import java.util.ArrayList;
import java.util.List;

public class GameOverPanel extends VBox {

    public GameOverPanel(Player winner, List<Player> players) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));

        setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); -fx-background-radius: 15;");
        setMaxSize(500, 450);

        Label title = new Label("GAME OVER");
        title.setFont(Font.font("System", FontWeight.BOLD, 48));
        title.setTextFill(Color.FIREBRICK);

        Label subTitle = new Label(winner.getName() + " Wins!");
        subTitle.setFont(Font.font("System", FontWeight.BOLD, 32));
        subTitle.setTextFill(BoardView.playerColor(winner.getColor()));

        VBox leaderboard = new VBox(10);
        leaderboard.setAlignment(Pos.CENTER);

        List<Player> sortedPlayers = new ArrayList<>(players);
        sortedPlayers.sort((p1, p2) -> Integer.compare(p2.getTotalVictoryPoints(), p1.getTotalVictoryPoints()));

        for (int i = 0; i < sortedPlayers.size(); i++) {
            Player p = sortedPlayers.get(i);
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMaxWidth(350);
            row.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 10; -fx-background-radius: 5;");

            Label rank = new Label("#" + (i + 1));
            rank.setFont(Font.font("System", FontWeight.BOLD, 18));
            rank.setPrefWidth(30);

            Circle icon = new Circle(15, BoardView.playerColor(p.getColor()));
            icon.setStroke(Color.BLACK);

            Label name = new Label(p.getName());
            name.setFont(Font.font("System", 18));
            name.setPrefWidth(120);

            Label score = new Label(p.getTotalVictoryPoints() + " VPs");
            score.setFont(Font.font("System", FontWeight.BOLD, 18));

            score.setMinWidth(80);

            row.getChildren().addAll(rank, icon, name, score);
            leaderboard.getChildren().add(row);
        }

        getChildren().addAll(title, subTitle, leaderboard);
    }
}