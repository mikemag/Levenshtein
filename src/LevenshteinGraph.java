/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: LevenshteinGraph.java
Maintenance Log:
    Started. Added generateNewOuter, findNeighbors, areNeighboring, and outerIntersects (23 Mar 2023 10:56)
    Changed outerIntersects to no longer be static and created outerContains for LevenshteinSingleSided (27 Mar 2023 10:40)
*/

import java.util.*;

public class LevenshteinGraph {
    private HashMap<String, HashSet<String>> searched;
    private HashMap<String, HashSet<String>> outer;
    public LevenshteinGraph(String root) {
        searched = new HashMap<>();
        outer = new HashMap<>();
        outer.put(root, new HashSet<>());
    }
    public void generateNewOuter(String[] dictionary, Map<Integer, Integer> lengthStartIndexes) {
        HashMap<String, HashSet<String>> newOuter = new HashMap<>();
        for (String outerWord : outer.keySet()) {
            HashSet<String> neighbors = findNeighbors(outerWord, dictionary, lengthStartIndexes);
            for (String neighbor : neighbors) {
                HashSet<String> neighborValues = newOuter.get(neighbor);
                if (neighborValues != null) {
                    newOuter.get(neighbor).add(outerWord);
                } else {
                    newOuter.put(neighbor, new HashSet<>(Arrays.asList(outerWord)));
                }
            }
        }
        searched.putAll(outer);
        outer = newOuter;
    }

    public HashSet<String> findNeighbors(String w, String[] dictionary, Map<Integer, Integer> lengthStartIndexes) {
        HashSet<String> neighbors = new HashSet<>();
        int endIndex = lengthStartIndexes.getOrDefault(w.length() + 2, dictionary.length);
        for (int i = lengthStartIndexes.getOrDefault(w.length() - 1, 0); i < endIndex; i++) {
            if (areNeighboring(w, dictionary[i]) && !searched.containsKey(dictionary[i]) && !outer.containsKey(dictionary[i])) {
                neighbors.add(dictionary[i]);
            }
        }
        return neighbors;
    }

    /**
     * Determines if two words are "neighboring" (If a single addition, removal, or change of letters will result in w).
     * It does this by checking to see if both words share all but one letter, and these letters are in the same order in each word.
     * It can determine this by traversing this word and comparing the current letter to the front of n.
     * If the words have unequal length, it will ensure w1 is shorter and the w1 index will be reduced by one if a difference is found.
     * This allows this method to have a worst-case big-O of O(n).
     * @param w1 First word, the length difference between w1 and w2 must not be more than 1.
     * @param w2 Second word, the length difference between w1 and w2 must not be more than 1.
     * @return Whether this is neighboring n.
     */

    public static boolean areNeighboring(String w1, String w2) {
        int w1l = w1.length();
        int w2l = w2.length();
        int lengthDifference = w1l - w2l;
        boolean foundDifference = false;

        // Checks to see if the words are neighboring if they are of the same length
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

        // Swaps the w1 and w2 if w1 is longer than w2, guaranteeing that w1 will be shorter after this
        if (lengthDifference > 0) {
            String t = w2;
            w2 = w1;
            w1 = t;
            w1l = w2l;
        }

        // Checks to see if the words are neighboring if the first one is shorter than the second one
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

    public boolean outerContains(String w) {
        return outer.containsKey(w);
    }

    public boolean outerIntersects(LevenshteinGraph g) {
        HashMap<String, HashSet<String>> outerCopy = outer;
        HashMap<String, HashSet<String>> otherOuter = g.outer;
        if (outerCopy.keySet().size() > otherOuter.keySet().size()) {
            HashMap<String, HashSet<String>> temp = otherOuter;
            otherOuter = outerCopy;
            outerCopy = temp;
        }
        for (String w : outerCopy.keySet()) {
            if (otherOuter.containsKey(w)) {
                return true;
            }
        }
        return false;
    }

    public int outerSize() {
        return outer.size();
    }
}
