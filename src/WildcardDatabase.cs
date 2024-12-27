using System.Text;

public class WildcardDatabase : LevenshteinDatabase
{
    protected readonly Dictionary<String, List<int>> _wildcardMap;

    public WildcardDatabase(FileInfo dictionarySource) : base(dictionarySource)
    {
        _wildcardMap = GetInitializedWildcardMap();
    }

    protected WildcardDatabase(FileInfo dictionarySource, bool initializeWildcardMap) : base(dictionarySource)
    {
        if (initializeWildcardMap)
        {
            _wildcardMap = GetInitializedWildcardMap();
        }
        else
        {
            _wildcardMap = new Dictionary<String, List<int>>();
        }
    }

    public List<String> LocalWildcardIdentities(int wordIndex)
    {
        List<String> identities = new List<String>();

        AddEachWildcard(wordIndex, identities, (wildcardSubstitute, wildcardIdentity,
            wildcardListObject) =>
        {
            if (_wildcardMap.ContainsKey(wildcardIdentity))
            {
                ((List<String>)wildcardListObject).Add(wildcardIdentity);
            }
        });
        return identities;
    }

    public List<String> AllWildcardIdentities(int wordIndex)
    {
        List<String> identities = new List<String>();

        AddEachWildcard(wordIndex, identities, (wildcardSubstitute, wildcardIdentity,
            wildcardMapObject) =>
        {
            ((List<String>)wildcardMapObject).Add(wildcardIdentity);
        });
        return identities;
    }

    private void PutEachWildcard(int wordIndex, Dictionary<String, List<int>> destination)
    {
        AddEachWildcard(wordIndex, destination, (wildcardSubstitute, wildcardIdentity,
            wildcardMapObject) =>
        {
            Dictionary<String, List<int>> map = (Dictionary<String, List<int>>)wildcardMapObject;

            map.TryAdd(wildcardIdentity, new List<int>());
            map[wildcardIdentity].Add(wildcardSubstitute);
        });
    }

    private void AddEachWildcard(int wordIndex, Object dataStructure,
        Action<int, String, Object> wildcardDataStructureAdder)
    {
        String word = this.Words[wordIndex];
        StringBuilder cardBuilder = new StringBuilder(word);
        int wordLength = word.Length;

        cardBuilder[0] = '*';
        wildcardDataStructureAdder(wordIndex, cardBuilder.ToString(), dataStructure);
        for (int i = 1; i < wordLength; i++)
        {
            cardBuilder[i - 1] = word.ElementAt(i - 1);
            cardBuilder[i] = '*';
            wildcardDataStructureAdder(wordIndex, cardBuilder.ToString(), dataStructure);
        }

        cardBuilder.Append('*');
        cardBuilder[wordLength - 1] = word.ElementAt(wordLength - 1);
        wildcardDataStructureAdder(wordIndex, cardBuilder.ToString(), dataStructure);
        for (int i = wordLength; i > 0; i--)
        {
            cardBuilder[i] = word.ElementAt(i - 1);
            cardBuilder[i - 1] = '*';
            wildcardDataStructureAdder(wordIndex, cardBuilder.ToString(), dataStructure);
        }
    }

    public override bool AreNeighbors(int wordIndex1, int wordIndex2)
    {
        return FindNeighbors(wordIndex1).Contains(wordIndex2);
    }

    public override int[] FindNeighbors(int wordIndex)
    {
        List<int> returnList = new List<int>();

        foreach (String wildcard in this.LocalWildcardIdentities(wordIndex))
        {
            foreach (int neighborIndex in _wildcardMap[wildcard])
            {
                if (neighborIndex != wordIndex && !returnList.Contains(neighborIndex))
                {
                    returnList.Add(neighborIndex);
                }
            }
        }

        return returnList.ToArray();
    }

    private Dictionary<String, List<int>> GetInitializedWildcardMap()
    {
        Dictionary<String, List<int>> returnMap = new Dictionary<String, List<int>>();

        for (int i = 0; i < this.Words.Length; i++)
        {
            PutEachWildcard(i, returnMap);
        }

        foreach (KeyValuePair<String, List<int>> entry in returnMap)
        {
            if (entry.Value.Count == 1)
            {
                returnMap.Remove(entry.Key);
            }
        }

        return returnMap;
    }

    public String WildcardMapToString()
    {
        StringBuilder mapBuilder = new StringBuilder();

        foreach (KeyValuePair<String, List<int>> entry in _wildcardMap)
        {
            String key = entry.Key;
            int keyWildcardIndex = WildcardDatabase.GetWildcardIndex(key);
            StringBuilder entryBuilder = new StringBuilder();
            entryBuilder.Append(key);

            foreach (int value in new SortedSet<int>(entry.Value))
            {
                String word = this.Words[value];

                if (word.Length < key.Length)
                {
                    entryBuilder.Append(" 0");
                    continue;
                }

                entryBuilder.Append(" " + this.Words[value].ElementAt(keyWildcardIndex));
            }

            mapBuilder.Append(entryBuilder + "\n");
        }

        return mapBuilder.ToString();
    }

    public static int GetWildcardIndex(String wildcard)
    {
        for (int i = 0; i < wildcard.Length; i++)
        {
            if (wildcard.ElementAt(i) != '*')
            {
                continue;
            }

            return i;
        }

        throw new ArgumentException("Input must contain a wildcard character '*'");
    }
}