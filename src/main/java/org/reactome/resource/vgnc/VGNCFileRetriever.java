package org.reactome.resource.vgnc;

import org.reactome.retrievers.SingleFileBasicFileRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class VGNCFileRetriever extends SingleFileBasicFileRetriever {

    public VGNCFileRetriever() {
        super("VGNC");
    }
}
