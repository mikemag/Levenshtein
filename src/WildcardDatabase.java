import java.io.*;
import java.util.*;

public class WildcardDatabase extends LevenshteinDatabase {
    protected final HashMap<String, ArrayList<Integer>> wildcardMap;

    public WildcardDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);
        wildcardMap = getInitializedWildcardMap();
    }

    protected WildcardDatabase(String dictionaryPath, boolean initializeWildcardMap) throws FileNotFoundException {
        super(dictionaryPath);
        if (initializeWildcardMap) {
            wildcardMap = getInitializedWildcardMap();
        } else {
            wildcardMap = new HashMap();
        }
    }

    public final ArrayList<String> localWildcardIdentities(int wordIndex) {
        ArrayList<String> identities = new ArrayList();

        addEachWildcard(wordIndex, identities, (wildcardSubstitute, wildcardIdentity, 
                wildcardListObject) -> {
            if (wildcardMap.containsKey(wildcardIdentity)) {
                ((ArrayList<String>)wildcardListObject).add(wildcardIdentity);
            }
        });
        return identities;
    }

    public final ArrayList<String> allWildcardIdentities(int wordIndex) {
        ArrayList<String> identities = new ArrayList();

        addEachWildcard(wordIndex, identities, (wildcardSubstitute, wildcardIdentity, 
                wildcardMapObject) -> {
            ((ArrayList<String>)wildcardMapObject).add(wildcardIdentity);
        });
        return identities;
    };

    private void putEachWildcard(int wordIndex, HashMap<String, ArrayList<Integer>> destination) {
        addEachWildcard(wordIndex, destination, (wildcardSubstitute, wildcardIdentity, 
                wildcardMapObject) -> {
            HashMap<String, ArrayList<Integer>> map = (HashMap<String, ArrayList<Integer>>)wildcardMapObject;

            map.putIfAbsent(wildcardIdentity, new ArrayList<>());
            map.get(wildcardIdentity).add(wildcardSubstitute);
        });
    }

    private void addEachWildcard(int wordIndex, Object dataStructure, wildcardDataStructureAdder wildcardDataStructureAdder) {
        String word = this.wordAt(wordIndex);
        StringBuilder cardBuilder = new StringBuilder(word);
        int wordLength = word.length();

        cardBuilder.setCharAt(0, '*');
        wildcardDataStructureAdder.addIdentityToStructure(wordIndex, cardBuilder.toString(), dataStructure);
        for (int i = 1; i < wordLength; i++) {
            cardBuilder.setCharAt(i - 1, word.charAt(i - 1));
            cardBuilder.setCharAt(i, '*');
            wildcardDataStructureAdder.addIdentityToStructure(wordIndex, cardBuilder.toString(), dataStructure);
        }

        cardBuilder.append('*');
        cardBuilder.setCharAt(wordLength - 1, word.charAt(wordLength - 1));
        wildcardDataStructureAdder.addIdentityToStructure(wordIndex, cardBuilder.toString(), dataStructure);
        for (int i = wordLength; i > 0; i--) {
            cardBuilder.setCharAt(i, word.charAt(i - 1));
            cardBuilder.setCharAt(i - 1, '*');
            wildcardDataStructureAdder.addIdentityToStructure(wordIndex, cardBuilder.toString(), dataStructure);
        }
    }

    @Override
    public boolean areNeighbors(int wordIndex1, int wordIndex2) {
        return Arrays.asList(findNeighbors(wordIndex1)).contains(wordIndex2);
    }
    
    @Override
    public Integer[] findNeighbors(int wordIndex) {
        ArrayList<Integer> returnList = new ArrayList();

        for (String wildcard : this.localWildcardIdentities(wordIndex)) {
            for (int neighborIndex : wildcardMap.get(wildcard)) {
                if (neighborIndex != wordIndex) {
                    returnList.add(neighborIndex);
                }
            }
        }

        return returnList.toArray(new Integer[0]);
    };

    private HashMap<String, ArrayList<Integer>> getInitializedWildcardMap() {
        HashMap<String, ArrayList<Integer>> returnMap = new HashMap();

        for (int i = 0; i < this.dictionary.length; i++) {
            putEachWildcard(i, returnMap);
        }
        
        Iterator<Map.Entry<String, ArrayList<Integer>>> wildcardIterator = returnMap.entrySet().iterator();

        while (wildcardIterator.hasNext()) {
            Map.Entry<String, ArrayList<Integer>> entry = wildcardIterator.next();

            if (entry.getValue().size() == 1) {
                wildcardIterator.remove();
            }
        }

        return returnMap;
    }

    public final String wildcardMapToString() {
        StringBuilder mapBuilder = new StringBuilder();

        for (Map.Entry<String, ArrayList<Integer>> entry : wildcardMap.entrySet()) {
            String key = entry.getKey();
            int keyWildcardIndex = WildcardDatabase.getWildcardIndex(key);
            StringBuilder entryBuilder = new StringBuilder();
            entryBuilder.append(key);

            for (int value : new TreeSet<Integer>(entry.getValue())) {
                String word = this.wordAt(value);

                if (word.length() < key.length()) {
                    entryBuilder.append(" 0");
                    continue;
                }

                entryBuilder.append(" " + this.wordAt(value).charAt(keyWildcardIndex));
            }

            mapBuilder.append(entryBuilder + "\n");
        }

        return mapBuilder.toString();
    }

    public static int getWildcardIndex(String wildcard) {
        for (int i = 0; i < wildcard.length(); i++) {
            if (wildcard.charAt(i) != '*') {
                continue;
            }

            return i;
        }

        throw new IllegalArgumentException("Input must contain a wildcard character '*'");
    }
}

@FunctionalInterface
interface wildcardDataStructureAdder {
    void addIdentityToStructure(int wordIndex, String wildcardIdentity, Object dataStructure);
}
