public class ArrayBFSGraph : LevenshteinBFSGraph {
    public override int Depth { get => _depth; }

    private int _depth;

    public override IFrontier Frontier {
        get => new FrontierHelper(_layerArray, _depth);
    }

    private struct FrontierHelper : IFrontier {
        private byte[] _layerArray;
        private int _depth;

        public FrontierHelper(byte[] layerArray, int depth) {
            _layerArray = layerArray;
            _depth = depth;
        }

        public int Count { 
            get {
                int count = 0;
                foreach (int layer in _layerArray) {
                    if (layer == _depth) {
                        count++;
                    }
                }
                return count;
            }
        }

        public bool Contains(int value) {
            return _layerArray[value] == _depth;
        }

        public IEnumerator<int> GetEnumerator() {
            return new FrontierEnumerator(_layerArray, _depth);
        }

        private class FrontierEnumerator : IEnumerator<int> {
            private byte[] _layerArray;
            private int _depth;
            private int _index = -1;

            int IEnumerator<int>.Current => _index;

            public object Current => _index;

            public FrontierEnumerator(byte[] layerArray, int depth) {
                _layerArray = layerArray;
                _depth = depth;
            }

            public bool MoveNext() {
                do {
                    _index++;
                    if (_index == _layerArray.Count()) {
                        _index--;
                        return false;
                    }
                } while (_layerArray[_index] != _depth);

                return true;
            }

            public void Reset() {
                _index = -1;
            }

            public void Dispose() {}
        }
    }

    public byte[] _layerArray;

    public ArrayBFSGraph(int root, LevenshteinDatabase database) : base(root, database) {
        _layerArray = new byte[database.Words.Count()];
        _depth = 1;
        _layerArray[root] = 1;
    }

    public override bool GenerateNewFrontier() {
        bool succeeded = false;

        for (int i = 0; i < _layerArray.Count(); i++) {
            if (_layerArray[i] != _depth) {
                continue;
            }

            foreach (int neighbor in _database.FindNeighbors(i)) {
                if (_layerArray[neighbor] != 0) {
                    continue;
                }

                succeeded = true;
                _layerArray[neighbor] = (byte)(_depth + 1);
            }
        }

        if (!succeeded) {
            return false;
        }

        _depth++;
        return true;
    }

    public override List<int[]> AllPathsTo(int outerWordIndex, bool reversed) {
        List<int[]> toReturn = new List<int[]>();
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
            if (_layerArray[neighbor] != currentDepth) {
                continue;
            }

            int[] newPath = new int[currentPath.Count()];
            currentPath.CopyTo(newPath, 0);
            newPath[pathIndex] = neighbor;
            AllPathsTo(paths, newPath, neighbor, currentDepth, pathIndex, pathIncrement);
        }
    }

    public override int NumberOfPathsTo(int outerWordIndex) {
        if (outerWordIndex == Root) {
            return 1;
        }

        int paths = 0;
        foreach (int neighbor in _database.FindNeighbors(outerWordIndex)) {
            if (_layerArray[neighbor] == _depth - 1) {
                paths += RecursiveNumberOfPathsTo(neighbor, _depth - 1);
            }
        }
        return paths;
    }

    private int RecursiveNumberOfPathsTo(int wordIndex, int currentDepth) {
        if (currentDepth == 1) {
            return 1;
        }

        int paths = 0;
        foreach (int neighbor in _database.FindNeighbors(wordIndex)) {
            if (_layerArray[neighbor] == currentDepth - 1) {
                paths += RecursiveNumberOfPathsTo(neighbor, currentDepth - 1);
            }
        }
        return paths;
    }

    public override List<int> FrontierIntersection(LevenshteinBFSGraph otherGraph) {
        List<int> intersection = new List<int>();

        if (otherGraph.GetType() != this.GetType()) {
            throw new ArgumentException("Cannot intersect LevenshteinBFSGraphs of different types");
        }

        for (int i = 0; i < _database.Words.Count(); i++) {
            if (_layerArray[i] == _depth && ((ArrayBFSGraph)otherGraph)._layerArray[i] == otherGraph.Depth) {
                intersection.Add(i);
            }
        }

        return intersection;
    }

    public override void Reset(int newRoot) {
        base.Reset(newRoot);
        Array.Clear(_layerArray);
        _depth = 1;
        _layerArray[newRoot] = 1;
    }
}
