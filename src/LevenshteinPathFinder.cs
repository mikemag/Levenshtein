using System.Text;

public abstract class LevenshteinPathFinder
{
    /** Set to true to display extra text for debugging. */
    protected const bool PRINT_EXTRA = false;

    /**
     * Returns the paths between two words as an List of LinkedLists of integers.
     *
     * This format is chosen because generating it is very memory and computationally
     * efficient. The user will most likely have to implement their own method to
     * convert to a different format, or use pathsToString if the goal is simply to
     * print it.
     */
    public abstract List<int[]>? GeneratePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database);

    /**
     * Converts paths to a String representation, where each path is on its own line and a change is denoted by [word1]-> [word2]
     * For example, the paths between "dog" and "cat" would be:
     */
    public static String PathsToString(List<int[]>? paths, LevenshteinDatabase database, bool showNumber,
        bool showDistance)
    {
        if (paths == null)
        {
            return "";
        }

        int pathNumber = 0;
        StringBuilder pathsBuilder = new StringBuilder();

        paths.Sort(LevenshteinPathFinder.PATH_COMPARATOR);

        foreach (int[] path in paths)
        {
            if (showNumber)
            {
                pathsBuilder.Append(++pathNumber + ". ");
            }

            pathsBuilder.Append(database.Words[path[0]]);

            for (int i = 1; i < path.Length; i++)
            {
                pathsBuilder.Append("-> " + database.Words[path[i]]);
            }

            pathsBuilder.Append("\n");
        }

        if (showDistance)
        {
            int distance = paths.ElementAt(0).Count() - 1;
            pathsBuilder.Append("Distance: " + distance);
        }

        return pathsBuilder.ToString();
    }

    /**
     * Compares paths by comparing the first words of each path to each other.
     * If the first words are identical, the next word is checked, then the next one, and so on.
     *
     * This is intended for converting the list returned by generatePaths to a consistent order.
     */
    public static readonly Comparer<int[]> PATH_COMPARATOR = Comparer<int[]>.Create((array1, array2) =>
    {
        int end = int.Min(array1.Count(), array2.Count());
        for (int i = 0; i < end; i++)
        {
            int comparison = array1[i].CompareTo(array2[i]);
            if (comparison != 0)
            {
                return comparison;
            }
        }

        ;
        if (array1.Count() < array2.Count())
        {
            return 1;
        }
        else if (array2.Count() < array1.Count())
        {
            return -1;
        }

        return 0;
    });
}