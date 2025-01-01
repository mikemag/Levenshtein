using System.Text;

public class DictionaryBFSGraph : LevenshteinBFSGraph {
    public override int Depth { get => _depth; }

    private int _depth;

    public override IFrontier Frontier { get => _frontierContainer; }
 
    private struct FrontierContainer : IFrontier {
        public Dictionary<int, List<int>> Frontier;

        public int Count => Frontier.Count;
        public IEnumerator<int> GetEnumerator() => Frontier.Keys.GetEnumerator();
        public bool Contains(int value) => Frontier.Keys.Contains(value);
    }   

    private FrontierContainer _frontierContainer;

    /**
     * _searched and _frontier are used both for reconstructing paths after
     * finishing the breadth-first search and ensuring each word is
     * contained in only one layer.
     *
     * The advantages of using an integer-keyed dictionary of lists are fast
     * access times and low memory usage if spare.
     *
     * The disadvantages are high memory usage if dense and the requirement
     * to allocate memory for new lists for each word in it.
     */
    private Dictionary<int, List<int>> _frontier { get => _frontierContainer.Frontier; set => _frontierContainer.Frontier = value; }
    private readonly Dictionary<int, List<int>> _searched;

    public DictionaryBFSGraph(int root, LevenshteinDatabase database) : base(root, database) {
        _searched = new Dictionary<int, List<int>>();
        _frontierContainer.Frontier = new Dictionary<int, List<int>>();
        _frontier.Add(root, new List<int>());
        _depth = 1;
    }

    public override bool GenerateNewFrontier() {
        Dictionary<int, List<int>> newOuter = new Dictionary<int, List<int>>();

        /*searched = searched.Concat(outer).ToDictionary(pair => pair.Key, pair => pair.Value);*/
        // The foreach loop is faster than Concat, which is extremely disappointing.
        foreach (KeyValuePair<int, List<int>> entry in _frontier) {
            _searched.Add(entry.Key, entry.Value);
        }

        foreach (int outerWord in _frontier.Keys) {
            int[] neighbors = _database.FindNeighbors(outerWord);

            foreach (int neighbor in neighbors) {
                if (_searched.ContainsKey(neighbor)) {
                    continue;
                }

                if (!newOuter.ContainsKey(neighbor)) {
                    List<int> listToAdd = new List<int>();
                    listToAdd.Add(outerWord);
                    newOuter.Add(neighbor, listToAdd);
                    continue;
                }

                newOuter[neighbor].Add(outerWord);
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
        List<int[]> toReturn = new List<int[]>();
        int[] previous = new int[Depth];

        if (Root == outerWordIndex) {
            previous[0] = Root;
            toReturn.Add(previous);
            return toReturn;
        }

        if (reversed) {
            previous[0] = outerWordIndex;
            AllPathsTo(toReturn, previous, _frontier[outerWordIndex], 0, 1);
        } else {
            previous[Depth - 1] = outerWordIndex;
            AllPathsTo(toReturn, previous, _frontier[outerWordIndex], Depth - 1, -1);
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
            AllPathsTo(paths, newPrevious, _searched[wordIndex], index, indexIncrement);
        }
    }

    public override int NumberOfPathsTo(int outerWordIndex) {
        if (outerWordIndex == Root) {
            return 1;
        }

        int paths = 0;
        foreach (int outerPrevious in _frontier[outerWordIndex]) {
            paths += RecursiveNumberOfPathsTo(outerPrevious);
        }
        return paths;
    }

    private int RecursiveNumberOfPathsTo(int searchedWordIndex) {
        if (_searched[searchedWordIndex].Count() == 0) {
            return 1;
        }

        int paths = 0;
        foreach (int previous in _searched[searchedWordIndex]) {
            paths += RecursiveNumberOfPathsTo(previous); 
        }

        return paths;
    }

    public override List<int> FrontierIntersection(LevenshteinBFSGraph otherGraph) {
        List<int> intersection = new List<int>();

        if (otherGraph.GetType() != this.GetType()) {
            throw new ArgumentException("Cannot intersect LevenshteinBFSGraphs of different types");
        }

        Dictionary<int, List<int>> biggerFrontier = this._frontier;
        Dictionary<int, List<int>> smallerFrontier = ((DictionaryBFSGraph)otherGraph)._frontier;

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

        Dictionary<int, List<int>>.Enumerator outerNumerator = _frontier.GetEnumerator();
        while (outerNumerator.MoveNext()) {
            AppendPathStrings(outerNumerator.Current.Key, outerNumerator.Current.Value, pathBuilder);

            pathBuilder.Append("\n");
        }

        return pathBuilder.ToString();
    }

    private void AppendPathStrings(int currentWord, List<int> previous, StringBuilder pathBuilder) {
        pathBuilder.Append(" " + _database.Words[currentWord]);

        if (previous.Count <= 1) {
            foreach (int word in previous) {
                AppendPathStrings(word, _searched[word], pathBuilder);
            }
            return;
        }

        pathBuilder.Append(" {");
        foreach(int word in previous) {
            AppendPathStrings(word, _searched[word], pathBuilder);
        }
        pathBuilder.Append(" }");
    }

    public override void Reset(int newRoot) {
        base.Reset(newRoot);
        _searched.Clear();
        _frontier.Clear();
        _frontier.Add(newRoot, new List<int>());
        _depth = 1;
    }
}
