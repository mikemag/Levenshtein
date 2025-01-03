using System.Text;

public class DictionaryBFSGraph : LevenshteinBFSGraph {
    public override int Depth { get => _depth; }

    private int _depth;

    public override IFrontier Frontier { get => _frontierContainer; }
 
    private struct FrontierContainer : IFrontier {
        public Dictionary<int, WordEntry> Frontier;

        public int Count => Frontier.Count;
        public IEnumerator<int> GetEnumerator() => Frontier.Keys.GetEnumerator();
        public bool Contains(int value) => Frontier.Keys.Contains(value);
    }   

    private FrontierContainer _frontierContainer;

    /**
     * _searched and _frontier are used both for ensuring each word is 
     * contained in only one layer and reconstructing and counting
     * paths after finishing the breadth-first search.
     *
     * The advantages of using an integer-keyed dictionary of WordEntries
     * are fast access times and low memory usage if spare.
     *
     * The disadvantages are high memory usage if dense and the requirement
     * to allocate memory for new lists for each word in it.
     */
    private Dictionary<int, WordEntry> _frontier { get => _frontierContainer.Frontier; set => _frontierContainer.Frontier = value; }
    private readonly Dictionary<int, WordEntry> _searched;

    /**
     * WordEntry is a container for two important properties of each word.
     *
     * PreviousWords is needed to reconstruct paths in AllPathsTo.
     *
     * PathCount is cached because the additional memory usage is 
     * inconsequential while allowing NumberOfPathsTo to be a fast dictionary
     * lookup.
     */
    private struct WordEntry {
        public int PathCount;
        public List<int> PreviousWords;

        public WordEntry(int pathCount, List<int> previousWords) {
            PathCount = pathCount;
            PreviousWords = previousWords;
        }
    }

    public DictionaryBFSGraph(int root, LevenshteinDatabase database) : base(root, database) {
        _searched = new Dictionary<int, WordEntry>();
        _frontierContainer.Frontier = new Dictionary<int, WordEntry>();
        _frontier.Add(root, new WordEntry(1, new List<int>()));
        _depth = 1;
    }

    public override bool GenerateNewFrontier() {
        Dictionary<int, WordEntry> newOuter = new Dictionary<int, WordEntry>();

        /*searched = searched.Concat(outer).ToDictionary(pair => pair.Key, pair => pair.Value);*/
        // The foreach loop is faster than Concat, which is extremely disappointing.
        foreach (KeyValuePair<int, WordEntry> entry in _frontier) {
            _searched.Add(entry.Key, entry.Value);
        }

        foreach (KeyValuePair<int, WordEntry> outerEntry in _frontier) {
            int[] neighbors = _database.FindNeighbors(outerEntry.Key);

            foreach (int neighbor in neighbors) {
                if (_searched.ContainsKey(neighbor)) {
                    continue;
                }

                if (!newOuter.ContainsKey(neighbor)) {
                    List<int> previousToAdd = new List<int>();
                    previousToAdd.Add(outerEntry.Key);

                    WordEntry entryToAdd = new WordEntry(outerEntry.Value.PathCount, previousToAdd);
                    newOuter.Add(neighbor, entryToAdd);
                    continue;
                }

                WordEntry entryCopy = newOuter[neighbor];
                entryCopy.PathCount += outerEntry.Value.PathCount;
                entryCopy.PreviousWords.Add(outerEntry.Key);
                newOuter[neighbor] = entryCopy;
            }
        }
        
        if (newOuter.Count == 0) {
            return false;
        }

        _frontier = newOuter;
        _depth++;
        return true;
    }

    public override List<int[]> AllPathsTo(int outerWordIndex, bool reversed) {
        List<int[]> toReturn = new List<int[]>(_frontier[outerWordIndex].PathCount);
        int[] previous = new int[Depth];

        if (Root == outerWordIndex) {
            previous[0] = Root;
            toReturn.Add(previous);
            return toReturn;
        }

        if (reversed) {
            previous[0] = outerWordIndex;
            AllPathsTo(toReturn, previous, _frontier[outerWordIndex].PreviousWords, 0, 1);
        } else {
            previous[Depth - 1] = outerWordIndex;
            AllPathsTo(toReturn, previous, _frontier[outerWordIndex].PreviousWords, Depth - 1, -1);
        }

        return toReturn;
    }

    private void AllPathsTo(List<int[]> paths, int[] currentPath, List<int> setToSearch, int index, int indexIncrement) {
        index += indexIncrement;

        if (setToSearch.Contains(Root)) {
            currentPath[index] = Root;
            paths.Add(currentPath);
            return;
        }

        foreach (int wordIndex in setToSearch) {
            int[] newPrevious = new int[currentPath.Length];
            currentPath.CopyTo(newPrevious, 0);
            newPrevious[index] = wordIndex;
            AllPathsTo(paths, newPrevious, _searched[wordIndex].PreviousWords, index, indexIncrement);
        }
    }

    public override int NumberOfPathsTo(int outerWordIndex) {
        return _frontier[outerWordIndex].PathCount;
    }

    public override List<int> FrontierIntersection(LevenshteinBFSGraph otherGraph) {
        List<int> intersection = new List<int>();

        if (otherGraph.GetType() != this.GetType()) {
            throw new ArgumentException("Cannot intersect LevenshteinBFSGraphs of different types");
        }

        Dictionary<int, WordEntry> biggerFrontier = this._frontier;
        Dictionary<int, WordEntry> smallerFrontier = ((DictionaryBFSGraph)otherGraph)._frontier;

        if (((DictionaryBFSGraph)otherGraph).Frontier.Count > this.Frontier.Count) {
            smallerFrontier = this._frontier;
            biggerFrontier = ((DictionaryBFSGraph)otherGraph)._frontier;
        }

        foreach (int wordIndex in smallerFrontier.Keys) {
            if (biggerFrontier.ContainsKey(wordIndex)) {
                intersection.Add(wordIndex);
            }
        }

        return intersection;
    }

    public String OuterPathString() {
        StringBuilder pathBuilder = new StringBuilder();

        Dictionary<int, WordEntry>.Enumerator outerNumerator = _frontier.GetEnumerator();
        while (outerNumerator.MoveNext()) {
            AppendPathStrings(outerNumerator.Current.Key, outerNumerator.Current.Value.PreviousWords, pathBuilder);

            pathBuilder.Append("\n");
        }

        return pathBuilder.ToString();
    }

    private void AppendPathStrings(int currentWord, List<int> previous, StringBuilder pathBuilder) {
        pathBuilder.Append(" " + _database.Words[currentWord]);

        if (previous.Count <= 1) {
            foreach (int word in previous) {
                AppendPathStrings(word, _searched[word].PreviousWords, pathBuilder);
            }
            return;
        }

        pathBuilder.Append(" {");
        foreach(int word in previous) {
            AppendPathStrings(word, _searched[word].PreviousWords, pathBuilder);
        }
        pathBuilder.Append(" }");
    }

    public override void Reset(int newRoot) {
        base.Reset(newRoot);
        _searched.Clear();
        _frontier.Clear();
        _frontier.Add(newRoot, new WordEntry(1, new List<int>()));
        _depth = 1;
    }
}
