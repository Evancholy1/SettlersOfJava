package settlersofjava.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import settlersofjava.events.EventBus;
import settlersofjava.events.GameEvent;
import settlersofjava.events.GameEventListener;

/**
 * Shows two die faces that update automatically on DICE_ROLLED events.
 * Listens to EventBus directly so GameController doesn't need to know about it.
 */
public class DiceView extends HBox implements GameEventListener {

    private static final double DIE_SIZE = 72.0;

    private final Image[] faces = new Image[7]; // indices 1–6
    private final ImageView view1 = new ImageView();
    private final ImageView view2 = new ImageView();

    public DiceView() {
        setSpacing(8);

        for (int i = 1; i <= 6; i++) {
            faces[i] = new Image(
                getClass().getResourceAsStream("/settlersofjava/ui/dice/dice" + i + ".png")
            );
        }

        for (ImageView v : new ImageView[]{view1, view2}) {
            v.setFitWidth(DIE_SIZE);
            v.setFitHeight(DIE_SIZE);
            v.setPreserveRatio(true);
            v.setImage(faces[1]);
            v.setOpacity(0.3); // dimmed until first roll
        }

        getChildren().addAll(view1, view2);
        EventBus.getInstance().register(this);
    }

    @Override
    public void onEvent(GameEvent event, Object payload) {
        if (event == GameEvent.DICE_ROLLED) {
            int[] roll = (int[]) payload;
            view1.setImage(faces[roll[0]]);
            view2.setImage(faces[roll[1]]);
            view1.setOpacity(1.0);
            view2.setOpacity(1.0);
        }
    }
}
