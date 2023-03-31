/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: LevenshteinGraph.java
Maintenance Log:
    Started. Added generateNewOuter, findNeighbors, areNeighboring, and outerIntersects (23 Mar 2023 10:56)
    Changed outerIntersects to no longer be static and created outerContains for LevenshteinSingleSided (27 Mar 2023 10:40)
    Added allPathsBetween, both the call for a single-sided algorithm and the recursive method which might work for dual sided (29 Mar 2023 10:57)
    allPathsBetween returns a TreeSet of LinkedLists now, added searchedSize and getOuterIntersection (29 Mar 2023 23:19)
    Removed pathsToString from this and added it to Levenshtein (31 Mar 9:39)
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
    
    /**
     * This method puts every word in outer into searched and replaces outer with a new outer containing the neighbors of the previous outer.
     * NOTE: A neighbor already in the graph is not added. Any path which arrives at a word later than another path will not be a levenshtein.
     * Generating the graph layer-by-layer and checking for links on each layer prevents generation and checking of words with distances past the target. 
     * It does this by temporarily creating a new HashMap to store the new outer.
     * For each word in the previous outer, all of its neighbors are added to the new outer.
     * The word is also added to the previous of each neighbor.
     * If a word generates a neighbor that's already in the new outer, the word is simply added to the previous of that neighbor.
     * Once it is finished iterating across outer, it is put into searched and replaced with newOuter.
     * @param dictionary Array of all legal words.
     * @param lengthStartIndexes A map, with the values being the first index of a word in dictionary of a length equal to its key.
     */
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

    /**
     * Finds the neighbors of w and returns it in a HashSet.
     * Narrows the search bounds in a few ways. Firstly, it only checks w against legal words.
     * Among those legal words, w is only checked against the ones with a length difference of less than one, as otherwise they cannot be neighboring.
     * Neighbors already in the graph will never be added to the new outer, as paths that arrive at a word later than another path will always be longer.
     * @param w Word to find the neighbors of.
     * @param dictionary Array of all legal words.
     * @param lengthStartIndexes A map, with the values being the first index of a word in dictionary of a length equal to its key.
     * @return HashSet containing the neighbors of w.
     */
    public HashSet<String> findNeighbors(String w, String[] dictionary, Map<Integer, Integer> lengthStartIndexes) {
        HashSet<String> neighbors = new HashSet<>();
        int endIndex = lengthStartIndexes.getOrDefault(w.length() + 2, dictionary.length);
        // Reduces the searching scope to only words with a length that allows them to be neighboring w
        for (int i = lengthStartIndexes.getOrDefault(w.length() - 1, 0); i < endIndex; i++) {
            // Ensures that neighbors already in the graph will not be added
            if (areNeighboring(w, dictionary[i]) && !outer.containsKey(dictionary[i]) && !searched.containsKey(dictionary[i])) {
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
     * @return True if w1 and w2 are neighboring, false otherwise.
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

    public TreeSet<LinkedList<String>> allPathsBetween(String w1, String w2, boolean reversed) {
        LinkedList<String> previous = new LinkedList<>(Arrays.asList(w2));
        if (w1.equals(w2)) {
            TreeSet<LinkedList<String>> toReturn = new TreeSet<>(PATH_COMPARATOR);
            toReturn.add(previous);
            return toReturn;
        } else {
            return allPathsBetween(new TreeSet<>(PATH_COMPARATOR), previous, w1, reversed);
        }
    }
    public TreeSet<LinkedList<String>> allPathsBetween(TreeSet<LinkedList<String>> paths, LinkedList<String> currentPath, String root, boolean reversed) {
        String currentWord;
        if (!reversed) {
            currentWord = currentPath.getFirst();
        } else {
            currentWord = currentPath.getLast();
        }

        HashSet<String> previousInGraph;
        if (outer.containsKey(currentWord)) {
            previousInGraph = new HashSet<>(outer.get(currentWord));
        } else {
            previousInGraph = new HashSet<>(searched.get(currentWord));
        }

        if (previousInGraph.contains(root)) {
            if (!reversed) {
                currentPath.addFirst(root);
            } else {
                currentPath.addLast(root);
            }
            paths.add(currentPath);
        } else {
            if (!reversed) {
                for (String w : previousInGraph) {
                    LinkedList<String> newPrevious = new LinkedList<>(currentPath);
                    newPrevious.addFirst(w);
                    paths = allPathsBetween(paths, newPrevious, root, reversed);
                }
            } else {
                for (String w : previousInGraph) {
                    LinkedList<String> newPrevious = new LinkedList<>(currentPath);
                    newPrevious.addLast(w);
                    paths = allPathsBetween(paths, newPrevious, root, reversed);
                }
            }
        }
        return paths;
    }

    /**
     * Checks if the outer of this graph contains w.
     * This method is for single-sided levenshtein algorithms, where each layer is checked against a single target word.
     * @param w Word to check.
     * @return True if the outer of this graph contains w, false otherwise.
     */
    public boolean outerContains(String w) {
        return outer.containsKey(w);
    }

    /**
     * Checks if there is any word in both the outside of this and g.
     * This method is for dual-sided levenshtein algorithms, where each layer is checked against the outer of another graph.
     * @param g Graph to check against.
     * @return If outer of this graph contains any words in the outer of the other graph.
     */
    public boolean outerIntersects(LevenshteinGraph g) {
        HashMap<String, HashSet<String>> outerCopy = outer;
        HashMap<String, HashSet<String>> otherOuter = g.outer;
        // This ensures that outerCopy is the shorter of the two outer maps
        // Iterating across the shorter map will decrease the number of checks it makes. Since containsKey() is O(1), this saves time.
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

    public HashSet<String> getOuterIntersection(LevenshteinGraph g) {
        HashSet<String> intersection = new HashSet<>();
        HashMap<String, HashSet<String>> outerCopy = outer;
        HashMap<String, HashSet<String>> otherOuter = g.outer;
        if (outerCopy.keySet().size() > otherOuter.keySet().size()) {
            HashMap<String, HashSet<String>> temp = otherOuter;
            otherOuter = outerCopy;
            outerCopy = temp;
        }
        for (String w : outerCopy.keySet()) {
            if (otherOuter.containsKey(w)) {
                intersection.add(w);
            }
        }
        return intersection;
    }

    /** @return The size of outer */
    public int outerSize() {
        return outer.size();
    }

    /** @return The size of searched */
    public int searchedSize() {
        return searched.size();
    }
    public static final Comparator<LinkedList<String>> PATH_COMPARATOR = (o1, o2) -> {
        Iterator<String> i1 = o1.iterator();
        Iterator<String> i2 = o2.iterator();
        while (i1.hasNext()) {
            int c = i1.next().compareTo(i2.next());
            if (c != 0) {
                return c;
            }
        }
        return 0;
    };

    public String toString() {
        return "Outer: \n" + outer + "\nSearched: \n" + searched;
    }
}
