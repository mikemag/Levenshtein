import java.io.*;
import java.util.*;

public class CacheDatabase extends WildcardDatabase {
    private final Integer[][] neighborArray;

    CacheDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);

        neighborArray = getInitializedNeighborArray();
    }

    CacheDatabase(String dictionaryPath, String wildcardMapPath) throws FileNotFoundException {
        super(dictionaryPath, false);

        fillWildcardMap(new File(wildcardMapPath));

        neighborArray = getInitializedNeighborArray();
    }

    @Override
    public Integer[] findNeighbors(int wordIndex) { 
        return neighborArray[wordIndex];
    }

    @Override
    public boolean areNeighbors(int wordIndex1, int wordIndex2) {
        Integer[] word1Neighbors = findNeighbors(wordIndex1);
        Integer[] word2Neighbors = findNeighbors(wordIndex2);

        if (word1Neighbors.length <= word2Neighbors.length) {
            return Arrays.asList(findNeighbors(wordIndex1)).contains(wordIndex2);
        }
        return Arrays.asList(findNeighbors(wordIndex2)).contains(wordIndex1);
    }

    private void fillWildcardMap(File inputFile) throws FileNotFoundException {
        Scanner input = new Scanner(inputFile);

        while (input.hasNextLine()) {
            Scanner line = new Scanner(input.nextLine());
            String key = line.next();
            ArrayList<Integer> value = new ArrayList();
            int wildcardIndex = WildcardDatabase.getWildcardIndex(key);

            StringBuilder valueBuilder = new StringBuilder(key);
            boolean firstValue = true;

            while (line.hasNext()) {
                char valueCharacter = line.next().charAt(0);

                if (firstValue) {
                    firstValue = false;

                    if (valueCharacter == '0') {
                        valueBuilder.deleteCharAt(wildcardIndex);
                        value.add(this.getWordIndex(valueBuilder.toString()));
                        valueBuilder.insert(wildcardIndex, "0");
                        continue;
                    }
                }

                valueBuilder.setCharAt(wildcardIndex, valueCharacter);
                value.add(this.getWordIndex(valueBuilder.toString()));
            }

            this.wildcardMap.put(key, value);
            line.close();
        }

        input.close();
    }

    private Integer[][] getInitializedNeighborArray() {
        Integer[][] initialArray = new Integer[this.dictionary.length][];

        for (int i = 0; i < this.dictionary.length; i++) {
            initialArray[i] = super.findNeighbors(i);
        }

        return initialArray;
    }

    public void wildcardMapToFile(File outFile) throws FileNotFoundException {
        Scanner mapStringScanner = new Scanner(this.wildcardMapToString());
        PrintStream output = new PrintStream(outFile);

        while (mapStringScanner.hasNextLine()) {
            output.println(mapStringScanner.nextLine());
        }
        output.close();
    }
}
