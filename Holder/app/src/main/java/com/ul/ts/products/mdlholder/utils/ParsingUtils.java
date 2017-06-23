package com.ul.ts.products.mdlholder.utils;

import android.util.*;

import java.util.*;

public class ParsingUtils {

    public static String addDotsToDate(String input) {
        return input.substring(0, 2) 	// DD
                + "." + input.substring(2, 4) 	// MM
                + "." + input.substring(4);
    }

    public static byte[] getImage(byte[] input) {
        byte[] magicNumber = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        String inputString = new String(input);
        String magic = new String(magicNumber);

        int start = inputString.indexOf(magic);
        Log.d("magic index", Integer.toString(start));
        return Arrays.copyOfRange(input, start, input.length);
    }

    public static String fromYYYYtoYY(String fromDate) {
        return fromDate.substring(0, 6) + fromDate.substring(fromDate.length()-2);
    }

    public static String mrzToBis(String mrz) {
        return mrz.substring(1, mrz.length()-1);
    }
}
