package flames;

import java.util.function.BiConsumer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** The name-entry form. Reports valid submissions, renders errors inline. */
public final class InputView extends VBox {

    private final TextField firstName = new TextField();
    private final TextField secondName = new TextField();
    private final CheckBox manual = new CheckBox("Cross the pairs myself");
    private final Label error = new Label();

    public InputView(BiConsumer<String, String> onCalculate) {
        super(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(32));
        setMaxWidth(460);

        Label title = new Label("F.L.A.M.E.S");
        title.getStyleClass().add("title");

        ImageView logo = null;
        try (java.io.InputStream icon =
                getClass().getResourceAsStream("/assets/icon/icon-128.png")) {
            if (icon != null) {
                logo = new ImageView(new Image(icon));
                logo.setFitHeight(84);
                logo.setPreserveRatio(true);
            }
        } catch (java.io.IOException ignored) {
            // Header works fine without the logo.
        }

        Label subtitle = new Label("Two names in. One verdict out.");
        subtitle.getStyleClass().add("subtitle");

        Label firstLabel = new Label("First name");
        firstLabel.setLabelFor(firstName);
        Label firstCount = counter();
        styleField(firstName, "e.g. Romeo",
                "First name — only letters count, the rest is ignored", firstCount);

        Label secondLabel = new Label("Second name");
        secondLabel.setLabelFor(secondName);
        Label secondCount = counter();
        styleField(secondName, "e.g. Juliet",
                "Second name — only letters count, the rest is ignored", secondCount);

        error.getStyleClass().add("error");
        error.setWrapText(true);
        error.setVisible(false);
        error.setManaged(false);

        manual.setTooltip(new Tooltip("Manual mode: you tap the matching pairs"));

        Button calculate = new Button("Reveal fate");
        calculate.getStyleClass().add("btn-primary");
        calculate.setDefaultButton(true);
        calculate.setTooltip(new Tooltip("Calculate the FLAMES verdict (Enter)"));
        calculate.setOnAction(e -> onCalculate.accept(firstName.getText(), secondName.getText()));

        Button clear = new Button("Clear");
        clear.getStyleClass().add("btn-ghost");
        clear.setTooltip(new Tooltip("Empty both name fields"));
        clear.setOnAction(e -> {
            firstName.clear();
            secondName.clear();
            showError(null);
            firstName.requestFocus();
        });

        HBox actions = new HBox(12, calculate, clear);
        actions.setAlignment(Pos.CENTER);

        Label disclaimer = new Label("Just for fun — not a fortune teller.");
        disclaimer.getStyleClass().add("hint");

        if (logo != null) {
            getChildren().add(logo);
        }
        getChildren().addAll(title, subtitle, firstLabel, firstName, firstCount,
                secondLabel, secondName, secondCount, error, manual, actions, disclaimer);
    }

    public boolean isManual() {
        return manual.isSelected();
    }

    public void setManual(boolean manualMode) {
        manual.setSelected(manualMode);
    }

    /** Shows an inline error, or hides it when message is null. */
    public void showError(String message) {
        error.setVisible(message != null);
        error.setManaged(message != null);
        if (message != null) {
            error.setText(message);
        }
    }

    public void keepNames(String first, String second) {
        firstName.setText(first);
        secondName.setText(second);
        showError(null);
    }

    public void focusFirst() {
        Platform.runLater(firstName::requestFocus);
    }

    private static Label counter() {
        Label counter = new Label("0/50");
        counter.getStyleClass().add("hint");
        counter.setMaxWidth(Double.MAX_VALUE);
        counter.setAlignment(Pos.CENTER_RIGHT);
        return counter;
    }

    private void styleField(TextField field, String prompt, String tip, Label counter) {
        field.setPromptText(prompt);
        field.setTooltip(new Tooltip(tip));
        field.getStyleClass().add("field");
        // ponytail: 50-codepoint cap — names longer than this never change the verdict
        field.setTextFormatter(new TextFormatter<String>(change -> {
            String next = change.getControlNewText();
            return next.codePointCount(0, next.length()) <= 50 ? change : null;
        }));
        field.textProperty().addListener((obs, old, value) -> {
            showError(null);
            counter.setText(value.codePointCount(0, value.length()) + "/50");
        });
    }
}
