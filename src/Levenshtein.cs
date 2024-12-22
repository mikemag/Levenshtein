using System.Diagnostics;
using System.CommandLine;

public class Levenshtein {
    private enum DatabaseType { cache, wildcard, lazy };
    private enum FinderAlgorithm { dual, single };

    private static readonly String[][] _testWords = [
        ["dog", "dot"], 
        ["dog", "dog"],
        ["saturday", "sunday"],
        ["sitting", "kitten"],
        ["dog", "doge"],
        ["dog", "dong"],
        ["dog", "og"],
        ["dog", "cat"],
        ["dog", "smart"],
        ["dog", "quack"],
        ["vulgates", "gumwood"],
        ["sweetly", "raddles"],
        ["bldr", "rewrote"],
        ["evacuee", "fall"],
        ["monkey", "business"]
    ];

    private static readonly String[][] _benchmarkWords = [
        ["dog", "dot"],
        ["dog", "dog"],
        ["dog", "cat"],
        ["dog", "quack"],
        ["vulgates", "gumwood"],
        ["monkey", "business"]
    ];

    // Run with --help flag to view command structure
    static int Main(String[] args) {
        Argument<FileInfo> dictionaryArg = new Argument<FileInfo>("dictionary", "The path to a file containing the list of words in the dictionary");
        Argument<DatabaseType> databaseArg = new Argument<DatabaseType>(name: "database", description: "The type of database algorithm", getDefaultValue: () => DatabaseType.cache);
        Argument<FinderAlgorithm> finderArg = new Argument<FinderAlgorithm>(name: "finder", description: "The path finder algorithm", getDefaultValue: () => FinderAlgorithm.dual);

        Option<FileInfo> wildcardMapOpt = new Option<FileInfo>("--card-map", "The path to a file containing the wildcard map. Only applicable if 'cache' is the selected database");

        Command testVerb = new Command("test", "Run built-in test cases")
                { dictionaryArg, databaseArg, finderArg, wildcardMapOpt };
        testVerb.SetHandler(Levenshtein.RunTest, dictionaryArg, databaseArg, finderArg, wildcardMapOpt);

        Command benchmarkVerb = new Command("benchmark", "Benchmark the performance of a database finder pair") 
                { dictionaryArg, databaseArg, finderArg, wildcardMapOpt };
        benchmarkVerb.SetHandler(Levenshtein.RunBenchmark, dictionaryArg, databaseArg, finderArg, wildcardMapOpt);

        Argument<String> wordArg1 = new Argument<String>(name: "start", description: "Start word");
        Argument<String> wordArg2 = new Argument<String>(name: "end", description: "Destination word");
        Command pathVerb = new Command("path", "Find the path between a pair of words") 
                { dictionaryArg, databaseArg, finderArg, wordArg1, wordArg2, wildcardMapOpt };
        pathVerb.SetHandler(Levenshtein.RunPath, dictionaryArg, databaseArg, finderArg, wordArg1, wordArg2, wildcardMapOpt);

        Argument<FileInfo> wildcardDestinationArg = new Argument<FileInfo>(name: "destination", description: "File to cache map to");
        Command cacheVerb = new Command("cache", "Cache the wildcard map to a file")
                { dictionaryArg, wildcardDestinationArg };
        cacheVerb.SetHandler(Levenshtein.RunCache, dictionaryArg, wildcardDestinationArg);

        RootCommand rootCommand = new RootCommand("CLI for the Levenshtein graph problem. See https://github.com/TristenYim/Levenshtein")
            { testVerb, benchmarkVerb, pathVerb, cacheVerb };

        return rootCommand.Invoke(args);
    }

    private static void RunPath(FileInfo dictionaryPath, DatabaseType databaseType, FinderAlgorithm finderAlgorithm, String word1, String word2, FileInfo wildcardPath) {
        LevenshteinDatabase database = GetDatabase(dictionaryPath, databaseType, wildcardPath);
        LevenshteinPathFinder finder = GetFinder(finderAlgorithm);

        WordPairTest(word1, word2, finder, database);
    }

    private static void RunTest(FileInfo dictionaryPath, DatabaseType databaseType, FinderAlgorithm finderAlgorithm, FileInfo wildcardPath) {
        long time0 = Stopwatch.GetTimestamp();
        long ticksPerMs = Stopwatch.Frequency / 1000;

        LevenshteinDatabase database = GetDatabase(dictionaryPath, databaseType, wildcardPath);
        LevenshteinPathFinder finder = GetFinder(finderAlgorithm);

        long time1 = Stopwatch.GetTimestamp();
        Console.WriteLine("Initialized in " + (time1 - time0) / ticksPerMs + " milliseconds\n");

        foreach (String[] wordPair in _testWords) {
            WordPairTest(wordPair[0], wordPair[1], finder, database);
        };

        Console.WriteLine("Total time (excluding init) is " + (Stopwatch.GetTimestamp() - time1) / ticksPerMs + " milliseconds");
    }

    private static void RunBenchmark(FileInfo dictionaryPath, DatabaseType databaseType, FinderAlgorithm finderAlgorithm, FileInfo wildcardPath) {
        long time0 = Stopwatch.GetTimestamp();
        long ticksPerMus = Stopwatch.Frequency / 1000000;

        LevenshteinDatabase database = GetDatabase(dictionaryPath, databaseType, wildcardPath);
        LevenshteinPathFinder finder = GetFinder(finderAlgorithm);

        long time1 = Stopwatch.GetTimestamp();
        Console.WriteLine("Initialized in " + (time1 - time0) / ticksPerMus / 1000 + " milliseconds\n");

        foreach (String[] wordPair in _benchmarkWords) {
            int wordIndex1 = database.Indexes[wordPair[0]];
            int wordIndex2 = database.Indexes[wordPair[1]];

            if (!CheckDictionaryFor(wordIndex1, wordIndex2, wordPair[0], wordPair[1])) {
                continue;
            };
            
            Console.WriteLine("Trying '" + wordPair[0] + "' to '" + wordPair[1] + "'");

            long time2 = Stopwatch.GetTimestamp();

            const int reps = 100000;

            for (int i = 0; i < reps; i++) {
                LevenshteinPathFinder.PathsToString(finder.GeneratePaths(wordIndex1, wordIndex2, database), database, false, false);
            }
            Console.WriteLine("Average time is " + (((Stopwatch.GetTimestamp() - time2) / ticksPerMus) / reps) + " microseconds\n");
        }
    }

    private static void RunCache(FileInfo dictionaryPath, FileInfo wildcardMapDestination) {
        CacheDatabase database = new CacheDatabase(dictionaryPath);
        database.WildcardMapToFile(wildcardMapDestination);
    }

    private static LevenshteinDatabase GetDatabase(FileInfo dictionaryPath, DatabaseType databaseType, FileInfo wildcardPath) {
        switch (databaseType) {
            case DatabaseType.cache:
                return new CacheDatabase(dictionaryPath, wildcardPath);
            case DatabaseType.wildcard:
                return new WildcardDatabase(dictionaryPath);
            case DatabaseType.lazy:
                return new LazyDatabase(dictionaryPath);
        }
        throw new ArgumentOutOfRangeException(databaseType.ToString());
    }

    private static LevenshteinPathFinder GetFinder(FinderAlgorithm finderAlgorithm) {
        switch (finderAlgorithm) {
            case FinderAlgorithm.dual:
                return new FinderDualSided();
            case FinderAlgorithm.single:
                return new FinderSingleSided();
        }
        throw new ArgumentOutOfRangeException(finderAlgorithm.ToString());
    }

    private static bool CheckDictionaryFor(int wordIndex1, int wordIndex2, String word1, String word2) {
        if (wordIndex1 < 0 || wordIndex2 < 0) {
            Console.Write("'");
            if (wordIndex1 < 0 && wordIndex2 < 0) {
                Console.Write(word1 + "' and '" + word1 + "' do");
            } else if (wordIndex1 < 0) {
                Console.Write(word1 + "' does");
            } else {
                Console.Write(word2 + "' does");
            }
            Console.WriteLine(" not exist in the dictionary, skipping '" + word1 + "' to '" + word2 + "'\n");
            return false;
        };
        return true;
    }

    private static void WordPairTest(String word1, String word2, LevenshteinPathFinder finder, LevenshteinDatabase database) {
        int wordIndex1 = database.Indexes[word1];
        int wordIndex2 = database.Indexes[word2];

        if (!CheckDictionaryFor(wordIndex1, wordIndex2, word1, word2)) {
            return;
        };

        List<int[]> paths = finder.GeneratePaths(wordIndex1, wordIndex2, database);

        if (paths == null) {
            Console.WriteLine("No path exists between '" + word1 + "' and '" + word2 + "'\n");
            return;
        };

        Console.WriteLine(LevenshteinPathFinder.PathsToString(paths, database, true, true) + "\n");
    }
}
