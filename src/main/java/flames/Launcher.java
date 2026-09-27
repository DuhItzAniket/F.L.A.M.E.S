package flames;

/**
 * Plain entry point for packaged launches. The main class must not extend
 * {@code Application} — otherwise the JavaFX launcher demands the modules
 * on the module path and refuses classpath mode. Delegates everything to
 * {@link MainApp}.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
