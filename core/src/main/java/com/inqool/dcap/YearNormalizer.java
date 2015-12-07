package com.inqool.dcap;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lukas Jane (inQool) 3. 11. 2015.
 */
public class YearNormalizer {

    //Takes input and returns the other parameters
    public static void normalizeCreatedYear(String input, AtomicInteger start, AtomicBoolean startValid, AtomicInteger end, AtomicBoolean endValid) {
        input = input.trim();
        //Match stuff like -304-1015 or 1992 - 1995
        Matcher m = Pattern.compile("\\A(-?\\d{1,4})\\s?-\\s?(\\d{1,4})\\z").matcher(input);
        if(m.matches()) {
            start.set(Integer.valueOf(m.group(1)));
            end.set(Integer.valueOf(m.group(2)));
            startValid.set(true);
            endValid.set(true);
            return;
        }
        //Match 1999 or -10
        else if(input.matches("\\A-?\\d{1,4}\\z")) {
            start.set(Integer.valueOf(input));
            end.set(Integer.valueOf(input));
            startValid.set(true);
            endValid.set(true);
            return;
        }
        //Match [1961?]
        m = Pattern.compile("\\A\\[?(-?\\d{1,4})\\??\\]?\\z").matcher(input);
        if(m.matches()) {
            start.set(Integer.valueOf(m.group(1)));
            end.set(Integer.valueOf(m.group(1)));
            startValid.set(true);
            endValid.set(true);
            return;
        }
        //Match 1.1.1970 or 03.1999
        m = Pattern.compile("\\A\\d{1,2}?\\.?\\d{1,2}.(\\d{1,4})\\z").matcher(input);
        if(m.matches()) {
            start.set(Integer.valueOf(m.group(1)));
            end.set(Integer.valueOf(m.group(1)));
            startValid.set(true);
            endValid.set(true);
            return;
        }
        //Match 1999-
        m = Pattern.compile("\\A(\\d{1,4})-\\z").matcher(input);
        if(m.matches()) {
            start.set(Integer.valueOf(m.group(1)));
            end.set(Integer.valueOf(m.group(1)));
            startValid.set(true);
            return;
        }
        //Match x20 - century
        m = Pattern.compile("\\A[xX](\\d{2})\\z").matcher(input);
        if(m.matches()) {
            String matched = m.group(1);    //20
            start.set(Integer.valueOf(matched + "00") - 100); //20 -> 2000 -> 1900
            end.set(Integer.valueOf(matched + "99") - 100); //20 -> 2099 -> 1999
            startValid.set(true);
            endValid.set(true);
            return;
        }
        //Match 199? or 199-
        m = Pattern.compile("\\A(\\d{3})[\\?\\-]\\z").matcher(input);
        if(m.matches()) {
            String matched = m.group(1);    //199
            start.set(Integer.valueOf(matched + "0")); //1990
            end.set(Integer.valueOf(matched + "9")); //1999
            startValid.set(true);
            endValid.set(true);
            return;
        }
        m = Pattern.compile("(\\d{4})").matcher(input);
        if(m.matches()) {
            start.set(Integer.valueOf(m.group(1)));
            end.set(Integer.valueOf(m.group(1)));
            startValid.set(true);
            endValid.set(true);
            return;
        }
    }

    //Normalizes date to one of
    //null, 1999, 1999-, 1999-2000
    public static String preNormalize(String input) {
        if(input == null) {
            return null;
        }
        AtomicInteger yearStart = new AtomicInteger();
        AtomicInteger yearEnd = new AtomicInteger();
        AtomicBoolean startValid = new AtomicBoolean();
        AtomicBoolean endValid = new AtomicBoolean();
        normalizeCreatedYear(input, yearStart, startValid, yearEnd, endValid);
        if(!startValid.get()) {
            return null;
        }
        String result = String.valueOf(yearStart.get());
        if(!endValid.get()) {
            return result + "-";
        }
        if(yearStart.get() == yearEnd.get()) {
            return result;
        }
        return result + "-" + yearEnd.get();
    }
}
