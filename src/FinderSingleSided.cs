public class FinderSingleSided : LevenshteinPathFinder {
    public override List<int[]> GeneratePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database) {
        if (wordIndex1 == wordIndex2) {
            List<int[]> paths = new List<int[]>();
            int[] path = { wordIndex1 };
            paths.Add(path);
            return paths;
        }
        LevenshteinBFSGraph graph = new DictionaryBFSGraph(wordIndex1, database);
        while (true) {
            bool generateNewFrontierSucceeded = graph.GenerateNewFrontier();

#pragma warning disable CS0162
            if (PRINT_EXTRA) {
                Console.WriteLine("Frontier: " + graph.Frontier.Count);
            }
#pragma warning restore CS0162

            if (!generateNewFrontierSucceeded) {
                return null;
            }
            
            if (graph.Frontier.Contains(wordIndex2)) {
                return graph.AllPathsTo(wordIndex2, false);
            }
        }
    }
}
