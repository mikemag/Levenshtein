using System.Diagnostics;

public class MetaAnalyzer
{
    public static void Analyze(LevenshteinDatabase database)
    {
        const int partitionsPerThread = 100;
        const int threads = 16;
        int partitionSize = database.Words.Count() / partitionsPerThread / threads;

        for (int t = 0; t < threads; t++)
        {
            int tt = t;
            Thread thread = new Thread(() =>
                ThreadGraphDiagnosis(threads, tt, partitionsPerThread, partitionSize, database));
            thread.Start();
        }

        List<PathDiagnostics> maxLengths = new List<PathDiagnostics>();
        List<PathDiagnostics> maxPaths = new List<PathDiagnostics>();
        maxLengths.Add(new PathDiagnostics(0, 0, 0, 0));
        maxPaths.Add(new PathDiagnostics(0, 0, 0, 0));

        for (int i = threads * partitionsPerThread * partitionSize; i < database.Words.Count(); i++)
        {
            MakeGraphDiagnostics(i, database, maxLengths, maxPaths);
        }

        ThreadPool.SetMinThreads(0, 1);
        ThreadPool.SetMaxThreads(threads, 1);

        foreach (PathDiagnostics path in maxLengths)
        {
            Console.WriteLine("(excess): " + path.ToString());
        }

        foreach (PathDiagnostics path in maxPaths)
        {
            Console.WriteLine("(excess): " + path.ToString());
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

    private static void ThreadGraphDiagnosis(int threads, int thread, int partitions, int size,
        LevenshteinDatabase database)
    {
        List<PathDiagnostics> maxLengths = new List<PathDiagnostics>();
        List<PathDiagnostics> maxPaths = new List<PathDiagnostics>();
        maxLengths.Add(new PathDiagnostics(0, 0, 0, 0));
        maxPaths.Add(new PathDiagnostics(0, 0, 0, 0));

        long ticksPerMs = Stopwatch.Frequency / 1000;

        for (int i = 0; i < partitions; i++)
        {
            long time0 = Stopwatch.GetTimestamp();
            for (int j = thread + i * threads; j < size * partitions * threads; j += partitions * threads)
            {
                /*Console.WriteLine(j);*/
                MakeGraphDiagnostics(j, database, maxLengths, maxPaths);
            }

            long time1 = Stopwatch.GetTimestamp();
            Console.WriteLine(
                $"Done with partition ({thread}): {i + 1} out of {partitions} in {(time1 - time0) / ticksPerMs}ms");
            if (thread == 0)
            {
                foreach (PathDiagnostics path in maxLengths)
                {
                    Console.WriteLine(path.ToString());
                }

                foreach (PathDiagnostics path in maxPaths)
                {
                    Console.WriteLine(path.ToString());
                }
            }
        }

        foreach (PathDiagnostics path in maxLengths)
        {
            Console.WriteLine(path.ToString());
        }

        foreach (PathDiagnostics path in maxPaths)
        {
            Console.WriteLine(path.ToString());
        }
    }

    private static void MakeGraphDiagnostics(int root, LevenshteinDatabase database, List<PathDiagnostics> maxLengths,
        List<PathDiagnostics> maxPaths)
    {
        LevenshteinGraph graph = new LevenshteinGraph(root);
        var pathCounts = new Dictionary<int, int>() { { root, 1 } };

        while (graph.GenerateNewOuter(database))
        {
            foreach (var outerEntry in graph.outer)
            {
                int numPaths = 0;
                foreach (var p in outerEntry.Value)
                {
                    numPaths += pathCounts[p];
                }

                pathCounts[outerEntry.Key] = numPaths;

                if (numPaths >= maxPaths[0].count)
                {
                    if (numPaths > maxPaths[0].count)
                    {
                        maxPaths.Clear();
                    }

                    maxPaths.Add(new PathDiagnostics(graph.Depth, numPaths, root, outerEntry.Key));
                }
            }
        }

        if (graph.Depth - 1 >= maxLengths[0].length)
        {
            if (graph.Depth - 1 > maxLengths[0].length)
            {
                maxLengths.Clear();
            }

            foreach (int furthestWord in graph.OuterKeys)
            {
                maxLengths.Add(new PathDiagnostics(graph.Depth - 1, pathCounts[furthestWord], root,
                    furthestWord));
            }
        }
    }

    public struct PathDiagnostics
    {
        public int length;
        public int count;
        public int word1;
        public int word2;

        public PathDiagnostics(int length, int count, int word1, int word2)
        {
            this.length = length;
            this.count = count;
            this.word1 = word1;
            this.word2 = word2;
        }

        public override String ToString()
        {
            return "Length: " + length + "\nCount: " + count + "\nWord1: " + word1 + "\nWord2: " + word2;
        }
    }
}