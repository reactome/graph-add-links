package org.reactome.resource.genecards;

import org.reactome.retrievers.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class GeneCardsFileRetriever extends SingleFileUniProtMapperRetriever {

    public GeneCardsFileRetriever() {
        super("GeneCards");
    }
}
