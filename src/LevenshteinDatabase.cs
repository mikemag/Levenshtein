public abstract class LevenshteinDatabase {
    public readonly String[] dictionary;

    protected LevenshteinDatabase(String dictionarySourcePath) {
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
    public static String[] makeDictionary(String sourcepath) {
        return File.ReadAllLines(sourcepath);
    }

    /**
     * Returns the set of all neighbors of a word in an array.
     *
     * @param wordIndex the index of the word in the dictionary
     * @return an array of the neighbors neighbors
     */
    public abstract int[] findNeighbors(int wordIndex);

    /**
     * Returns true if and only if two words are neighboring.
     * While this shouldn't be needed for good Levenshtein
     * algorithms, it has been left as public.
     *
     * @param wordIndex1 the first word index
     * @param wordIndex2 the second word index
     * @return if the neighbors are neighboring
     */
    public abstract bool areNeighbors(int wordIndex1, int wordIndex2);

    /**
     * Finds the word at an index in the dictionary.
     *
     * @param wordIndex the index
     * @return the word associated with it
     */
    public String wordAt(int wordIndex) {
        return dictionary[wordIndex];
    }

    /**
     * Finds the index associated with a word using binary
     * search.
     *
     * @param word the word
     * @return the index associated with it
     */
    public int getWordIndex(String word) {
        return Array.BinarySearch(dictionary, word, COMPARE_WORDS);
    }

    /**
     * Compares words in the dictionary based first on their
     * length then their letter. This can be used for searching
     * or generating the dictionary.
     */
    public static readonly Comparer<String> COMPARE_WORDS = Comparer<String>.Create((s1, s2) => {
        int c = s1.Length - s2.Length;

        if (c == 0) {
            return s1.CompareTo(s2);
        } else {
            return c;
        }
    });
}
