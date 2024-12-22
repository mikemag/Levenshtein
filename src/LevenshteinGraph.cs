public class LevenshteinGraph {
    public int Depth { get; private set; }
    public int OuterCount { get => outer.Count; }
    public int SearchedCount { get => searched.Count; }

    private Dictionary<int, List<int>> outer;

    /**
     * Searched is used both for reconstructing paths after finishing the
     * breadth-first search and ensuring each word is contained in only one
     * layer.
     */
    private Dictionary<int, List<int>> searched;

    /**
     * Initializes the graph, with searched being completely empty and outer only containing the root word with no previous.
     */
    public LevenshteinGraph(int root) {
        searched = new Dictionary<int, List<int>>();
        outer = new Dictionary<int, List<int>>();
        outer.Add(root, new List<int>());
        Depth = 1;
    }
    
    /**
     * This method puts every word in outer into searched and replaces outer with a new outer containing the neighbors of the previous outer.
     * NOTE: A neighbor already in the graph is not added. Any path which arrives at a word later than another path will not be a levenshtein.
     * Generating the graph layer-by-layer and checking for links on each layer prevents generation and checking of words with distances past the target. 
     * It does this by temporarily creating a new Dictionary to store the new outer.
     * For each word in the previous outer, all of its neighbors are added to the new outer.
     * The word is also added to the previous of each neighbor.
     * If a word generates a neighbor that's already in the new outer, the word is simply added to the previous of that neighbor.
     * Once it is finished iterating across outer, it is put into searched and replaced with newOuter.
     */
    public bool GenerateNewOuter(LevenshteinDatabase database) {
        Dictionary<int, List<int>> newOuter = new Dictionary<int, List<int>>();

        /*searched = searched.Concat(outer).ToDictionary(pair => pair.Key, pair => pair.Value);*/
        // The foreach loop is faster than Concat, which is extremely disappointing.
        foreach (KeyValuePair<int, List<int>> entry in outer) {
            searched.Add(entry.Key, entry.Value);
        }

        foreach (int outerWord in outer.Keys) {
            int[] neighbors = database.FindNeighbors(outerWord);

            foreach (int neighbor in neighbors) {
                if (searched.ContainsKey(neighbor)) {
                    continue;
                }

                if (newOuter.ContainsKey(neighbor)) {
                    newOuter[neighbor].Add(outerWord);
                } else {
                    List<int> listToAdd = new List<int>();
                    listToAdd.Add(outerWord);
                    newOuter.Add(neighbor, listToAdd);
                }
            }
        }
        
        if (newOuter.Count == 0) {
            return false;
        }

        outer = newOuter;
        Depth++;
        return true;
    }

    /**
     * Finds all paths between wordIndex1 and wordIndex2 after a breadth-first
     * search has been completed.
     *
     * Does this by reading the values of the searched map (Lists containing
     * every previous word), adding each to a copy of the path and recursively
     * calling the helper method with these copies, returning once the path has
     * reached the its destination word.
     */
    public List<LinkedList<int>> AllPathsBetween(int wordIndex1, int wordIndex2, bool reversed) {
        LinkedList<int> previous = new LinkedList<int>();
        previous.AddFirst(wordIndex2);
        List<LinkedList<int>> toReturn = new List<LinkedList<int>>();

        if (wordIndex1 == wordIndex2) {
            toReturn.Add(previous);
        } else if (reversed) {
            AllPathsBetween(toReturn, previous, wordIndex1, outer[wordIndex2], (i, p) => p.AddLast(i));
        } else {
            AllPathsBetween(toReturn, previous, wordIndex1, outer[wordIndex2], (i, p) => p.AddFirst(i));
        }

        return toReturn;
    }

    private void AllPathsBetween(List<LinkedList<int>> paths, LinkedList<int> currentPath, int root, List<int> setToSearch, Action<int, LinkedList<int>> pathAdder) {
        if (setToSearch.Contains(root)) {
            pathAdder(root, currentPath);
            paths.Add(currentPath);
            return;
        }

        foreach (int wordIndex in setToSearch) {
            LinkedList<int> newPrevious = new LinkedList<int>(currentPath);

            pathAdder(wordIndex, newPrevious);
            AllPathsBetween(paths, newPrevious, root, searched[wordIndex], pathAdder);
        }
    }

    /**
     * Checks if the outer of this graph contains wordIndex.
     */
    public bool OuterContains(int wordIndex) {
        return outer.ContainsKey(wordIndex);
    }

    public static List<int> OuterIntersection(LevenshteinGraph graph1, LevenshteinGraph graph2) {
        List<int> intersection = new List<int>();
        Dictionary<int, List<int>> outerCopy = graph1.outer;
        Dictionary<int, List<int>> otherOuter = graph2.outer;

        if (outerCopy.Keys.Count > otherOuter.Keys.Count) {
            Dictionary<int, List<int>> temp = otherOuter;
            otherOuter = outerCopy;
            outerCopy = temp;
        }

        foreach (int wordIndex in outerCopy.Keys) {
            if (otherOuter.ContainsKey(wordIndex)) {
                intersection.Add(wordIndex);
            }
        }

        return intersection;
    }

    public override String ToString() {
        return "Outer: \n" + outer + "\nSearched: \n" + searched;
    }
}
