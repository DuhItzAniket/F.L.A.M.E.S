package flames;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.BiConsumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Past games, newest first. Replay refills the names and runs the
 * automatic show; clearing wipes the local file.
 */
public final class HistoryDialog {

    private HistoryDialog() {
    }

    public static void show(Window owner, String stylesheet, HistoryStore store,
            BiConsumer<String, String> onReplay) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("History");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
        }

        VBox rows = new VBox(8);
        rows.setPadding(new Insets(4));
        List<HistoryStore.Entry> entries = store.load();
        if (entries.isEmpty()) {
            Label empty = new Label("No games yet — go play one.");
            empty.getStyleClass().add("subtitle");
            rows.getChildren().add(empty);
        }
        for (HistoryStore.Entry entry : entries) {
            Label text = new Label(describe(entry));
            text.getStyleClass().add("recap");
            text.setWrapText(true);
            Button replay = new Button("Replay");
            replay.getStyleClass().add("btn-ghost");
            replay.setTooltip(new Tooltip("Play these names again"));
            replay.setOnAction(e -> {
                dialog.close();
                onReplay.accept(entry.name1(), entry.name2());
            });
            HBox row = new HBox(12, text, replay);
            row.setAlignment(Pos.CENTER_LEFT);
            rows.getChildren().add(row);
        }

        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(Math.min(320, 48 + entries.size() * 44));
        scroll.getStyleClass().add("history-scroll");

        Button clear = new Button("Clear history");
        clear.getStyleClass().add("btn-ghost");
        clear.setTooltip(new Tooltip("Delete all past games"));
        clear.setOnAction(e -> {
            store.clear();
            dialog.close();
        });

        VBox content = new VBox(12, scroll, clear);
        content.setPadding(new Insets(16));
        content.setMinWidth(380);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private static String describe(HistoryStore.Entry entry) {
        String when;
        try {
            when = Instant.parse(entry.at()).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("d MMM"));
        } catch (Exception bad) {
            when = entry.at();
        }
        String what;
        try {
            what = FlamesCategory.fromLetter(entry.category()).title();
        } catch (IllegalArgumentException bad) {
            what = String.valueOf(entry.category());
        }
        return entry.name1() + " \u2665 " + entry.name2() + " — " + what + " · " + when;
    }
}
