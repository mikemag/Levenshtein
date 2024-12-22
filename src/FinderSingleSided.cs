public class FinderSingleSided : LevenshteinPathFinder {
    public override List<int[]> GeneratePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database) {
        if (wordIndex1 == wordIndex2) {
            List<int[]> paths = new List<int[]>();
            int[] path = { wordIndex1 };
            paths.Add(path);
            return paths;
        }
        LevenshteinGraph g = new LevenshteinGraph(wordIndex1);
        while (true) {
            bool generateNewOuterSucceeded = g.GenerateNewOuter(database);
            if (PRINT_EXTRA) {
                Console.WriteLine("Outer: " + g.OuterCount);
                Console.WriteLine("Searched: " + g.SearchedCount);
            }

            if (!generateNewOuterSucceeded) {
                return null;
            }
            
            if (g.OuterContains(wordIndex2)) {
                return g.AllPathsBetween(wordIndex1, wordIndex2, false);
            }
        }
    }
}
