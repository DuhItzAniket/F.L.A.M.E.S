package flames;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * JavaFX entry point. Owns the stage and swaps views; game rules stay in
 * {@link FlamesEngine}, never here.
 */
public final class MainApp extends Application {

    private StackPane content;
    private InputView inputView;
    private Scene scene;
    private final Settings settings = new Settings();
    private SoundBank sounds;
    private String stylesheet;

    @Override
    public void start(Stage stage) {
        loadFonts();
        sounds = new SoundBank(settings);
        inputView = new InputView(this::calculate);

        content = new StackPane(inputView);
        content.setAlignment(Pos.CENTER);

        Button gear = new Button();
        gear.setGraphic(gearGraphic());
        gear.getStyleClass().add("icon-btn");
        gear.setTooltip(new Tooltip("Settings"));
        gear.setOnAction(e -> {
            sounds.play("click");
            SettingsDialog.show(stage, settings, this::applyTheme);
        });

        StackPane root = new StackPane(content, gear);
        StackPane.setAlignment(gear, Pos.TOP_RIGHT);
        StackPane.setMargin(gear, new Insets(10));

        scene = new Scene(root, 640, 560);
        applyTheme();

        stage.setTitle("F.L.A.M.E.S");
        for (String size : new String[]{"16", "32", "48"}) {
            InputStream icon = getClass().getResourceAsStream("/assets/icon/icon-" + size + ".png");
            if (icon != null) {
                stage.getIcons().add(new Image(icon));
            }
        }
        stage.setMinWidth(480);
        stage.setMinHeight(540);
        stage.setScene(scene);
        stage.show();
        inputView.focusFirst();
    }

    /** Stylesheet URL for a theme, or null if the file is missing. */
    static String stylesheetFor(String theme) {
        String file = Settings.THEME_DARK.equals(theme) ? "flames-dark.css" : "flames.css";
        URL css = MainApp.class.getResource("/assets/" + file);
        return css == null ? null : css.toExternalForm();
    }

    private void applyTheme() {
        stylesheet = stylesheetFor(settings.getTheme());
        if (scene != null) {
            scene.getStylesheets().clear();
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet);
            }
        }
    }

    /**
     * Registers the bundled display faces. If anything is missing the UI
     * simply renders in the platform default — fonts are branding, not logic.
     */
    private static void loadFonts() {
        for (String file : new String[]{"GentiumBookPlus-Regular.ttf", "GentiumBookPlus-Bold.ttf"}) {
            try (InputStream in = MainApp.class.getResourceAsStream("/assets/fonts/" + file)) {
                if (in != null) {
                    Font.loadFont(in, 16);
                }
            } catch (IOException ignored) {
                // fall back to the platform default font
            }
        }
    }

    private void calculate(String first, String second) {
        sounds.play("click");
        try {
            FlamesOutcome outcome = FlamesEngine.calculate(first, second);
            EliminationView elimination = new EliminationView(outcome, () -> showResult(outcome));
            content.getChildren().setAll(elimination);
            elimination.play();
        } catch (IllegalArgumentException ex) {
            sounds.play("error");
            inputView.showError(ex.getMessage());
        }
    }

    private void showResult(FlamesOutcome outcome) {
        sounds.play("fanfare");
        content.getChildren().setAll(new ResultView(outcome,
                () -> {
                    inputView.keepNames(outcome.displayName1(), outcome.displayName2());
                    content.getChildren().setAll(inputView);
                    inputView.focusFirst();
                },
                () -> {
                    inputView.keepNames("", "");
                    content.getChildren().setAll(inputView);
                    inputView.focusFirst();
                }));
    }

    /** Gear mark drawn in code: ring, eight teeth, hub. Theme-aware via CSS. */
    private static Node gearGraphic() {
        Group gear = new Group();
        Circle ring = new Circle(9);
        ring.setFill(Color.TRANSPARENT);
        ring.getStyleClass().add("gear-stroke");
        gear.getChildren().add(ring);
        for (int i = 0; i < 8; i++) {
            Rectangle tooth = new Rectangle(-2, -14, 4, 5);
            tooth.getStyleClass().add("gear-fill");
            Group holder = new Group(tooth);
            holder.setRotate(i * 45.0);
            gear.getChildren().add(holder);
        }
        Circle hub = new Circle(3.2);
        hub.getStyleClass().add("gear-fill");
        gear.getChildren().add(hub);
        return gear;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
