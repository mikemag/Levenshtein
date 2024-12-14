import java.util.*;

public class LevenshteinGraph {
    private HashMap<String, HashSet<String>> outer;

    /**
     * Searched is used both for reconstructing paths after finishing the
     * breadth-first search and ensuring each word is contained in only one
     * layer.
     */
    private HashMap<String, HashSet<String>> searched;

    /**
     * Initializes the graph, with searched being completely empty and outer only containing the root word with no previous.
     * @param root Word to put in the outer layer of the graph.
     */
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
    public void generateNewOuter(LevenshteinDatabase database) {
        HashMap<String, HashSet<String>> newOuter = new HashMap<>();

        searched.putAll(outer);

        for (String outerWord : outer.keySet()) {
            HashSet<String> neighbors = database.findNeighbors(outerWord);

            for (String neighbor : neighbors) {
                if (searched.containsKey(neighbor)) {
                    continue;
                }

                HashSet<String> neighborsNeighbors = newOuter.get(neighbor);
                if (neighborsNeighbors != null) {
                    newOuter.get(neighbor).add(outerWord);
                } else {
                    newOuter.put(neighbor, new HashSet<>(Arrays.asList(outerWord)));
                }
            }
        }

        outer = newOuter;
    }

    /**
     * Recursively finds all paths between w1 and w2.
     * TODO: Finish this
     * @param w1 First word.
     * @param w2 Second word.
     * @param reversed If false, w1 -> w2 is returned. If true, w2 -> w1 is returned.
     * @return All paths between w1 and w2, with each path being expressed a LinkedList of the words in the path, and the paths being stored in a TreeSet.
     */
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

        // On the first iteration (Where current word is w1 from the non-recursive call), currentWord will be in outer.
        // After
        HashSet<String> setToSearch;
        if (outer.containsKey(currentWord)) {
            setToSearch = new HashSet<>(outer.get(currentWord));
        } else {
            setToSearch = new HashSet<>(searched.get(currentWord));
        }

        if (setToSearch.contains(root)) {
            if (!reversed) {
                currentPath.addFirst(root);
            } else {
                currentPath.addLast(root);
            }
            paths.add(currentPath);
        } else {
            if (!reversed) {
                for (String w : setToSearch) {
                    LinkedList<String> newPrevious = new LinkedList<>(currentPath);
                    newPrevious.addFirst(w);
                    paths = allPathsBetween(paths, newPrevious, root, reversed);
                }
            } else {
                for (String w : setToSearch) {
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

    /**
     * Compares paths by comparing the first words of each path to each other.
     * If the first words are identical, the next word is checked, then the next one, and so on.
     * @return Returns 0 if the paths are identical, < 0 if the first word in o1 comes before o2, and >1 if it comes after.
     */
    public static final Comparator<LinkedList<String>> PATH_COMPARATOR = (o1, o2) -> {
        Iterator<String> i1 = o1.iterator();
        Iterator<String> i2 = o2.iterator();
        while (i1.hasNext()) {
            if (!i2.hasNext()) {
                return 1;
            }
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
