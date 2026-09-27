package flames;

import javafx.geometry.Insets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import javax.imageio.ImageIO;

/** The verdict moment: who, what it means, and how to play again. */
public final class ResultView extends VBox {

    private final Label word;

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

        HBox stepper = new HBox(8);
        stepper.getStyleClass().add("stepper");
        stepper.setAlignment(Pos.CENTER);
        for (String stage : new String[]{"Names", "Cross out", "Count", "Reveal"}) {
            Label step = new Label(stage);
            step.getStyleClass().add("step-done");
            stepper.getChildren().add(step);
        }

        Label caption = new Label(captionFor(outcome));
        caption.getStyleClass().add("subtitle");
        caption.setWrapText(true);
        caption.setAlignment(Pos.CENTER);
        caption.setTextAlignment(TextAlignment.CENTER);

        Label word = new Label(outcome.category().title());
        word.getStyleClass().add("word");
        this.word = word;

        Label meaning = new Label(outcome.category().meaning());
        meaning.getStyleClass().add("meaning");
        meaning.setWrapText(true);
        meaning.setAlignment(Pos.CENTER);
        meaning.setTextAlignment(TextAlignment.CENTER);

        int bond = FlamesEngine.bondPercent(
                outcome.displayName1(), outcome.displayName2());
        Label bondLabel = new Label("Bond " + bond + "%");
        bondLabel.getStyleClass().add("subtitle");
        ProgressBar bondBar = new ProgressBar(bond / 100.0);
        bondBar.getStyleClass().add("bond-bar");
        bondBar.setMaxWidth(280);
        bondBar.setTooltip(new Tooltip("Share of letters that paired up"));

        Label recapTitle = new Label("How it happened");
        recapTitle.getStyleClass().add("subtitle");
        VBox recap = new VBox(2);
        recap.setAlignment(Pos.CENTER);
        int round = 1;
        for (FlamesEngine.Round step : FlamesEngine.eliminationRounds(outcome.remainingCount())) {
            Label row = new Label(round++ + " · " + spaced(step.inPlay())
                    + " → out: " + step.removed() + " → left: " + spaced(step.remaining()));
            row.getStyleClass().add("recap");
            recap.getChildren().add(row);
        }

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

        Button save = new Button("Save card");
        save.getStyleClass().add("btn-ghost");
        save.setTooltip(new Tooltip("Save this verdict as a PNG image"));
        save.setOnAction(e -> saveCard(save, outcome));

        Button copy = new Button("Copy result");
        copy.getStyleClass().add("btn-ghost");
        copy.setTooltip(new Tooltip("Copy the verdict as text"));
        copy.setOnAction(e -> copyResult(copy, outcome));

        HBox share = new HBox(12, save, copy);
        share.setAlignment(Pos.CENTER);

        getChildren().addAll(stepper, names, caption, word, meaning,
                bondLabel, bondBar, recapTitle, recap, actions, share);
    }

    /** Plain-text verdict for clipboard and tests. */
    static String shareText(FlamesOutcome outcome) {
        return outcome.displayName1() + " \u2665 " + outcome.displayName2()
                + " \u2192 " + outcome.category().title()
                + " (F.L.A.M.E.S)";
    }

    private void copyResult(Button button, FlamesOutcome outcome) {
        try {
            ClipboardContent content = new ClipboardContent();
            content.putString(shareText(outcome));
            Clipboard.getSystemClipboard().setContent(content);
            flash(button, "Copied!");
        } catch (RuntimeException failed) {
            flash(button, "Copy failed");
        }
    }

    private void saveCard(Button button, FlamesOutcome outcome) {
        if (getScene() == null) {
            flash(button, "Save failed");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save verdict card");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG image", "*.png"));
        chooser.setInitialFileName(cardFileName(outcome));
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) {
            return;
        }
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".png")) {
            file = new File(file.getParent(), file.getName() + ".png");
        }
        try {
            VBox card = buildCard(outcome);
            card.applyCss();
            card.layout();
            StackPane layer = new StackPane(card);
            layer.setMouseTransparent(true);
            layer.setPickOnBounds(false);
            layer.setTranslateX(-10000);
            Node root = getScene().getRoot();
            if (!(root instanceof StackPane)) {
                flash(button, "Save failed");
                return;
            }
            ((StackPane) root).getChildren().add(layer);
            WritableImage image = card.snapshot(new SnapshotParameters(), null);
            ((StackPane) root).getChildren().remove(layer);
            if (image == null) {
                flash(button, "Save failed");
                return;
            }
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            flash(button, "Saved!");
        } catch (RuntimeException | IOException failed) {
            flash(button, "Save failed");
        }
    }

    /** The shareable card; also used by tests for construction. */
    static VBox buildCard(FlamesOutcome outcome) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(440);
        try (InputStream icon =
                ResultView.class.getResourceAsStream("/assets/icon/icon-128.png")) {
            if (icon != null) {
                ImageView logo = new ImageView(new Image(icon));
                logo.setFitHeight(64);
                logo.setPreserveRatio(true);
                card.getChildren().add(logo);
            }
        } catch (IOException ignored) {
            // Card works fine without the logo.
        }
        Label brand = new Label("F.L.A.M.E.S");
        brand.getStyleClass().add("card-title");
        Label names = new Label(outcome.displayName1() + "  \u2665  " + outcome.displayName2());
        names.getStyleClass().add("names");
        names.setWrapText(true);
        names.setAlignment(Pos.CENTER);
        names.setTextAlignment(TextAlignment.CENTER);
        Label word = new Label(outcome.category().title());
        word.getStyleClass().add("word");
        Label meaning = new Label(outcome.category().meaning());
        meaning.getStyleClass().add("meaning");
        meaning.setWrapText(true);
        meaning.setAlignment(Pos.CENTER);
        meaning.setTextAlignment(TextAlignment.CENTER);
        Label foot = new Label("Bond " + FlamesEngine.bondPercent(
                outcome.displayName1(), outcome.displayName2()) + "% · just for fun");
        foot.getStyleClass().add("hint");
        card.getChildren().addAll(brand, names, word, meaning, foot);
        return card;
    }

    private static String cardFileName(FlamesOutcome outcome) {
        return "flames-" + slug(outcome.displayName1())
                + "-" + slug(outcome.displayName2()) + ".png";
    }

    private static String slug(String name) {
        String flat = FlamesEngine.normalize(name);
        int codepoints = flat.codePointCount(0, flat.length());
        String slug = codepoints > 20
                ? flat.substring(0, flat.offsetByCodePoints(0, 20))
                : flat;
        return slug.isEmpty() ? "names" : slug;
    }

    private static void flash(Button button, String text) {
        String original = button.getText();
        button.setText(text);
        PauseTransition pause = new PauseTransition(Duration.millis(1400));
        pause.setOnFinished(e -> button.setText(original));
        pause.play();
    }

    /** Drops the verdict word in once the view is shown. */
    public void reveal() {
        word.setOpacity(0);
        word.setTranslateY(-26);
        FadeTransition fade = new FadeTransition(Duration.millis(350), word);
        fade.setToValue(1);
        fade.play();
        TranslateTransition drop = new TranslateTransition(Duration.millis(380), word);
        drop.setToY(0);
        drop.setInterpolator(Interpolator.EASE_OUT);
        drop.play();
    }

    private static String spaced(String letters) {
        return String.join(" ", letters.split(""));
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
