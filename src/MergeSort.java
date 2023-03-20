
/*
Author: Tristen Yim
Project Name: Merge Sort (GS10-04)
filename: mergeSort.java
Purpose:
    To sort any kind of sortable data
Maintenance Log:
    Started mergeSort, starting to implement the merging and checking which string goes after which (30 Jan 2023 10:41)
    Finished mergeSort, and made it output to a reversed file. Currently, AllNALl.txt only has about 180 lines, but I can change that. Takes about 70 milliseconds to sort (1 Feb 2023)
    I changed the testing file to one that contains about 1850 lines. On average, it takes about 200 milliseconds to sort. Only about 2.5x longer for a file that's 10x longer
        Also updated the heading (2 Feb 2023 10:02)
    Copied this code and adapted it for sorting StudentData (6 Mar 2023 10:18)
*/

import java.util.*;

public class MergeSort {
    /**
     * This uses a merge sort algorithm to sort a list
     *
     * In a merge sort algorithm, the list is continuously split in half until it is sorted (When each
     * sublist only has one object). After each split, the two sublists are "merged" into one sorted list.
     *
     * To merge, the front of the two sublists are compared. The one that goes first is removed from that
     * sublist and added to the main list. This repeats until the entire list is sorted.
     *
     * MergerSort is an O(logn) algorithm since the repeated splitting of the data repeatedly divides the work in half.
     *
     * @param unsorted The unsorted list
     * @param c The comparator used to determine the sorted order
     * @return Returns the sorted list
     */
    public static Object[] sort(Object[] unsorted, Comparator c) {
        splitAndMerge(unsorted, c);
        return unsorted;
    }
    private static void splitAndMerge(Object[] o, Comparator c) {
        if (o.length > 1) {
            Object[] side1 = Arrays.copyOfRange(o, 0, o.length / 2);
            Object[] side2 = Arrays.copyOfRange(o, o.length / 2, o.length);
            splitAndMerge(side1, c);
            splitAndMerge(side2, c);
            merge(o, side1, side2, c);
        }
    }
    private static void merge(Object[] merged, Object[] half1, Object[] half2, Comparator c) {
        // The index is kept track of to ensure the program doesn't try to read past the bounds of either part
        int half1Index = 0;
        int half2Index = 0;
        for (int i = 0; i < merged.length; i++) {
            if (half2.length == half2Index || half1.length > half1Index
                    && c.compare(half1[half1Index], half2[half2Index]) <= 0) {
                merged[i] = half1[half1Index];
                half1Index++;
            } else {
                merged[i] = half2[half2Index];
                half2Index++;
            }
        }
    }
}