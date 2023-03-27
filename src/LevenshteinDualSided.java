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
*/

//TODO: Remove all LevenshteinNode dependence

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LevenshteinDualSided extends Levenshtein {
    public static void main(String[] args) throws IOException {
        Levenshtein test = new LevenshteinDualSided("src/Dictionary.txt");
        long time1 = System.nanoTime();
        String w1 = "monkey";
        String w2 = "business";
        test.generatePaths(w1, w2, time1);
        System.out.println("Done in " + (System.nanoTime() - time1)/1000000 + " milliseconds.");
    }
    public LevenshteinDualSided(String filename) throws IOException {
        super(new String[(int) Files.lines(Paths.get("src/Dictionary.txt")).count()], new HashMap<>());
        Scanner s = new Scanner(new File(filename));
        int i = 0;
        while (s.hasNext()) {
            String word = s.next();
            dictionary[i] = word;
            lengthStartIndexes.putIfAbsent(word.length(), i);
            i++;
        }
        //TODO: Add mergesort
    }

    @Override
    protected ArrayList<ArrayList<String>> generatePaths(String w1, String w2, long startTime) {
        LevenshteinGraph g1 = new LevenshteinGraph(w1);
        LevenshteinGraph g2 = new LevenshteinGraph(w2);
        while(true) {
            int g1OSize = g1.outerSize();
            int g2OSize = g2.outerSize();
            System.out.println(g1OSize + " " + g2OSize);
            if (g1OSize <= g2OSize) {
                g1.generateNewOuter(dictionary, lengthStartIndexes);
            } else {
                g2.generateNewOuter(dictionary, lengthStartIndexes);
            }
            if (g1OSize == 0 || g2OSize == 0 || g1.outerIntersects(g2)) {
                //System.out.println("Start set size: "  + (searched1.size() + outer1.size()));
                //System.out.println("End set size: "  + (searched2.size() + outer2.size()));
                return new ArrayList<>();
            }
            if (PRINT_EXTRA) {
                //System.out.println("Current Searched: " + searched.size() + "\n" + outer);
                //System.out.println("Total Searched Nodes: " + searched.size());
                System.out.println((System.nanoTime() - startTime) / 1000000);
            }
        }
    }
}
