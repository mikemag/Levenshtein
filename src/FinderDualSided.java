import java.util.*;

public class FinderDualSided extends LevenshteinPathFinder {
    /**
     * TODO: ADD PROPER DESCRIPTION
     * @param word1 Starting word.
     * @param word2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between word1 and word2
     */
    @Override
    public ArrayList<LinkedList<Integer>> generatePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database, long startTime) {
        if (wordIndex1 == wordIndex2) {
            ArrayList<LinkedList<Integer>> path = new ArrayList();
            path.add(new LinkedList<>(Arrays.asList(wordIndex1)));
            return path;
        }

        LevenshteinGraph graph1 = new LevenshteinGraph(wordIndex1);
        LevenshteinGraph graph2 = new LevenshteinGraph(wordIndex2);

        while(true) {
            int graph1OSize = graph1.outerSize();
            int graph2OSize = graph2.outerSize();
            boolean generateNewOuterSucceeded;

            if (graph1OSize <= graph2OSize) {
                generateNewOuterSucceeded = graph1.generateNewOuter(database);
            } else {
                generateNewOuterSucceeded = graph2.generateNewOuter(database);
            }

            if (!generateNewOuterSucceeded) {
                return null;
            } 

            if (PRINT_EXTRA) {
                System.out.println("Start Outer: " + graph1OSize);
                System.out.println("Target Outer: " + graph2OSize);
                System.out.println("Start Searched: " + graph1.searchedSize());
                System.out.println("Target Searched: " + graph2.searchedSize());
                System.out.println("Total Searched: " + (graph1OSize + graph2OSize + graph1.searchedSize() + graph2.searchedSize()));
                System.out.println("Current Time: " + (System.nanoTime() - startTime) / 1000000 + "\n");
            }

            ArrayList<Integer> outerIntersection = LevenshteinGraph.outerIntersection(graph1, graph2);

            if (outerIntersection.size() != 0) {
                return graphsToPaths(graph1, graph2, wordIndex1, wordIndex2, outerIntersection);
            }
        }
    }

    /**
     * On each graph, the paths between the root word and the intersection words are found.
     * Then, these paths are "stitched together", such that all unique paths from the staring word to the ending word.
     * @param graph1 Starting word graph.
     * @param graph2 Ending word graph.
     * @param word1 Starting word.
     * @param word2 Ending word.
     * @param intersection Set of words which are shared between the outer layers of graph1 and graph2.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between word1 and word2.
     */
    private static ArrayList<LinkedList<Integer>> graphsToPaths(LevenshteinGraph graph1, LevenshteinGraph graph2, int wordIndex1, int wordIndex2, ArrayList<Integer> intersection) {
        ArrayList<LinkedList<Integer>> pathsToReturn = new ArrayList();
        ArrayList<LinkedList<Integer>> graph1Paths = new ArrayList(); 
        ArrayList<LinkedList<Integer>> graph2Paths = new ArrayList();
        for (int word : intersection) {
            graph1Paths.addAll(graph1.allPathsBetween(wordIndex1, word, false));
            graph2Paths.addAll(graph2.allPathsBetween(wordIndex2, word, true));
        }
        // For each word in intersection, there may be multiple paths to it from the starting word and there may be multiple to the ending word.
        // To account for this, each rootPath is indexed and then a unique path for each destinationPath is added to pathsToReturn.
        // To make sure only legal paths are added, the rootPath and destinationPath are first checked for a shared intersection word.
        for (LinkedList<Integer> rootPath : graph1Paths) {
            for (LinkedList<Integer> destinationPath : graph2Paths) {
                LinkedList<Integer> pathToAdd = (LinkedList<Integer>)rootPath.clone();
                if (pathToAdd.removeLast().equals(destinationPath.getFirst())) {
                    pathToAdd.addAll(destinationPath);
                    pathsToReturn.add(pathToAdd);
                }
            }
        }
        return pathsToReturn;
    }
}
