/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: LevenshteinOneSided.java
Maintenance Log:
    Started. Wrote generatePaths and generateNewOuter, however, they are untested for now (20 Mar 2023 1:12)
*/

import java.io.*;
import java.nio.file.*;
import java.util.*;

public abstract class LevenshteinDualSided extends Levenshtein {
    public LevenshteinDualSided(String filename) throws IOException {
        super(new LevenshteinNode[(int) Files.lines(Paths.get("src/Dictionary.txt")).count()], new HashMap<>());
        Scanner s = new Scanner(new File(filename));
        int i = 0;
        while (s.hasNext()) {
            String word = s.next();
            dictionary[i] = new LevenshteinNode(word);
            lengthStartIndexes.putIfAbsent(word.length(), i);
            i++;
        }
    }

    @Override
    protected LevenshteinNode[] generatePaths(String w1, String w2, long startTime) {
        LevenshteinNode[] nodeStorage = Arrays.copyOf(dictionary, dictionary.length);
        HashSet<LevenshteinNode> searched1 = new HashSet<>();
        HashSet<LevenshteinNode> searched2 = new HashSet<>();
        HashSet<LevenshteinNode> outer1 = new HashSet<>(Arrays.asList(Levenshtein.binarySearch(nodeStorage, w1)));
        HashSet<LevenshteinNode> outer2 = new HashSet<>(Arrays.asList(Levenshtein.binarySearch(nodeStorage, w2)));
        while(true) {
            if (outer1.size() >= outer2.size()) {
                if (generateNewOuter(searched1, outer1, outer2, nodeStorage, startTime)) {
                    break;
                }
            } else {
                if (generateNewOuter(searched2, outer2, outer1, nodeStorage, startTime)) {
                    break;
                }
            }
        }
        return nodeStorage;
    }
    private boolean generateNewOuter(HashSet<LevenshteinNode> searched, HashSet<LevenshteinNode> outer,
                                     HashSet<LevenshteinNode> otherOuter, LevenshteinNode[] nodeStorage, long startTime) {
        HashSet<LevenshteinNode> newOuter = new HashSet<>();
        for (LevenshteinNode n: outer) {
            HashSet<LevenshteinNode> neighbors = n.findNeighbors(nodeStorage, lengthStartIndexes, searched, outer);
            for (LevenshteinNode neighbor : neighbors) {
                if(newOuter.contains(neighbor)) {
                    neighbor.addPrevious(n);
                } else {
                    newOuter.add(neighbor);
                }
            }
        }
        searched.addAll(outer);
        outer = newOuter;
        if (outer.isEmpty()) {
            return true;
        }
        for (LevenshteinNode n : outer) {
            if (otherOuter.contains(n)) {
                return true;
            }
        }
        if (PRINT_EXTRA) {
            System.out.println("Current Searched:" + searched.size() + "\n" + outer);
            System.out.println("Total Searched Nodes: " + searched.size());
            System.out.println((System.nanoTime() - startTime) / 1000000);
        }
        return false;
    }
}
