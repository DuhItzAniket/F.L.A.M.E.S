package flames;

import java.util.prefs.Preferences;

/**
 * Persisted user preferences (theme, sound, volume). Backed by
 * {@link Preferences} so choices survive restarts with zero dependencies.
 * Testable by injecting any node.
 */
public final class Settings {

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";

    private final Preferences prefs;

    public Settings() {
        this(Preferences.userNodeForPackage(Settings.class));
    }

    Settings(Preferences prefs) {
        this.prefs = prefs;
    }

    public String getTheme() {
        return THEME_DARK.equals(prefs.get("theme", THEME_LIGHT)) ? THEME_DARK : THEME_LIGHT;
    }

    public void setTheme(String theme) {
        prefs.put("theme", THEME_DARK.equals(theme) ? THEME_DARK : THEME_LIGHT);
        flush();
    }

    public boolean isSoundEnabled() {
        return prefs.getBoolean("sound", true);
    }

    public void setSoundEnabled(boolean enabled) {
        prefs.putBoolean("sound", enabled);
        flush();
    }

    /** Ambient ember background; motion-sensitive users can switch it off. */
    public boolean isAmbientEnabled() {
        return prefs.getBoolean("ambient", true);
    }

    public void setAmbientEnabled(boolean enabled) {
        prefs.putBoolean("ambient", enabled);
        flush();
    }

    /** Volume in 0..1, clamped. */
    public double getVolume() {
        return clamp(prefs.getDouble("volume", 0.7));
    }

    public void setVolume(double volume) {
        prefs.putDouble("volume", clamp(volume));
        flush();
    }

    private static double clamp(double volume) {
        return Math.min(1.0, Math.max(0.0, volume));
    }

    private void flush() {
        try {
            prefs.flush();
        } catch (Exception ignored) {
            // Preferences are best-effort; the app works without them.
        }
    }
}
