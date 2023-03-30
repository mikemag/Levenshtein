/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: Levenshtein.java
Maintenance Log:
    Started. Added all the necessary collection such as the dictionary and lengthStartIndexes, all abstract methods such as generatePaths,
        and also added a constructor and a static binary search method (19 Mar 2023 23:09)
    Added methods to replace some of those in LevenshteinNode (22 Mar 2023 10:57)
    Changed dictionary to contain Strings instead of LevenshteinNodes (23 Mar 2023 10:57)
    Moved the generating of dictionary into the constructor here. Also made the constructor sort the dictionary (29 Mar 2023 22:53)
*/

import java.io.*;
import java.nio.file.*;
import java.util.*;

public abstract class Levenshtein {
    /** Set to true to display extra text for debugging. */
    protected static final boolean PRINT_EXTRA = false;

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
     * Reads a dictionary from a file, storing each word into the array, then sorting it, then determining lengthStartIndexes.
     * @param filepath Name of the file to read from dictionary to.
     * @throws IOException
     */
    public Levenshtein(String filepath) throws IOException {
        dictionary = new String[(int) Files.lines(Paths.get("src/Dictionary.txt")).count()];
        lengthStartIndexes = new HashMap<>();
        Scanner s = new Scanner(new File(filepath));
        for (int i = 0; s.hasNext(); i++) {
            dictionary[i] = s.next();
        }
        MergeSort.sort(dictionary, COMPARE_BY_LENGTH);
        for (int i = 0; i < dictionary.length; i++) {
            lengthStartIndexes.putIfAbsent(dictionary[i].length(), i);
        }
    }

    /**
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between w1 and w2
     */
    protected abstract TreeSet<LinkedList<String>> generatePaths(String w1, String w2, long startTime);

    /**
     * Comparator which sorts strings first by length then their natural ordering, which is useful for sorting the dictionary to avoid
     * searching words that cannot be neighboring or when performing binary search.
     */
    private static final Comparator<String> COMPARE_BY_LENGTH = (o1, o2) -> {
        int c = o1.length() - o2.length();
        if (c == 0) {
            return o1.compareTo(o2);
        } else {
            return c;
        }
    };
}