/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: LevenshteinOneSided.java
Maintenance Log:
    Started. Wrote generatePaths and generateNewOuter, however, they are untested for now (20 Mar 2023 1:12)
    Generates the paths from Monkey to Business in about 1 second with my algorithm and my isNeighboring method (Does so in 0.8 with Mike's).
    Converted the HashSets to HashMaps of Levenshtein Nodes with a String key (20 Mar 2023 20:54)
    Working on converting the HashMaps to LevenshteinGraphs (23 Mar 2023 10:57)
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
        LevenshteinNode[] paths = test.generatePaths(w1, w2, time1);
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

    //@Override
    /*protected LevenshteinNode[] generatePaths(String w1, String w2, long startTime) {
        HashMap<String, LevenshteinNode> searched1 = new HashMap<>();
        HashMap<String, LevenshteinNode> searched2 = new HashMap<>();
        HashMap<String, LevenshteinNode> outer1 = new HashMap<>();
        HashMap<String, LevenshteinNode> outer2 = new HashMap<>();
        outer1.put(w1, new LevenshteinNode(w1));
        outer2.put(w2, new LevenshteinNode(w2));
        while(true) {
            System.out.println(outer1.size() + " " + outer2.size());
            HashMap<String, LevenshteinNode> outer;
            HashMap<String, LevenshteinNode> otherOuter;
            HashMap<String, LevenshteinNode> searched;
            if (outer1.size() <= outer2.size()) {
                searched1.putAll(outer1);
                outer1 = generateNewOuter(searched1, outer1);
                outer = outer1;
                otherOuter = outer2;
                searched = searched1;
            } else {
                searched2.putAll(outer2);
                outer2 = generateNewOuter(searched2, outer2);
                outer = outer2;
                otherOuter = outer1;
                searched = searched2;
            }
            if (outer.isEmpty() || containsAny(outer, otherOuter)) {
                System.out.println("Start set size: "  + (searched1.size() + outer1.size()));
                System.out.println("End set size: "  + (searched2.size() + outer2.size()));
                return new LevenshteinNode[0];
            }
            if (PRINT_EXTRA) {
                System.out.println("Current Searched: " + searched.size() + "\n" + outer);
                System.out.println("Total Searched Nodes: " + searched.size());
                System.out.println((System.nanoTime() - startTime) / 1000000);
            }
        }
    }*/

    @Override
    protected LevenshteinNode[] generatePaths(String w1, String w2, long startTime) {
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
            if (g1OSize == 0 || g2OSize == 0 || LevenshteinGraph.outerIntersects(g1, g2)) {
                //System.out.println("Start set size: "  + (searched1.size() + outer1.size()));
                //System.out.println("End set size: "  + (searched2.size() + outer2.size()));
                return new LevenshteinNode[0];
            }
            if (PRINT_EXTRA) {
                //System.out.println("Current Searched: " + searched.size() + "\n" + outer);
                //System.out.println("Total Searched Nodes: " + searched.size());
                System.out.println((System.nanoTime() - startTime) / 1000000);
            }
        }
    }

    /*private HashMap<String, LevenshteinNode> generateNewOuter(HashMap<String, LevenshteinNode> searched, HashMap<String, LevenshteinNode> outer) {
        HashMap<String, LevenshteinNode> newOuter = new HashMap<>();
        for (LevenshteinNode n : outer.values()) {
            HashSet<LevenshteinNode> neighbors = n.findNeighbors(dictionary, lengthStartIndexes, new HashSet<>(searched.values()), new HashSet<>(outer.values()));
            for (LevenshteinNode neighbor : neighbors) {
                if (newOuter.keySet().contains(neighbor)) {
                    neighbor.addPrevious(n);
                } else {
                    newOuter.put(neighbor.getWord(), neighbor);
                }
            }
        }
        return newOuter;
    }

    private boolean containsAny(HashMap<String, LevenshteinNode> s1, HashMap<String, LevenshteinNode> s2) {
        if (s1.size() > s2.size()) {
            HashMap<String, LevenshteinNode> t = s1;
            s1 = s2;
            s2 = t;
        }
        for (String w : s1.keySet()) {
            if (s2.keySet().contains(w)) {
                return true;
            }
        }
        return false;
    }*/

    @Override
    public int getDistance(String w1, String w2) {
        return 0;
    }

    @Override
    public List<String> getAllPaths(String w1, String w2) {
        return null;
    }
}
