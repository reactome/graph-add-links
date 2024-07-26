package org.reactome.resource.hgnc;

import org.reactome.retrievers.SingleFileBasicFileRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/26/2023
 */
public class HGNCFileRetriever extends SingleFileBasicFileRetriever {

    public HGNCFileRetriever() {
        super("HGNC");
    }
}
