package org.reactome.resource.monarch;

import org.reactome.retrievers.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class MonarchFileRetriever extends SingleFileUniProtMapperRetriever {

    public MonarchFileRetriever() {
        super("Monarch");
    }
}
