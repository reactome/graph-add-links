package org.reactome.resource.hmdbprotein;

import org.reactome.retrievers.SingleFileBasicFileRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class HMDBProteinFileRetriever extends SingleFileBasicFileRetriever {

    public HMDBProteinFileRetriever() {
        super("HMDBProtein");
    }
}
