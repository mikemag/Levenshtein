public class MetaAnalyzer {
    public static void Analyze(LevenshteinDatabase database) {
        const int count = 1;
        List<String> paths = new List<String>();

        for (int i = 0; i < count; i++) {
            AppendPathsFrom(i, database, paths);
        }

        foreach (String path in paths) {
            Console.WriteLine(path);
        }
    }

    public static void AppendPathsFrom(int wordIndex, LevenshteinDatabase database, List<String> paths) {
        LevenshteinGraph graph = new LevenshteinGraph(wordIndex);

        while (graph.GenerateNewOuter(database)) {
            paths.Add(graph.OuterPathString(database));
        }
    }
}
