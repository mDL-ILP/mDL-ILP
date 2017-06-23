package com.ul.ts.products.mdlreader.utils;

import android.util.Log;

import java.util.Arrays;

public class ParsingUtils {

    public static String BCDtoString(byte bcd) {
        StringBuilder sb = new StringBuilder();
        byte high = (byte) (bcd & 0xf0);
        high >>>= (byte) 4;
        high = (byte) (high & 0x0f);
        byte low = (byte) (bcd & 0x0f);
        sb.append(high);
        sb.append(low);
        return sb.toString();
    }

    public static String BCDtoString(byte[] bcd) {
        StringBuilder sb = new StringBuilder();
        for (byte aBcd : bcd) {
            sb.append(BCDtoString(aBcd));
        }
        return sb.toString();
    }

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
