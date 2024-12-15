import java.util.*;

public class LevenshteinGraph {
    private HashMap<Integer, ArrayList<Integer>> outer;

    /**
     * Searched is used both for reconstructing paths after finishing the
     * breadth-first search and ensuring each word is contained in only one
     * layer.
     */
    private HashMap<Integer, ArrayList<Integer>> searched;

    /**
     * Initializes the graph, with searched being completely empty and outer only containing the root word with no previous.
     * @param root Word to put in the outer layer of the graph.
     */
    public LevenshteinGraph(int root) {
        searched = new HashMap<>();
        outer = new HashMap<>();
        outer.put(root, new ArrayList());
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
        HashMap<Integer, ArrayList<Integer>> newOuter = new HashMap<>();

        searched.putAll(outer);

        for (int outerWord : outer.keySet()) {
            Integer[] neighbors = database.findNeighbors(outerWord);

            for (int neighbor : neighbors) {
                if (searched.containsKey(neighbor)) {
                    continue;
                }

                ArrayList<Integer> neighborsNeighbors = newOuter.get(neighbor);
                if (neighborsNeighbors != null) {
                    newOuter.get(neighbor).add(outerWord);
                } else {
                    newOuter.put(neighbor, new ArrayList<Integer>(Arrays.asList(outerWord)));
                }
            }
        }

        outer = newOuter;
    }

    /**
     * Recursively finds all paths between wordIndex1 and wordIndex2.
     * TODO: Finish this
     * @param w1 First word.
     * @param w2 Second word.
     * @param reversed If false, wordIndex1 -> wordIndex2 is returned. If true, wordIndex2 -> wordIndex1 is returned.
     * @return All paths between wordIndex1 and wordIndex2, with each path being expressed a LinkedList of the words in the path, and the paths being stored in a TreeSet.
     */
    public TreeSet<LinkedList<Integer>> allPathsBetween(int wordIndex1, int wordIndex2, boolean reversed) {
        LinkedList<Integer> previous = new LinkedList<>(Arrays.asList(wordIndex2));
        if (wordIndex1 == wordIndex2) {
            TreeSet<LinkedList<Integer>> toReturn = new TreeSet<>(PATH_COMPARATOR);
            toReturn.add(previous);
            return toReturn;
        } else {
            return allPathsBetween(new TreeSet<>(PATH_COMPARATOR), previous, wordIndex1, reversed);
        }
    }
    public TreeSet<LinkedList<Integer>> allPathsBetween(TreeSet<LinkedList<Integer>> paths, LinkedList<Integer> currentPath, int root, boolean reversed) {
        int currentWordIndex;
        if (!reversed) {
            currentWordIndex = currentPath.getFirst();
        } else {
            currentWordIndex = currentPath.getLast();
        }

        // On the first iteration (Where current word is wordIndex1 from the non-recursive call), currentWord will be in outer.
        // After
        HashSet<Integer> setToSearch;
        if (outer.containsKey(currentWordIndex)) {
            setToSearch = new HashSet<>(outer.get(currentWordIndex));
        } else {
            setToSearch = new HashSet<>(searched.get(currentWordIndex));
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
                for (Integer wordIndex : setToSearch) {
                    LinkedList<Integer> newPrevious = new LinkedList<>(currentPath);
                    newPrevious.addFirst(wordIndex);
                    paths = allPathsBetween(paths, newPrevious, root, reversed);
                }
            } else {
                for (Integer wordIndex : setToSearch) {
                    LinkedList<Integer> newPrevious = new LinkedList<>(currentPath);
                    newPrevious.addLast(wordIndex);
                    paths = allPathsBetween(paths, newPrevious, root, reversed);
                }
            }
        }
        return paths;
    }

    /**
     * Checks if the outer of this graph contains wordIndex.
     * This method is for single-sided levenshtein algorithms, where each layer is checked against a single target word.
     * @param wordIndex Word to check.
     * @return True if the outer of this graph contains wordIndex, false otherwise.
     */
    public boolean outerContains(int wordIndex) {
        return outer.containsKey(wordIndex);
    }

    public static ArrayList<Integer> outerIntersection(LevenshteinGraph graph1, LevenshteinGraph graph2) {
        ArrayList<Integer> intersection = new ArrayList();
        HashMap<Integer, ArrayList<Integer>> outerCopy = graph1.outer;
        HashMap<Integer, ArrayList<Integer>> otherOuter = graph2.outer;

        if (outerCopy.keySet().size() > otherOuter.keySet().size()) {
            HashMap<Integer, ArrayList<Integer>> temp = otherOuter;
            otherOuter = outerCopy;
            outerCopy = temp;
        }

        for (int wordIndex : outerCopy.keySet()) {
            if (otherOuter.containsKey(wordIndex)) {
                intersection.add(wordIndex);
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
    public static final Comparator<LinkedList<Integer>> PATH_COMPARATOR = (o1, o2) -> {
        Iterator<Integer> i1 = o1.iterator();
        Iterator<Integer> i2 = o2.iterator();
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
