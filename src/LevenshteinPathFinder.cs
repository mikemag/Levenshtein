using System.Text;

public abstract class LevenshteinPathFinder {
    /** Set to true to display extra text for debugging. */
    protected const bool PRINT_EXTRA = false;

    /**
     * Returns the paths between two words as an List of LinkedLists of integers.
     *
     * This format is chosen because generating it is very memory and computationally
     * efficient. The user will most likely have to implement their own method to
     * convert to a different format, or use pathsToString if the goal is simply to
     * print it.
     *
     * @param word1 starting word
     * @param word2 ending word
     * @param database database used for the internal graph
     * @param startTime approximate time (gotten from System.nanoTime()) that this function was called
     * @return the list of paths
     */
    public abstract List<LinkedList<int>> generatePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database, long startTime);

    /**
     * Converts paths to a String representation, where each path is on its own line and a change is denoted by [word1]-> [word2]
     * For example, the paths between "dog" and "cat" would be:
     * @param paths A TreeSet of LinkedLists of Strings, with each LinkedList representing a path between the start and end words.
     * @param showNumber Whether to add a number before each path (to count how many paths there are).
     * @param showDistance Whether to add the distance to the end of the String.
     * @return The LinkedList of paths, represented as Strings.
     */
    public static String pathsToString(List<LinkedList<int>> paths, LevenshteinDatabase database, bool showNumber, bool showDistance) {
        if (paths == null) {
            return "";
        }

        int pathNumber = 0;
        StringBuilder pathsBuilder = new StringBuilder();

        paths.Sort(LevenshteinPathFinder.PATH_COMPARATOR);

        foreach (LinkedList<int> path in paths) {
            if (showNumber) {
                pathsBuilder.Append(++pathNumber + ". ");
            }

            IEnumerator<int> pathIter = path.GetEnumerator();
            pathIter.MoveNext();
            pathsBuilder.Append(database.wordAt(pathIter.Current));

            while (pathIter.MoveNext()) {
                pathsBuilder.Append("-> " + database.wordAt(pathIter.Current));
            }
            pathsBuilder.Append("\n");
        }

        if (showDistance) {
            int distance = paths.ElementAt(0).Count - 1;
            pathsBuilder.Append("Distance: " + distance);
        }

        return pathsBuilder.ToString();
    }

    /**
     * Compares paths by comparing the first words of each path to each other.
     * If the first words are identical, the next word is checked, then the next one, and so on.
     *
     * This is intended for converting the list returned by generatePaths to a consistent order.
     *
     * @return 0 if the paths are identical, < 0 if the first 
     *         nonequal word in o1 comes before o2, and > 1 
     *         if it comes after.
     */
    public static readonly Comparer<LinkedList<int>> PATH_COMPARATOR = Comparer<LinkedList<int>>.Create((o1, o2) => {
        IEnumerator<int> i1 = o1.GetEnumerator();
        IEnumerator<int> i2 = o2.GetEnumerator();
        while (i1.MoveNext()) {
            if (!i2.MoveNext()) {
                return 1;
            }
            int c = i1.Current.CompareTo(i2.Current);
            if (c != 0) {
                return c;
            }
        }
        return 0;
    });
}
