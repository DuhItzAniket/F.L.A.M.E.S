package flames;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

/** The verdict moment: who, what it means, and how to play again. */
public final class ResultView extends VBox {

    public ResultView(FlamesOutcome outcome, Runnable onChangeNames, Runnable onNewNames) {
        super(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(32));
        setMaxWidth(520);

        Label names = new Label(outcome.displayName1() + "  \u2665  " + outcome.displayName2());
        names.getStyleClass().add("names");
        names.setWrapText(true);
        names.setAlignment(Pos.CENTER);
        names.setTextAlignment(TextAlignment.CENTER);

        Label caption = new Label(captionFor(outcome));
        caption.getStyleClass().add("subtitle");
        caption.setWrapText(true);
        caption.setAlignment(Pos.CENTER);
        caption.setTextAlignment(TextAlignment.CENTER);

        Label word = new Label(outcome.category().title());
        word.getStyleClass().add("word");

        Label meaning = new Label(outcome.category().meaning());
        meaning.getStyleClass().add("meaning");
        meaning.setWrapText(true);
        meaning.setAlignment(Pos.CENTER);
        meaning.setTextAlignment(TextAlignment.CENTER);

        Button change = new Button("Change names");
        change.getStyleClass().add("btn-primary");
        change.setDefaultButton(true);
        change.setTooltip(new Tooltip("Back to the names, keeping what you typed (Enter)"));
        change.setOnAction(e -> onChangeNames.run());
        Platform.runLater(change::requestFocus);

        Button again = new Button("Start over");
        again.getStyleClass().add("btn-ghost");
        again.setTooltip(new Tooltip("Clear everything and play again"));
        again.setOnAction(e -> onNewNames.run());

        HBox actions = new HBox(12, change, again);
        actions.setAlignment(Pos.CENTER);

        getChildren().addAll(names, caption, word, meaning, actions);
    }

    private static String captionFor(FlamesOutcome outcome) {
        if (outcome.remainingCount() == 0) {
            return "Every letter cancelled out — a perfect match.";
        }
        return outcome.remainingCount() + (outcome.remainingCount() == 1
                ? " letter left standing."
                : " letters left standing.");
    }
}
