import java.io.*;
import java.util.*;

public class WildcardDatabase extends LevenshteinDatabase {
    private HashMap<String, HashSet<String>> wildcardMap;

    // This is for testing
    public static void main (String args[]) throws FileNotFoundException {
        WildcardDatabase database = new WildcardDatabase(args[0]);
        String[] dict = database.getDictionary();
        database.wildcardMap = new HashMap();
        for (int i = 0; i < dict.length; i++) {
            database.putWildcards(dict[i], WildcardDatabase.findWildcardIdentities(dict[i]));
        }
        System.out.println(database.wildcardMap.toString());
    }

    public WildcardDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);
    }

    public static ArrayList<String> findWildcardIdentities(String word) {
        ArrayList<String> returnIdentities = new ArrayList();
        ArrayList<Character> cardBuilder = new ArrayList(word.length());
        for (int i = 0; i < word.length(); i++) {
            cardBuilder.add(word.charAt(i));
        }

        cardBuilder.set(0, '*');
        returnIdentities.add(Arrays.toString(cardBuilder.toArray()));
        for (int i = 1; i < word.length(); i++) {
            cardBuilder.set(i - 1, word.charAt(i - 1));
            cardBuilder.set(i, '*');
            returnIdentities.add(Arrays.toString(cardBuilder.toArray()));
        }

        cardBuilder.set(cardBuilder.size() - 1, word.charAt(cardBuilder.size() - 1));
        cardBuilder.add(0, '*');
        returnIdentities.add(Arrays.toString(cardBuilder.toArray()));
        for (int i = 1; i <= word.length(); i++) {
            cardBuilder.set(i - 1, word.charAt(i - 1));
            cardBuilder.set(i, '*');
            returnIdentities.add(Arrays.toString(cardBuilder.toArray()));
        }

        return returnIdentities;
    }

    private void putWildcards(String word, ArrayList<String> wildcards) {
        for (String wildcard: wildcards) {
            if (!wildcardMap.containsKey(wildcard)) {
                wildcardMap.put(wildcard, new HashSet<String>());
            }
            wildcardMap.get(wildcard).add(word);
        }
    }

    // These are placeholders
    public boolean areNeighbors(String word1, String word2) {
        return false;
    }

    public HashSet<String> findNeighbors(String word) {
        return null;
    };
}
