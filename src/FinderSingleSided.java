import java.util.*;

public class FinderSingleSided extends LevenshteinPathFinder {
    /**
     * TODO: ADD PROPER DESCRIPTION
     * @param word1 Starting word.
     * @param word2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between word1 and word2.
     */
    @Override
    public ArrayList<LinkedList<Integer>> generatePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database, long startTime) {
        if (wordIndex1 == wordIndex2) {
            ArrayList<LinkedList<Integer>> path = new ArrayList();
            path.add(new LinkedList<>(Arrays.asList(wordIndex1)));
            return path;
        }
        LevenshteinGraph g = new LevenshteinGraph(wordIndex1);
        while (true) {
            boolean generateNewOuterSucceeded = g.generateNewOuter(database);
            if (PRINT_EXTRA) {
                System.out.println("Outer: " + g.outerSize());
                System.out.println("Searched: " + g.searchedSize());
                System.out.println("Total Searched: " + (g.outerSize() + g.searchedSize()));
                System.out.println("Current Time: " + (System.nanoTime() - startTime) / 1000000 + "\n");
            }

            if (!generateNewOuterSucceeded) {
                return null;
            }
            
            if (g.outerContains(wordIndex2)) {
                return g.allPathsBetween(wordIndex1, wordIndex2, false);
            }
        }
    }
}
