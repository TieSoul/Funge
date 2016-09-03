package com.company;

public class Utility {
    public static int encodeString(String str) {
        int r = 0;
        for (char c : str.toCharArray()) {
            r <<= 8;
            r |= c;
        }
        return r;
    }
}
