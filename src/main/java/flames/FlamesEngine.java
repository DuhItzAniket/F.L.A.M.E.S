package flames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pure FLAMES domain logic. No JavaFX imports — safe to test headless.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Normalize: lowercase (ROOT locale), keep Unicode letters only.
 *       Spaces, punctuation, digits and symbols are dropped.</li>
 *   <li>Cancel: remaining count = sum over each letter of
 *       |count in name 1 − count in name 2|.</li>
 *   <li>Eliminate: circular count over F·L·A·M·E·S removing every
 *       step-th letter until one survives. A count of 0 (names fully
 *       cancel, e.g. identical names) wraps to a full 6-cycle.</li>
 * </ol>
 *
 * <p>The removal order is recorded so the UI can animate the exact
 * calculation instead of faking one.
 */
public final class FlamesEngine {

    private static final List<Character> LETTERS = List.of('F', 'L', 'A', 'M', 'E', 'S');

    private FlamesEngine() {
    }

    /** Lowercases the name and keeps letters only. Never returns null. */
    public static String normalize(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        name.toLowerCase(Locale.ROOT).codePoints()
                .filter(Character::isLetter)
                .forEach(letters::appendCodePoint);
        return letters.toString();
    }

    /** Letters left after cross-cancellation of the two normalized names. */
    public static int remainingCount(String name1, String name2) {
        int remaining = 0;
        for (int diff : frequencies(normalize(name1), normalize(name2)).values()) {
            remaining += Math.abs(diff);
        }
        return remaining;
    }

    /**
     * One entry per cancelled pair, in deterministic order: distinct shared
     * letters by first appearance in the first name, each repeated as many
     * times as it pairs up. The UI pops one pair per entry, so
     * {@code size() == (len1 + len2 - remainingCount) / 2}.
     * Non-BMP letters are counted but never listed (they cannot pair
     * visually); this is documented, not silent.
     */
    public static List<Integer> cancellationOrder(String name1, String name2) {
        String first = normalize(name1);
        String second = normalize(name2);
        // Shared letters by first appearance in the first name.
        List<Integer> distinct = new ArrayList<>();
        Map<Integer, Integer> left = new HashMap<>();
        Map<Integer, Integer> right = new HashMap<>();
        first.codePoints().forEach(cp -> left.merge(cp, 1, Integer::sum));
        second.codePoints().forEach(cp -> right.merge(cp, 1, Integer::sum));
        first.codePoints().filter(cp -> right.getOrDefault(cp, 0) > 0).forEach(cp -> {
            if (!distinct.contains(cp)) {
                distinct.add(cp);
            }
        });
        List<Integer> pops = new ArrayList<>();
        for (int cp : distinct) {
            int pairs = Math.min(left.get(cp), right.get(cp));
            for (int i = 0; i < pairs && cp <= 0xFFFF; i++) {
                pops.add(cp);
            }
        }
        return List.copyOf(pops);
    }

    private static Map<Integer, Integer> frequencies(String first, String second) {
        Map<Integer, Integer> frequencies = new HashMap<>();
        first.codePoints().forEach(cp -> frequencies.merge(cp, 1, Integer::sum));
        second.codePoints().forEach(cp -> frequencies.merge(cp, -1, Integer::sum));
        return frequencies;
    }

    /**
     * Runs the full calculation.
     *
     * @throws IllegalArgumentException if either name is empty after
     *         normalization (blank, punctuation-only, digits-only, …)
     */
    public static FlamesOutcome calculate(String name1, String name2) {
        String first = normalize(name1);
        String second = normalize(name2);
        if (first.isEmpty() || second.isEmpty()) {
            throw new IllegalArgumentException(emptyInputMessage(first.isEmpty(), second.isEmpty()));
        }

        int count = remainingCount(name1, name2);
        List<Round> rounds = eliminationRounds(count);
        List<Character> removed = new ArrayList<>();
        for (Round round : rounds) {
            removed.add(round.removed());
        }
        char survivor = rounds.get(rounds.size() - 1).remaining().charAt(0);
        return new FlamesOutcome(
                FlamesCategory.fromLetter(survivor),
                count,
                removed,
                name1.strip(),
                name2.strip());
    }

    /** One elimination round, for the recap table and the animation. */
    public record Round(String inPlay, char removed, String remaining) {
    }

    /**
     * The full elimination sequence for a count (0 wraps to a full cycle,
     * same rule as {@link #calculate}). Always 5 rounds for F·L·A·M·E·S.
     */
    public static List<Round> eliminationRounds(int count) {
        List<Character> ring = new ArrayList<>(LETTERS);
        int step = count == 0 ? ring.size() : count;
        List<Round> rounds = new ArrayList<>();
        int index = 0;
        while (ring.size() > 1) {
            String inPlay = letters(ring);
            index = (index + step - 1) % ring.size();
            char removed = ring.remove(index);
            rounds.add(new Round(inPlay, removed, letters(ring)));
        }
        return List.copyOf(rounds);
    }

    /**
     * Bond meter 0–100: share of letters that paired up across both names.
     * Identical names score 100, nothing in common scores 0.
     */
    public static int bondPercent(String name1, String name2) {
        String first = normalize(name1);
        String second = normalize(name2);
        int total = first.codePointCount(0, first.length())
                + second.codePointCount(0, second.length());
        if (total == 0) {
            return 0;
        }
        return (int) Math.round(
                200.0 * cancellationOrder(name1, name2).size() / total);
    }

    private static String letters(List<Character> ring) {
        StringBuilder text = new StringBuilder();
        for (char letter : ring) {
            text.append(letter);
        }
        return text.toString();
    }

    private static String emptyInputMessage(boolean firstEmpty, boolean secondEmpty) {
        if (firstEmpty && secondEmpty) {
            return "Enter two names using letters to play.";
        }
        return firstEmpty
                ? "The first name has no letters in it."
                : "The second name has no letters in it.";
    }
}
