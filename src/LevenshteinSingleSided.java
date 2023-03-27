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
    Renamed to "LevenshteinSingleSided.java" (19 Mar 2023 17:02)
    LevenshteinOneSided now finds both the path between nodes and length, but is still a normally worse algorithm than LevenshteinDualSided (20 Mar 2023 1:12)
    Converted the HashSets to HashMaps of Levenshtein Nodes with a String key (20 Mar 2023 20:54)
    Finished incorporating LevenshteinGraph and removing LevenshteinNode (27 Mar 2023 10:40)
*/

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LevenshteinSingleSided extends Levenshtein {
    public LevenshteinSingleSided(String filename) throws IOException {
        super(new String[(int) Files.lines(Paths.get("src/Dictionary.txt")).count()], new HashMap<>());
        Scanner s = new Scanner(new File(filename));
        int i = 0;
        while (s.hasNext()) {
            String word = s.next();
            dictionary[i] = word;
            lengthStartIndexes.putIfAbsent(word.length(), i);
            i++;
        }
    }

    /** This is just for testing. */
    public static void main(String[] args) throws IOException {
        Levenshtein test = new LevenshteinSingleSided("src/Dictionary.txt");
        long time1 = System.nanoTime();
        String w1 = "dog";
        String w2 = "cat";
        test.generatePaths(w1, w2, time1);
        System.out.println("Done in " + (System.nanoTime() - time1) / 1000000 + " milliseconds");
        //TODO: Add mergesort
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
    protected ArrayList<ArrayList<String>> generatePaths(String w1, String w2, long startTime) {
        LevenshteinGraph g = new LevenshteinGraph(w1);
        while (true) {
            int gOSize = g.outerSize();
            System.out.println(gOSize);
            g.generateNewOuter(dictionary, lengthStartIndexes);
            if (gOSize == 0 || g.outerContains(w2)) {
               return new ArrayList<>();
            }
            if (PRINT_EXTRA) {
                //System.out.println("\n" + outer);
                //System.out.println("Searched Nodes: " + searched.size());
                System.out.println("Total Time:" + (System.nanoTime() - startTime) / 1000000);
            }
        }
    }
}
