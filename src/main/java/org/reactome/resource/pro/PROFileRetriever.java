package org.reactome.resource.pro;

import org.reactome.retrievers.SingleFileBasicFileRetriever;


/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/17/2023
 */
public class PROFileRetriever extends SingleFileBasicFileRetriever {

    public PROFileRetriever() {
        super("PRO");
    }
}
