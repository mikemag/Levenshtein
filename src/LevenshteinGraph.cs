using System.Text;

namespace levenshtein;

public class LevenshteinGraph
{
    public int Depth { get; private set; }
    public int OuterCount => Outer.Count;
    public int SearchedCount => _searched.Count;
    public Dictionary<int, List<int>>.KeyCollection OuterKeys => Outer.Keys;
    public Dictionary<int, List<int>> Outer;

    /**
     * Searched is used both for reconstructing paths after finishing the
     * breadth-first search and ensuring each word is contained in only one
     * layer.
     */
    private readonly Dictionary<int, List<int>> _searched;

    /**
     * Initializes the graph, with searched being completely empty and outer only containing the root word with no previous.
     */
    public LevenshteinGraph(int root)
    {
        _searched = new Dictionary<int, List<int>>(50000);
        Outer = new Dictionary<int, List<int>> { { root, [] } };
        Depth = 1;
    }

    public LevenshteinGraph()
    {
        _searched = new Dictionary<int, List<int>>(50000);
        Outer = new Dictionary<int, List<int>>();
        Depth = 1;
    }

    public void Reset(int root)
    {
        _searched.Clear();
        Outer = new Dictionary<int, List<int>> { { root, [] } };
        Depth = 1;
    }

    /**
     * This method puts every word in outer into searched and replaces outer with a new outer containing the neighbors of the previous outer.
     * NOTE: A neighbor already in the graph is not added. Any path which arrives at a word later than another path will not be a levenshtein.
     * Generating the graph layer-by-layer and checking for links on each layer prevents generation and checking of words with distances past the target.
     * It does this by temporarily creating a new Dictionary to store the new outer.
     * For each word in the previous outer, all of its neighbors are added to the new outer.
     * The word is also added to the previous of each neighbor.
     * If a word generates a neighbor that's already in the new outer, the word is simply added to the previous of that neighbor.
     * Once it is finished iterating across outer, it is put into searched and replaced with newOuter.
     */
    public bool GenerateNewOuter(LevenshteinDatabase database)
    {
        var newOuter = new Dictionary<int, List<int>>(256);

        /*searched = searched.Concat(outer).ToDictionary(pair => pair.Key, pair => pair.Value);*/
        // The foreach loop is faster than Concat, which is extremely disappointing.
        foreach (var (key, value) in Outer)
        {
            _searched.Add(key, value);
        }

        foreach (var outerWord in Outer.Keys)
        {
            var neighbors = database.FindNeighbors(outerWord);

            foreach (var neighbor in neighbors)
            {
                if (_searched.ContainsKey(neighbor))
                {
                    continue;
                }

                if (!newOuter.TryGetValue(neighbor, out var value))
                {
                    var listToAdd = new List<int> { outerWord };
                    newOuter.Add(neighbor, listToAdd);
                    continue;
                }

                value.Add(outerWord);
            }
        }

        if (newOuter.Count == 0)
        {
            return false;
        }

        Outer = newOuter;
        Depth++;
        return true;
    }

    /**
     * Finds all paths between wordIndex1 and wordIndex2 after a breadth-first
     * search has been completed.
     *
     * Does this by reading the values of the searched map (Lists containing
     * every previous word), adding each to a copy of the path and recursively
     * calling the helper method with these copies, returning once the path has
     * reached the its destination word.
     */
    public List<int[]> AllPathsBetween(int wordIndex1, int wordIndex2, bool reversed)
    {
        List<int[]> toReturn = [];
        var previous = new int[Depth];

        if (wordIndex1 == wordIndex2)
        {
            previous[0] = wordIndex1;
            toReturn.Add(previous);
            return toReturn;
        }

        if (reversed)
        {
            previous[0] = wordIndex2;
            AllPathsBetween(toReturn, previous, wordIndex1, Outer[wordIndex2], 0, 1);
        }
        else
        {
            previous[Depth - 1] = wordIndex2;
            AllPathsBetween(toReturn, previous, wordIndex1, Outer[wordIndex2], Depth - 1, -1);
        }

        return toReturn;
    }

    private void AllPathsBetween(List<int[]> paths, int[] currentPath, int root, List<int> setToSearch, int index,
        int indexIncrement)
    {
        index += indexIncrement;

        if (setToSearch.Contains(root))
        {
            currentPath[index] = root;
            paths.Add(currentPath);
            return;
        }

        foreach (var wordIndex in setToSearch)
        {
            var newPrevious = new int[currentPath.Length];
            currentPath.CopyTo(newPrevious, 0);
            newPrevious[index] = wordIndex;
            AllPathsBetween(paths, newPrevious, root, _searched[wordIndex], index, indexIncrement);
        }
    }

    /**
     * Checks if the outer of this graph contains wordIndex.
     */
    public bool OuterContains(int wordIndex)
    {
        return Outer.ContainsKey(wordIndex);
    }

    public static List<int> OuterIntersection(LevenshteinGraph graph1, LevenshteinGraph graph2)
    {
        List<int> intersection = [];
        var outerCopy = graph1.Outer;
        var otherOuter = graph2.Outer;

        if (outerCopy.Keys.Count > otherOuter.Keys.Count)
        {
            (otherOuter, outerCopy) = (outerCopy, otherOuter);
        }

        foreach (var wordIndex in outerCopy.Keys)
        {
            if (otherOuter.ContainsKey(wordIndex))
            {
                intersection.Add(wordIndex);
            }
        }

        return intersection;
    }

    public string OuterPathString(LevenshteinDatabase database)
    {
        var pathBuilder = new StringBuilder();

        var outerNumerator = Outer.GetEnumerator();
        while (outerNumerator.MoveNext())
        {
            AppendPathStrings(outerNumerator.Current.Key, outerNumerator.Current.Value, database, pathBuilder);

            pathBuilder.Append('\n');
        }

        return pathBuilder.ToString();
    }

    private void AppendPathStrings(int currentWord, List<int> previous, LevenshteinDatabase database,
        StringBuilder pathBuilder)
    {
        pathBuilder.Append(" " + database.Words[currentWord]);

        if (previous.Count <= 1)
        {
            foreach (var word in previous)
            {
                AppendPathStrings(word, _searched[word], database, pathBuilder);
            }

            return;
        }

        pathBuilder.Append(" {");
        foreach (var word in previous)
        {
            AppendPathStrings(word, _searched[word], database, pathBuilder);
        }

        pathBuilder.Append(" }");
    }

    public void WritePathStreams(int databaseWordCount, List<MemoryStream> streamList)
    {
        var outerNumerator = Outer.GetEnumerator();
        while (outerNumerator.MoveNext())
        {
            var stream = new MemoryStream();
            var writer = new BinaryWriter(stream);
            WritePathStreams(outerNumerator.Current.Key, outerNumerator.Current.Value, databaseWordCount, writer);

            writer.Write(databaseWordCount);
            streamList.Add(stream);
        }
    }

    private void WritePathStreams(int currentWord, List<int> previousWords, int databaseWordCount, BinaryWriter writer)
    {
        writer.Write(currentWord); // This is a line delimiter

        if (previousWords.Count <= 1)
        {
            foreach (var previousWord in previousWords)
            {
                WritePathStreams(previousWord, _searched[previousWord], databaseWordCount, writer);
            }

            return;
        }

        writer.Write(databaseWordCount + 1); // This marks an open bracket
        foreach (var previousWord in previousWords)
        {
            WritePathStreams(previousWord, _searched[previousWord], databaseWordCount, writer);
        }

        writer.Write(databaseWordCount + 2); // This marks a closed bracket
    }

    public void WriteOuterBinary(int databaseWordCount, BinaryWriter writer)
    {
        writer.Write(databaseWordCount);
        writer.Write(databaseWordCount + Depth);

        foreach (var key in Outer.Keys)
        {
            writer.Write(key);
        }
    }
}