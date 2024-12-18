using System.Text;

public class WildcardDatabase : LevenshteinDatabase {
    protected readonly Dictionary<String, List<int>> wildcardMap;

    public WildcardDatabase(String dictionaryPath) : base(dictionaryPath) {
        wildcardMap = getInitializedWildcardMap();
    }

    protected WildcardDatabase(String dictionaryPath, bool initializeWildcardMap) : base(dictionaryPath) {
        if (initializeWildcardMap) {
            wildcardMap = getInitializedWildcardMap();
        } else {
            wildcardMap = new Dictionary<String, List<int>>();
        }
    }

    public List<String> localWildcardIdentities(int wordIndex) {
        List<String> identities = new List<String>();

        addEachWildcard(wordIndex, identities, (wildcardSubstitute, wildcardIdentity, 
                wildcardListObject) => {
            if (wildcardMap.ContainsKey(wildcardIdentity)) {
                ((List<String>)wildcardListObject).Add(wildcardIdentity);
            }
        });
        return identities;
    }

    public List<String> allWildcardIdentities(int wordIndex) {
        List<String> identities = new List<String>();

        addEachWildcard(wordIndex, identities, (wildcardSubstitute, wildcardIdentity, 
                wildcardMapObject) => {
            ((List<String>)wildcardMapObject).Add(wildcardIdentity);
        });
        return identities;
    }

    private void putEachWildcard(int wordIndex, Dictionary<String, List<int>> destination) {
        addEachWildcard(wordIndex, destination, (wildcardSubstitute, wildcardIdentity, 
                wildcardMapObject) => {
            Dictionary<String, List<int>> map = (Dictionary<String, List<int>>)wildcardMapObject;

            map.TryAdd(wildcardIdentity, new List<int>());
            map[wildcardIdentity].Add(wildcardSubstitute);
        });
    }

    private void addEachWildcard(int wordIndex, Object dataStructure, Action<int, String, Object> wildcardDataStructureAdder) {
        String word = this.wordAt(wordIndex);
        StringBuilder cardBuilder = new StringBuilder(word);
        int wordLength = word.Length;

        cardBuilder[0] = '*';
        wildcardDataStructureAdder(wordIndex, cardBuilder.ToString(), dataStructure);
        for (int i = 1; i < wordLength; i++) {
            cardBuilder[i - 1] = word.ElementAt(i - 1);
            cardBuilder[i] = '*';
            wildcardDataStructureAdder(wordIndex, cardBuilder.ToString(), dataStructure);
        }

        cardBuilder.Append('*');
        cardBuilder[wordLength - 1] = word.ElementAt(wordLength - 1);
        wildcardDataStructureAdder(wordIndex, cardBuilder.ToString(), dataStructure);
        for (int i = wordLength; i > 0; i--) {
            cardBuilder[i] = word.ElementAt(i - 1);
            cardBuilder[i - 1] = '*';
            wildcardDataStructureAdder(wordIndex, cardBuilder.ToString(), dataStructure);
        }
    }

    public override bool areNeighbors(int wordIndex1, int wordIndex2) {
        return findNeighbors(wordIndex1).Contains(wordIndex2);
    }
    
    public override int[] findNeighbors(int wordIndex) {
        List<int> returnList = new List<int>();

        foreach (String wildcard in this.localWildcardIdentities(wordIndex)) {
            foreach (int neighborIndex in wildcardMap[wildcard]) {
                if (neighborIndex != wordIndex) {
                    returnList.Add(neighborIndex);
                }
            }
        }

        return returnList.ToArray();
    }

    private Dictionary<String, List<int>> getInitializedWildcardMap() {
        Dictionary<String, List<int>> returnMap = new Dictionary<String, List<int>>();

        for (int i = 0; i < this.dictionary.Length; i++) {
            putEachWildcard(i, returnMap);
        }
        
        foreach (KeyValuePair<String, List<int>> entry in returnMap) {
            if (entry.Value.Count == 1) {
                returnMap.Remove(entry.Key);
            }
        }

        return returnMap;
    }

    public String wildcardMapToString() {
        StringBuilder mapBuilder = new StringBuilder();

        foreach (KeyValuePair<String, List<int>> entry in wildcardMap) {
            String key = entry.Key;
            int keyWildcardIndex = WildcardDatabase.getWildcardIndex(key);
            StringBuilder entryBuilder = new StringBuilder();
            entryBuilder.Append(key);

            foreach (int value in new SortedSet<int>(entry.Value)) {
                String word = this.wordAt(value);

                if (word.Length < key.Length) {
                    entryBuilder.Append(" 0");
                    continue;
                }

                entryBuilder.Append(" " + this.wordAt(value).ElementAt(keyWildcardIndex));
            }

            mapBuilder.Append(entryBuilder + "\n");
        }

        return mapBuilder.ToString();
    }

    public static int getWildcardIndex(String wildcard) {
        for (int i = 0; i < wildcard.Length; i++) {
            if (wildcard.ElementAt(i) != '*') {
                continue;
            }

            return i;
        }

        throw new ArgumentException("Input must contain a wildcard character '*'");
    }
}
