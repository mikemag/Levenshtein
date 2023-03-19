/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: Levenshtein.java
Maintenance Log:
    Started. Already had ideas as to how I was going to find the Levenshtein between two words (13 Mar 2023 23:07)
        Started the findDistance method. Currently, the LevenshteinNodes are stored in a LinkedList of HashSets (Pi Day Bomb Squad)
        It works for some pairs of words, but underestimates for others. After some testing, looks like a problem with isNeighboring (1:27)
        It works for all tested pairs except monkey -> business. Going to have to work that out and make the algorithm more efficient as it takes nearly 9 seconds for dog and quack (2:05)
    Attempted to fix findDistance but failed so miserably that I have to essentially restart (15 Mar 2023 0:45)
    It now finds monkey -> business in 25 seconds, using the LinkedList of HashSets to store each layer (15 Mar 2023 10:57)
    Restructuring how the data is stored to allow it to find all paths. Changing a HashMap to a TreeMap caused it to break (16 Mar 2023 10:55)
    Now takes about 13 seconds to go from business to monkey, assuming the words are fed in the correct order (17 Mar 2023 15:25)
*/

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Levenshtein {
    private final LevenshteinNode[] dictionary;
    private final Map<Integer, Integer> lengthStartIndexes;
    public Levenshtein(String filename) throws IOException {;
        dictionary = new LevenshteinNode[(int)Files.lines(Paths.get("src/Dictionary.txt")).count()];
        lengthStartIndexes = new HashMap<>();
        Scanner s = new Scanner(new File(filename));
        int i = 0;
        while (s.hasNext()) {
            String word = s.next();
            dictionary[i] = new LevenshteinNode(word);
            lengthStartIndexes.putIfAbsent(word.length(), i);
            i++;
        }
    }
    public static void main(String[] args) throws IOException {
        Levenshtein test = new Levenshtein("src/Dictionary.txt");
        long time1 = System.nanoTime();
        String w1 = "business";
        String w2 = "monkey";
        System.out.println("Distance between '" + w1 + "' and '" + w2 + "': " + test.findDistance(w1, w2, time1));
        System.out.println((System.nanoTime() - time1) / 1000000);
    }
    public int findDistance(String w1, String w2, long time1) {
        LevenshteinNode[] nodeStorage = Arrays.copyOf(dictionary, dictionary.length);
        HashSet<LevenshteinNode> searchedNodes = new HashSet<>();
        HashSet<LevenshteinNode> outer = new HashSet<>();
        LevenshteinNode endWord = null;
        for (LevenshteinNode n : nodeStorage) {
            if (n.getWord().equals(w1)) {
                outer.add(n);
            } else if (n.getWord().equals(w2)) {
                endWord = n;
            }
        }
        int distance = 0;
        while (!outer.contains(endWord)) {
            HashSet<LevenshteinNode> newOuter = new HashSet<>();
            for (LevenshteinNode n : outer) {
                HashSet<LevenshteinNode> neighbors = n.findNeighbors(nodeStorage, lengthStartIndexes, searchedNodes, outer);
                for (LevenshteinNode neighbor: neighbors) {
                    if (newOuter.contains(neighbor)) {
                        neighbor.addPrevious(n);
                    } else {
                        newOuter.add(neighbor);
                    }
                }
            }
            if (newOuter.isEmpty()) {
                return -1;
            }
            System.out.println("\n" + outer);
            searchedNodes.addAll(outer);
            outer = newOuter;
            System.out.println("Searched Nodes: " + searchedNodes.size());
            System.out.println((System.nanoTime() - time1) / 1000000);
            distance++;
        }
        return distance;
    }
    public void printAllPaths(LevenshteinNode n) {
        for (LevenshteinNode p : n.getPrevious()) {
            printAllPaths(n);
        }
    }
}
