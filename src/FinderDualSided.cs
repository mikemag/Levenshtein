public class FinderDualSided : LevenshteinPathFinder {
    public override List<int[]> GeneratePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database) {
        if (wordIndex1 == wordIndex2) {
            List<int[]> paths = new List<int[]>();
            int[] path = { wordIndex1 };
            paths.Add(path);
            return paths;
        }

        LevenshteinGraph graph1 = new LevenshteinGraph(wordIndex1);
        LevenshteinGraph graph2 = new LevenshteinGraph(wordIndex2);

        while(true) {
            int graph1OSize = graph1.OuterCount;
            int graph2OSize = graph2.OuterCount;
            bool generateNewOuterSucceeded;

            if (graph1OSize <= graph2OSize) {
                generateNewOuterSucceeded = graph1.GenerateNewOuter(database);
            } else {
                generateNewOuterSucceeded = graph2.GenerateNewOuter(database);
            }

            if (!generateNewOuterSucceeded) {
                return null;
            } 

#pragma warning disable CS0162
            if (PRINT_EXTRA) {
                Console.WriteLine("Start Outer: " + graph1OSize);
                Console.WriteLine("Target Outer: " + graph2OSize);
            }
#pragma warning restore CS0162

            List<int> outerIntersection = LevenshteinGraph.OuterIntersection(graph1, graph2);

            if (outerIntersection.Count != 0) {
                return GraphsToPaths(graph1, graph2, wordIndex1, wordIndex2, outerIntersection);
            }
        }
    }

    /**
     * On each graph, the paths between the root word and the intersection words are found.
     * Then, these paths are "stitched together", such that all unique paths from the staring word to the ending word.
     */
    private static List<int[]> GraphsToPaths(LevenshteinGraph graph1, LevenshteinGraph graph2, int wordIndex1, int wordIndex2, List<int> intersection) {
        List<int[]> pathsToReturn = new List<int[]>();
        List<int[]> graph1Paths = new List<int[]>();
        List<int[]> graph2Paths = new List<int[]>();

        foreach (int word in intersection) {
            graph1Paths.AddRange(graph1.AllPathsBetween(wordIndex1, word, false));
            graph2Paths.AddRange(graph2.AllPathsBetween(wordIndex2, word, true));
        }
        // For each word in intersection, there may be multiple paths to it from the starting word and there may be multiple to the ending word.
        // To account for this, each rootPath is indexed and then a unique path for each destinationPath is added to pathsToReturn.
        // To make sure only legal paths are added, the rootPath and destinationPath are first checked for a shared intersection word.
        foreach (int[] rootPath in graph1Paths) {
            foreach (int[] destinationPath in graph2Paths) {
                if (rootPath[graph1.Depth - 1] == destinationPath[0]) {
                    int[] pathToAdd = new int[graph1.Depth + graph2.Depth - 1];
                    rootPath.CopyTo(pathToAdd, 0);
                    destinationPath.CopyTo(pathToAdd, graph1.Depth - 1);
                    pathsToReturn.Add(pathToAdd);
                }
            }
        }

        return pathsToReturn;
    }
}
