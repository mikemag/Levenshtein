import java.io.*;
import java.util.*;

// Average Monkey -> ... -> Business time: 433 milliseconds.
public class LevenshteinDualSided extends Levenshtein {
    /** Tester method for LevenshteinDualSided */
    public static void main(String[] args) throws IOException {
        Levenshtein test = new LevenshteinDualSided("resources/Dictionary370k.txt");
        String w1 = "monkey";
        String w2 = "business";
        long time1 = System.nanoTime();
        System.out.println(Levenshtein.pathsToString(test.generatePaths(w1, w2, time1), false, false));
        System.out.println("Done in " + (System.nanoTime() - time1)/1000000 + " milliseconds.");
        long time2 = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            Levenshtein.pathsToString(test.generatePaths(w1, w2, time2), false, false);
        }
        System.out.println("Average Time: " + (System.nanoTime() - time2)/100000000 + " milliseconds");
    }

    /**
     * See Levenshtein(String filepath)
     * Reads a dictionary from a file, storing each word into the array, then sorting it, then determining lengthStartIndexes.
     * @param filepath Name of the file to read from dictionary to.
     * @throws IOException
     */
    public LevenshteinDualSided(String filepath) throws IOException {
        super(new WildcardDatabase(filepath));
    }

    /**
     * TODO: ADD PROPER DESCRIPTION
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between w1 and w2
     */
    @Override
    protected TreeSet<LinkedList<String>> generatePaths(String w1, String w2, long startTime) {
        if (w1.equals(w2)) {
            TreeSet<LinkedList<String>> path = new TreeSet<>(LevenshteinGraph.PATH_COMPARATOR);
            path.add(new LinkedList<>(Arrays.asList(w1)));
            return path;
        }
        LevenshteinGraph g1 = new LevenshteinGraph(w1);
        LevenshteinGraph g2 = new LevenshteinGraph(w2);
        while(true) {
            int g1OSize = g1.outerSize();
            int g2OSize = g2.outerSize();
            if (g1OSize <= g2OSize) {
                g1.generateNewOuter(database);
            } else {
                g2.generateNewOuter(database);
            }
            if (PRINT_EXTRA) {
                System.out.println("Start Outer: " + g1OSize);
                System.out.println("Target Outer: " + g2OSize);
                System.out.println("Start Searched: " + g1.searchedSize());
                System.out.println("Target Searched: " + g2.searchedSize());
                System.out.println("Total Searched: " + (g1OSize + g2OSize + g1.searchedSize() + g2.searchedSize()));
                System.out.println("Current Time: " + (System.nanoTime() - startTime) / 1000000 + "\n");
            }
            if (g1OSize == 0 || g2OSize == 0) {
                return null;
            } else if (g1.outerIntersects(g2)) {
                return graphsToPaths(g1, g2, w1, w2, g1.getOuterIntersection(g2));
            }
        }
    }

    /**
     * On each graph, the paths between the root word and the intersection words are found.
     * Then, these paths are "stitched together", such that all unique paths from the staring word to the ending word.
     * @param g1 Starting word graph.
     * @param g2 Ending word graph.
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @param intersection Set of words which are shared between the outer layers of g1 and g2.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between w1 and w2.
     */
    private TreeSet<LinkedList<String>> graphsToPaths(LevenshteinGraph g1, LevenshteinGraph g2, String w1, String w2, HashSet<String> intersection) {
        TreeSet<LinkedList<String>> pathsToReturn = new TreeSet<>(LevenshteinGraph.PATH_COMPARATOR);
        TreeSet<LinkedList<String>> g1Paths = new TreeSet<>(LevenshteinGraph.PATH_COMPARATOR);
        TreeSet<LinkedList<String>> g2Paths = new TreeSet<>(LevenshteinGraph.PATH_COMPARATOR);
        for (String s : intersection) {
            g1Paths.addAll(g1.allPathsBetween(w1, s, false));
            g2Paths.addAll(g2.allPathsBetween(w2, s, true));
        }
        // For each word in intersection, there may be multiple paths to it from the starting word and there may be multiple to the ending word.
        // To account for this, each rootPath is indexed and then a unique path for each destinationPath is added to pathsToReturn.
        // To make sure only legal paths are added, the rootPath and destinationPath are first checked for a shared intersection word.
        for (LinkedList<String> rootPath : g1Paths) {
            for (LinkedList<String> destinationPath : g2Paths) {
                LinkedList<String> pathToAdd = (LinkedList<String>)rootPath.clone();
                if (pathToAdd.removeLast().equals(destinationPath.getFirst())) {
                    pathToAdd.addAll(destinationPath);
                    pathsToReturn.add(pathToAdd);
                }
            }
        }
        return pathsToReturn;
    }
}
