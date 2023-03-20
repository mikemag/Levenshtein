import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

public abstract class LevenshteinNode<T extends LevenshteinNode> implements Comparable<LevenshteinNode> {
    /** The word this node represents. */
    protected final String word;

    /**
     * Constructs with this node with word initialized to word, and previous and next empty.
     * @param word Value to initialize word to.
     */
    protected LevenshteinNode(String word) {
        this.word = word;
    }

    /**
     * @param dictionary Dictionary to use.
     * @param lengthStartIndexes Map whose key is a word length and value is the first index of a word with that
     *                           length in dictionary.
     * @return HashSet of neighboring nodes.
     */
    public abstract HashSet<T> findNeighbors(T[] dictionary, Map<Integer, Integer> lengthStartIndexes,
                                                                   HashSet<T> ignore1, HashSet<T> ignore2);

    /**
     * Determines if this word is "neighboring" n (If a single addition, removal, or change of letters will result in w).
     * It does this by checking to see if both words share all but one letter, and these letters are in the same order in each word.
     * It can determine this by traversing this word and comparing the current letter to the front of n.
     * The front of w is pushed back if the letter comparison evaluates true or if the front letter does not need to be checked again.
     * This allows this method to have a worst-case big-O of O(n).
     * @param n Word to compare to, the distance between the lengths of these words must not be more than 1.
     * @return Whether this is neighboring n.
     */
    protected boolean isNeighboring(T n) {
        String w = n.getWord();
        if (this.word.equals(w)) {
            return false;
        }
        boolean foundDifference = false;
        int wIndex = 0;
        for (int i = 0; i < this.word.length() && wIndex < w.length(); i++) {
            if (this.word.charAt(i) == w.charAt(wIndex)) {
                wIndex++;
            } else {
                if (foundDifference) {
                    return false;
                } else {
                    foundDifference = true;
                    int lengthDifference = this.word.length() - w.length();
                    if (lengthDifference < 0) {
                        wIndex++;
                        i--;
                    } else if (lengthDifference == 0) {
                        wIndex++;
                    }
                }
            }
        }
        return true;
    }

    /** Returns the word this node represents. */
    public String getWord() {
        return word;
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
