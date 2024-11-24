import java.io.*;
import java.util.*;

public class WildcardDatabase extends LevenshteinDatabase {
    private HashMap<String, HashSet<String>> wildcardMap;

    // This is for testing
    public static void main (String args[]) throws FileNotFoundException {
        //ArrayList<char[]> printList = WildcardDatabase.findWildcardIdentities(args[0]);
        //for (char[] identity : printList) {
        //    System.out.println(Arrays.toString(identity));
        //}
        WildcardDatabase database = new WildcardDatabase(args[0]);
        String[] dict = database.getDictionary();
        database.wildcardMap = new HashMap();
        long t1 = System.nanoTime() / 1000000;
        for (int i = 0; i < dict.length; i++) {
            database.putWildcards(dict[i], WildcardDatabase.findWildcardIdentities(dict[i]));
        }
        long t2 = System.nanoTime() / 1000000;
        System.out.println("Init Time: " + (t2 - t1) + " ms");
        //System.out.println(database.wildcardMap.size());
        //System.out.println(database.wildcardMap.toString());
        //System.out.println(database.findNeighbors(args[1], new HashSet()).toString());
        //System.out.println(database.findNeighbors(args[2], new HashSet()).toString());
        //System.out.println(database.findNeighbors(args[3], new HashSet()).toString());
        //System.out.println(database.areNeighbors(args[1], args[2]));
        //System.out.println(database.areNeighbors(args[1], args[3]));
    }

    public WildcardDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);
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
