package flames;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Local game history, newest first, capped at 50. A plain TSV file under
 * the user home — no database, no network, human-readable. All IO is
 * best-effort: a broken history never breaks the game.
 */
public final class HistoryStore {

    static final int CAP = 50;

    /** One played game. {@code at} is an ISO instant. */
    public record Entry(String at, String name1, String name2,
            char category, int count, boolean manual) {
    }

    private final Path file;

    public HistoryStore() {
        this(Path.of(System.getProperty("user.home"), ".flames", "history.tsv"));
    }

    HistoryStore(Path file) {
        this.file = file;
    }

    public synchronized List<Entry> load() {
        List<Entry> entries = new ArrayList<>();
        List<String> lines;
        try {
            if (!Files.isRegularFile(file)) {
                return entries;
            }
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            return entries;
        }
        for (String line : lines) {
            Entry entry = parse(line);
            if (entry != null) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public synchronized void add(Entry entry) {
        List<Entry> entries = load();
        entries.add(0, entry);
        while (entries.size() > CAP) {
            entries.remove(entries.size() - 1);
        }
        write(entries);
    }

    public synchronized void clear() {
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // Best effort; the list view just shows what load() returns.
        }
    }

    private void write(List<Entry> entries) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            List<String> lines = new ArrayList<>();
            for (Entry entry : entries) {
                lines.add(String.join("\t",
                        entry.at(),
                        clean(entry.name1()),
                        clean(entry.name2()),
                        String.valueOf(entry.category()),
                        String.valueOf(entry.count()),
                        entry.manual() ? "manual" : "auto"));
            }
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, file, java.nio.file.StandardCopyOption.ATOMIC_MOVE,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException notAtomic) {
                Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // Best effort.
        }
    }

    private static Entry parse(String line) {
        String[] parts = line.split("\t", -1);
        if (parts.length != 6 || parts[0].isBlank() || parts[1].isBlank()
                || parts[2].isBlank() || parts[3].length() != 1
                || "FLAMES".indexOf(parts[3].charAt(0)) < 0) {
            return null;
        }
        int count;
        try {
            count = Integer.parseInt(parts[4]);
        } catch (NumberFormatException bad) {
            return null;
        }
        if (count < 0) {
            return null;
        }
        return new Entry(parts[0], parts[1], parts[2], parts[3].charAt(0),
                count, "manual".equals(parts[5]));
    }

    private static String clean(String name) {
        String flat = name.replaceAll("[\\t\\r\\n|]", " ").strip();
        int codepoints = flat.codePointCount(0, flat.length());
        if (codepoints <= 60) {
            return flat;
        }
        return flat.substring(0, flat.offsetByCodePoints(0, 60));
    }
}
