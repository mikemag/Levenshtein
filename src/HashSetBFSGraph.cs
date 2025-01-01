public class HashSetBFSGraph : LevenshteinBFSGraph {
    public override int Depth { get => _layers.Count; }

    public override IFrontier Frontier { get => _layerContainer; }

    private struct LayerContainer : IFrontier {
        public List<HashSet<int>> Layers;

        public int Count => Layers[Layers.Count - 1].Count;
        public IEnumerator<int> GetEnumerator() => Layers[Layers.Count - 1].GetEnumerator();
        public bool Contains(int value) => Layers[Layers.Count - 1].Contains(value);
    }

    private LayerContainer _layerContainer;

    private HashSet<int> _frontier => _layers[Depth - 1];

    private List<HashSet<int>> _layers;

    public HashSetBFSGraph(int root, LevenshteinDatabase database) : base(root, database) {
        _layers = new List<HashSet<int>>();
        _layers.Add(new HashSet<int>());
        _layerContainer.Layers = _layers;
        _frontier.Add(root);
    }

    public override bool GenerateNewFrontier() {
        HashSet<int> newFrontier = new HashSet<int>();

        foreach (int outerWordIndex in _frontier) {
            foreach (int outerNeighbor in _database.FindNeighbors(outerWordIndex)) {
                if (_frontier.Contains(outerNeighbor) || (Depth != 1 && _layers[Depth - 2].Contains(outerNeighbor))) {
                    continue;
                }

                newFrontier.Add(outerNeighbor);
            }
        }

        if (newFrontier.Count == 0) {
            return false;
        }

        _layers.Add(newFrontier);
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
            AllPathsTo(toReturn, previous, outerWordIndex, Depth - 1, 0, 1);
        } else {
            previous[Depth - 1] = outerWordIndex;
            AllPathsTo(toReturn, previous, outerWordIndex, Depth - 1, Depth - 1, -1);
        }

        return toReturn;
    }

    private void AllPathsTo(List<int[]> paths, int[] currentPath, int currentWord, int currentLayer, int index, int indexIncrement) {
        index += indexIncrement;
        currentLayer--;

        if (currentLayer == 0) {
            currentPath[index] = Root;
            paths.Add(currentPath);
            return;
        }

        foreach (int wordIndex in _database.FindNeighbors(currentWord)) {
            if (!_layers[currentLayer].Contains(wordIndex)) {
                continue;
            }

            int[] newPrevious = new int[currentPath.Length];
            currentPath.CopyTo(newPrevious, 0);
            newPrevious[index] = wordIndex;
            AllPathsTo(paths, newPrevious, wordIndex, currentLayer, index, indexIncrement);
        }
    }

    public override int NumberOfPathsTo(int outerWordIndex) {
        if (outerWordIndex == Root) {
            return 1;
        }

        int paths = 0;
        foreach (int outerNeighbor in _database.FindNeighbors(outerWordIndex)) {
            if (_layers[Depth - 2].Contains(outerNeighbor)) {
                paths += RecursiveNumberOfPathsTo(outerNeighbor, Depth - 1);
            }
        }
        return paths;
    }

    private int RecursiveNumberOfPathsTo(int wordIndex, int currentLayer) {
        currentLayer--;

        if (currentLayer == 0) {
            return 1;
        }

        int paths = 0;
        foreach (int neighbor in _database.FindNeighbors(wordIndex)) {
            if (_layers[currentLayer - 1].Contains(neighbor)) {
                paths += RecursiveNumberOfPathsTo(neighbor, currentLayer); 
            }
        }

        return paths;
    }

    public override List<int> FrontierIntersection(LevenshteinBFSGraph otherGraph) {
        List<int> intersection = new List<int>();

        if (otherGraph.GetType() != this.GetType()) {
            throw new ArgumentException("Cannot intersect LevenshteinBFSGraphs of different types");
        }

        HashSet<int> biggerFrontier = this._frontier;
        HashSet<int> smallerFrontier = ((HashSetBFSGraph)otherGraph)._frontier;

        if (otherGraph.Frontier.Count > this.Frontier.Count) {
            biggerFrontier = smallerFrontier;
            smallerFrontier = this._frontier;
        }

        foreach (int wordIndex in smallerFrontier) {
            if (biggerFrontier.Contains(wordIndex)) {
                intersection.Add(wordIndex);
            }
        }

        return intersection;
    }

    public override void Reset(int newRoot) {
        base.Reset(newRoot);
        _layers.Clear();
    }
}
