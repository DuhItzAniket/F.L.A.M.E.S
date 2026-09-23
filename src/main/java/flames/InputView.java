package flames;

import java.util.function.BiConsumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
    private final Label error = new Label();

    public InputView(BiConsumer<String, String> onCalculate) {
        super(12);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(32));
        setMaxWidth(460);

        Label title = new Label("F.L.A.M.E.S");
        title.getStyleClass().add("title");

        ImageView logo = new ImageView(
                new Image(getClass().getResourceAsStream("/assets/icon/icon-128.png")));
        logo.setFitHeight(84);
        logo.setPreserveRatio(true);

        Label subtitle = new Label("Two names in. One verdict out.");
        subtitle.getStyleClass().add("subtitle");

        Label firstLabel = new Label("First name");
        firstLabel.setLabelFor(firstName);
        styleField(firstName, "e.g. Romeo", "First name — only letters count, the rest is ignored");

        Label secondLabel = new Label("Second name");
        secondLabel.setLabelFor(secondName);
        styleField(secondName, "e.g. Juliet", "Second name — only letters count, the rest is ignored");

        error.getStyleClass().add("error");
        error.setVisible(false);
        error.setManaged(false);

        Button calculate = new Button("Reveal fate");
        calculate.getStyleClass().add("btn-primary");
        calculate.setDefaultButton(true);
        calculate.setTooltip(new javafx.scene.control.Tooltip("Calculate the FLAMES verdict (Enter)"));
        calculate.setOnAction(e -> onCalculate.accept(firstName.getText(), secondName.getText()));

        Button clear = new Button("Clear");
        clear.getStyleClass().add("btn-ghost");
        clear.setTooltip(new javafx.scene.control.Tooltip("Empty both name fields"));
        clear.setOnAction(e -> {
            firstName.clear();
            secondName.clear();
            showError(null);
            firstName.requestFocus();
        });

        HBox actions = new HBox(12, calculate, clear);
        actions.setAlignment(Pos.CENTER);

        getChildren().addAll(logo, title, subtitle, firstLabel, firstName,
                secondLabel, secondName, error, actions);
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
        firstName.requestFocus();
    }

    private void styleField(TextField field, String prompt, String tip) {
        field.setPromptText(prompt);
        field.setTooltip(new Tooltip(tip));
        field.getStyleClass().add("field");
        // ponytail: 50-char cap — names longer than this never change the verdict
        field.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= 50 ? change : null));
        field.textProperty().addListener((obs, old, value) -> showError(null));
    }
}
