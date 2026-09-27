package flames;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryStoreTest {

    @TempDir
    Path dir;

    private HistoryStore store() {
        return new HistoryStore(dir.resolve("history.tsv"));
    }

    private HistoryStore.Entry entry(String name1, String name2) {
        return new HistoryStore.Entry(Instant.now().toString(), name1, name2, 'M', 6, false);
    }

    @Test
    void emptyUntilPlayed() {
        assertTrue(store().load().isEmpty());
    }

    @Test
    void addLoadsNewestFirst() {
        HistoryStore store = store();
        store.add(entry("romeo", "juliet"));
        store.add(entry("anna", "anna"));
        List<HistoryStore.Entry> entries = store.load();
        assertEquals(2, entries.size());
        assertEquals("anna", entries.get(0).name1());
        assertEquals("romeo", entries.get(1).name1());
        assertEquals('M', entries.get(0).category());
    }

    @Test
    void capsAtFifty() {
        HistoryStore store = store();
        for (int i = 0; i < 60; i++) {
            store.add(entry("a" + i, "b" + i));
        }
        List<HistoryStore.Entry> entries = store.load();
        assertEquals(HistoryStore.CAP, entries.size());
        assertEquals("a59", entries.get(0).name1());
    }

    @Test
    void clearEmpties() {
        HistoryStore store = store();
        store.add(entry("romeo", "juliet"));
        store.clear();
        assertTrue(store.load().isEmpty());
    }

    @Test
    void corruptLinesAreSkipped() throws IOException {
        Path file = dir.resolve("history.tsv");
        Files.writeString(file, "garbage\n"
                + Instant.now() + "\tromeo\tjuliet\tM\t6\tauto\n"
                + Instant.now() + "\tromeo\tjuliet\tX\t6\tauto\n"
                + Instant.now() + "\tromeo\tjuliet\tM\t-6\tauto\n"
                + "a\tb\n");
        List<HistoryStore.Entry> entries = store().load();
        assertEquals(1, entries.size());
        assertEquals("romeo", entries.get(0).name1());
    }
}
