package org.reactome.resource.kegg;

import org.reactome.resource.UniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/26/2023
 */
public class KEGGFileRetriever extends UniProtMapperRetriever {

    public KEGGFileRetriever() {
        super("KEGG");
    }
}
