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
    void countOfOneFallsStraightThroughToSiblings() {
        FlamesOutcome outcome = FlamesEngine.calculate("a", "ab");
        assertEquals(1, outcome.remainingCount());
        assertEquals(FlamesCategory.SIBLINGS, outcome.category());
        assertEquals(List.of('F', 'L', 'A', 'M', 'E'), outcome.eliminationOrder());
    }

    @Test
    void invariantsHoldAcrossManyPairs() {
        String[] names = {"romeo", "juliet", "a", "ab", "abc", "anna",
                "Christopher", "Jo", "x", "yz", "FLAMES", "qwerty"};
        for (String first : names) {
            for (String second : names) {
                FlamesOutcome outcome = FlamesEngine.calculate(first, second);
                assertEquals(5, outcome.eliminationOrder().size());
                assertTrue(!outcome.eliminationOrder().contains(outcome.category().letter()));
                assertEquals(outcome.category(),
                        FlamesEngine.calculate(first, second).category());
            }
        }
    }

    @Test
    void categoriesAreCompleteAndDescribed() {
        StringBuilder letters = new StringBuilder();
        for (FlamesCategory category : FlamesCategory.values()) {
            letters.append(category.letter());
            assertTrue(!category.title().isBlank());
            assertTrue(!category.meaning().isBlank());
        }
        assertEquals("FLAMES", letters.toString());
    }

    @Test
    void longAndMessyInputsStillCompute() {
        String first = "Ab".repeat(250) + "  !! \t";
        String second = "\n zy!!".repeat(100);
        FlamesOutcome outcome = FlamesEngine.calculate(first, second);
        assertEquals(5, outcome.eliminationOrder().size());
        assertEquals(first.strip(), outcome.displayName1());
    }

    @Test
    void cancellationOrderPairsSharedLettersDeterministically() {
        assertEquals(List.of((int) 'j', (int) 'n'),
                FlamesEngine.cancellationOrder("john", "jane"));
        assertEquals(List.of((int) 'a', (int) 'a', (int) 'n', (int) 'n'),
                FlamesEngine.cancellationOrder("anna", "anna"));
        assertEquals(List.of(), FlamesEngine.cancellationOrder("abc", "def"));
    }

    @Test
    void cancellationOrderMatchesRemainingCount() {
        String[][] pairs = {{"romeo", "juliet"}, {"aabb", "ab"}, {"anna", "anna"}};
        for (String[] pair : pairs) {
            int len1 = FlamesEngine.normalize(pair[0]).length();
            int len2 = FlamesEngine.normalize(pair[1]).length();
            int count = FlamesEngine.remainingCount(pair[0], pair[1]);
            assertEquals((len1 + len2 - count) / 2,
                    FlamesEngine.cancellationOrder(pair[0], pair[1]).size());
        }
    }

    @Test
    void everyCategoryResolvesFromItsLetter() {
        for (FlamesCategory category : FlamesCategory.values()) {
            assertEquals(category, FlamesCategory.fromLetter(category.letter()));
        }
        assertThrows(IllegalArgumentException.class, () -> FlamesCategory.fromLetter('X'));
    }
}
