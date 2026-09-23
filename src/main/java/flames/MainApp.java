package flames;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * JavaFX entry point. Intentionally thin: Phase 1 proves the runtime boots
 * and shows branded identity. The full game UI lands in Phase 3.
 */
public final class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Label identity = new Label("F.L.A.M.E.S");
        identity.setStyle("-fx-font-size: 42px; -fx-font-weight: bold;");
        stage.setTitle("F.L.A.M.E.S");
        stage.setScene(new Scene(new StackPane(identity), 560, 360));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
