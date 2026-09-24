import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * The single source of truth for UI sound effects. Synthesizes tiny 16-bit
 * mono WAVs with pure Java — no recordings, no downloads, no licensing
 * questions. Regenerate with:
 *
 *   javac -d mk tools/SoundGenerator.java
 *   java -cp mk SoundGenerator src/main/resources/assets/sound
 */
public final class SoundGenerator {

    private static final int RATE = 44100;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("usage: SoundGenerator <output-dir>");
        }
        File out = new File(args[0]);
        if (!out.isDirectory() && !out.mkdirs()) {
            throw new java.io.IOException("cannot create " + out);
        }
        write(new File(out, "click.wav"), blip(1800, 30));
        write(new File(out, "tick.wav"), blip(1200, 40));
        write(new File(out, "pop.wav"), sweep(300, 900, 90));
        write(new File(out, "error.wav"), blip(140, 160));
        write(new File(out, "fanfare.wav"), concat(
                note(523, 90), note(659, 90), note(784, 90), note(1047, 220)));
        System.out.println("sounds written to " + out);
    }

    /** Pure sine blip with exponential decay. */
    private static byte[] blip(double freq, int ms) {
        return tone(new double[]{freq}, new double[]{1.0}, ms);
    }

    /** One piano-ish note (fundamental + soft octave). */
    private static byte[] note(double freq, int ms) {
        return tone(new double[]{freq, freq * 2}, new double[]{1.0, 0.3}, ms);
    }

    private static byte[] tone(double[] freqs, double[] amps, int ms) {
        int n = ms * RATE / 1000;
        byte[] pcm = new byte[n * 2];
        for (int i = 0; i < n; i++) {
            double t = (double) i / RATE;
            double sample = 0;
            for (int k = 0; k < freqs.length; k++) {
                sample += amps[k] * Math.sin(2 * Math.PI * freqs[k] * t);
            }
            double decay = Math.exp(-4.0 * i / n);
            int v = (int) (sample * decay * 12000);
            pcm[2 * i] = (byte) (v & 0xFF);
            pcm[2 * i + 1] = (byte) ((v >>> 8) & 0xFF);
        }
        return pcm;
    }

    private static byte[] sweep(double from, double to, int ms) {
        int n = ms * RATE / 1000;
        byte[] pcm = new byte[n * 2];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            phase += 2 * Math.PI * (from + (to - from) * i / n) / RATE;
            int v = (int) (Math.sin(phase) * Math.exp(-3.0 * i / n) * 12000);
            pcm[2 * i] = (byte) (v & 0xFF);
            pcm[2 * i + 1] = (byte) ((v >>> 8) & 0xFF);
        }
        return pcm;
    }

    private static byte[] concat(byte[]... parts) throws Exception {
        try (ByteArrayOutputStream all = new ByteArrayOutputStream()) {
            for (byte[] part : parts) {
                all.write(part);
            }
            return all.toByteArray();
        }
    }

    private static void write(File file, byte[] pcm) throws Exception {
        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
            out.writeBytes("RIFF");
            writeIntLE(out, 36 + pcm.length);
            out.writeBytes("WAVEfmt ");
            writeIntLE(out, 16);
            writeShortLE(out, 1); // PCM
            writeShortLE(out, 1); // mono
            writeIntLE(out, RATE);
            writeIntLE(out, RATE * 2);
            writeShortLE(out, 2); // block align
            writeShortLE(out, 16); // bits
            out.writeBytes("data");
            writeIntLE(out, pcm.length);
            out.write(pcm);
        }
        double seconds = (double) pcm.length / (RATE * 2);
        System.out.println(file.getName() + " " + file.length()
                + " bytes, " + Math.round(seconds * 1000) + " ms");
    }

    private static void writeShortLE(DataOutputStream out, int v) throws Exception {
        out.write(v & 0xFF);
        out.write((v >>> 8) & 0xFF);
    }

    private static void writeIntLE(DataOutputStream out, int v) throws Exception {
        for (int i = 0; i < 4; i++) {
            out.write((v >>> (8 * i)) & 0xFF);
        }
    }
}
