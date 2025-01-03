public class ArrayBFSGraph : LevenshteinBFSGraph {
    public override int Depth { get => _depth; }

    private int _depth;

    public override IFrontier Frontier {
        get => new FrontierHelper(_wordArray, _depth);
    }

    private struct FrontierHelper : IFrontier {
        private WordEntry[] _wordArray;
        private int _depth;

        public FrontierHelper(WordEntry[] wordArray, int depth) {
            _wordArray = wordArray;
            _depth = depth;
        }

        public int Count { 
            get {
                int count = 0;
                foreach (WordEntry word in _wordArray) {
                    if (word.Depth == _depth) {
                        count++;
                    }
                }
                return count;
            }
        }

        public bool Contains(int value) {
            return _wordArray[value].Depth == _depth;
        }

        public IEnumerator<int> GetEnumerator() {
            return new FrontierEnumerator(_wordArray, _depth);
        }

        private class FrontierEnumerator : IEnumerator<int> {
            private WordEntry[] _wordArray;
            private int _depth;
            private int _index = -1;

            int IEnumerator<int>.Current => _index;

            public object Current => _index;

            public FrontierEnumerator(WordEntry[] wordArray, int depth) {
                _wordArray = wordArray;
                _depth = depth;
            }

            public bool MoveNext() {
                do {
                    _index++;
                    if (_index == _wordArray.Count()) {
                        _index--;
                        return false;
                    }
                } while (_wordArray[_index].Depth != _depth);

                return true;
            }

            public void Reset() {
                _index = -1;
            }

            public void Dispose() {}
        }
    }

    /**
     * WordEntry is a container for two important properties of each word.
     *
     * Depth contains the depth of the graph when the word was added to the
     * frontier. This is necessary for path reconstruction.
     *
     * PathCount is cached because the additional memory usage is 
     * inconsequential while allowing NumberOfPathsTo to be a array
     * lookup.
     */
    private struct WordEntry {
        public int PathCount;
        public byte Depth;

        public WordEntry(int pathCount, byte depth) {
            PathCount = pathCount;
            Depth = depth;
        }
    }

    /**
     * _wordArray is used both to ensure each word is contained in only 
     * one layer and reconstructing and counting paths after 
     * finishing the breadth-first search.
     *
     * The advantages of using a single array are memory efficiency and
     * zero reallocation between resets and.
     *
     * The disadvantages are the reliance on a LevenshteinDatabase that
     * caches neighbors, requirement to pre-allocate the entire array,
     * extra time it takes to find the previous words, and slow frontier
     * indexing.
     */
    private WordEntry[] _wordArray;

    public ArrayBFSGraph(int root, LevenshteinDatabase database) : base(root, database) {
        _wordArray = new WordEntry[database.Words.Count()];
        _depth = 1;
        _wordArray[root] = new WordEntry(1, 1);
    }

    public override bool GenerateNewFrontier() {
        bool succeeded = false;

        for (int i = 0; i < _wordArray.Count(); i++) {
            if (_wordArray[i].Depth != _depth) {
                continue;
            }

            foreach (int neighbor in _database.FindNeighbors(i)) {
                if (_wordArray[neighbor].Depth == 0) {
                    succeeded = true;
                    _wordArray[neighbor] = new WordEntry(_wordArray[i].PathCount, (byte)(_depth + 1));
                } else if (_wordArray[neighbor].Depth == _depth + 1) {
                    WordEntry wordCopy = _wordArray[neighbor];
                    wordCopy.PathCount += _wordArray[i].PathCount;
                    _wordArray[neighbor] = wordCopy;
                }
            }
        }

        if (!succeeded) {
            return false;
        }

        _depth++;
        return true;
    }

    public override List<int[]> AllPathsTo(int outerWordIndex, bool reversed) {
        List<int[]> toReturn = new List<int[]>(_wordArray[outerWordIndex].PathCount);
        int[] previous = new int[_depth];

        if (Root == outerWordIndex) {
            previous[0] = Root;
            toReturn.Add(previous);
            return toReturn;
        }

        if (reversed) {
            previous[0] = outerWordIndex;
            AllPathsTo(toReturn, previous, outerWordIndex, Depth, 0, 1);
        } else {
            previous[Depth - 1] = outerWordIndex;
            AllPathsTo(toReturn, previous, outerWordIndex, Depth, Depth - 1, -1);
        }

        return toReturn;
    }

    private void AllPathsTo(List<int[]> paths, int[] currentPath, int currentWord, int currentDepth, int pathIndex, int pathIncrement) {
        pathIndex += pathIncrement;
        currentDepth--;

        if (currentDepth == 1) {
            currentPath[pathIndex] = Root;
            paths.Add(currentPath);
            return;
        }

        foreach (int neighbor in _database.FindNeighbors(currentWord)) {
            if (_wordArray[neighbor].Depth != currentDepth) {
                continue;
            }

            int[] newPath = new int[currentPath.Count()];
            currentPath.CopyTo(newPath, 0);
            newPath[pathIndex] = neighbor;
            AllPathsTo(paths, newPath, neighbor, currentDepth, pathIndex, pathIncrement);
        }
    }

    public override int NumberOfPathsTo(int outerWordIndex) {
        return _wordArray[outerWordIndex].PathCount;
    }

    public override List<int> FrontierIntersection(LevenshteinBFSGraph otherGraph) {
        List<int> intersection = new List<int>();

        if (otherGraph.GetType() != this.GetType()) {
            throw new ArgumentException("Cannot intersect LevenshteinBFSGraphs of different types");
        }

        for (int i = 0; i < _database.Words.Count(); i++) {
            if (_wordArray[i].Depth == _depth && ((ArrayBFSGraph)otherGraph)._wordArray[i].Depth == otherGraph.Depth) {
                intersection.Add(i);
            }
        }

        return intersection;
    }

    public override void Reset(int newRoot) {
        base.Reset(newRoot);
        Array.Clear(_wordArray);
        _depth = 1;
        _wordArray[newRoot] = new WordEntry(1, 1);
    }
}
