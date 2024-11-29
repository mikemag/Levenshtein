import java.io.*;
import java.util.*;

public class WildcardDatabase extends LevenshteinDatabase {
    protected HashMap<String, HashSet<Character>> wildcardMap;

    public WildcardDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);
        initializeWildcardMap();

        //System.out.println(this.wildcardMap.toString());
    }

    protected WildcardDatabase(String dictionaryPath, boolean initializeWildcardMap) throws FileNotFoundException {
        super(dictionaryPath);
        if (initializeWildcardMap) {
            initializeWildcardMap();
        }
    }

    public ArrayList<String> localWildcardIdentities(String word) {
        ArrayList<String> identities = new ArrayList();

        addEachWildcard(word, identities, (wildcardSubstitute, wildcardIdentity, 
                wildcardListObject) -> {
            if (wildcardMap.containsKey(wildcardIdentity)) {
                ((ArrayList<String>)wildcardListObject).add(wildcardIdentity);
            }
        });
        return identities;
    }

    public static ArrayList<String> allWildcardIdentities(String word) {
        ArrayList<String> identities = new ArrayList();

        addEachWildcard(word, identities, (wildcardSubstitute, wildcardIdentity, 
                wildcardMapObject) -> {
            ((ArrayList<String>)wildcardMapObject).add(wildcardIdentity);
        });
        return identities;
    };

    private void putEachWildcard(String word) {
        addEachWildcard(word, wildcardMap, (wildcardSubstitute, wildcardIdentity, 
                wildcardMapObject) -> {
            HashMap<String, HashSet<Character>> map = (HashMap<String, HashSet<Character>>)wildcardMapObject;

            map.putIfAbsent(wildcardIdentity, new HashSet<>());
            map.get(wildcardIdentity).add(wildcardSubstitute);
        });
    }

    private static void addEachWildcard(String word, Object dataStructure, wildcardDataStructureAdder wildcardDataStructureAdder) {
        StringBuilder cardBuilder = new StringBuilder(word);
        int wordLength = word.length();

        cardBuilder.setCharAt(0, '*');
        wildcardDataStructureAdder.addIdentityToStructure(word.charAt(0), cardBuilder.toString(), dataStructure);
        for (int i = 1; i < wordLength; i++) {
            cardBuilder.setCharAt(i - 1, word.charAt(i - 1));
            cardBuilder.setCharAt(i, '*');
            wildcardDataStructureAdder.addIdentityToStructure(word.charAt(i), cardBuilder.toString(), dataStructure);
        }

        cardBuilder.append('*');
        cardBuilder.setCharAt(wordLength - 1, word.charAt(wordLength - 1));
        wildcardDataStructureAdder.addIdentityToStructure('0', cardBuilder.toString(), dataStructure);
        for (int i = wordLength; i > 0; i--) {
            cardBuilder.setCharAt(i, word.charAt(i - 1));
            cardBuilder.setCharAt(i - 1, '*');
            wildcardDataStructureAdder.addIdentityToStructure('0', cardBuilder.toString(), dataStructure);
        }
    }

    public boolean areNeighbors(String word1, String word2) {
        HashSet<String> word1Neighbors = this.findNeighbors(word1, new HashSet());
        return word1Neighbors.contains(word2);
    }

    public HashSet<String> findNeighbors(String word, HashSet<String> blacklist) {
        HashSet<String> returnSet = new HashSet();

        for (String wildcard : this.localWildcardIdentities(word)) {
            for (Character character : wildcardMap.get(wildcard)) {
                returnSet.add(wildcardMapValueToString(wildcard, character));
            }
            returnSet.removeAll(blacklist);
        }
        return returnSet;
    };

    private void initializeWildcardMap() {
        wildcardMap = new HashMap();

        for (int i = 0; i < this.dictionary.length; i++) {
            this.putEachWildcard(this.dictionary[i]);
        }
        
        Iterator<Map.Entry<String, HashSet<Character>>> wildcardIterator = wildcardMap.entrySet().iterator();

        while (wildcardIterator.hasNext()) {
            Map.Entry<String, HashSet<Character>> entry = wildcardIterator.next();

            if (entry.getValue().size() == 1) {
                wildcardIterator.remove();
            }
        }
    }

    protected String wildcardMapValueToString(String key, Character value) {
        for (int i = 0; i < key.length(); i++) {
            if (key.charAt(i) == '*') {
                StringBuilder returnBuilder = new StringBuilder(key);
                if (value != '0') {
                    returnBuilder.replace(i, i + 1, value.toString());
                } else {
                    returnBuilder.deleteCharAt(i);
                };
                return returnBuilder.toString();
            }
        }
        throw new IllegalArgumentException("Key contains no asterisk (*) character");
    }

    public String wildcardMapToString() {
        StringBuilder mapBuilder = new StringBuilder();

        for (Map.Entry<String, HashSet<Character>> entry : wildcardMap.entrySet()) {
            StringBuilder entryBuilder = new StringBuilder();

            entryBuilder.append(entry.getKey());

            for (Character value : new TreeSet<Character>(entry.getValue())) {
                entryBuilder.append(" " + value.toString());
            }

            mapBuilder.append(entryBuilder + "\n");
        }

        return mapBuilder.toString();
    }

    public static final Comparator<String> COMPARE_BY_LENGTH = (o1, o2) -> {
        int delta = o1.length() - o2.length();
        if (delta == 0) {
            return o1.compareTo(o2);
        } else {
            return delta;
        }
    };
}

@FunctionalInterface
interface wildcardDataStructureAdder {
    void addIdentityToStructure(Character wildcardSubstitute, String wildcardIdentity, Object dataStructure);
}
