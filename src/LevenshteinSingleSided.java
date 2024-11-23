import java.io.*;
import java.util.*;

public class LevenshteinSingleSided extends Levenshtein {
    public LevenshteinSingleSided(String pathname) throws IOException {
        super(pathname);
    }

    /** This is just for testing. */
    public static void main(String[] args) throws IOException {
        Levenshtein test = new LevenshteinSingleSided("resources/Dictionary370k.txt");
        long time1 = System.nanoTime();
        String w1 = "dog";
        String w2 = "cat";
        System.out.println("Here are all the paths between '" + w1 + "' and '" + w2 + "': \n");
        System.out.println(Levenshtein.pathsToString(test.generatePaths(w1, w2, time1), false, false));
        System.out.println("Done in " + (System.nanoTime() - time1) / 1000000 + " milliseconds");
    }

    /**
     * TODO: ADD PROPER DESCRIPTION
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between w1 and w2.
     */
    @Override
    protected TreeSet<LinkedList<String>> generatePaths(String w1, String w2, long startTime) {
        if (w1.equals(w2)) {
            TreeSet<LinkedList<String>> path = new TreeSet<>(LevenshteinGraph.PATH_COMPARATOR);
            path.add(new LinkedList<>(Arrays.asList(w1)));
            return path;
        }
        LevenshteinGraph g = new LevenshteinGraph(w1);
        while (true) {
            g.generateNewOuter(dictionary, lengthStartIndexes);
            if (PRINT_EXTRA) {
                System.out.println("Outer: " + g.outerSize());
                System.out.println("Searched: " + g.searchedSize());
                System.out.println("Total Searched: " + (g.outerSize() + g.searchedSize()));
                System.out.println("Current Time: " + (System.nanoTime() - startTime) / 1000000 + "\n");
            }
            if (g.outerSize() == 0) {
                return null;
            } else if (g.outerContains(w2)) {
                return g.allPathsBetween(w1, w2, false);
            }
        }
    }
}
