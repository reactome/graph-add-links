package org.reactome.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/16/2023
 */
public class StringUtils {

    public static String quote(String string) {
        return "\"" + string + "\"";
    }

    public static String getLongest(List<String> strings) {
        List<String> modifiableStrings = new ArrayList<>(strings);
        modifiableStrings.sort((s1, s2) -> Integer.compare(s2.length(), s1.length()));
        return modifiableStrings.get(0);
    }
}
