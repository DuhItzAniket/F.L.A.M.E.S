package flames;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * View tests. They run headless via Monocle (see the surefire config in
 * pom.xml) and do real work: error cycles, submit wiring, verdict content,
 * tile layout, and fail-fast construction.
 */
class ViewTest {

    private static final AtomicBoolean TOOLKIT_STARTED = new AtomicBoolean(false);

    @BeforeAll
    static void startToolkit() throws Exception {
        if (TOOLKIT_STARTED.compareAndSet(false, true)) {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            assertTrue(latch.await(15, TimeUnit.SECONDS), "JavaFX toolkit did not start");
        }
    }

    @AfterAll
    static void clearTestPrefs() throws Exception {
        java.util.prefs.Preferences.userRoot().node("flames-viewtest").removeNode();
    }

    private static SoundBank testSounds() {
        return new SoundBank(new Settings(
                java.util.prefs.Preferences.userRoot().node("flames-viewtest")));
    }

    @FunctionalInterface
    private interface FxTask {
        void run() throws Exception;
    }

    /** Runs the task on the FX thread, rethrowing failures unwrapped. */
    private static void fx(FxTask task) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });
        assertTrue(done.await(15, TimeUnit.SECONDS), "FX thread timed out");
        Throwable t = failure.get();
        if (t instanceof Error e) {
            throw e;
        }
        if (t instanceof Exception e) {
            throw e;
        }
    }

    private static Label labeled(List<Node> nodes, String style) {
        List<Label> found = new ArrayList<>();
        collect(nodes, Label.class, found);
        return found.stream()
                .filter(l -> l.getStyleClass().contains(style))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no ." + style + " label"));
    }

    private static <T extends Node> void collect(List<Node> nodes, Class<T> type, List<T> out) {
        for (Node node : nodes) {
            if (type.isInstance(node)) {
                out.add(type.cast(node));
            }
            if (node instanceof javafx.scene.Parent parent) {
                collect(parent.getChildrenUnmodifiable(), type, out);
            }
        }
    }

    private static TextField field(InputView view, int index) {
        List<TextField> fields = new ArrayList<>();
        collect(view.getChildren(), TextField.class, fields);
        return fields.get(index);
    }

    private static Button button(InputView view, String text) {
        List<Button> buttons = new ArrayList<>();
        collect(view.getChildren(), Button.class, buttons);
        return buttons.stream()
                .filter(b -> b.getText().equals(text))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no button '" + text + "'"));
    }

    @Test
    void errorShowsAndClears() throws Exception {
        fx(() -> {
            InputView view = new InputView((a, b) -> {
            });
            Label error = labeled(view.getChildren(), "error");
            assertFalse(error.isVisible());
            view.showError("The first name has no letters in it.");
            assertTrue(error.isVisible());
            assertEquals("The first name has no letters in it.", error.getText());
            view.showError(null);
            assertFalse(error.isVisible());
        });
    }

    @Test
    void submitPassesBothNames() throws Exception {
        fx(() -> {
            List<String> submitted = new ArrayList<>();
            InputView view = new InputView((a, b) -> submitted.add(a + "|" + b));
            field(view, 0).setText("Romeo");
            field(view, 1).setText("Juliet");
            Button calculate = button(view, "Reveal fate");
            assertTrue(calculate.isDefaultButton());
            calculate.fire();
            assertEquals(List.of("Romeo|Juliet"), submitted);
        });
    }

    @Test
    void keepNamesRefillsAndClearsError() throws Exception {
        fx(() -> {
            InputView view = new InputView((a, b) -> {
            });
            view.showError("boom");
            view.keepNames("Romeo", "Juliet");
            assertEquals("Romeo", field(view, 0).getText());
            assertEquals("Juliet", field(view, 1).getText());
            assertFalse(labeled(view.getChildren(), "error").isVisible());
        });
    }

    @Test
    void verdictShowsCategoryAndNames() throws Exception {
        fx(() -> {
            ResultView view = new ResultView(
                    FlamesEngine.calculate("john", "jane"), () -> {
                    }, () -> {
                    });
            assertEquals("Enemies", labeled(view.getChildren(), "word").getText());
            assertFalse(labeled(view.getChildren(), "meaning").getText().isBlank());
            String names = labeled(view.getChildren(), "names").getText();
            assertTrue(names.contains("john") && names.contains("jane"));
        });
    }

    @Test
    void zeroCountVerdictExplainsItself() throws Exception {
        fx(() -> {
            ResultView view = new ResultView(
                    FlamesEngine.calculate("anna", "anna"), () -> {
                    }, () -> {
                    });
            assertEquals("Marriage", labeled(view.getChildren(), "word").getText());
            assertTrue(labeled(view.getChildren(), "subtitle").getText().contains("cancelled"));
        });
    }

    @Test
    void eliminationStartsWithSixTilesAndStatus() throws Exception {
        fx(() -> {
            EliminationView view = new EliminationView(
                    FlamesEngine.calculate("john", "jane"), testSounds(), () -> {
                    });
            List<StackPane> tiles = new ArrayList<>();
            collect(view.getChildren(), StackPane.class, tiles);
            String letters = tiles.stream()
                    .filter(t -> t.getStyleClass().contains("tile"))
                    .map(t -> (Label) t.getChildren().get(0))
                    .map(Label::getText)
                    .reduce("", (a, b) -> a + b);
            assertEquals("FLAMES", letters);
            assertEquals("Steady\u2026", labeled(view.getChildren(), "status").getText());
        });
    }

    @Test
    void eliminationRejectsNonFlamesLetters() {
        FlamesOutcome bad = new FlamesOutcome(FlamesCategory.FRIENDS, 3,
                List.of('F', 'L', 'X', 'M', 'E'), "a", "b");
        assertThrows(IllegalArgumentException.class,
                () -> new EliminationView(bad, testSounds(), () -> {
                }));
    }

    @Test
    void cancellationLaysOutOneChipPerLetter() throws Exception {
        fx(() -> {
            EliminationView view = new EliminationView(
                    FlamesEngine.calculate("john", "jane"), testSounds(), () -> {
                    });
            List<StackPane> chips = new ArrayList<>();
            collect(view.getChildren(), StackPane.class, chips);
            long count = chips.stream()
                    .filter(p -> p.getStyleClass().contains("chip"))
                    .count();
            assertEquals(8, count);
        });
    }

    @Test
    void skipFinishesOnceWithEndStates() throws Exception {
        fx(() -> {
            AtomicInteger done = new AtomicInteger();
            EliminationView view = new EliminationView(
                    FlamesEngine.calculate("john", "jane"), testSounds(), done::incrementAndGet);
            view.finish();
            view.finish();
            assertEquals(1, done.get());

            List<StackPane> tiles = new ArrayList<>();
            collect(view.getChildren(), StackPane.class, tiles);
            for (StackPane tile : tiles) {
                if (!tile.getStyleClass().contains("tile")) {
                    continue;
                }
                String letter = ((Label) tile.getChildren().get(0)).getText();
                if (letter.equals("E")) {
                    assertTrue(tile.getStyleClass().contains("tile-winner"));
                } else {
                    assertEquals(0.0, tile.getOpacity());
                    assertEquals(46.0, tile.getTranslateY());
                }
            }
            List<javafx.scene.shape.Line> slashes = new ArrayList<>();
            collect(view.getChildren(), javafx.scene.shape.Line.class, slashes);
            long crossed = slashes.stream()
                    .filter(s -> s.isVisible() && s.getEndX() == 30).count();
            long fallen = slashes.stream()
                    .filter(s -> s.isVisible() && s.getEndX() == 46).count();
            assertEquals(4, crossed);
            assertEquals(5, fallen);
        });
    }

    @Test
    void skipRightAfterPlayDoesNotThrow() throws Exception {
        fx(() -> {
            AtomicInteger done = new AtomicInteger();
            EliminationView view = new EliminationView(
                    FlamesEngine.calculate("anna", "anna"), testSounds(), done::incrementAndGet);
            view.play();
            view.finish();
            assertEquals(1, done.get());
        });
    }

    @Test
    void bothThemesShipTheirStylesheet() {
        assertNotNull(MainApp.class.getResource("/assets/flames.css"));
        assertNotNull(MainApp.class.getResource("/assets/flames-dark.css"));
        assertTrue(MainApp.stylesheetFor(Settings.THEME_DARK).endsWith("flames-dark.css"));
        assertTrue(MainApp.stylesheetFor("neon").endsWith("flames.css"));
    }

    @Test
    void themesDefineTheSameClasses() throws Exception {
        assertEquals(cssClasses("/assets/flames.css"), cssClasses("/assets/flames-dark.css"));
    }

    private static java.util.Set<String> cssClasses(String resource) throws Exception {
        java.util.Set<String> classes = new java.util.TreeSet<>();
        try (var in = MainApp.class.getResourceAsStream(resource);
                var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8))) {
            var pattern = java.util.regex.Pattern.compile("^\\.([a-z-]+)\\b");
            String line;
            while ((line = reader.readLine()) != null) {
                var matcher = pattern.matcher(line.strip());
                if (matcher.find()) {
                    classes.add(matcher.group(1));
                }
            }
        }
        assertFalse(classes.isEmpty(), resource + " defines no classes");
        return classes;
    }
}
