public class FinderDualSided : LevenshteinPathFinder {
    public override List<int[]> GeneratePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database) {
        if (wordIndex1 == wordIndex2) {
            List<int[]> paths = new List<int[]>();
            int[] path = { wordIndex1 };
            paths.Add(path);
            return paths;
        }

        LevenshteinBFSGraph graph1 = new DictionaryBFSGraph(wordIndex1, database);
        LevenshteinBFSGraph graph2 = new DictionaryBFSGraph(wordIndex2, database);

        while(true) {
            bool generateNewFrontierSucceeded;

            if (graph1.Frontier.Count <= graph2.Frontier.Count) {
                generateNewFrontierSucceeded = graph1.GenerateNewFrontier();
            } else {
                generateNewFrontierSucceeded = graph2.GenerateNewFrontier();
            }

            if (!generateNewFrontierSucceeded) {
                return null;
            } 

#pragma warning disable CS0162
            if (PRINT_EXTRA) {
                Console.WriteLine("Start Frontier: " + graph1.Frontier.Count);
                Console.WriteLine("Target Frontier: " + graph2.Frontier.Count);
            }
#pragma warning restore CS0162

            List<int> frontierIntersection = graph1.FrontierIntersection(graph2);

            if (frontierIntersection.Count != 0) {
                return GraphsToPaths(graph1, graph2, frontierIntersection);
            }
        }
    }

    /**
     * On each graph, the paths between the root word and the intersection words are found.
     * Then, these paths are "stitched together", such that all unique paths from the staring word to the ending word.
     */
    private static List<int[]> GraphsToPaths(LevenshteinBFSGraph graph1, LevenshteinBFSGraph graph2, List<int> intersection) {
        List<int[]> pathsToReturn = new List<int[]>();
        List<int[]> graph1Paths = new List<int[]>();
        List<int[]> graph2Paths = new List<int[]>();

        foreach (int word in intersection) {
            graph1Paths.AddRange(graph1.AllPathsTo(word, false));
            graph2Paths.AddRange(graph2.AllPathsTo(word, true));
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
