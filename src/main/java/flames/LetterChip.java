package flames;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.util.Duration;

/**
 * One clickable letter tile shared by the automatic and manual crossing
 * stages. Fixed 40×46 box, centered letter, pen slash overlay.
 */
final class LetterChip {
    final StackPane box;
    final Label label;
    final Line slash;
    final int codePoint;
    boolean taken;

    LetterChip(int codePoint) {
        this.codePoint = codePoint;
        this.label = new Label(new String(Character.toChars(codePoint)));
        this.label.getStyleClass().add("chip-label");
        this.slash = new Line(0, 0, 0, 0);
        this.slash.getStyleClass().add("slash");
        this.slash.setVisible(false);
        this.slash.setManaged(false);
        this.slash.setLayoutX(5);
        this.slash.setLayoutY(5);
        this.box = new StackPane(label, slash);
        this.box.getStyleClass().add("chip");
        this.box.setMinSize(40, 46);
        this.box.setMaxSize(40, 46);
    }

    /** Draws the pen slash across the tile; returns the animation. */
    static Timeline drawSlash(Line slash, double endX, double endY) {
        slash.setVisible(true);
        Timeline draw = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(slash.endXProperty(), 0),
                        new KeyValue(slash.endYProperty(), 0)),
                new KeyFrame(Duration.millis(240),
                        new KeyValue(slash.endXProperty(), endX),
                        new KeyValue(slash.endYProperty(), endY)));
        draw.play();
        return draw;
    }
}
