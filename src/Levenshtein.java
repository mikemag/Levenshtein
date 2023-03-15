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
*/

import java.io.*;
import java.util.*;

public class Levenshtein {
    public final ArrayList<String> dictionary;
    public final HashMap<Integer, Integer> lengthStarts;
    public Levenshtein(String filename) throws FileNotFoundException {
        dictionary = new ArrayList<>();
        lengthStarts = new HashMap<>();
        Scanner s = new Scanner(new File(filename));
        int i = 0;
        while (s.hasNext()) {
            String word = s.next();
            dictionary.add(word);
            lengthStarts.putIfAbsent(word.length(), i);
            i++;
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        long time1 = System.nanoTime();
        Levenshtein test = new Levenshtein("src/Dictionary.txt");
        System.out.println("Distance between 'monkey' and 'business': " + test.findDistance("monkey", "business", time1));
        System.out.println((System.nanoTime() - time1) / 1000000);
    }
    public int findDistance(String w1, String w2, long time1) {
        LinkedList<HashSet<LevenshteinNode>> g1 = new LinkedList<>();
        LinkedList<HashSet<LevenshteinNode>> g2 = new LinkedList<>();
        g1.add(new HashSet<>(Arrays.asList(new LevenshteinNode(w1))));
        g2.add(new HashSet<>(Arrays.asList(new LevenshteinNode(w2))));
        LinkedList<HashSet<LevenshteinNode>> currentGraph = g1;
        LinkedList<HashSet<LevenshteinNode>> otherGraph = g2;
        while (!intersects(g1.getLast(), g2.getLast())) {
            HashSet<LevenshteinNode> newNeighbors = new HashSet<>();
            boolean foundNeighbors = false;
            for (LevenshteinNode n : currentGraph.getLast()) {
                if (n.findNeighbors(dictionary, lengthStarts.getOrDefault(n.getWord().length() - 1, 0),
                        lengthStarts.getOrDefault(n.getWord().length() + 2, dictionary.size()))) {
                    foundNeighbors = true;
                    for (String w : n.getNeighbors()) {
                        LevenshteinNode neighborNode = new LevenshteinNode(w);
                        if (!graphContains(currentGraph, neighborNode)) {
                            newNeighbors.add(neighborNode);
                        }
                    }
                }
            }
            if (!foundNeighbors) {
                return -1;
            }
            currentGraph.add(newNeighbors);
            LinkedList<HashSet<LevenshteinNode>> temp = currentGraph;
            currentGraph = otherGraph;
            otherGraph = temp;
            System.out.println("Graph 1 Size: " + graphSize(g1));
            System.out.println("Graph 2 Size: " + graphSize(g2));
            System.out.println((System.nanoTime() - time1) / 1000000);
        }
        return g1.size() + g2.size() - 2;
    }
    public int findDistanceAlt(String w1, String w2) {
        LinkedList<HashSet<LevenshteinNode>> g1 = new LinkedList<>();
        g1.add(new HashSet<>(Arrays.asList(new LevenshteinNode(w1))));
        LevenshteinNode destination = new LevenshteinNode(w2);
        while (!g1.getLast().contains(destination)) {
            HashSet<LevenshteinNode> newNeighbors = new HashSet<>();
            boolean foundNeighbors = false;
            for (LevenshteinNode n : g1.getLast()) {
                if (n.findNeighbors(dictionary, lengthStarts.getOrDefault(n.getWord().length() - 1, 0),
                        lengthStarts.getOrDefault(n.getWord().length() + 2, dictionary.size()))) {
                    foundNeighbors = true;
                    for (String w : n.getNeighbors()) {
                        LevenshteinNode neighborNode = new LevenshteinNode(w);
                        if (!graphContains(g1, neighborNode)) {
                            newNeighbors.add(neighborNode);
                        }
                    }
                }
            }
            if (!foundNeighbors) {
                return -1;
            }
            g1.add(newNeighbors);
            System.out.println("Graph 1 Size: " + graphSize(g1));
        }
        return g1.size() - 1;
    }
    public boolean intersects(HashSet<LevenshteinNode> hs1, HashSet<LevenshteinNode> hs2) {
        for (LevenshteinNode n : hs1) {
            if (hs2.contains(n)) {
                return true;
            }
        }
        return false;
    }
    public boolean graphContains(LinkedList<HashSet<LevenshteinNode>> g, LevenshteinNode n) {
        for(HashSet<LevenshteinNode> hs : g) {
            if (hs.contains(n)) {
                return true;
            }
        }
        return false;
    }
    public int graphSize(LinkedList<HashSet<LevenshteinNode>> g) {
        int size = 0;
        for (HashSet<LevenshteinNode> hs : g) {
            size += hs.size();
        }
        return size;
    }
}
