package flames;

import java.util.prefs.Preferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTest {

    private Preferences node;
    private Settings settings;

    @BeforeEach
    void setUp() {
        node = Preferences.userRoot().node("flames-test");
        settings = new Settings(node);
    }

    @AfterEach
    void tearDown() throws Exception {
        node.removeNode();
    }

    @Test
    void defaultsAreSane() {
        assertEquals(Settings.THEME_LIGHT, settings.getTheme());
        assertTrue(settings.isSoundEnabled());
        assertTrue(settings.isAmbientEnabled());
        assertEquals(0.7, settings.getVolume());
    }

    @Test
    void themeRoundTripsAndNormalizes() {
        settings.setTheme(Settings.THEME_DARK);
        assertEquals(Settings.THEME_DARK, settings.getTheme());
        settings.setTheme("neon");
        assertEquals(Settings.THEME_LIGHT, settings.getTheme());
    }

    @Test
    void soundToggleRoundTrips() {
        settings.setSoundEnabled(false);
        assertFalse(settings.isSoundEnabled());
        settings.setSoundEnabled(true);
        assertTrue(settings.isSoundEnabled());
    }

    @Test
    void ambientToggleRoundTrips() {
        settings.setAmbientEnabled(false);
        assertFalse(settings.isAmbientEnabled());
        settings.setAmbientEnabled(true);
        assertTrue(settings.isAmbientEnabled());
    }

    @Test
    void volumeClampsAndRoundTrips() {
        settings.setVolume(0.4);
        assertEquals(0.4, settings.getVolume());
        settings.setVolume(-2.0);
        assertEquals(0.0, settings.getVolume());
        settings.setVolume(99.0);
        assertEquals(1.0, settings.getVolume());
    }
}
