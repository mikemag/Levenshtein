/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: Levenshtein.java
Maintenance Log:
    Started. Added all the necessary collection such as the dictionary and lengthStartIndexes, all abstract methods such as generatePaths,
        and also added a constructor and a static binary search method (19 Mar 2023 23:09)
*/

import java.util.*;

public abstract class Levenshtein<T extends LevenshteinNode> {
    /** Set to true to display extra text for debugging. */
    protected static final boolean PRINT_EXTRA = false;

    /**
     * Dictionary of words, which is read from a file and stored as an array of LevenshteinNodes with previous being empty.
     * This allows for each call of generatePaths to add.
     */
    protected final T[] dictionary;

    /**
     * The key represents the length of a word, and the value is the first index in dictionary of a word of that length.
     */
    protected final Map<Integer, Integer> lengthStartIndexes;

    /**
     * Reads a dictionary from a file, storing each word as a LevenshteinNode with previous being empty.
     * @param dictionary Value to set this dictionary to.
     * @param lengthStartIndexes Value to set this lengthStartIndexes to.
     */
    protected Levenshtein(T[] dictionary, Map<Integer, Integer> lengthStartIndexes) {
        this.dictionary = dictionary;
        this.lengthStartIndexes = lengthStartIndexes;
    }

    /**
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return An array which contains a modified version of dictionary with every path between the words found and appropriate pointers stored in each node.
     *         This only generates the information required to find the paths - It does not directly tell you what the paths are.
     */
    protected abstract T[] generatePaths(String w1, String w2, long startTime);

    /**
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @return Levenshtein distance between the two words.
     */
    public abstract int getDistance(String w1, String w2);

    /**
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @return Returns a list of strings, with each string being a representation of a path between the two words, with arrows between words, denoting where a change occurred.
     */
    public abstract List<String> getAllPaths (String w1, String w2);

    /**
     * Comparator which sorts strings first by length then their natural ordering, which is useful for sorting the dictionary to avoid
     * searching words that cannot be neighboring or when performing binary search.
     */
    private static final Comparator<String> SORTED_BY_LENGTH = (o1, o2) -> {
        int c = o1.length() - o2.length();
        if (c == 0) {
            return o1.compareTo(o2);
        } else {
            return c;
        }
    };

    /**
     * Uses a recursive implementation of binarySearch to find the index of w in a.
     * Used to get a pointer to an important word (Such as the start word, end word, or words required to trace paths).
     * @param a Array to search. Must be sorted in the natural sorting order of Strings.
     * @param w Word to search for. Must not override compareTo.
     * @return The pointer to the node that corresponds to w in a.
     * @throws Exception Throws if w is not found in array.
     */
    public static LevenshteinNode binarySearch(LevenshteinNode[] a, String w) {
        return binarySearch(a, w, 0, a.length);
    }
    private static LevenshteinNode binarySearch(LevenshteinNode[] a, String w, int min, int max) {
        int average = (min + max) / 2 + (min + max) % 2;
        int c = SORTED_BY_LENGTH.compare(a[average].getWord(), w);
        if (max - min != 2) {
            System.out.println(max - min);
            System.out.println(a[average].getWord());
            if (c > 0) {
                return binarySearch(a, w, min, average);
            } else {
                return binarySearch(a, w, average, max);
            }
        } else {
            if (c == 0) {
                return a[average];
            }
            System.out.println(w + " does not exist in the provided array.");
            System.out.println("Last test word: " + a[average].getWord());
            return null;
        }
    }
}