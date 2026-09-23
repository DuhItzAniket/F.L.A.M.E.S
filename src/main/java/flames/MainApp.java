package flames;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
        inputView = new InputView(this::calculate, () -> {
        });

        root = new StackPane(inputView);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 640, 560);
        scene.getStylesheets().add(getClass().getResource("/assets/flames.css").toExternalForm());

        stage.setTitle("F.L.A.M.E.S");
        stage.setMinWidth(480);
        stage.setMinHeight(540);
        stage.setScene(scene);
        stage.show();
    }

    private void calculate(String first, String second) {
        try {
            showResult(FlamesEngine.calculate(first, second));
        } catch (IllegalArgumentException ex) {
            inputView.showError(ex.getMessage());
        }
    }

    private void showResult(FlamesOutcome outcome) {
        root.getChildren().setAll(new ResultView(outcome,
                () -> {
                    inputView.keepNames(outcome.displayName1(), outcome.displayName2());
                    root.getChildren().setAll(inputView);
                },
                () -> {
                    inputView.keepNames("", "");
                    root.getChildren().setAll(inputView);
                }));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
