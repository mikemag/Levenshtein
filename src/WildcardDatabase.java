import java.io.*;
import java.util.*;

public class WildcardDatabase extends LevenshteinDatabase {
    private HashMap<String, HashSet<String>> wildcardMap;

    public WildcardDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);
        String[] dictionary = this.getDictionary();
        wildcardMap = new HashMap();
        for (int i = 0; i < dictionary.length; i++) {
            this.putWildcards(dictionary[i], WildcardDatabase.findWildcardIdentities(dictionary[i]));
        }
    }

    public static ArrayList<String> findWildcardIdentities(String word) {
        ArrayList<String> returnIdentities = new ArrayList();
        StringBuilder cardBuilder = new StringBuilder(word);
        int wordLength = word.length();

        cardBuilder.setCharAt(0, '*');
        returnIdentities.add(cardBuilder.toString());
        for (int i = 1; i < wordLength; i++) {
            cardBuilder.setCharAt(i - 1, word.charAt(i - 1));
            cardBuilder.setCharAt(i, '*');
            returnIdentities.add(cardBuilder.toString());
        }

        cardBuilder.append('*');
        cardBuilder.setCharAt(wordLength - 1, word.charAt(wordLength - 1));
        returnIdentities.add(cardBuilder.toString());
        for (int i = wordLength; i > 0; i--) {
            cardBuilder.setCharAt(i, word.charAt(i - 1));
            cardBuilder.setCharAt(i - 1, '*');
            returnIdentities.add(cardBuilder.toString());
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

    public boolean areNeighbors(String word1, String word2) {
        HashSet<String> word1Neighbors = this.findNeighbors(word1, new HashSet());
        return word1Neighbors.contains(word2);
    }

    public HashSet<String> findNeighbors(String word, HashSet<String> blacklist) {
        HashSet<String> returnSet = new HashSet();
        for (String wildcard : WildcardDatabase.findWildcardIdentities(word)) {
            returnSet.addAll(wildcardMap.get(wildcard));
            returnSet.removeAll(blacklist);
        }
        return returnSet;
    };
}
