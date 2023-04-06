
/*
Author: Tristen Yim
Project: isReverse (GS14-02)
Filename: reverse.java
Maintenance Log:
    Started. (9 April 2023 9:36)
        Finished (9:58)
 */
public class reverse {
    public static void main(String[] args) {
        System.out.println(isReverse("Hello", "olleh"));
        System.out.println(isReverse("Hello", "ollea"));
    }
    public static boolean isReverse(String s1, String s2) {
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
