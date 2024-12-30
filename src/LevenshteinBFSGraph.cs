public abstract class LevenshteinBFSGraph {
    /**
     * The initial word, which is also the only node in the
     * initial frontier.
     */
    public int Root { get; private set; }

    /**
     * Due to how many functions of a BFS graph require a
     * LevenshteinDatabase, one has been included for internal 
     * use. Always refer to the internal database rather than 
     * an external one from a parameter, when possible.
     */
    protected readonly LevenshteinDatabase _database;

    /**
     * C# has a strange limitation where you can define a
     * property of type which implements a single interface, but
     * not multiple. As a workaround, this interface defines the
     * properties and methods we care about from those two
     * ICollection and IEnumerable.
     */
    public IFrontier Frontier { get; }

    public interface IFrontier {
        public int Count { get; }
        public IEnumerator<int> GetEnumerator();
    }

    public LevenshteinBFSGraph(int root, LevenshteinDatabase database, IFrontier frontier) {
        Root = root;
        _database = database;
        Frontier = frontier;
    }

    /**
     * Generates a new frontier by finding the neighbors of the 
     * current frontier and removing any which have already been 
     * searched.
     *
     * If the new frontier is empty, the current frontier will not
     * be replaced and this method will return false.
     */
    public abstract bool GenerateNewFrontier();

    /**
     * Finds all shortest paths from the root to a word index that 
     * exists in the frontier, represented as a List of int arrays 
     * due to its balance of readability and memory allocation.
     *
     * If reversed, this will instead return each path from the 
     * word index to the root.
     */
    public abstract List<int[]> AllPathsTo(int outerWordIndex, bool reversed);

    /**
     * Finds the number of unique shortest paths from the root to 
     * a word index that exists in the frontier.
     */
    public abstract int NumberOfPathsTo(int outerWordIndex);

    /**
     * Finds the intersection of the frontiers of this and another 
     * LevenshteinBFSGraph with the same implementation.
     */
    public abstract List<int> FrontierIntersection(LevenshteinBFSGraph otherGraph);

    /**
     * Replaces the root and resets all state modified by
     * GenerateNewFrontier without deallocating the memory it takes.
     */
    public virtual void Reset(int newRoot) {
        Root = newRoot;
    }
}
