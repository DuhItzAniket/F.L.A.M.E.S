package flames;

import java.io.InputStream;
import java.net.URL;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX entry point. Owns the stage and swaps views; game rules stay in
 * {@link FlamesEngine}, never here.
 */
public final class MainApp extends Application {

    private StackPane root;
    private InputView inputView;

    @Override
    public void start(Stage stage) {
        inputView = new InputView(this::calculate);

        root = new StackPane(inputView);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 640, 560);
        URL css = getClass().getResource("/assets/flames.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

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

    private void calculate(String first, String second) {
        try {
            FlamesOutcome outcome = FlamesEngine.calculate(first, second);
            EliminationView elimination = new EliminationView(outcome, () -> showResult(outcome));
            root.getChildren().setAll(elimination);
            elimination.play();
        } catch (IllegalArgumentException ex) {
            inputView.showError(ex.getMessage());
        }
    }

    private void showResult(FlamesOutcome outcome) {
        root.getChildren().setAll(new ResultView(outcome,
                () -> {
                    inputView.keepNames(outcome.displayName1(), outcome.displayName2());
                    root.getChildren().setAll(inputView);
                    inputView.focusFirst();
                },
                () -> {
                    inputView.keepNames("", "");
                    root.getChildren().setAll(inputView);
                    inputView.focusFirst();
                }));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
