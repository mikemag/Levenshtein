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
*/

import java.util.*;

public class LevenshteinNode {
    private String word;
    private Set<String> neighbors;
    private int layer;
    public static Comparator<LevenshteinNode> NODE_COMPARATOR = (o1, o2) -> {
        int l1 = o1.getLayer();
        int l2 = o2.getLayer();
        if (l1 == l2) {
            return o1.getWord().compareTo(o2.getWord());
        } else if (Math.abs(l1) == Math.abs(l2)) {
            return -l1 + l2;
        } else {
            return Math.abs(l1) - Math.abs(l2);
        }
    };
    /*public static void main(String[] args) {
        LevenshteinNode temp = new LevenshteinNode("monkey");
        System.out.println(temp.isNeighboring("money"));
        LevenshteinNode temp2 = new LevenshteinNode("money");
        System.out.println(temp2.isNeighboring("coney"));
        LevenshteinNode temp3 = new LevenshteinNode("coney");
        System.out.println(temp3.isNeighboring("cones"));
        LevenshteinNode temp4 = new LevenshteinNode("cones");
        System.out.println(temp4.isNeighboring("cines"));
        LevenshteinNode temp5 = new LevenshteinNode("cines");
        System.out.println(temp5.isNeighboring("chines"));
        LevenshteinNode temp6 = new LevenshteinNode("chines");
        System.out.println(temp6.isNeighboring("chinese"));
        LevenshteinNode temp7 = new LevenshteinNode("chinese");
        System.out.println(temp7.isNeighboring("achinese"));
        LevenshteinNode temp8 = new LevenshteinNode("achinese");
        System.out.println(temp8.isNeighboring("achiness"));
        LevenshteinNode temp9 = new LevenshteinNode("achiness");
        System.out.println(temp9.isNeighboring("ashiness"));
        LevenshteinNode temp10 = new LevenshteinNode("ashiness");
        System.out.println(temp10.isNeighboring("mashiness"));
        LevenshteinNode temp11 = new LevenshteinNode("mashiness");
        System.out.println(temp11.isNeighboring("mushiness"));
        LevenshteinNode temp12 = new LevenshteinNode("mushiness");
        System.out.println(temp12.isNeighboring("bushiness"));
        LevenshteinNode temp13 = new LevenshteinNode("bushiness");
        System.out.println(temp13.isNeighboring("business"));
        LevenshteinNode temp14 = new LevenshteinNode("business");
        System.out.println(temp14.isNeighboring("bushiness"));
        System.out.println(temp13.isNeighboring("mushiness"));
        System.out.println(temp12.isNeighboring("mashiness"));
        System.out.println(temp11.isNeighboring("ashiness"));
        System.out.println(temp10.isNeighboring("achiness"));
        System.out.println(temp9.isNeighboring("achinese"));
        System.out.println(temp8.isNeighboring("chinese"));
        System.out.println(temp7.isNeighboring("chines"));
        System.out.println(temp6.isNeighboring("cines"));
        System.out.println(temp5.isNeighboring("cones"));
        System.out.println(temp4.isNeighboring("coney"));
        System.out.println(temp3.isNeighboring("money"));
        System.out.println(temp2.isNeighboring("monkey"));
    }*/

    /**
     * Constructs with this word initialized to word, this layer set to layer and neighbors initialized as empty.
     * @param word Value to initialize word to.
     * @param layer Value to initialize layer to.
     */
    public LevenshteinNode(String word, int layer) {
        this.word = word;
        neighbors = new HashSet<>();
        this.layer = layer;
    }

    /**
     * Adds all neighbors from the dictionary to neighbors. Return true if a neighbor was found, and false otherwise.
     * @param dictionary Dictionary to use.
     * @param startIndex Index to start checking for neighbors.
     * @param endIndex Index to stop checking for neighbors.
     * @return If a neighbor was found.
     */
    public boolean findNeighbors(List<String> dictionary, int startIndex, int endIndex) {
        boolean foundNeighbor = false;
        for (int i = startIndex; i < endIndex; i++) {
            if (this.isNeighboring(dictionary.get(i))) {
                foundNeighbor = true;
                this.neighbors.add(dictionary.get(i));
            }
        }
        return foundNeighbor;
    }

    /**
     * Determines if this word is "neighboring" w (If a single addition, removal, or change of letters will result in w).
     * It does this by checking to see if both words share all but one letter, and these letters are in the same order in each word.
     * It can determine this by traversing this word and comparing the current letter to the front of w.
     * The front of w is pushed back if the letter comparison evaluates true or if the front letter does not need to be checked again.
     * This allows this method to have a worst-case big-O of O(n).
     * @param w Word to compare to, the distance between the lengths of these words must not be more than 1.
     * @return Whether this is neighboring w.
     */
    private boolean isNeighboring(String w) {
        if (this.word.equals(w)) {
            return false;
        }
        boolean foundDifference = false;
        int wIndex = 0;
        for (int i = 0; i < this.word.length() && wIndex < w.length(); i++) {
            if (this.word.charAt(i) == w.charAt(wIndex)) {
                wIndex++;
            } else {
                if (foundDifference == true) {
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

    /** Returns the layer this node lies on */
    public int getLayer() {
        return layer;
    }

    /** Returns a set of the values of neighbors. */
    public Set<String> getNeighbors() {
        return neighbors;
    }

    /**
     * Adds a neighboring word to the list of neighbors.
     * NOTE: This does not check to see if these words are actually neighboring.
     * @param n Neighboring word to add, must actually be neighboring.
     */
    public void addNeighbor(String n) {
        neighbors.add(n);
    }

    /** @return "[word] - [neighbors]" */
    public String toString() {
        return word + "(" + layer + ") - " + neighbors;
    }

    /**
     * Returns true if the objects are equal, or if the other object is a LevenshteinNode and the words of both are equal.
     * @param o Other object.
     * @return Whether the objects are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LevenshteinNode node = (LevenshteinNode) o;
        return Objects.equals(word, node.word);
    }

    /** @return Hashcode of the internal word. */
    @Override
    public int hashCode() {
        return Objects.hash(word);
    }
}
