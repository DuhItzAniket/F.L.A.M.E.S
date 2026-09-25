package flames;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Ember particle layer: ambient rising motes plus radial bursts, drawn on
 * one canvas with a fixed pool (no allocation per frame, no dependencies).
 * Sits behind the UI, ignores the mouse, and idles the timer when empty.
 * Colors follow the active theme; alpha stays low so text always wins.
 */
public final class EmberField extends Canvas {

    private static final int MAX = 240;
    private static final int AMBIENT_CAP = 90;

    private final double[] px = new double[MAX];
    private final double[] py = new double[MAX];
    private final double[] vx = new double[MAX];
    private final double[] vy = new double[MAX];
    private final double[] life = new double[MAX];
    private final double[] span = new double[MAX];
    private final double[] size = new double[MAX];
    private final double[] seed = new double[MAX];
    private int alive;
    private long lastSpawn;

    private boolean ambient = true;
    private boolean dark;
    private final AnimationTimer clock = new AnimationTimer() {
        @Override
        public void handle(long now) {
            tick(now);
        }
    };
    private boolean ticking;

    public EmberField() {
        setMouseTransparent(true);
    }

    public void setTheme(boolean dark) {
        this.dark = dark;
    }

    public void setAmbientEnabled(boolean ambient) {
        this.ambient = ambient;
        if (ambient) {
            start();
        }
    }

    /** Radial burst of n embers. Coordinates are in this canvas's space. */
    public void burst(double x, double y, int n, double speed) {
        for (int i = 0; i < n; i++) {
            int slot = nextSlot();
            if (slot < 0) {
                return;
            }
            double angle = Math.random() * Math.PI * 2;
            double power = speed * (0.35 + Math.random() * 0.65);
            px[slot] = x;
            py[slot] = y;
            vx[slot] = Math.cos(angle) * power;
            vy[slot] = Math.sin(angle) * power - speed * 0.55;
            span[slot] = life[slot] = 500 + Math.random() * 700;
            size[slot] = 1.5 + Math.random() * 3;
            seed[slot] = Math.random() * 10;
        }
        start();
    }

    /** Celebration shower across the top of the field. */
    public void celebrate() {
        double w = getWidth();
        double y = getHeight() * 0.22;
        for (int i = 0; i < 5; i++) {
            burst(w * (0.12 + 0.19 * i), y, 26, 130);
        }
    }

    /** Live particle count, for tests and the idle check. */
    int aliveCount() {
        return alive;
    }

    private int nextSlot() {
        if (alive >= MAX) {
            return -1;
        }
        return alive++;
    }

    private void start() {
        if (!ticking) {
            ticking = true;
            lastSpawn = 0;
            clock.start();
        }
    }

    private void tick(long now) {
        double w = getWidth();
        double h = getHeight();
        if (ambient && alive < AMBIENT_CAP && now - lastSpawn > 80_000_000L) {
            lastSpawn = now;
            int slot = nextSlot();
            if (slot >= 0) {
                px[slot] = Math.random() * w;
                py[slot] = h + 4;
                vx[slot] = 0;
                vy[slot] = -(14 + Math.random() * 26);
                span[slot] = life[slot] = 4000 + Math.random() * 4000;
                size[slot] = 1 + Math.random() * 2.2;
                seed[slot] = Math.random() * 10;
            }
        }
        double dt = 1 / 60.0;
        GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, w, h);
        for (int i = alive - 1; i >= 0; i--) {
            life[i] -= dt * 1000;
            if (life[i] <= 0) {
                alive--;
                px[i] = px[alive];
                py[i] = py[alive];
                vx[i] = vx[alive];
                vy[i] = vy[alive];
                life[i] = life[alive];
                span[i] = span[alive];
                size[i] = size[alive];
                seed[i] = seed[alive];
                continue;
            }
            vy[i] -= 8 * dt;
            px[i] += (vx[i] + Math.sin(now / 900_000_000.0 + seed[i]) * 9) * dt;
            py[i] += vy[i] * dt;
            double fade = life[i] / span[i];
            double alpha = (dark ? 0.5 : 0.22) * Math.min(1, fade * 2);
            g.setFill(dark
                    ? Color.rgb(232, 150, 74, alpha)
                    : Color.rgb(200, 120, 50, alpha));
            double s = size[i] * (0.5 + fade * 0.5);
            g.fillOval(px[i], py[i], s, s);
        }
        if (alive == 0 && !ambient) {
            clock.stop();
            ticking = false;
        }
    }
}
