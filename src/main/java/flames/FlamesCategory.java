package flames;

/**
 * The six FLAMES outcomes. Titles and meanings are the classic playground
 * categories; kept short because the result view shows them verbatim.
 */
public enum FlamesCategory {
    FRIENDS('F', "Friends", "A bond built on trust and laughter."),
    LOVE('L', "Love", "Sparks fly — this one is special."),
    AFFECTION('A', "Affection", "Warm feelings and fondness."),
    MARRIAGE('M', "Marriage", "Forever looks good on you two."),
    ENEMIES('E', "Enemies", "A legendary rivalry for the ages."),
    SIBLINGS('S', "Siblings", "Like family — bickering included.");

    private final char letter;
    private final String title;
    private final String meaning;

    FlamesCategory(char letter, String title, String meaning) {
        this.letter = letter;
        this.title = title;
        this.meaning = meaning;
    }

    public char letter() {
        return letter;
    }

    public String title() {
        return title;
    }

    public String meaning() {
        return meaning;
    }

    public static FlamesCategory fromLetter(char letter) {
        for (FlamesCategory category : values()) {
            if (category.letter == letter) {
                return category;
            }
        }
        throw new IllegalArgumentException("Not a FLAMES letter: " + letter);
    }
}
