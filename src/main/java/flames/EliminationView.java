package flames;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Replays the engine's recorded elimination order tile by tile, then hands
 * off to the result view. Click anywhere to skip. Status text narrates every
 * step so the sequence never depends on color alone.
 */
public final class EliminationView extends VBox {

    private static final double STEP_MS = 500;

    private final FlamesOutcome outcome;
    private final Runnable onDone;
    private final Map<Character, Label> tiles = new HashMap<>();
    private final Label status = new Label();
    private Timeline timeline;
    private boolean finished;

    public EliminationView(FlamesOutcome outcome, Runnable onDone) {
        super(16);
        this.outcome = outcome;
        this.onDone = onDone;
        for (char removed : outcome.eliminationOrder()) {
            if ("FLAMES".indexOf(removed) < 0) {
                throw new IllegalArgumentException("Not a FLAMES letter: " + removed);
            }
        }
        setAlignment(Pos.CENTER);
        setPadding(new Insets(32));

        Label heading = new Label("Counting the letters\u2026");
        heading.getStyleClass().add("names");

        Label names = new Label(countLine(outcome));
        names.getStyleClass().add("subtitle");

        HBox row = new HBox(10);
        row.getStyleClass().add("tiles");
        for (char letter : new char[]{'F', 'L', 'A', 'M', 'E', 'S'}) {
            Label tile = new Label(String.valueOf(letter));
            tile.getStyleClass().add("tile");
            tile.setTooltip(new Tooltip(FlamesCategory.fromLetter(letter).title()));
            tile.setAccessibleText(letter + ", " + FlamesCategory.fromLetter(letter).title()
                    + ", still standing.");
            tiles.put(letter, tile);
            row.getChildren().add(tile);
        }

        status.getStyleClass().add("status");
        status.setText("Steady\u2026");

        Label hint = new Label("click or press Enter to skip");
        hint.getStyleClass().add("hint");

        setFocusTraversable(true);
        setOnMouseClicked(e -> finish());
        setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER, SPACE -> finish();
                default -> {
                }
            }
        });
        getChildren().addAll(heading, names, row, status, hint);
    }

    /** Starts the elimination sequence. Safe to call once the view is shown. */
    public void play() {
        if (finished) {
            return;
        }
        Platform.runLater(this::requestFocus);
        timeline = new Timeline();
        var order = outcome.eliminationOrder();
        for (int i = 0; i < order.size(); i++) {
            final int step = i;
            timeline.getKeyFrames().add(new KeyFrame(
                    Duration.millis(STEP_MS * (step + 1)),
                    e -> strike(order.get(step), order.size() - step)));
        }
        timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(STEP_MS * order.size() + 700),
                e -> crown()));
        timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(STEP_MS * order.size() + 1600),
                e -> finish()));
        timeline.play();
    }

    private void strike(char letter, int left) {
        Label tile = tiles.get(letter);
        addStyle(tile, "tile-out");
        tiles.get(letter).setAccessibleText(letter + ", out.");
        status.setText(left == 1
                ? letter + " falls \u2014 one survives."
                : letter + " falls \u2014 " + left + " remain.");
    }

    private void crown() {
        char winner = outcome.category().letter();
        Label tile = tiles.get(winner);
        addStyle(tile, "tile-winner");
        tiles.get(winner).setAccessibleText(
                winner + ", " + outcome.category().title() + ", the verdict.");
        status.setText(winner + " stands alone.");
    }

    private void finish() {
        if (finished) {
            return;
        }
        finished = true;
        if (timeline != null) {
            timeline.stop();
        }
        var order = outcome.eliminationOrder();
        for (int i = 0; i < order.size(); i++) {
            strike(order.get(i), order.size() - i);
        }
        crown();
        onDone.run();
    }

    private static void addStyle(Label tile, String style) {
        if (!tile.getStyleClass().contains(style)) {
            tile.getStyleClass().add(style);
        }
    }

    private static String countLine(FlamesOutcome outcome) {
        if (outcome.remainingCount() == 0) {
            return "Every letter cancelled out.";
        }
        return outcome.remainingCount() + (outcome.remainingCount() == 1
                ? " letter left to count by."
                : " letters left to count by.");
    }
}
