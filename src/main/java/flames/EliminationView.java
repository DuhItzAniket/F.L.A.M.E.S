package flames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.util.Duration;

/**
 * The step-by-step show, replaying the engine's data with motion:
 * shared letters cross out pair by pair, then the FLAMES ring is counted
 * around hop by hop until one letter survives. Click, Enter, or Space
 * skips to the verdict. Status text narrates every step so nothing
 * depends on color or motion alone.
 */
public final class EliminationView extends VBox {

    /** One letter chip: a fixed tile with a pen slash drawn across on crossing. */
    private static final class Chip {
        final StackPane box;
        final Label label;
        final Line slash;
        final int codePoint;
        boolean taken;

        Chip(int codePoint) {
            this.codePoint = codePoint;
            this.label = new Label(new String(Character.toChars(codePoint)));
            this.label.getStyleClass().add("chip-label");
            this.slash = new Line(0, 0, 0, 0);
            this.slash.getStyleClass().add("slash");
            this.slash.setVisible(false);
            this.box = new StackPane(label, slash);
            this.box.getStyleClass().add("chip");
            this.box.setMinSize(40, 46);
            this.box.setMaxSize(40, 46);
        }
    }

    private final FlamesOutcome outcome;
    private final SoundBank sounds;
    private final Runnable onDone;
    private final Label status = new Label();
    private final VBox cancelBox = new VBox(10);
    private final HBox ringBox = new HBox(10);
    private final List<Chip> row1 = new ArrayList<>();
    private final List<Chip> row2 = new ArrayList<>();
    private final List<Integer> pops;
    private final int chipTotal;
    private final Map<Character, Label> tiles = new HashMap<>();
    private Label lastHop;
    private Timeline timeline;
    private boolean finished;

    public EliminationView(FlamesOutcome outcome, SoundBank sounds, Runnable onDone) {
        super(16);
        this.outcome = outcome;
        this.sounds = sounds;
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

        pops = FlamesEngine.cancellationOrder(
                outcome.displayName1(), outcome.displayName2());
        chipTotal = pops.size() * 2 + outcome.remainingCount();
        buildChips(row1, outcome.displayName1());
        buildChips(row2, outcome.displayName2());
        cancelBox.setAlignment(Pos.CENTER);
        cancelBox.getChildren().addAll(
                chipRow(outcome.displayName1(), row1),
                chipRow(outcome.displayName2(), row2));

        ringBox.getStyleClass().add("tiles");
        ringBox.setVisible(false);
        ringBox.setManaged(false);
        for (char letter : new char[]{'F', 'L', 'A', 'M', 'E', 'S'}) {
            Label tile = new Label(String.valueOf(letter));
            tile.getStyleClass().add("tile");
            tile.setTooltip(new Tooltip(FlamesCategory.fromLetter(letter).title()));
            tile.setAccessibleText(letter + ", " + FlamesCategory.fromLetter(letter).title()
                    + ", still standing.");
            tiles.put(letter, tile);
            ringBox.getChildren().add(tile);
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
        getChildren().addAll(heading, cancelBox, ringBox, status, hint);
    }

    /** Starts the sequence. Safe to call once the view is shown. */
    public void play() {
        if (finished) {
            return;
        }
        Platform.runLater(this::requestFocus);
        timeline = new Timeline();
        double at = 500;

        if (pops.isEmpty()) {
            at = after(at + 200, () -> {
                status.setText("No shared letters \u2014 every letter counts.");
            });
        } else {
            at = after(at, () -> status.setText("Crossing out the shared letters\u2026"));
            double interval = Math.min(450, 3000.0 / pops.size());
            for (int k = 0; k < pops.size(); k++) {
                final int step = k;
                at = after(at + interval, () -> {
                    Chip[] pair = prepareCancel(step);
                    drawSlash(pair[0].slash);
                    drawSlash(pair[1].slash);
                    sounds.play("pop");
                });
            }
        }

        List<Chip> survivors = survivors();
        if (survivors.isEmpty()) {
            at = after(at + 700, () -> {
                status.setText("Nothing left \u2014 a perfect round.");
            });
        } else {
            at = after(at + 200, () -> status.setText("Counting the survivors\u2026"));
            double countInterval = Math.min(300, 2500.0 / survivors.size());
            for (int i = 0; i < survivors.size(); i++) {
                final int spoken = i + 1;
                final Chip chip = survivors.get(i);
                at = after(at + countInterval, () -> {
                    applyCount(chip, spoken);
                    punch(chip.label);
                    sounds.play("tick");
                    status.setText("Counting the survivors\u2026 " + spoken);
                });
            }
            final int total = survivors.size();
            at = after(at + 400, () -> status.setText(total
                    + (total == 1 ? " letter stands." : " letters stand.")));
        }

        at = after(at + 500, () -> {
            cancelBox.setVisible(false);
            cancelBox.setManaged(false);
            ringBox.setVisible(true);
            ringBox.setManaged(true);
        });
        List<Character> ring = new ArrayList<>(List.of('F', 'L', 'A', 'M', 'E', 'S'));
        int step = outcome.remainingCount() == 0
                ? ring.size() : outcome.remainingCount();
        int index = 0;
        var order = outcome.eliminationOrder();
        for (int e = 0; e < order.size(); e++) {
            double hopInterval = Math.min(110, 1100.0 / step);
            for (int i = 0; i < step; i++) {
                final int hop = (index + i) % ring.size();
                final int spoken = i + 1;
                at = after(at + hopInterval, () -> {
                    showHop(tiles.get(ring.get(hop)));
                    status.setText("Counting\u2026 " + spoken);
                    sounds.play("tick");
                });
            }
            index = (index + step - 1) % ring.size();
            final char fallen = ring.remove(index);
            final int left = ring.size();
            at = after(at + 260, () -> {
                prepareStrike(fallen, left);
                punch(tiles.get(fallen));
                sounds.play("pop");
            });
            at += 200;
        }

        at = after(at + 500, () -> {
            prepareCrown();
            punch(tiles.get(outcome.category().letter()));
        });
        after(at + 900, this::finish);
        timeline.play();
    }

    /** Schedules work at an absolute time; returns that time. */
    private double after(double at, Runnable work) {
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(at), e -> {
            if (!finished) {
                work.run();
            }
        }));
        return at;
    }

    private VBox chipRow(String name, List<Chip> chips) {
        Label caption = new Label(name.isEmpty() ? "?" : name);
        caption.getStyleClass().add("row-label");
        FlowPane flow = new FlowPane(6, 6);
        flow.getStyleClass().add("chips");
        for (Chip chip : chips) {
            flow.getChildren().add(chip.box);
        }
        VBox row = new VBox(4, caption, flow);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    private void buildChips(List<Chip> row, String name) {
        FlamesEngine.normalize(name).codePoints().forEach(cp -> row.add(new Chip(cp)));
    }

    private Chip take(List<Chip> row, int codePoint) {
        for (Chip chip : row) {
            if (!chip.taken && chip.codePoint == codePoint) {
                chip.taken = true;
                return chip;
            }
        }
        throw new IllegalStateException("No chip left for " + codePoint);
    }

    /** Marks the k-th pair crossed out; returns it for animation. */
    private Chip[] prepareCancel(int k) {
        int cp = pops.get(k);
        Chip first = take(row1, cp);
        Chip second = take(row2, cp);
        int left = chipTotal - 2 * (k + 1);
        for (Chip chip : new Chip[]{first, second}) {
            addStyle(chip.label, "chip-off");
            chip.label.setAccessibleText("Crossed out.");
        }
        status.setText(left == 0
                ? "Every letter cancelled out."
                : left + (left == 1 ? " letter remains." : " letters remain."));
        return new Chip[]{first, second};
    }

    private List<Chip> survivors() {
        List<Chip> survivors = new ArrayList<>();
        for (Chip chip : row1) {
            if (!chip.taken) {
                survivors.add(chip);
            }
        }
        for (Chip chip : row2) {
            if (!chip.taken) {
                survivors.add(chip);
            }
        }
        return survivors;
    }

    /** Marks a chip as counted survivor number n. */
    private static void applyCount(Chip chip, int n) {
        addStyle(chip.box, "chip-count");
        chip.label.setAccessibleText("Counts as " + n + ".");
    }

    private void showHop(Label tile) {
        if (lastHop != null) {
            lastHop.getStyleClass().remove("tile-hop");
        }
        lastHop = tile;
        addStyle(tile, "tile-hop");
    }

    /** Strikes a fallen tile; announces how many stand. */
    private void prepareStrike(char letter, int left) {
        Label tile = tiles.get(letter);
        if (lastHop == tile) {
            lastHop = null;
        } else if (lastHop != null) {
            lastHop.getStyleClass().remove("tile-hop");
            lastHop = null;
        }
        tile.getStyleClass().remove("tile-hop");
        addStyle(tile, "tile-out");
        tile.setAccessibleText(letter + ", out.");
        status.setText(left == 1
                ? letter + " falls \u2014 one survives."
                : letter + " falls \u2014 " + left + " remain.");
    }

    private void prepareCrown() {
        char winner = outcome.category().letter();
        Label tile = tiles.get(winner);
        tile.getStyleClass().remove("tile-hop");
        addStyle(tile, "tile-winner");
        tile.setAccessibleText(winner + ", " + outcome.category().title() + ", the verdict.");
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
        for (int k = 0; k < pops.size(); k++) {
            for (Chip chip : prepareCancel(k)) {
                chip.slash.setVisible(true);
                chip.slash.setEndX(30);
                chip.slash.setEndY(36);
            }
        }
        List<Chip> counted = survivors();
        for (int i = 0; i < counted.size(); i++) {
            applyCount(counted.get(i), i + 1);
        }
        cancelBox.setVisible(false);
        cancelBox.setManaged(false);
        ringBox.setVisible(true);
        ringBox.setManaged(true);
        List<Character> ring = new ArrayList<>(List.of('F', 'L', 'A', 'M', 'E', 'S'));
        for (char fallen : outcome.eliminationOrder()) {
            ring.remove((Character) fallen);
            prepareStrike(fallen, ring.size());
            tiles.get(fallen).setOpacity(0.55);
        }
        prepareCrown();
        onDone.run();
    }

    /** Draws the pen slash across a chip, top-left to bottom-right. */
    private static void drawSlash(Line slash) {
        slash.setVisible(true);
        Timeline draw = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(slash.endXProperty(), 0),
                        new KeyValue(slash.endYProperty(), 0)),
                new KeyFrame(Duration.millis(240),
                        new KeyValue(slash.endXProperty(), 30),
                        new KeyValue(slash.endYProperty(), 36)));
        draw.play();
    }

    private static void punch(Label label) {
        ScaleTransition punch = new ScaleTransition(Duration.millis(320), label);
        punch.setToX(1.28);
        punch.setToY(1.28);
        punch.setCycleCount(2);
        punch.setAutoReverse(true);
        punch.play();
    }

    private static void addStyle(javafx.scene.Node node, String style) {
        if (!node.getStyleClass().contains(style)) {
            node.getStyleClass().add(style);
        }
    }
}
