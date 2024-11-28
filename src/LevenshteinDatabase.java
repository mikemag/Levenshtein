import java.io.*;
import java.util.*;

public abstract class LevenshteinDatabase {
    public final String[] dictionary;

    protected LevenshteinDatabase(String dictionarySourcePath) throws FileNotFoundException {
        dictionary = makeDictionary(dictionarySourcePath);
    }

    /**
     * Reads a file at the specified location and returns a word
     * list generated reading the file. 
     *
     * @param sourcepath path to the file containing the word list.
     *                   This file must be sorted sequentially with
     *                   alphabetical order being the tiebreaker,
     *                   contain one and only one word per line,
     *                   and contain only lowercase letters
     * @return the generated dictionary
     */
    public static String[] makeDictionary(String sourcepath) throws FileNotFoundException {
        ArrayList<String> dictionaryList = new ArrayList();
        Scanner source = new Scanner(new File(sourcepath));

        while(source.hasNext()) {
            dictionaryList.add(source.next());
        }
        source.close();

        return dictionaryList.toArray(new String[0]);
    }

    /**
     * Returns the set all neighbors of a word, excluding any
     * words in the blacklist.
     * The blacklist is useful for ignoring a list of already-
     * processed words, as an optimal Levenshtein path should never
     * loop back on itself.
     *
     * @param word the word to find the neighbors of
     * @param blacklist set of words to ignore
     * @return the set of neighbors
     */
    public abstract HashSet<String> findNeighbors(String word, HashSet<String> blacklist);

    /**
     * Returns true if and only if two words are neighboring.
     * While this shouldn't be needed for good Levenshtein
     * algorithms, it has been left as public.
     *
     * @param word1 the first word
     * @param word2 the the second word
     * @return if the neighbors are neighboring
     */
    public abstract boolean areNeighbors(String word1, String word2);
}
