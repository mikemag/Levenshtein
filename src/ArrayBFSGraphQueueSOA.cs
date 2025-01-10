using System.Runtime.CompilerServices;

public class ArrayBFSGraphQueueSOA : LevenshteinBFSGraph {
    public override int Depth { get => _depth; }

    private int _depth;

    public override IFrontier Frontier {
        get => new FrontierHelper(depths, _depth);
    }

    private class FrontierHelper : IFrontier {
        private byte[] _depths;
        private int _depth;

        public FrontierHelper(byte[] depths, int depth) {
            _depths = depths;
            _depth = depth;
        }

        public int Count { 
            get {
                int count = 0;
                foreach (var d in _depths) {
                    if (d == _depth) {
                        count++;
                    }
                }
                return count;
            }
        }

        public bool Contains(int value) {
            return _depths[value] == _depth;
        }

        public IEnumerator<int> GetEnumerator() {
            return new FrontierEnumerator(_depths, _depth);
        }

        private class FrontierEnumerator : IEnumerator<int> {
            private byte[] _depths;
            private int _depth;
            private int _index = -1;

            int IEnumerator<int>.Current => _index;

            public object Current => _index;

            public FrontierEnumerator(byte[] depths, int depth) {
                _depths = depths;
                _depth = depth;
            }

            public bool MoveNext() {
                do {
                    _index++;
                    if (_index == _depths.Count()) {
                        _index--;
                        return false;
                    }
                } while (_depths[_index] != _depth);

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
    public struct WordEntry {
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
    public int[] pathCounts;
    public byte[] depths;
    private Queue<int> _queue;

    public ArrayBFSGraphQueueSOA(int root, LevenshteinDatabase database) : base(root, database) {
        pathCounts = new int[database.Words.Count()];
        depths = new byte[database.Words.Count()];
        _depth = 1;
        pathCounts[root] = 1;
        depths[root] = 1;
        _queue = new Queue<int>();
        _queue.Enqueue(root);
        _queue.Enqueue(-1);
    }

    public override void Reset(int newRoot) {
        base.Reset(newRoot);
        Array.Clear(depths);
        Array.Clear(pathCounts); // mmmfixme: don't really need to clear, but do need to check depths[i] != 0 before using elsewhere.
        _depth = 1;
        pathCounts[newRoot] = 1;
        depths[newRoot] = 1;
        _queue.Clear();
        _queue.Enqueue(newRoot);
        _queue.Enqueue(-1);
    }

    public override bool GenerateNewFrontier()
    {
        var succeeded = false;

        while (_queue.TryDequeue(out var i))
        {
            if (i == -1)
            {
                _queue.Enqueue(-1);
                break;
            }

            [MethodImpl(MethodImplOptions.AggressiveInlining)]
            void ConsiderNeighbor(int neighbor, int neighborDepth)
            {
                if (neighborDepth == 0)
                {
                    succeeded = true;
                    depths[neighbor] = (byte)(_depth + 1);
                    pathCounts[neighbor] = pathCounts[i];
                    _queue.Enqueue(neighbor);
                }
                else if (neighborDepth == _depth + 1)
                {
                    pathCounts[neighbor] += pathCounts[i];
                }
            }

            var nl = _database.FindNeighbors(i);
            var nll = nl.Length & ~3;
            var j = 0;
            for (; j < nll; j += 4)
            {
                var neighbor0 = nl[j];
                var neighbor1 = nl[j + 1];
                var neighbor2 = nl[j + 2];
                var neighbor3 = nl[j + 3];

                ConsiderNeighbor(neighbor0, depths[neighbor0]);
                ConsiderNeighbor(neighbor1, depths[neighbor1]);
                ConsiderNeighbor(neighbor2, depths[neighbor2]);
                ConsiderNeighbor(neighbor3, depths[neighbor3]);
            }

            for (; j < nl.Length; j++)
            {
                var neighbor = nl[j];
                ConsiderNeighbor(neighbor, depths[neighbor]);
            }
        }

        if (!succeeded) return false;

        _depth++;
        return true;
    }

    public override List<int[]> AllPathsTo(int outerWordIndex, bool reversed) {
        List<int[]> toReturn = new List<int[]>(pathCounts[outerWordIndex]);
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
            if (depths[neighbor] != currentDepth) {
                continue;
            }

            int[] newPath = new int[currentPath.Count()];
            currentPath.CopyTo(newPath, 0);
            newPath[pathIndex] = neighbor;
            AllPathsTo(paths, newPath, neighbor, currentDepth, pathIndex, pathIncrement);
        }
    }

    public override int NumberOfPathsTo(int outerWordIndex) {
        return pathCounts[outerWordIndex];
    }

    public override List<int> FrontierIntersection(LevenshteinBFSGraph otherGraph) {
        List<int> intersection = new List<int>();

        if (otherGraph.GetType() != this.GetType()) {
            throw new ArgumentException("Cannot intersect LevenshteinBFSGraphs of different types");
        }

        for (int i = 0; i < _database.Words.Count(); i++) {
            if (depths[i] == _depth && ((ArrayBFSGraph)otherGraph)._wordArray[i].Depth == otherGraph.Depth) {
                intersection.Add(i);
            }
        }

        return intersection;
    }
}