import java.io.*;
import java.util.*;

public class CacheDatabase extends WildcardDatabase {
    CacheDatabase(String dictionaryPath) throws FileNotFoundException {
        super(dictionaryPath);
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
