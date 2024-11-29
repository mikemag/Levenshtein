import java.io.*;
import java.util.*;

public class CacheDatabase extends WildcardDatabase {
    private HashMap<String, HashSet<String>> neighborMap;

    CacheDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);

        initializeNeighborMap();
    }

    CacheDatabase(String dictionaryPath, String wildcardMapPath) throws FileNotFoundException {
        super(dictionaryPath, false);
        this.wildcardMap = new HashMap();
        Scanner input = new Scanner(new File(wildcardMapPath));

        while (input.hasNextLine()) {
            Scanner line = new Scanner(input.nextLine());
            String key = line.next();
            HashSet<Character> value = new HashSet<Character>();

            while (line.hasNext()) {
                value.add(line.next().charAt(0));
            }

            this.wildcardMap.put(key, value);
            line.close();
        }

        input.close();

        initializeNeighborMap();
    }

    @Override
    public HashSet<String> findNeighbors(String word, HashSet<String> blacklist) { 
        HashSet<String> returnSet = neighborMap.get(word);
        returnSet.removeAll(blacklist);
        return returnSet;
    }

    @Override
    public boolean areNeighbors(String word1, String word2) {
        return findNeighbors(word1, new HashSet<String>()).contains(word2);
    }

    private void initializeNeighborMap() {
        neighborMap = new HashMap();
        HashSet<String> emptyBlacklist = new HashSet();
        for (String word : this.dictionary) {
            neighborMap.put(word, super.findNeighbors(word, emptyBlacklist));
        }
    }

    public void wildcardMapToFile(File outFile) throws FileNotFoundException {
        Scanner mapStringScanner = new Scanner(this.wildcardMapToString());
        PrintStream output = new PrintStream(outFile);

        while (mapStringScanner.hasNextLine()) {
            output.println(mapStringScanner.nextLine());
        }
        output.close();
    }
}
