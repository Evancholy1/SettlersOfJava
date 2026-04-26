package settlersofjava.ui;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import settlersofjava.cards.DevelopmentCard;
import settlersofjava.cards.KnightCard;
import settlersofjava.cards.VictoryPointCard;

public class DevCardView extends StackPane {
    public DevCardView(DevelopmentCard card, Runnable onClick) {
        Rectangle bg = new Rectangle(50, 70);
        bg.setArcWidth(8);
        bg.setArcHeight(8);
        bg.setStrokeWidth(2);

        if (card instanceof KnightCard && card.isPlayed()) {
            bg.setFill(Color.web("#9932CC")); // Purple for played Knights
            bg.setStroke(Color.web("#4B0082"));
        } else {
            bg.setFill(Color.web("#FFD700")); // Gold/Yellow for unplayed
            bg.setStroke(Color.web("#DAA520"));
        }

        Text label = new Text(card.getCardName());
        label.setWrappingWidth(45);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setFont(Font.font("System", 10));

        getChildren().addAll(bg, label);
        setAlignment(Pos.CENTER);

        // VPs can't be played. Locked cards can't be played. Played cards can't be clicked twice.
        if (!card.isPlayed() && !card.isLocked() && !(card instanceof VictoryPointCard)) {
            setOnMouseClicked(e -> onClick.run());
            setStyle("-fx-cursor: hand;");
        } else if (card.isLocked()) {
            setOpacity(0.6); // dim it to show it was bought this turn
        }
    }
}