/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: LevenshteinOneSided.java
Maintenance Log:
    Started. Wrote generatePaths and generateNewOuter, however, they are untested for now (20 Mar 2023 1:12)
    Generates the paths from Monkey to Business in about 1 second with my algorithm and my isNeighboring method (Does so in 0.8 with Mike's).
    Converted the HashSets to HashMaps of Levenshtein Nodes with a String key (20 Mar 2023 20:54)
    Working on converting the HashMaps to LevenshteinGraphs (23 Mar 2023 10:57)
    Finished incorporating LevenshteinGraph and removing LevenshteinNode (27 Mar 2023 10:40)
    Moved the generating of dictionary into the superclass. (29 Mar 2023 22:53)
        It now converts the raw data stored in the graphs to a something usable, a TreeSet of paths stored as ArrayLists
        It can also convert to a string, allowing you to make a graph at http://www.webgraphviz.com/ (30 Mar 2023 0:14)
    Fixed calls of pathsToString in this and LevenshteinSingleSided (6 April 2023 9:54)
    Added more comments (7 April 2023 9:20)
    Added a heading to graphsToPaths
*/

import java.io.*;
import java.util.*;

// Average Monkey -> ... -> Business time: 433 milliseconds.
public class LevenshteinDualSided extends Levenshtein {
    /** Tester method for LevenshteinDualSided */
    public static void main(String[] args) throws IOException {
        Levenshtein test = new LevenshteinDualSided("src/Dictionary.txt");
        String w1 = "monkey";
        String w2 = "business";
        long time1 = System.nanoTime();
        System.out.println(Levenshtein.pathsToString(test.generatePaths(w1, w2, time1), false, false));
        System.out.println("Done in " + (System.nanoTime() - time1)/1000000 + " milliseconds.");
        long time2 = System.nanoTime();
        for (int i = 0; i < 500; i++) {
            Levenshtein.pathsToString(test.generatePaths(w1, w2, time2), false, false);
        }
        System.out.println("Average Time: " + (System.nanoTime() - time2)/500000000 + " milliseconds");
    }

    /**
     * See Levenshtein(String filepath)
     * Reads a dictionary from a file, storing each word into the array, then sorting it, then determining lengthStartIndexes.
     * @param filepath Name of the file to read from dictionary to.
     * @throws IOException
     */
    public LevenshteinDualSided(String filepath) throws IOException {
        super(filepath);
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
                g1.generateNewOuter(dictionary, lengthStartIndexes);
            } else {
                g2.generateNewOuter(dictionary, lengthStartIndexes);
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
