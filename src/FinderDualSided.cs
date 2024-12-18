public class FinderDualSided : LevenshteinPathFinder {
    /**
     * TODO: ADD PROPER DESCRIPTION
     * @param word1 Starting word.
     * @param word2 Ending word.
     * @param startTime Approximate time (gotten from System.nanoTime()) that this function was called.
     * @return A TreeSet of LinkedLists, with each list representing a unique levenshtein path between word1 and word2
     */
    public override List<LinkedList<int>> generatePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database, long startTime) {
        if (wordIndex1 == wordIndex2) {
            List<LinkedList<int>> path = new List<LinkedList<int>>();
            LinkedList<int> pathList = new LinkedList<int>();
            pathList.AddFirst(wordIndex1);
            path.Add(pathList);
            return path;
        }

        LevenshteinGraph graph1 = new LevenshteinGraph(wordIndex1);
        LevenshteinGraph graph2 = new LevenshteinGraph(wordIndex2);

        while(true) {
            int graph1OSize = graph1.outerSize();
            int graph2OSize = graph2.outerSize();
            bool generateNewOuterSucceeded;

            if (graph1OSize <= graph2OSize) {
                generateNewOuterSucceeded = graph1.generateNewOuter(database);
            } else {
                generateNewOuterSucceeded = graph2.generateNewOuter(database);
            }

            if (!generateNewOuterSucceeded) {
                return null;
            } 

            if (PRINT_EXTRA) {
                Console.WriteLine("Start Outer: " + graph1OSize);
                Console.WriteLine("Target Outer: " + graph2OSize);
                Console.WriteLine("Start Searched: " + graph1.searchedSize());
                Console.WriteLine("Target Searched: " + graph2.searchedSize());
                Console.WriteLine("Total Searched: " + (graph1OSize + graph2OSize + graph1.searchedSize() + graph2.searchedSize()));
                Console.WriteLine("Current Time: " + (DateTime.Now.Nanosecond - startTime) / 1000000 + "\n");
            }

            List<int> outerIntersection = LevenshteinGraph.outerIntersection(graph1, graph2);

            if (outerIntersection.Count != 0) {
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
    private static List<LinkedList<int>> graphsToPaths(LevenshteinGraph graph1, LevenshteinGraph graph2, int wordIndex1, int wordIndex2, List<int> intersection) {
        List<LinkedList<int>> pathsToReturn = new List<LinkedList<int>>();
        List<LinkedList<int>> graph1Paths = new List<LinkedList<int>>(); 
        List<LinkedList<int>> graph2Paths = new List<LinkedList<int>>();
        foreach (int word in intersection) {
            graph1Paths.AddRange(graph1.allPathsBetween(wordIndex1, word, false));
            graph2Paths.AddRange(graph2.allPathsBetween(wordIndex2, word, true));
        }
        // For each word in intersection, there may be multiple paths to it from the starting word and there may be multiple to the ending word.
        // To account for this, each rootPath is indexed and then a unique path for each destinationPath is added to pathsToReturn.
        // To make sure only legal paths are added, the rootPath and destinationPath are first checked for a shared intersection word.
        foreach (LinkedList<int> rootPath in graph1Paths) {
            foreach (LinkedList<int> destinationPath in graph2Paths) {
                LinkedList<int> pathToAdd = new LinkedList<int>(rootPath);
                if (pathToAdd.Last.Value == destinationPath.First.Value) {
                    pathToAdd.RemoveLast();

                    /*pathToAdd = new LinkedList<int>(pathToAdd.Concat(destinationPath));*/
                    foreach (int node in destinationPath) {
                        pathToAdd.AddLast(node);
                    }
                    pathsToReturn.Add(pathToAdd);
                }
            }
        }
        return pathsToReturn;
    }
}
