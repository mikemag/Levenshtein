import java.io.*;
import java.util.*;

public class CacheDatabase extends WildcardDatabase {
    private final HashMap<String, HashSet<String>> neighborMap;

    CacheDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);

        neighborMap = getInitializedNeighborMap();
    }

    CacheDatabase(String dictionaryPath, String wildcardMapPath) throws FileNotFoundException {
        super(dictionaryPath, false);

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

        neighborMap = getInitializedNeighborMap();
    }

    @Override
    public HashSet<String> findNeighbors(String word) { 
        return new HashSet<String>(neighborMap.get(word));
    }

    @Override
    public boolean areNeighbors(String word1, String word2) {
        return findNeighbors(word1).contains(word2);
    }

    private HashMap<String, HashSet<String>> getInitializedNeighborMap() {
        HashMap<String, HashSet<String>> returnMap = new HashMap();

        for (String word : this.dictionary) {
            returnMap.put(word, super.findNeighbors(word));
        }

        return returnMap;
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
