public class FinderDualSided : LevenshteinPathFinder {
    public override List<LinkedList<int>> GeneratePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database, long startTime) {
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

            if (PRINT_EXTRA) {
                Console.WriteLine("Start Outer: " + graph1OSize);
                Console.WriteLine("Target Outer: " + graph2OSize);
                Console.WriteLine("Start Searched: " + graph1.SearchedCount);
                Console.WriteLine("Target Searched: " + graph2.SearchedCount);
                Console.WriteLine("Total Searched: " + (graph1OSize + graph2OSize + graph1.SearchedCount + graph2.SearchedCount));
                Console.WriteLine("Current Time: " + (DateTime.Now.Nanosecond - startTime) / 1000000 + "\n");
            }

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
    private static List<LinkedList<int>> GraphsToPaths(LevenshteinGraph graph1, LevenshteinGraph graph2, int wordIndex1, int wordIndex2, List<int> intersection) {
        List<LinkedList<int>> pathsToReturn = new List<LinkedList<int>>();
        List<LinkedList<int>> graph1Paths = new List<LinkedList<int>>(); 
        List<LinkedList<int>> graph2Paths = new List<LinkedList<int>>();
        foreach (int word in intersection) {
            graph1Paths.AddRange(graph1.AllPathsBetween(wordIndex1, word, false));
            graph2Paths.AddRange(graph2.AllPathsBetween(wordIndex2, word, true));
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
