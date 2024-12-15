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
     * Returns the set of all neighbors of a word in an array.
     *
     * @param wordIndex the index of the word in the dictionary
     * @return an array of the neighbors neighbors
     */
    public abstract Integer[] findNeighbors(int wordIndex);

    /**
     * Returns true if and only if two words are neighboring.
     * While this shouldn't be needed for good Levenshtein
     * algorithms, it has been left as public.
     *
     * @param wordIndex1 the first word index
     * @param wordIndex2 the second word index
     * @return if the neighbors are neighboring
     */
    public abstract boolean areNeighbors(int wordIndex1, int wordIndex2);

    /**
     * Finds the word at an index in the dictionary.
     *
     * @param wordIndex the index
     * @return the word associated with it
     */
    public final String wordAt(int wordIndex) {
        return dictionary[wordIndex];
    }

    /**
     * Finds the index associated with a word using binary
     * search.
     *
     * @param word the word
     * @return the index associated with it
     */
    public final int getWordIndex(String word) {
        return Arrays.binarySearch(dictionary, word, COMPARE_WORDS);
    }

    /**
     * Compares words in the dictionary based first on their
     * length then their letter. This can be used for searching
     * or generating the dictionary.
     */
    public static final Comparator<String> COMPARE_WORDS = (o1, o2) -> {
        int c = o1.length() - o2.length();

        if (c == 0) {
            return o1.compareTo(o2);
        } else {
            return c;
        }
    };
}
