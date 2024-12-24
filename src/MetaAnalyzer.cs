public class MetaAnalyzer {
    public static void Analyze(LevenshteinDatabase database) {
        const int count = 1;
        FileStream output = new FileStream("/home/fathom/temp2.txt", FileMode.Create);
        GraphToBinary(count, 1, 0, database, output);

    }

    private static void GraphToBinary(int max, int partitions, int offset, LevenshteinDatabase database, FileStream output) {
        MemoryStream graphStream = new MemoryStream();
        BinaryWriter writer = new BinaryWriter(graphStream);

        for (int i = offset; i < max; i += partitions) {
            LevenshteinGraph graph = new LevenshteinGraph(i);

            while (graph.GenerateNewOuter(database)) {
                graph.WriteOuterBinary(database.Words.Count(), writer);
            }
        }
        writer.Close();

        output.Write(graphStream.ToArray());
        graphStream.Dispose();
        writer.Dispose();
        }

        }
    }

    private static void MakeGraphDiagnostics(int root, LevenshteinDatabase database, List<PathDiagnostics> maxLengths, List<PathDiagnostics> maxPaths) {
        Dictionary<int, List<int[]>> pathDictionary = new Dictionary<int, List<int[]>>();
        LevenshteinGraph graph = new LevenshteinGraph(root);

        bool generateFirstOuterSucceeded = graph.GenerateNewOuter(database);

        foreach (int key in graph.OuterKeys) {
            pathDictionary.Add(key, graph.AllPathsBetween(root, key, false));
        }

        if (!generateFirstOuterSucceeded) {
            return;
        }

        while (graph.GenerateNewOuter(database)) {
            foreach(int outerWord in graph.OuterKeys) {
                int numPaths = graph.NumberOfPathsFrom(outerWord);
                if (numPaths >= maxPaths[0].count) {
                    if (numPaths > maxPaths[0].count) {
                        maxPaths.Clear();
                    };
                    maxPaths.Add(new PathDiagnostics(graph.Depth, numPaths, root, outerWord));
                }
            }
        }

        if (graph.Depth - 1 >= maxLengths[0].length) {
            if (graph.Depth - 1 > maxLengths[0].length) {
                maxLengths.Clear();
            }
            foreach (int furthestWord in graph.OuterKeys) {
                maxLengths.Add(new PathDiagnostics(graph.Depth - 1, graph.NumberOfPathsFrom(furthestWord), root, furthestWord));
            }
        }
    }

    public struct PathDiagnostics {
        public int length;
        public int count;
        public int word1;
        public int word2;

        public PathDiagnostics(int length, int count, int word1, int word2) {
            this.length = length;
            this.count = count;
            this.word1 = word1;
            this.word2 = word2;
        }

        public override String ToString() {
            return "Length: " + length + "\nCount: " + count + "\nWord1: " + word1 + "\nWord2: " + word2;
        }
    }
}
