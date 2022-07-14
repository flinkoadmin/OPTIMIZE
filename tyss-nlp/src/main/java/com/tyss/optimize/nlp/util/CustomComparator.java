package com.tyss.optimize.nlp.util;

import java.math.BigDecimal;
import java.util.Comparator;

public class CustomComparator implements Comparator<String> {

    @Override
    public int compare(String str1, String str2) {
        try{
            BigDecimal val1 = new BigDecimal(str1);
            BigDecimal val2 = new BigDecimal(str2);
            return val1.compareTo(val2);
        } catch (Exception e){
            return str1.compareTo(str2);
        }
    }
}
