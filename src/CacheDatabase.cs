using System.Text;

public class CacheDatabase : WildcardDatabase {
    private readonly int[][] _neighborArray;

    public CacheDatabase(String dictionaryPath) : base(dictionaryPath) {
        _neighborArray = GetInitializedNeighborArray();
    }

    public CacheDatabase(String dictionaryPath, String wildcardMapPath) : base (dictionaryPath, false) {
        FillWildcardMap(wildcardMapPath);

        _neighborArray = GetInitializedNeighborArray();
    }

    public override int[] FindNeighbors(int wordIndex) { 
        return _neighborArray[wordIndex];
    }

    public override bool AreNeighbors(int wordIndex1, int wordIndex2) {
        int[] word1Neighbors = FindNeighbors(wordIndex1);
        int[] word2Neighbors = FindNeighbors(wordIndex2);

        if (word1Neighbors.Length <= word2Neighbors.Length) {
            return FindNeighbors(wordIndex1).Contains(wordIndex2);
        }
        return FindNeighbors(wordIndex2).Contains(wordIndex1);
    }

    private void FillWildcardMap(String inputFile) {
        /*StreamReader input = new StreamReader(inputFile);*/
        String[] lines = File.ReadAllLines(inputFile);

        foreach (String lineString in lines) {
            String[] line = lineString.Split(" "); 
            String key = line[0];
            List<int> value = new List<int>();
            int wildcardIndex = WildcardDatabase.GetWildcardIndex(key);

            StringBuilder valueBuilder = new StringBuilder(key);

            for (int i = 1; i < line.Length; i++) {
                char valueCharacter = line[i].ElementAt(0);

                if (valueCharacter == '0') {
                    valueBuilder.Remove(wildcardIndex, 1);
                    value.Add(this.Indexes[valueBuilder.ToString()]);
                    valueBuilder.Insert(wildcardIndex, "0");
                    continue;
                }

                valueBuilder[wildcardIndex] = valueCharacter;
                value.Add(this.Indexes[valueBuilder.ToString()]);
            }

            this._wildcardMap.Add(key, value);
        }
    }

    private int[][] GetInitializedNeighborArray() {
        int[][] initialArray = new int[this.Words.Length][];

        for (int i = 0; i < this.Words.Length; i++) {
            initialArray[i] = base.FindNeighbors(i);
        }

        return initialArray;
    }

    public void WildcardMapToFile(String inputPath) {
        StreamWriter writer = new StreamWriter(inputPath);

        writer.Write(this.WildcardMapToString());
    }
}
