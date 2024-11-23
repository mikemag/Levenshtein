import java.io.*;
import java.nio.file.*;
import java.util.*;

public abstract class Levenshtein {
    /** Set to true to display extra text for debugging. */
    protected static final boolean PRINT_EXTRA = false;

    /**
     * Dictionary of words, which is read from a file and stored as an array of LevenshteinNodes with previous being empty.
     * This allows for each call of generatePaths to add.
     */
    protected final String[] dictionary;

    /**
     * The key represents the length of a word, and the value is the first index in dictionary of a word of that length.
     */
    protected final Map<Integer, Integer> lengthStartIndexes;

    /**
     * Reads a dictionary from a file, storing each word into the array, then sorting it, then determining lengthStartIndexes.
     * @param filepath Name of the file to read from dictionary to.
     * @throws IOException
     */
    public Levenshtein(String filepath) throws IOException {
        ArrayList<String> listDictionary = new ArrayList();
        lengthStartIndexes = new HashMap<>();
        Scanner s = new Scanner(new File(filepath));
        while(s.hasNext()) {
            listDictionary.add(s.next());
        }
        dictionary = listDictionary.toArray(new String[0]);
        Arrays.sort(dictionary, COMPARE_BY_LENGTH);
        for (int i = 0; i < dictionary.length; i++) {
            lengthStartIndexes.putIfAbsent(dictionary[i].length(), i);
        }
    }

    /**
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between w1 and w2
     */
    protected abstract TreeSet<LinkedList<String>> generatePaths(String w1, String w2, long startTime);

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

    /**
     * Comparator which sorts strings first by length then their natural ordering, which is used to sort the dictionary.
     */
    private static final Comparator<String> COMPARE_BY_LENGTH = (o1, o2) -> {
        int c = o1.length() - o2.length();
        if (c == 0) {
            return o1.compareTo(o2);
        } else {
            return c;
        }
    };
}
