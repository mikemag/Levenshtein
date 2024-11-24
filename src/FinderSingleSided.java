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
    public TreeSet<LinkedList<String>> generatePaths(String word1, String word2, LevenshteinDatabase database, long startTime) {
        if (word1.equals(word2)) {
            TreeSet<LinkedList<String>> path = new TreeSet<>(LevenshteinGraph.PATH_COMPARATOR);
            path.add(new LinkedList<>(Arrays.asList(word1)));
            return path;
        }
        LevenshteinGraph g = new LevenshteinGraph(word1);
        while (true) {
            g.generateNewOuter(database);
            if (PRINT_EXTRA) {
                System.out.println("Outer: " + g.outerSize());
                System.out.println("Searched: " + g.searchedSize());
                System.out.println("Total Searched: " + (g.outerSize() + g.searchedSize()));
                System.out.println("Current Time: " + (System.nanoTime() - startTime) / 1000000 + "\n");
            }
            if (g.outerSize() == 0) {
                return null;
            } else if (g.outerContains(word2)) {
                return g.allPathsBetween(word1, word2, false);
            }
        }
    }
}
