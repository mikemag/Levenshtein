/*
Author: Tristen Yim
Project: Levenshtein Distance (GS14-01)
Filename: Levenshtein.java
Maintenance Log:
    Started. Already had ideas as to how I was going to find the Levenshtein between two words (13 Mar 2023 23:07)
        Started the findDistance method. Currently, the LevenshteinNodes are stored in a LinkedList of HashSets (Pi Day Bomb Squad)
        It works for some pairs of words, but underestimates for others. After some testing, looks like a problem with isNeighboring (1:27)
        It works for all tested pairs except monkey -> business. Going to have to work that out and make the algorithm more efficient as it takes nearly 9 seconds for dog and quack (2:05)
    Attempted to fix findDistance but failed so miserably that I have to essentially restart (15 Mar 2023 0:45)
*/

import java.io.*;
import java.util.*;

public class Levenshtein {
    public final ArrayList<String> dictionary;
    public final HashMap<Integer, Integer> lengthStarts;
    public Levenshtein(String filename) throws FileNotFoundException {
        dictionary = new ArrayList<>();
        lengthStarts = new HashMap<>();
        Scanner s = new Scanner(new File(filename));
        int i = 0;
        while (s.hasNext()) {
            String word = s.next();
            dictionary.add(word);
            lengthStarts.putIfAbsent(word.length(), i);
            i++;
        }
    }
    public static void main(String[] args) throws FileNotFoundException {
        long time1 = System.nanoTime();
        Levenshtein test = new Levenshtein("src\\Dictionary.txt");
        System.out.println("Distance between 'monkey' and 'business': " + test.findDistance("monkey", "business"));
        System.out.println((System.nanoTime() - time1) / 1000000);
    }
    public int findDistance(String w1, String w2) {

    }
}
