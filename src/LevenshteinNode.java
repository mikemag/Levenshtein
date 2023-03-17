/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: LevenshteinNode.java
Maintenance Log:
    Started. Already had ideas as to how I was going to implement isNeighboring (13 Mar 2023 21:50)
        Wrote isNeighboring and confirmed that it works, no matter what operation is being done (22:19)
        Added getWord, getNeighbors, and addNeighbor. Also wrote comments for each method (22:54)
        Added findNeighbors (Pi Day 0:33)
        Added toString (Pi Day, 0:50)
        Added the condition to isNeighboring to make sure it doesn't incorrectly evaluate pairs which are one away in length but have 2 changes (1:32)
    By testing each comparison in monkey -> business, I was able to find the problems with and fix isNeighboring (Pi Day 22:32)
        Made findNeighbors a boolean which returns whether a neighbor was found (23:40)
        Added a test to ensure that the program doesn't think that a word neighbors itself (15 Mar 2023 0:14)
    Restructured this and Levenshtein so that findNeighbors returns a Set of neighboring nodes instead of stings
        and accepts lengthStartIndexes instead of the start and end index. Makes everything easier to read (16 Mar 2023 10:13)
*/

import java.util.*;

public class LevenshteinNode implements Comparable<LevenshteinNode> {
    private final String word;
    private HashSet<LevenshteinNode> previous;
    private HashSet<LevenshteinNode> next;

    /**
     * Constructs with this node with word initialized to word, and previous and next empty.
     * @param word Value to initialize word to.
     */
    public LevenshteinNode(String word) {
        this.word = word;
        previous = new HashSet<>();
        next = new HashSet<>();
    }

    /**
     * Constructs with this node with word initialized to word, and next empty, and previous containing the previous node.
     * @param word Value to initialize word to.
     * @param previous Node to initialize previous with.
     */
    public LevenshteinNode(String word, LevenshteinNode previous) {
        this.word = word;
        this.previous = new HashSet<>(Arrays.asList(previous));
        next = new HashSet<>();
    }

    /**
     * Adds all neighbors from the dictionary to a HashSet of neighbors and returns it.
     * Uses lengthStartIndexes to reduce the scope of the dictionary that is searched, since no words that have a
     * length difference of more than two can possibly be neighboring.
     * @param dictionary Dictionary to use.
     * @param lengthStartIndexes Map whose key is a word length and value is the first index of a word with that
     *                           length in dictionary.
     * @return HashSet of neighboring nodes.
     */
    public Set<LevenshteinNode> findNeighbors(LevenshteinNode[] dictionary, Map<Integer, Integer> lengthStartIndexes) {
        Set<LevenshteinNode> neighbors = new HashSet<>();
        int startIndex = lengthStartIndexes.getOrDefault(this.word.length() - 1, 0);
        int endIndex = lengthStartIndexes.getOrDefault(this.word.length() + 2, dictionary.length);
        for (int i = startIndex; i < endIndex; i++) {
            if (this.isNeighboring(dictionary[i])) {
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
    private boolean isNeighboring(LevenshteinNode n) {
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

    /** @param previous Node to add to this previous. */
    public void addPrevious(LevenshteinNode previous) {
        this.previous.add(previous);
    }

    /** Returns the word this node represents. */
    public String getWord() {
        return word;
    }

    /** @return "[word] - [neighbors]" */
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
