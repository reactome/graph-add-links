package org.reactome.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 2/6/2024
 */
public class UniProtUtils {
    public static boolean isValidUniProtId(String uniprotId) {
        // From https://www.uniprot.org/help/accession_numbers
        Pattern validUniProtPattern = Pattern.compile("[OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}");
        Matcher validUniProtMatcher = validUniProtPattern.matcher(uniprotId);

        return validUniProtMatcher.find();
    }
}
