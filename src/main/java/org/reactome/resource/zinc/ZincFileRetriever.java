package org.reactome.resource.zinc;

import org.reactome.retrievers.SingleFileBasicFileRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class ZincFileRetriever extends SingleFileBasicFileRetriever {

    public ZincFileRetriever() {
        super("Zinc");
    }
}
