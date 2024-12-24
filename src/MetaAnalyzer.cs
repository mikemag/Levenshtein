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

    public static void AppendPathsFrom(int wordIndex, LevenshteinDatabase database, List<String> paths) {
        LevenshteinGraph graph = new LevenshteinGraph(wordIndex);

        while (graph.GenerateNewOuter(database)) {
            paths.Add(graph.OuterPathString(database));
        }
    }
}
