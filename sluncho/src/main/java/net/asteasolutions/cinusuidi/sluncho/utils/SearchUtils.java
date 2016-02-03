/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.asteasolutions.cinusuidi.sluncho.utils;

import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;

/**
 *
 * @author mihail
 */
public final class SearchUtils {
    public static String cleanup(String str) {
        String result = str.replace("?", "");
        result = result.replace(".", "");
        result = result.replace("!", "");
        result = QueryParserUtil.escape(str.trim());
        int lastLen = result.length();
        int newLen = lastLen;
        
        do {
            lastLen = newLen;
            result = result.replaceAll("  ", " ");
            newLen = result.length();
        } while(newLen < lastLen);
        return result;
    }
    public static String replaceSpacesWithOR(String str) {
        return cleanup(str).replaceAll(" ", " OR ");
    }
}
