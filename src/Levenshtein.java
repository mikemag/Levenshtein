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
*/

import java.io.*;
import java.util.*;
import java.util.function.Predicate;

public class Levenshtein {
    private final ArrayList<LevenshteinNode> dictionary;
    private final Map<Integer, Integer> lengthStartIndexes;
    public Levenshtein(String filename) throws FileNotFoundException {
        dictionary = new ArrayList<>();
        lengthStartIndexes = new HashMap<>();
        Scanner s = new Scanner(new File(filename));
        int i = 0;
        while (s.hasNext()) {
            String word = s.next();
            dictionary.add(new LevenshteinNode(word));
            lengthStartIndexes.putIfAbsent(word.length(), i);
            i++;
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        long time1 = System.nanoTime();
        Levenshtein test = new Levenshtein("src/Dictionary.txt");
        String w1 = "monkey";
        String w2 = "business";
        System.out.println("Distance between '" + w1 + "' and '" + w2 + "': " + test.findDistance(w1, w2, time1));
        System.out.println((System.nanoTime() - time1) / 1000000);
    }
    public int findDistance(String w1, String w2, long time1) {
        LevenshteinNode[] nodeStorage = new LevenshteinNode[dictionary.size()];
        nodeStorage = dictionary.toArray(nodeStorage);
        HashSet<LevenshteinNode> g1 = new HashSet<>();
        HashSet<LevenshteinNode> g2 = new HashSet<>();
        HashSet<LevenshteinNode> g1Outer = new HashSet<>(Arrays.asList(new LevenshteinNode(w1)));
        HashSet<LevenshteinNode> g2Outer = new HashSet<>(Arrays.asList(new LevenshteinNode(w2)));
        HashSet<LevenshteinNode> currentGraph = g1;
        HashSet<LevenshteinNode> otherGraph = g2;
        HashSet<LevenshteinNode> currentOuter = g1Outer;
        HashSet<LevenshteinNode> otherOuter = g2Outer;
        while (!intersects(g1Outer, g2Outer)) {
            HashSet<LevenshteinNode> currentNeoOuter = new HashSet<>();
            for (LevenshteinNode n : currentOuter) {
                Set<LevenshteinNode> nNeighbors = n.findNeighbors(nodeStorage, lengthStartIndexes);
                if (!nNeighbors.isEmpty()) {
                    for (LevenshteinNode nNeighbor: nNeighbors) {
                        if (currentOuter.contains(nNeighbor)) {
                            if (currentNeoOuter.contains(nNeighbor)) {
                                nNeighbor.addPrevious(n);
                            } else {
                                currentNeoOuter.add(nNeighbor);
                            }
                        }
                    }
                }
            }
            if (currentNeoOuter.isEmpty()) {
                return -1;
            }
            System.out.println(currentNeoOuter);
            currentGraph.addAll(currentOuter);
            currentOuter.clear();
            currentOuter.addAll(currentNeoOuter);
            HashSet<LevenshteinNode> temp = currentGraph;
            currentGraph = otherGraph;
            otherGraph = temp;
            HashSet<LevenshteinNode> temp2 = currentOuter;
            currentOuter = otherOuter;
            otherOuter = temp2;
            System.out.println("Graph 1 Size: " + g1.size());
            System.out.println("Graph 2 Size: " + g2.size());
            System.out.println((System.nanoTime() - time1) / 1000000);
        }
        return g1.size() + g2.size() - 2;
    }
    /*public int findDistanceAlt(String w1, String w2) {
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
    }*/
    public boolean intersects(HashSet<LevenshteinNode> hs1, HashSet<LevenshteinNode> hs2) {
        for (LevenshteinNode n : hs1) {
            if (hs2.contains(n)) {
                return true;
            }
        }
        return false;
    }
}
