package flames;

import java.util.List;

/**
 * The complete, deterministic result of one FLAMES calculation.
 *
 * @param category         the surviving category
 * @param remainingCount   letters left after cross-cancellation
 * @param eliminationOrder letters removed in order (always 5 entries);
 *                         the UI replays this sequence for animation
 * @param displayName1     first name as typed (trimmed), for the result view
 * @param displayName2     second name as typed (trimmed), for the result view
 */
public record FlamesOutcome(
        FlamesCategory category,
        int remainingCount,
        List<Character> eliminationOrder,
        String displayName1,
        String displayName2) {

    public FlamesOutcome {
        eliminationOrder = List.copyOf(eliminationOrder);
    }
}
