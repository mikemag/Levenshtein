using System.Diagnostics;

public class Levenshtein {
    // Levenshtein DICTIONARY DATABASE FINDER WORD1 WORD2 (WILDCARDMAP)
    public static void Main(String[] args) {
        long time1 = Stopwatch.GetTimestamp();
        float musPerTick = 1000000 / (float)Stopwatch.Frequency;
        String dictionaryPath = args[0];

        LevenshteinDatabase database;

        if (args[1] == "lazy") {
            database = new LazyDatabase(dictionaryPath);
        } else if (args[1] == "wildcard") {
            database = new WildcardDatabase(dictionaryPath);
        } else if (args[1] == "cache") {
            database = new CacheDatabase(dictionaryPath, args[5]);
        } else {
            throw new ArgumentException("Illegal database type!");
        }

        LevenshteinPathFinder finder;
        if (args[2] == "double") {
            finder = new FinderDualSided();
        } else if (args[2] == "single") {
            finder = new FinderSingleSided();
        } else {
            throw new ArgumentException("Illegal finder algorithm type!");
        }
        long time2 = Stopwatch.GetTimestamp();
        Console.WriteLine("Init Time: " + (long)((time2 - time1) * musPerTick / 1000L) + " milliseconds");

        int wordIndex1 = database.Indexes[args[3]];
        int wordIndex2 = database.Indexes[args[4]];

        long time3 = Stopwatch.GetTimestamp();
        Console.WriteLine(LevenshteinPathFinder.PathsToString(finder.GeneratePaths(wordIndex1, wordIndex2, database, time1), database, true, true));
        Console.WriteLine("Done in " + (long)((Stopwatch.GetTimestamp() - time3) * musPerTick) + " microseconds.");
        long time4 = Stopwatch.GetTimestamp();

        for (int i = 0; i < 100000; i++) {
            LevenshteinPathFinder.PathsToString(finder.GeneratePaths(wordIndex1, wordIndex2, database, time2), database, false, false);
        }
        Console.WriteLine("Average Time: " + (long)(((Stopwatch.GetTimestamp() - time4) * musPerTick) / 100000) + " microseconds");
    }
}
