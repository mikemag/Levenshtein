import java.io.*;
import java.util.*;

public class LazyDatabase extends LevenshteinDatabase {
    private final HashMap<Integer, Integer> lengthStartIndexes;

    public LazyDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);

        lengthStartIndexes = new HashMap<>();
        for (int i = 0; i < this.dictionary.length; i++) {
            lengthStartIndexes.putIfAbsent(this.dictionary[i].length(), i);
        }
    }

    @Override
    public boolean areNeighbors(int wordIndex1, int wordIndex2) {
        return areNeighboring(this.wordAt(wordIndex1), this.wordAt(wordIndex2));
    }

    @Override
    public HashSet<Integer> findNeighbors(int wordIndex) {
        HashSet<Integer> neighbors = new HashSet<>();

        String w = this.wordAt(wordIndex);
        int endIndex = lengthStartIndexes.getOrDefault(w.length() + 2, dictionary.length);
        // Reduces the searching scope to only words with a length that allows them to be neighboring w
        for (int i = lengthStartIndexes.getOrDefault(w.length() - 1, 0); i < endIndex; i++) {
            // Ensures that neighbors already in the graph will not be added
            if (areNeighboring(w, this.dictionary[i])) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    protected static boolean areNeighboring(String w1, String w2) {
        int w1l = w1.length();
        int w2l = w2.length();
        int lengthDifference = w1l - w2l;
        boolean foundDifference = false;

        if (lengthDifference == 0) {
            for (int i = 0; i < w1l; i++) {
                if (w1.charAt(i) != w2.charAt(i)) {
                    if (foundDifference) {
                        return false;
                    } else {
                        foundDifference = true;
                    }
                }
            }
            // If a difference was never found, the words are equal, and false is still returned
            return foundDifference;
        }

        // The next part requires word1 to be shorter than word2,
        // which is why they need to be swapped
        if (lengthDifference > 0) {
            String t = w2;
            w2 = w1;
            w1 = t;
            w1l = w2l;
        }

        for (int i = 0, w2Index = 0; i < w1l; i++, w2Index++) {
            if (w1.charAt(i) != w2.charAt(w2Index)) {
                if (foundDifference) {
                    return false;
                } else {
                    foundDifference = true;
                    i--;
                }
            }
        }
        return true;
    }
};
