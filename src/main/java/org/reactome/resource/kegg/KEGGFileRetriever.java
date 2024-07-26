package org.reactome.resource.kegg;

import org.reactome.retrievers.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/26/2023
 */
public class KEGGFileRetriever extends SingleFileUniProtMapperRetriever {

    public KEGGFileRetriever() {
        super("KEGG");
    }
}
