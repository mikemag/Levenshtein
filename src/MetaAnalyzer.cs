using System.Diagnostics;

public class MetaAnalyzer {
    public static void Analyze(LevenshteinDatabase database) {
        const int partitionsPerThread = 100;
        const int threads = 16;
        int partitionSize = database.Words.Count() / partitionsPerThread / threads;

        for (int t = 0; t < threads; t++) {
            int threadId = t;
            Thread thread = new Thread(() => ThreadGraphDiagnosis(threads, threadId, partitionsPerThread, partitionSize, database));
            thread.Start();
        }

        List<PathDiagnostics> maxLengths = new List<PathDiagnostics>();
        List<PathDiagnostics> maxPaths = new List<PathDiagnostics>();
        maxLengths.Add(new PathDiagnostics(0, 0, 0, 0));
        maxPaths.Add(new PathDiagnostics(0, 0, 0, 0));

        LevenshteinBFSGraph graph = new DictionaryBFSGraph(0, database);
        for (int i = threads * partitionsPerThread * partitionSize; i < database.Words.Count(); i++) {
            MakeGraphDiagnostics(i, database, maxLengths, maxPaths, graph);
        }

        foreach (PathDiagnostics path in maxLengths) {
            Console.WriteLine("(excess): " + path.ToString());
        }
        foreach (PathDiagnostics path in maxPaths) {
            Console.WriteLine("(excess): " + path.ToString());
        }
    }

    private static void ThreadGraphDiagnosis(int threads, int thread, int partitions, int size, LevenshteinDatabase database) {
        LevenshteinBFSGraph graph = new DictionaryBFSGraph(0, database);

        List<PathDiagnostics> maxLengths = new List<PathDiagnostics>();
        List<PathDiagnostics> maxPaths = new List<PathDiagnostics>();
        maxLengths.Add(new PathDiagnostics(0, 0, 0, 0));
        maxPaths.Add(new PathDiagnostics(0, 0, 0, 0));

        long ticksPerMS = Stopwatch.Frequency / 1000;

        for (int i = 0; i < partitions; i++) {
            long time0 = Stopwatch.GetTimestamp();
            for (int j = thread + i * threads; j < size * partitions * threads; j += partitions * threads) {
                /*Console.WriteLine(j);*/
                MakeGraphDiagnostics(j, database, maxLengths, maxPaths, graph);
            }
            Console.WriteLine($"Done with partition ({thread}): {i + 1} out of {partitions} in {(Stopwatch.GetTimestamp() - time0) / ticksPerMS} milliseconds");
        }

        foreach (PathDiagnostics path in maxLengths) {
            Console.WriteLine(path.ToString());
        }
        foreach (PathDiagnostics path in maxPaths) {
            Console.WriteLine(path.ToString());
        }
    }

    private static void MakeGraphDiagnostics(int root, LevenshteinDatabase database, List<PathDiagnostics> maxLengths, List<PathDiagnostics> maxPaths, LevenshteinBFSGraph graph) {
        Dictionary<int, List<int[]>> pathDictionary = new Dictionary<int, List<int[]>>();

        graph.Reset(root);

        bool generateFirstOuterSucceeded = graph.GenerateNewFrontier();

        foreach (int key in graph.Frontier) {
            pathDictionary.Add(key, graph.AllPathsTo(key, false));
        }

        if (!generateFirstOuterSucceeded) {
            return;
        }

        while (graph.GenerateNewFrontier()) {
            foreach(int outerWord in graph.Frontier) {
                int numPaths = graph.NumberOfPathsTo(outerWord);
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
            foreach (int furthestWord in graph.Frontier) {
                maxLengths.Add(new PathDiagnostics(graph.Depth - 1, graph.NumberOfPathsTo(furthestWord), root, furthestWord));
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
