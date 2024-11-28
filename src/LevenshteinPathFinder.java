import java.util.*;

public abstract class LevenshteinPathFinder {
    /** Set to true to display extra text for debugging. */
    protected static final boolean PRINT_EXTRA = false;

    /**
     * @param word1 Starting word.
     * @param word2 Ending word.
     * @param database Database used for the internal graph.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between w1 and w2
     */
    public abstract TreeSet<LinkedList<String>> generatePaths(String word1, String word2, LevenshteinDatabase database, long startTime);

    /**
     * Converts paths to a String representation, where each path is on its own line and a change is denoted by [word1]-> [word2]
     * For example, the paths between "dog" and "cat" would be:
     * @param paths A TreeSet of LinkedLists of Strings, with each LinkedList representing a path between the start and end words.
     * @param showNumber Whether to add a number before each path (to count how many paths there are).
     * @param showDistance Whether to add the distance to the end of the String.
     * @return The LinkedList of paths, represented as Strings.
     */
    public static String pathsToString(TreeSet<LinkedList<String>> paths, boolean showNumber, boolean showDistance) {
        int pathNumber = 0;
        StringBuilder pathsBuilder = new StringBuilder();
        if (paths == null) {
            return "";
        }
        for (LinkedList<String> l : paths) {
            if (showNumber) {
                pathsBuilder.append(++pathNumber + ". ");
            }
            Iterator<String> listIter = l.iterator();
            pathsBuilder.append(listIter.next());
            while (listIter.hasNext()) {
                pathsBuilder.append("-> " + listIter.next());
            }
            pathsBuilder.append("\n");
        }
        if (showDistance) {
            Iterator<LinkedList<String>> pathIter = paths.iterator();
            int distance = pathIter.next().size() - 1;
            pathsBuilder.append("Distance: " + distance);
        }
        return pathsBuilder.toString();
    }
}
