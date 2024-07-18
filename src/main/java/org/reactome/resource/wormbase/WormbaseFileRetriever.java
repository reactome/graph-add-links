package org.reactome.resource.wormbase;

import org.reactome.resource.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class WormbaseFileRetriever extends SingleFileUniProtMapperRetriever {

    public WormbaseFileRetriever() {
        super("Wormbase");
    }
}
