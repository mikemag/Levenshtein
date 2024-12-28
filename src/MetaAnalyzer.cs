using System.Diagnostics;

namespace levenshtein;

public static class MetaAnalyzer
{
    public static void Analyze(LevenshteinDatabase database)
    {
        const int threadCount = 16;
        var threads = new Thread[threadCount];
        var maxLengths = new List<PathDiagnostics>[threadCount];
        var maxPaths = new List<PathDiagnostics>[threadCount];

        for (var t = 0; t < threadCount; t++)
        {
            var tt = t; // Closure capture
            threads[t] = new Thread(() => ThreadGraphDiagnosis(threadCount, tt, maxLengths, maxPaths, database));
            threads[t].Start();
        }

        var finalMaxLengths = new List<PathDiagnostics> { new(0, 0, 0, 0) };
        var finalMaxPaths = new List<PathDiagnostics> { new(0, 0, 0, 0) };

        for (var t = 0; t < threadCount; t++)
        {
            threads[t].Join();

            if (maxPaths[t][0].Count >= finalMaxPaths[0].Count)
            {
                if (maxPaths[t][0].Count > finalMaxPaths[0].Count)
                {
                    finalMaxPaths.Clear();
                }

                finalMaxPaths.AddRange(maxPaths[t]);
            }

            if (maxLengths[t][0].Length >= finalMaxLengths[0].Length)
            {
                if (maxLengths[t][0].Length > finalMaxLengths[0].Length)
                {
                    finalMaxLengths.Clear();
                }

                finalMaxLengths.AddRange(maxLengths[t]);
            }
        }

        Console.WriteLine("Max lengths:");
        foreach (var path in finalMaxLengths)
        {
            Console.WriteLine(path.ToString(database));
        }

        Console.WriteLine("Max paths:");
        foreach (var path in finalMaxPaths)
        {
            Console.WriteLine(path.ToString(database));
        }
    }

    private static void GraphToBinary(int max, int partitions, int offset, LevenshteinDatabase database,
        FileStream output)
    {
        MemoryStream graphStream = new MemoryStream();
        BinaryWriter writer = new BinaryWriter(graphStream);

        for (int i = offset; i < max; i += partitions)
        {
            LevenshteinGraph graph = new LevenshteinGraph(i);

            while (graph.GenerateNewOuter(database))
            {
                graph.WriteOuterBinary(database.Words.Count(), writer);
            }
        }

        writer.Close();

        output.Write(graphStream.ToArray());
        graphStream.Dispose();
        writer.Dispose();
    }

    private static void AddPathsForPartition(int max, int partitions, int offset, LevenshteinDatabase database,
        FileStream output)
    {
        List<MemoryStream> paths = new List<MemoryStream>();

        for (int i = offset; i < max; i += partitions)
        {
            LevenshteinGraph graph = new LevenshteinGraph(i);

            while (graph.GenerateNewOuter(database))
            {
                graph.WritePathStreams(database.Words.Count(), paths);
            }
        }

        foreach (MemoryStream stream in paths)
        {
            output.Write(stream.ToArray());
            stream.Dispose();
        }
    }

    private static void ThreadGraphDiagnosis(int threadCount, int threadId, List<PathDiagnostics>[] threadMaxLengths,
        List<PathDiagnostics>[] threadMaxPaths, LevenshteinDatabase database)
    {
        var maxLengths = new List<PathDiagnostics> { new(0, 0, 0, 0) };
        var maxPaths = new List<PathDiagnostics> { new(0, 0, 0, 0) };
        threadMaxLengths[threadId] = maxLengths;
        threadMaxPaths[threadId] = maxPaths;
        var pathCounts = new Dictionary<int, int>(300000);

        const int partitions = 100;
        var totalWords = database.Words.Length;
        var ticksPerMs = Stopwatch.Frequency / 1000;
        for (var i = 0; i < partitions; i++)
        {
            var time0 = Stopwatch.GetTimestamp();
            for (var j = threadId + i * threadCount; j < totalWords; j += partitions * threadCount)
            {
                MakeGraphDiagnostics(j, database, maxLengths, maxPaths, pathCounts);
            }

            var time1 = Stopwatch.GetTimestamp();
            Console.WriteLine(
                $"Done with partition ({threadId}): {i + 1} out of {partitions} in {(time1 - time0) / ticksPerMs}ms");
            // if (i == 1) break; // mmmfixme: tmp testing
        }
    }

    private static void MakeGraphDiagnostics(int root, LevenshteinDatabase database, List<PathDiagnostics> maxLengths,
        List<PathDiagnostics> maxPaths, Dictionary<int, int> pathCounts)
    {
        LevenshteinGraph graph = new LevenshteinGraph(root);
        pathCounts[root] = 1; // Sufficient "reset" of pathCounts ;)

        while (graph.GenerateNewOuter(database))
        {
            foreach (var outerEntry in graph.Outer)
            {
                var numPaths = 0;
                foreach (var p in outerEntry.Value)
                {
                    numPaths += pathCounts[p];
                }

                pathCounts[outerEntry.Key] = numPaths;

                if (numPaths >= maxPaths[0].Count)
                {
                    if (numPaths > maxPaths[0].Count)
                    {
                        maxPaths.Clear();
                    }

                    maxPaths.Add(new PathDiagnostics(graph.Depth, numPaths, root, outerEntry.Key));
                }
            }
        }

        if (graph.Depth - 1 >= maxLengths[0].Length)
        {
            if (graph.Depth - 1 > maxLengths[0].Length)
            {
                maxLengths.Clear();
            }

            foreach (var furthestWord in graph.OuterKeys)
            {
                maxLengths.Add(new PathDiagnostics(graph.Depth - 1, pathCounts[furthestWord], root, furthestWord));
            }
        }
    }

    private readonly struct PathDiagnostics(int length, int count, int word1, int word2)
    {
        public readonly int Length = length;
        public readonly int Count = count;

        public override string ToString()
        {
            // return "Length: " + length + "\nCount: " + count + "\nWord1: " + word1 + "\nWord2: " + word2;
            return $"{Length}, {Count}, {word1}, {word2}";
        }

        public string ToString(LevenshteinDatabase database)
        {
            return $"Length={Length}, paths={Count}, {database.Words[word1]} --> {database.Words[word2]}";
        }
    }
}