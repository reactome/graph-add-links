package org.reactome.resource.xenbase;

import org.reactome.retrievers.SingleFileBasicFileRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class XenbaseFileRetriever extends SingleFileBasicFileRetriever {

    public XenbaseFileRetriever() {
        super("Xenbase");
    }
}
