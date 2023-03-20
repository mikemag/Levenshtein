import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public class LevenshteinNode implements Comparable<LevenshteinNode> {
    /** The word this node represents. */
    protected final String word;

    /** List of previous words. */
    private final ArrayList<LevenshteinNode> previous;

    /**
     * Constructs with this node with word initialized to word, and previous and next empty.
     * @param word Value to initialize word to.
     */
    protected LevenshteinNode(String word) {
        this.word = word;
        previous = new ArrayList<>();
    }

    /**
     * Adds all neighbors from the dictionary to a HashSet of neighbors and returns it.
     * Uses lengthStartIndexes to reduce the scope of the dictionary that is searched, since no words that have a
     * length difference of more than two can possibly be neighboring.
     * Also uses ignore to reduce the scope of the neighbors returned, as there is no reason to check neighbors
     * that have already been searched in Levenshtein
     * @param dictionary Dictionary to use.
     * @param lengthStartIndexes Map whose key is a word length and value is the first index of a word with that
     *                           length in dictionary.
     * @return HashSet of neighboring nodes.
     */
    public HashSet<LevenshteinNode> findNeighbors(LevenshteinNode[] dictionary, Map<Integer, Integer> lengthStartIndexes,
                                                                   HashSet<LevenshteinNode> ignore1, HashSet<LevenshteinNode> ignore2) {
        HashSet<LevenshteinNode> neighbors = new HashSet<>();
        int endIndex = lengthStartIndexes.getOrDefault(this.word.length() + 2, dictionary.length);
        for (int i = lengthStartIndexes.getOrDefault(this.word.length() - 1, 0); i < endIndex; i++) {
            if (this.isNeighboring(dictionary[i]) && !ignore2.contains(dictionary[i]) && !ignore1.contains(dictionary[i])) {
                dictionary[i].addPrevious(this);
                neighbors.add(dictionary[i]);
            }
        }
        return neighbors;
    }

    /**
     * Determines if this word is "neighboring" n (If a single addition, removal, or change of letters will result in w).
     * It does this by checking to see if both words share all but one letter, and these letters are in the same order in each word.
     * It can determine this by traversing this word and comparing the current letter to the front of n.
     * The front of w is pushed back if the letter comparison evaluates true or if the front letter does not need to be checked again.
     * This allows this method to have a worst-case big-O of O(n).
     * @param n Word to compare to, the distance between the lengths of these words must not be more than 1.
     * @return Whether this is neighboring n.
     */
    protected boolean isNeighboring(LevenshteinNode n) {
        String w1 = this.word;
        String w2 = n.word;

        /*if (w1.equals(w2)) {
            return false;
        }*/
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
            return foundDifference;
        }

        if (lengthDifference > 0) {
            String t = w2;
            w2 = w1;
            w1 = t;
            w1l = w2l;
        }

        int w2Index = 0;
        for (int i = 0; i < w1l; i++, w2Index++) {
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

    /** Returns the word this node represents. */
    public String getWord() {
        return word;
    }

    /** @param previous Node to add to this previous. */
    public void addPrevious(LevenshteinNode previous) {
        this.previous.add(previous);
    }

    /** @return HashSet of previous words */
    public ArrayList<LevenshteinNode> getPrevious() {
        return previous;
    }

    /** @return "[word] - [list of previous]" */

    public String toString() {
        return word + " - " + previous.size();
    }

    /** @return Output from calling compareTo between the two words. */
    @Override
    public int compareTo(LevenshteinNode o) {
        return this.word.compareTo(o.word);
    }

    /** @return Hashcode of the internal word. */
    @Override
    public int hashCode() {
        return Objects.hash(word);
    }
}
