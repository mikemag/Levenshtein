/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: LevenshteinOneSided.java
Maintenance Log:
    Started. Already had ideas as to how I was going to find the Levenshtein between two words (13 Mar 2023 23:07)
        Started the findDistance method. Currently, the LevenshteinNodes are stored in a LinkedList of HashSets (Pi Day Bomb Squad)
        It works for some pairs of words, but underestimates for others. After some testing, looks like a problem with isNeighboring (1:27)
        It works for all tested pairs except monkey -> business. Going to have to work that out and make the algorithm more efficient as it takes nearly 9 seconds for dog and quack (2:05)
    Attempted to fix findDistance but failed so miserably that I have to essentially restart (15 Mar 2023 0:45)
    It now finds monkey -> business in 25 seconds, using the LinkedList of HashSets to store each layer (15 Mar 2023 10:57)
    Restructuring how the data is stored to allow it to find all paths. Changing a HashMap to a TreeMap caused it to break (16 Mar 2023 10:55)
    Now takes about 13 seconds to go from business to monkey, assuming the words are fed in the correct order (17 Mar 2023 15:25)
    Renamed to "LevenshteinOneSided.java" (19 Mar 2023 17:02)
*/

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LevenshteinOneSided extends Levenshtein<LevenshteinNodePrevOnly> {
    public LevenshteinOneSided(String filename) throws IOException {
        super(new LevenshteinNodePrevOnly[(int) Files.lines(Paths.get("src/Dictionary.txt")).count()], new HashMap<>());
        Scanner s = new Scanner(new File(filename));
        int i = 0;
        while (s.hasNext()) {
            String word = s.next();
            dictionary[i] = new LevenshteinNodePrevOnly(word);
            lengthStartIndexes.putIfAbsent(word.length(), i);
            i++;
        }
    }

    /** This is just for testing. */
    public static void main(String[] args) throws IOException {
        Levenshtein test = new LevenshteinOneSided("src/Dictionary.txt");
        long time1 = System.nanoTime();
        String w1 = "business";
        String w2 = "monkey";
        List<String> paths = test.getAllPaths(w1, w2);
        for (String p : paths) {
            System.out.println(p);
        }
        Scanner distanceScanner = new Scanner(paths.get(0));
        int distance = -1;
        while (distanceScanner.hasNext()) {
            distanceScanner.next();
            distance++;
        }
        System.out.println("Distance between '" + w1 + "' and '" + w2 + "': " + distance);
        System.out.println((System.nanoTime() - time1) / 1000000);
    }

    /**
     * TODO: ADD PROPER DESCRIPTION
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return An array which contains a modified version of dictionary with every path between the words found and appropriate pointers stored in each node.
     *         This only generates the information required to find the paths - It does not directly tell you what the paths are.
     */
    @Override
    protected LevenshteinNodePrevOnly[] generatePaths(String w1, String w2, long startTime) {
        LevenshteinNodePrevOnly[] nodeStorage = Arrays.copyOf(dictionary, dictionary.length);
        HashSet<LevenshteinNodePrevOnly> searched = new HashSet<>();
        HashSet<LevenshteinNodePrevOnly> outer = new HashSet<>(Arrays.asList((LevenshteinNodePrevOnly)Levenshtein.binarySearch(nodeStorage, w1)));
        LevenshteinNodePrevOnly endWord = (LevenshteinNodePrevOnly)Levenshtein.binarySearch(nodeStorage, w2);
        while (!outer.contains(endWord)) {
            HashSet<LevenshteinNodePrevOnly> newOuter = new HashSet<>();
            for (LevenshteinNodePrevOnly n : outer) {
                HashSet<LevenshteinNodePrevOnly> neighbors = n.findNeighbors(nodeStorage, lengthStartIndexes, searched, outer);
                for (LevenshteinNodePrevOnly neighbor: neighbors) {
                    if (newOuter.contains(neighbor)) {
                        neighbor.addPrevious(n);
                    } else {
                        newOuter.add(neighbor);
                    }
                }
            }
            if (newOuter.isEmpty()) {
               break;
            }
            if (PRINT_EXTRA) {
                System.out.println("\n" + outer);
                System.out.println("Searched Nodes: " + searched.size());
                System.out.println((System.nanoTime() - startTime) / 1000000);
            }
            searched.addAll(outer);
            outer = newOuter;
        }
        return nodeStorage;
    }

    /**
     * First, generatePaths is called, then it searches for the node representing the end word in the returned array.
     * Then it recursively calls getDistance(private) on each previous node until there are no previous nodes (Once it reaches the start word).
     * Each call adds one to the total distance.
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @return Levenshtein distance between the two words.
     */
    @Override
    public int getDistance(String w1, String w2) {
        LevenshteinNodePrevOnly[] pathsRevealed = generatePaths(w1, w2, System.nanoTime());
        return getDistance((LevenshteinNodePrevOnly)Levenshtein.binarySearch(pathsRevealed, w2));
    }
    private int getDistance(LevenshteinNodePrevOnly n) {
        if (n.getPrevious().size() == 0) {
            return 0;
        } else {
            return getDistance(n) + 1;
        }
    }

    /**
     * First, generatePaths is called, then it searches for the node representing the end word in the returned array.
     * Then it recursively calls getAllPaths(private) on each of the previous nodes until it reaches the start word, which has no previous.
     * On each call, it adds its own word to the front of the previously generated text. That way, once you reach the first word, the
     * returned sequence denotes the order of changes between the two words.
     * Each path is then added to an ArrayList and returned.
     * @param w1 Starting word.
     * @param w2 Ending word.
     * @return Returns a list of strings, with each string being a representation of a path between the two words, with arrows between words, denoting where a change occurred.
     */
    @Override
    public List<String> getAllPaths(String w1, String w2) {
        LevenshteinNodePrevOnly[] pathsRevealed = generatePaths(w1, w2, System.nanoTime());
        LevenshteinNodePrevOnly n = (LevenshteinNodePrevOnly)Levenshtein.binarySearch(pathsRevealed, w2);
        List<String> paths = new ArrayList<>();
        for (LevenshteinNodePrevOnly prev : n.getPrevious()) {
            getAllPaths(prev, "-> " + n.getWord(), paths);
        }
        return paths;
    }
    private void getAllPaths(LevenshteinNodePrevOnly n, String after, List<String> paths) {
        if (n.getPrevious().size() == 0) {
            paths.add(n.getWord() + after);
        } else {
            for (LevenshteinNodePrevOnly prev : n.getPrevious()) {
                getAllPaths(prev, "-> " + n.getWord() + after, paths);
            }
        }
    }
}
