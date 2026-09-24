package flames;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * The settings dialog opened from the gear button. Applies changes live and
 * persists them via {@link Settings}.
 */
public final class SettingsDialog {

    private SettingsDialog() {
    }

    public static void show(Window owner, Settings settings, Runnable onThemeChanged) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Settings");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        ChoiceBox<String> theme = new ChoiceBox<>();
        theme.getItems().addAll("Light", "Dark");
        theme.setValue(Settings.THEME_DARK.equals(settings.getTheme()) ? "Dark" : "Light");
        theme.setTooltip(new Tooltip("Application theme"));
        theme.setOnAction(e -> {
            settings.setTheme("Dark".equals(theme.getValue())
                    ? Settings.THEME_DARK : Settings.THEME_LIGHT);
            onThemeChanged.run();
            String css = MainApp.stylesheetFor(settings.getTheme());
            dialog.getDialogPane().getStylesheets().clear();
            if (css != null) {
                dialog.getDialogPane().getStylesheets().add(css);
            }
        });

        HBox themeRow = new HBox(12, new Label("Theme"), theme);
        themeRow.setAlignment(Pos.CENTER_LEFT);

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

        VBox content = new VBox(12, themeRow, sound, volumeRow, volumeLabel);
        content.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(content);
        String css = MainApp.stylesheetFor(settings.getTheme());
        if (css != null) {
            dialog.getDialogPane().getStylesheets().add(css);
        }
        dialog.showAndWait();
    }
}
