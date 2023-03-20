import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public abstract class LevenshteinDualSided extends Levenshtein<LevenshteinNodePrevOnly> {
    public LevenshteinDualSided(String filename) throws IOException {
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

    @Override
    protected LevenshteinNodePrevOnly[] generatePaths(String w1, String w2, long startTime) {
        LevenshteinNodePrevOnly[] nodeStorage = Arrays.copyOf(dictionary, dictionary.length);
        HashSet<LevenshteinNodePrevOnly> searched1 = new HashSet<>();
        HashSet<LevenshteinNodePrevOnly> searched2 = new HashSet<>();
        HashSet<LevenshteinNodePrevOnly> outer1 = new HashSet<>(Arrays.asList((LevenshteinNodePrevOnly)Levenshtein.binarySearch(nodeStorage, w1)));
        HashSet<LevenshteinNodePrevOnly> outer2 = new HashSet<>(Arrays.asList((LevenshteinNodePrevOnly)Levenshtein.binarySearch(nodeStorage, w2)));
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
    private boolean generateNewOuter(HashSet<LevenshteinNodePrevOnly> searched, HashSet<LevenshteinNodePrevOnly> outer,
                                     HashSet<LevenshteinNodePrevOnly> otherOuter, LevenshteinNodePrevOnly[] nodeStorage, long startTime) {
        HashSet<LevenshteinNodePrevOnly> newOuter = new HashSet<>();
        for (LevenshteinNodePrevOnly n: outer) {
            HashSet<LevenshteinNodePrevOnly> neighbors = n.findNeighbors(nodeStorage, lengthStartIndexes, searched, outer);
            for (LevenshteinNodePrevOnly neighbor : neighbors) {
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
        for (LevenshteinNodePrevOnly n : outer) {
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
