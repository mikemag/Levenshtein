using System.Text;

public class CacheDatabase : WildcardDatabase {
    private readonly int[][] neighborArray;

    public CacheDatabase(String dictionaryPath) : base(dictionaryPath) {
        neighborArray = getInitializedNeighborArray();
    }

    public CacheDatabase(String dictionaryPath, String wildcardMapPath) : base (dictionaryPath, false) {
        fillWildcardMap(wildcardMapPath);

        neighborArray = getInitializedNeighborArray();
    }

    public override int[] findNeighbors(int wordIndex) { 
        return neighborArray[wordIndex];
    }

    public override bool areNeighbors(int wordIndex1, int wordIndex2) {
        int[] word1Neighbors = findNeighbors(wordIndex1);
        int[] word2Neighbors = findNeighbors(wordIndex2);

        if (word1Neighbors.Length <= word2Neighbors.Length) {
            return findNeighbors(wordIndex1).Contains(wordIndex2);
        }
        return findNeighbors(wordIndex2).Contains(wordIndex1);
    }

    private void fillWildcardMap(String inputFile) {
        /*StreamReader input = new StreamReader(inputFile);*/
        String[] lines = File.ReadAllLines(inputFile);

        foreach (String lineString in lines) {
            String[] line = lineString.Split(" "); 
            String key = line[0];
            List<int> value = new List<int>();
            int wildcardIndex = WildcardDatabase.getWildcardIndex(key);

            StringBuilder valueBuilder = new StringBuilder(key);

            for (int i = 1; i < line.Length; i++) {
                char valueCharacter = line[i].ElementAt(0);

                if (valueCharacter == '0') {
                    valueBuilder.Remove(wildcardIndex, 1);
                    value.Add(this.getWordIndex(valueBuilder.ToString()));
                    valueBuilder.Insert(wildcardIndex, "0");
                    continue;
                }

                valueBuilder[wildcardIndex] = valueCharacter;
                value.Add(this.getWordIndex(valueBuilder.ToString()));
            }

            this.wildcardMap.Add(key, value);
        }
    }

    private int[][] getInitializedNeighborArray() {
        int[][] initialArray = new int[this.dictionary.Length][];

        for (int i = 0; i < this.dictionary.Length; i++) {
            initialArray[i] = base.findNeighbors(i);
        }

        return initialArray;
    }

    public void wildcardMapToFile(String inputPath) {
        StreamWriter writer = new StreamWriter(inputPath);

        writer.Write(this.wildcardMapToString());
    }
}
