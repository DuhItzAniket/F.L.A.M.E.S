package flames;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * The settings dialog opened from the gear button. Applies changes live and
 * persists them via {@link Settings}. (Theme control arrives in Phase 12.)
 */
public final class SettingsDialog {

    private SettingsDialog() {
    }

    public static void show(Window owner, String stylesheet, Settings settings) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Settings");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
        }

        CheckBox sound = new CheckBox("Sound effects");
        sound.setSelected(settings.isSoundEnabled());
        sound.setTooltip(new Tooltip("Play ticks, pops, and fanfares"));
        sound.setOnAction(e -> settings.setSoundEnabled(sound.isSelected()));

        Label volumeLabel = new Label();
        volumeLabel.getStyleClass().add("hint");
        Slider volume = new Slider(0, 100, settings.getVolume() * 100);
        volume.setTooltip(new Tooltip("Effect volume"));
        volume.setPrefWidth(220);
        Runnable refresh = () -> volumeLabel.setText("Volume: "
                + Math.round(volume.getValue()) + "%");
        volume.valueProperty().addListener((obs, old, value) -> {
            settings.setVolume(value.doubleValue() / 100);
            refresh.run();
        });
        refresh.run();

        HBox volumeRow = new HBox(12, new Label("Volume"), volume);
        volumeRow.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(12, sound, volumeRow, volumeLabel);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
}
