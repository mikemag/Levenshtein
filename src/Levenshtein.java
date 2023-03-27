/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: Levenshtein.java
Maintenance Log:
    Started. Added all the necessary collection such as the dictionary and lengthStartIndexes, all abstract methods such as generatePaths,
        and also added a constructor and a static binary search method (19 Mar 2023 23:09)
    Added methods to replace some of those in LevenshteinNode (22 Mar 2023 10:57)
    Changed dictionary to contain Strings instead of LevenshteinNodes (23 Mar 2023 10:57)
*/

import java.util.*;

public abstract class Levenshtein {
    /** Set to true to display extra text for debugging. */
    protected static final boolean PRINT_EXTRA = true;

    /**
     * Dictionary of words, which is read from a file and stored as an array of LevenshteinNodes with previous being empty.
     * This allows for each call of generatePaths to add.
     */
    protected final String[] dictionary;

    /**
     * The key represents the length of a word, and the value is the first index in dictionary of a word of that length.
     */
    protected final Map<Integer, Integer> lengthStartIndexes;

    /**
     * Reads a dictionary from a file, storing each word as a LevenshteinNode with previous being empty.
     * @param dictionary Value to set this dictionary to.
     * @param lengthStartIndexes Value to set this lengthStartIndexes to.
     */
    protected Levenshtein(String[] dictionary, Map<Integer, Integer> lengthStartIndexes) {
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
    protected abstract ArrayList<ArrayList<String>> generatePaths(String w1, String w2, long startTime);

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
}