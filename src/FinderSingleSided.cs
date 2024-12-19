public class FinderSingleSided : LevenshteinPathFinder {
    public override List<LinkedList<int>> generatePaths(int wordIndex1, int wordIndex2, LevenshteinDatabase database, long startTime) {
        if (wordIndex1 == wordIndex2) {
            List<LinkedList<int>> path = new List<LinkedList<int>>();
            LinkedList<int> pathList = new LinkedList<int>();
            pathList.AddFirst(wordIndex1);
            path.Add(new LinkedList<int>(pathList));
            return path;
        }
        LevenshteinGraph g = new LevenshteinGraph(wordIndex1);
        while (true) {
            bool generateNewOuterSucceeded = g.generateNewOuter(database);
            if (PRINT_EXTRA) {
                Console.WriteLine("Outer: " + g.outerSize());
                Console.WriteLine("Searched: " + g.searchedSize());
                Console.WriteLine("Total Searched: " + (g.outerSize() + g.searchedSize()));
                Console.WriteLine("Current Time: " + (DateTime.Now.Nanosecond - startTime) / 1000000 + "\n");
            }

            if (!generateNewOuterSucceeded) {
                return null;
            }
            
            if (g.outerContains(wordIndex2)) {
                return g.allPathsBetween(wordIndex1, wordIndex2, false);
            }
        }
    }
}
