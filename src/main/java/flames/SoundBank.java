package flames;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.media.AudioClip;

/**
 * UI sound effects from the bundled WAVs. Null-safe (missing files are
 * skipped) and exception-safe (machines without audio just stay silent).
 * Volume and mute come from {@link Settings} at play time.
 */
public final class SoundBank {

    private final Map<String, AudioClip> clips = new HashMap<>();
    private final Settings settings;

    public SoundBank(Settings settings) {
        this.settings = settings;
        for (String name : new String[]{"click", "tick", "pop", "error", "fanfare"}) {
            URL sound = getClass().getResource("/assets/sound/" + name + ".wav");
            if (sound != null) {
                try {
                    clips.put(name, new AudioClip(sound.toExternalForm()));
                } catch (RuntimeException ignored) {
                    // Audio unavailable — the app stays silent but works.
                }
            }
        }
    }

    /** Plays the named effect at the user's volume, unless muted. */
    public void play(String name) {
        if (!settings.isSoundEnabled()) {
            return;
        }
        AudioClip clip = clips.get(name);
        if (clip != null) {
            try {
                clip.play(settings.getVolume());
            } catch (RuntimeException ignored) {
                // Audio unavailable — stay silent.
            }
        }
    }
}
