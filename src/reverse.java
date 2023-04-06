/*
Author: Tristen Yim
Project: isReverse (GS14-02)
Filename: reverse.java
Maintenance Log:
    Started. (9 April 2023 9:36)
        Finished. (9:58)
*/
public class reverse {
    public static void main(String[] args) {
        System.out.println(isReverse("Hello", "olleh"));
        System.out.println(isReverse("Hello", "ollea"));
    }

    /**
     * False is returned is the words are not of equal length.
     * Then converts strings to all lowercase char arrays and simultaneously iterates across them, one from the front and one from the back.
     * This iteration is performed recursively, with a third parameter, the current index, being added to a helper method.
     * Checks if each character is equal on each iteration, returning false if not.
     * If it reaches the end of the words without returning false, they must be reverses and true is returned.
     * @param s1 First string.
     * @param s2 Second string.
     * @return True if first string is the reverse of the second string (ignoring caps), false otherwise.
     */
    public static boolean isReverse(String s1, String s2) {
        // Length is checked once here to avoid making extra checks in the recursive call.
        if (s1.length() != s2.length()) {
            return false;
        }
        return isReverse(s1.toLowerCase().toCharArray(), s2.toLowerCase().toCharArray(), 0);
    }
    private static boolean isReverse(char[] a1, char[] a2, int i) {
        if (i == a1.length) {
            return true;
        } else if (a1[i] != a2[a2.length - i - 1]) {
            return false;
        }
        return isReverse(a1, a2, ++i);
    }
}
