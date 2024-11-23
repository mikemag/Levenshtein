# Levenshtein

## Overview

This project was inspired by a problem from Building Java Programs (BJP). Nicknamed the "Levenshtein Distance" problem, Chapter 11's Programming Project 1 adds a unique restriction to the Levenshtein distance algorithm. Here is the full problem taken directly from the textbook:

>*Write a program that computes the edit distance (also called the Levenshtein distance, for its creator Vladimir Levenshtein) between two words. The edit distance between two strings is the minimum number of operations that are needed to transform one string into the other. For this program, an operation is a substitution of a single character, such as from “brisk” to “brick”. The edit distance between the words “dog” and “cat” is 3, following the chain of “dot”, “cot”, and “cat” to transform “dog” into “cat”. When you compute the edit distance between two words, each intermediate word must be an actual valid word. Edit distances are useful in applications that need to determine how similar two strings are, such as spelling checkers.*\
>
>*Read your input from a dictionary text file. From this file, compute a map from every word to its immediate neighbors, that is, the words that have an edit distance of 1 from it. Once this map is built, you can walk it to find paths from one word to another.*\
>
>*A good way to process paths to walk the neighbor map is to use a linked list of words to visit, starting with the beginning word, such as “dog”. Your algorithm should repeatedly remove the front word of the list and add all of its neighbors to the end of the list, until the ending word (such as “cat”) is found or until the list becomes empty, which indicates that no path exists between the two words.*

The aim of this project is to explore the efficiency of different ways of solving this problem.

## Goals

#### &nbsp;&nbsp;Attempt to find the most time-efficient solution to this problem

While it's unlikely the absolute most efficient solution will be found by a single amateur programmer, searching for it is a  unique and rewarding challenge.

#### &nbsp;&nbsp;Start with sorted dictionary files

This dictionary should be nothing more than a simple word list. Each line must contain one word exactly and be unique, no repeating words. Words must not contain any characters other than lowercase letters. Sort it by length, with alphabetical order used as a tiebreaker. Using multiple dictionaries with differing sizes may be helpful depending on what is being tested: Assume all are fair game, as long as they meet these requirements. Finally, the efficiency of the algorithm used to sort the dictionaries need not be considered, as that is outside the scope of this project.

#### &nbsp;&nbsp;Consider the efficiency of *every stage* of each solutions

Every stage after the sorting and reading of the dictionaries must be considered. For example, if a solution involves caching pre-computed data (such as by writing a map of words to neighbors to memory or a file), consider not just the time it takes to find a Levenshtein path but also the time it takes to generate this data. In other words, if an algorithm can find a path quickly after an 8-hour setup, is this actually that efficient?

#### &nbsp;&nbsp;Do not bother with multithreading

This is beyond the scope of this project.
