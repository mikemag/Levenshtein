import java.io.*;

public class Levenshtein {
    // Levenshtein DICTIONARY DATABASE FINDER WORD1 WORD2 (WILDCARDMAP)
    public static void main(String[] args) throws FileNotFoundException {
        long time1 = System.nanoTime();
        String dictionaryPath = args[0];

        LevenshteinDatabase database;

        if (args[1].equals("lazy")) {
            database = new LazyDatabase(dictionaryPath);
        } else if (args[1].equals("wildcard")) {
            database = new WildcardDatabase(dictionaryPath);
        } else if (args[1].equals("cache")) {
            database = new CacheDatabase(dictionaryPath, args[5]);
        } else {
            throw new IllegalArgumentException("Illegal database type!");
        }

        LevenshteinPathFinder finder;
        if (args[2].equals("double")) {
            finder = new FinderDualSided();
        } else if (args[2].equals("single")) {
            finder = new FinderSingleSided();
        } else {
            throw new IllegalArgumentException("Illegal finder algorithm type!");
        }
        long time2 = System.nanoTime();
        System.out.println("Init Time: " + (time2 - time1) / 1000000 + " milliseconds");

        int wordIndex1 = database.getWordIndex(args[3]);
        int wordIndex2 = database.getWordIndex(args[4]);

        long time3 = System.nanoTime();
        System.out.println(LevenshteinPathFinder.pathsToString(finder.generatePaths(wordIndex1, wordIndex2, database, time1), database, true, true));

        System.out.println("Done in " + (System.nanoTime() - time3)/1000 + " microseconds.");
        long time4 = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            LevenshteinPathFinder.pathsToString(finder.generatePaths(wordIndex1, wordIndex2, database, time2), false, false);
            LevenshteinPathFinder.pathsToString(finder.generatePaths(wordIndex1, wordIndex2, database, time2), database, false, false);
        }
        System.out.println("Average Time: " + (System.nanoTime() - time4)/1000000 + " microseconds");
    }
}
