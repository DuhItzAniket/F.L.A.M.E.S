package flames;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Manual mode: the player crosses the pairs themselves, pen-on-paper
 * style. Tap a letter, then its match in the other name; "Count it"
 * runs the ring on whatever is left standing.
 */
public final class ManualView extends VBox {

    private final List<LetterChip> row1 = new ArrayList<>();
    private final List<LetterChip> row2 = new ArrayList<>();
    private final SoundBank sounds;
    private final IntConsumer onDone;
    private final Label status = new Label();
    private final Label counter = new Label();
    private LetterChip selected;

    public ManualView(String name1, String name2, SoundBank sounds,
            IntConsumer onDone, Runnable onBack) {
        super(14);
        this.sounds = sounds;
        this.onDone = onDone;
        setAlignment(Pos.CENTER);
        setPadding(new Insets(32));
        setMaxWidth(520);

        Label heading = new Label("Cross them out yourself");
        heading.getStyleClass().add("names");

        buildRow(row1, name1);
        buildRow(row2, name2);

        status.getStyleClass().add("status");
        status.setWrapText(true);
        status.setAlignment(Pos.CENTER);
        status.setText("Tap a letter, then its match in the other name.");
        counter.getStyleClass().add("subtitle");
        refreshCounter();

        Button done = new Button("Count it");
        done.getStyleClass().add("btn-primary");
        done.setDefaultButton(true);
        done.setTooltip(new Tooltip("Count the survivors and run the ring (Enter)"));
        done.setOnAction(e -> {
            sounds.play("click");
            onDone.accept(remaining());
        });

        Button back = new Button("Back");
        back.getStyleClass().add("btn-ghost");
        back.setTooltip(new Tooltip("Back to the names"));
        back.setOnAction(e -> onBack.run());

        HBox actions = new HBox(12, done, back);
        actions.setAlignment(Pos.CENTER);

        setFocusTraversable(true);
        getChildren().addAll(heading, chipRow(name1, row1), chipRow(name2, row2),
                status, counter, actions);
        Platform.runLater(this::requestFocus);
    }

    private void buildRow(List<LetterChip> row, String name) {
        FlamesEngine.normalize(name).codePoints().forEach(cp -> {
            LetterChip chip = new LetterChip(cp);
            chip.box.setOnMouseClicked(e -> pick(chip));
            chip.box.setFocusTraversable(true);
            Tooltip.install(chip.box, new Tooltip("Cross this letter"));
            chip.box.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case ENTER, SPACE -> {
                        e.consume();
                        pick(chip);
                    }
                    default -> {
                    }
                }
            });
            row.add(chip);
        });
    }

    private VBox chipRow(String name, List<LetterChip> chips) {
        Label caption = new Label(name.isEmpty() ? "?" : name);
        caption.getStyleClass().add("row-label");
        FlowPane flow = new FlowPane(6, 6);
        flow.getStyleClass().add("chips");
        for (LetterChip chip : chips) {
            flow.getChildren().add(chip.box);
        }
        VBox row = new VBox(4, caption, flow);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private void pick(LetterChip chip) {
        if (chip.taken) {
            return;
        }
        if (selected == null) {
            select(chip);
        } else if (selected == chip) {
            deselect();
            status.setText("Tap a letter, then its match in the other name.");
        } else if (sameRow(selected, chip)) {
            deselect();
            select(chip);
        } else if (selected.codePoint == chip.codePoint) {
            cross(selected, chip);
            deselect();
        } else {
            sounds.play("error");
            status.setText("Pairs must match — pick the same letter in both names.");
        }
    }

    private void select(LetterChip chip) {
        selected = chip;
        chip.box.getStyleClass().add("chip-pick");
        sounds.play("click");
        status.setText("Now tap its match in the other name.");
    }

    private void deselect() {
        if (selected != null) {
            selected.box.getStyleClass().remove("chip-pick");
            selected = null;
        }
    }

    private void cross(LetterChip first, LetterChip second) {
        for (LetterChip chip : new LetterChip[]{first, second}) {
            chip.taken = true;
            chip.label.getStyleClass().add("chip-off");
            LetterChip.drawSlash(chip.slash, 30, 36);
        }
        sounds.play("pop");
        refreshCounter();
    }

    private boolean sameRow(LetterChip a, LetterChip b) {
        return row1.contains(a) == row1.contains(b);
    }

    private int remaining() {
        int left = 0;
        for (List<LetterChip> row : List.of(row1, row2)) {
            for (LetterChip chip : row) {
                if (!chip.taken) {
                    left++;
                }
            }
        }
        return left;
    }

    private void refreshCounter() {
        int left = remaining();
        counter.setText(left == 0
                ? "Nothing left — a full round awaits."
                : left + (left == 1 ? " letter will count." : " letters will count."));
    }
}
