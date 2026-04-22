package com.sarthak.library.author_book_management.util;

import java.util.ArrayList;
import java.util.List;

public class CsvUtil {
    public static String escapeCSV(String value){
        if(value==null)return "";
        return  "\"" + value.replace("\"","\"\"") + "\"";
    }

    public static String[] parseCsvLine(String line){
        List<String> result = new ArrayList<>();
        StringBuilder current  = new StringBuilder();
        boolean inQuotes = false;

        for (int i  = 0 ; i < line.length() ; i++ ){
            char ch = line.charAt(i);
            if(ch== '"'){
                inQuotes = !inQuotes;
            }else if (ch == ',' && !inQuotes){
                result.add(current.toString());
                current.setLength(0);
            }else{
                current.append(ch);
            }
        }
        result.add(current.toString());
        //result.toArray will straight up -> give up an object
        //this basically tells it to make a string array
        return result.toArray(new String[0]);
    }
}
