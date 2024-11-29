import java.io.*;
import java.util.*;

public class CacheDatabase extends WildcardDatabase {
    CacheDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);
    }

    CacheDatabase(String dictionaryPath, String wildcardMapPath) throws FileNotFoundException {
        super(dictionaryPath, false);
        this.wildcardMap = new HashMap();
        Scanner input = new Scanner(new File(wildcardMapPath));

        while (input.hasNextLine()) {
            Scanner line = new Scanner(input.nextLine());
            String key = line.next();
            HashSet<Character> value = new HashSet<Character>();

            while (line.hasNext()) {
                value.add(line.next().charAt(0));
            }

            this.wildcardMap.put(key, value);
            line.close();
        }

        input.close();
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
