This is Tristen's Levenshtein project. I've forked it so I can add a series of commits illustrating various possibilities.
I'll show some correctness, quality, and performance improvements over a series of hopefully small commits.

## Current progress

I chose to focus on the time taken for the 2nd partition on the 0th thread for the 370k dictionary. Why? Well, I had instrumented the 0th thread 
for some extra telemetry, and I just kinda stuck with the 2nd iteration. Everything is well warmed up at that point, so it seemed reasonable.

At this point we have ~78.4% improvement, going from an initial time of 46426ms to 10046ms.

If you look through the diffs, they fell into only a few categories:

1. Removed superfluous code: some write-only code, and some always false checks involving hasing into the core data structures.
These were largely identified by the IDE's (Rider) builtin analysis. This wasn an initial ~7% improvement.
2. Algorithmic improvement: counting the paths back to the root word was quite slow, and the change here resulted in the 2nd largest single improvement of ~34%.
3. Memory: reusing memory yielded the rest of the results, along with the biggest single improvement of ~44%.

## Next steps

This program uses a very large amount of memory, and as originally written it churns through it very quickly. This results in a lot of allocation, and 
a lot of GC time. Reusing memory whenever you can in cases like this is usually a big win, and that's been the case here. 

The next big step wrt memory is to look at the parent lists in the graph. These are all small List<int>, and there's a ton of them.
Pooling these could help. A histogram of sizes would be a big help in deciding what approach to take. I suspect most of these are a
single element long. It's possible that folding the smallest sizes into the dict entry could be a nice win. Pre-sizing these to 16 helped
massively, but they're still the largest source of churn in the heap.

## A word on optimization

It's important to revisit optimizations as your program changes to see if they're still a win or not. You might reject an idea because
it is a small loss, even when you thought it was good, but then find with other optimizations you get the win you expect. Or you might
find something was a win at one point, but with other changes it's a decent loss now. Tuning of things like dictionary and list sizes
should be revisited often because they're so easy to tweak.

In short, never assume a win last week is a win today.

### Original README.md follows

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
