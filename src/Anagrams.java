/*
Author: Tristen Yim
Project: Anagrams (GS14-03)
Filename: Anagrams.java
Maintenance Log:
    Started (6 April 2023 10:14)
        Notably wrote findAnagrams, toAnagram, and toCanonical, but have not tested (10:56)
    Added more comments (7 April 2023 9:20)
*/

import java.io.*;
import java.util.*;

public class Anagrams {
    private final String[] dictionary;
    private final int[] lengthStartIndexes;
    private HashMap<String, String> wordKey;
    private HashMap<String, ArrayList<String>> canonicalKey;

    /**
     * Reads the file at filepath and puts each line into dictionary. Then sorts the dictionary.
     * The first index of each length of word is put into lengthStartIndexes (with the index being the word length).
     * @param filepath Path of the file to read the dictionary from.
     * @throws IOException
     */
    public Anagrams(String filepath) throws IOException {
        ArrayList<String> listDictionary = new ArrayList();
        Scanner s = new Scanner(new File(filepath));
        while(s.hasNext()) {
            listDictionary.add(s.next());
        }
        dictionary = listDictionary.toArray(new String[0]);
        Arrays.sort(dictionary, COMPARE_BY_LENGTH);
        lengthStartIndexes = new int[dictionary[dictionary.length - 1].length()];
        int currentLength = -1;
        for (int i = 0; i < dictionary.length; i++) {
            int l = dictionary[i].length();
            if (l != currentLength) {
                currentLength = l;
                lengthStartIndexes[l] = i;
            }
        }
    }

    private void generateAnagrams(int length) {
        int maxI = lengthStartIndexes[length + 1];
        for (int i = lengthStartIndexes[length]; i < maxI; i++) {
            String w = dictionary[i];
            String canonical = toCanonical(w);
            wordKey.put(w, canonical);
            ArrayList<String> anagrams = canonicalKey.getOrDefault(canonical, new ArrayList<>());
            anagrams.add(w);
            canonicalKey.put(canonical, anagrams);
        }
    }

    private ArrayList<String> findAnagrams(String w) {
        String canonical = wordKey.getOrDefault(w, null);
        if (canonical.equals(null)) {
            generateAnagrams(w.length());
        }
        ArrayList<String> anagrams = canonicalKey.get(canonical);
        anagrams.remove(w);
        return  anagrams;
    }

    /**
     * Converts w to a char array and sorts that array naturally. Then calls toString on that array and returns it.
     * @param w Word to convert.
     * @return w in its canonical form. Note that since this returns a string representation of an array of w, it won't return the raw canonical string.
     *         For example, toCanonical
     */
    private String toCanonical(String w) {
        char[] a = w.toLowerCase().toCharArray();
        Arrays.sort(a);
        return Arrays.toString(a);
    }

    /**
     * Comparator which sorts strings first by length then their natural ordering, which is used to sort the dictionary.
     */
    private static final Comparator<String> COMPARE_BY_LENGTH = (o1, o2) -> {
        int c = o1.length() - o2.length();
        if (c == 0) {
            return o1.compareTo(o2);
        } else {
            return c;
        }
    };
}
