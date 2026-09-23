package flames;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlamesEngineTest {

    @Test
    void normalizeLowercasesAndDropsNonLetters() {
        assertEquals("john", FlamesEngine.normalize("  JoHn! "));
        assertEquals("jose", FlamesEngine.normalize("J0o-s_e+1"));
        assertEquals("", FlamesEngine.normalize("  !!! 123 "));
        assertEquals("", FlamesEngine.normalize(null));
    }

    @Test
    void normalizeKeepsUnicodeLetters() {
        assertEquals("zo\u00eb", FlamesEngine.normalize("Zo\u00eb"));
        assertEquals("m\u00f6ller", FlamesEngine.normalize("M\u00f6ller"));
    }

    @Test
    void remainingCountCancelsSharedLetters() {
        // john vs jane: j and n cancel -> o, h, a, e remain = 4
        assertEquals(4, FlamesEngine.remainingCount("john", "jane"));
        assertEquals(0, FlamesEngine.remainingCount("anna", "anna"));
        assertEquals(0, FlamesEngine.remainingCount("ALICE", "alice"));
    }

    @Test
    void remainingCountHandlesRepeatsAndCase() {
        // aabb vs ab: a: 2-1, b: 2-1 -> 2
        assertEquals(2, FlamesEngine.remainingCount("aabb", "ab"));
        assertEquals(4, FlamesEngine.remainingCount("John", "JANE"));
    }

    @Test
    void knownOutcomeJohnJaneIsEnemies() {
        FlamesOutcome outcome = FlamesEngine.calculate("john", "jane");
        assertEquals(FlamesCategory.ENEMIES, outcome.category());
        assertEquals(4, outcome.remainingCount());
        assertEquals(List.of('M', 'L', 'F', 'A', 'S'), outcome.eliminationOrder());
    }

    @Test
    void identicalNamesWrapToFullCycleAndMarry() {
        FlamesOutcome outcome = FlamesEngine.calculate("anna", "anna");
        assertEquals(0, outcome.remainingCount());
        assertEquals(FlamesCategory.MARRIAGE, outcome.category());
        assertEquals(5, outcome.eliminationOrder().size());
    }

    @Test
    void eliminationOrderAlwaysRemovesFiveAndSurvivorStandsApart() {
        String[][] pairs = {{"romeo", "juliet"}, {"a", "b"}, {"abc", "defghi"}, {"anna", "anna"}};
        for (String[] pair : pairs) {
            FlamesOutcome outcome = FlamesEngine.calculate(pair[0], pair[1]);
            assertEquals(5, outcome.eliminationOrder().size(), pair[0] + "/" + pair[1]);
            assertTrue(!outcome.eliminationOrder().contains(outcome.category().letter()));
        }
    }

    @Test
    void calculationIsDeterministic() {
        assertEquals(
                FlamesEngine.calculate("romeo", "juliet").category(),
                FlamesEngine.calculate("Romeo!!", "  JULIET ").category());
    }

    @Test
    void resultKeepsDisplayNamesAsTyped() {
        FlamesOutcome outcome = FlamesEngine.calculate("  Romeo ", "Juliet");
        assertEquals("Romeo", outcome.displayName1());
        assertEquals("Juliet", outcome.displayName2());
    }

    @Test
    void blankNamesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> FlamesEngine.calculate("", "jane"));
        assertThrows(IllegalArgumentException.class, () -> FlamesEngine.calculate("   ", "jane"));
        assertThrows(IllegalArgumentException.class, () -> FlamesEngine.calculate("john", "!!!"));
        assertThrows(IllegalArgumentException.class, () -> FlamesEngine.calculate("123", "456"));
        assertThrows(IllegalArgumentException.class, () -> FlamesEngine.calculate(null, null));
    }

    @Test
    void errorMessagesNameTheOffendingInput() {
        IllegalArgumentException first = assertThrows(
                IllegalArgumentException.class, () -> FlamesEngine.calculate("", "jane"));
        assertTrue(first.getMessage().toLowerCase().contains("first"));
        IllegalArgumentException second = assertThrows(
                IllegalArgumentException.class, () -> FlamesEngine.calculate("john", ""));
        assertTrue(second.getMessage().toLowerCase().contains("second"));
    }

    @Test
    void everyCategoryResolvesFromItsLetter() {
        for (FlamesCategory category : FlamesCategory.values()) {
            assertEquals(category, FlamesCategory.fromLetter(category.letter()));
        }
        assertThrows(IllegalArgumentException.class, () -> FlamesCategory.fromLetter('X'));
    }
}
