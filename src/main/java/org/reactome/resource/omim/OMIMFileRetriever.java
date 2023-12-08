package org.reactome.resource.omim;

import org.reactome.resource.UniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class OMIMFileRetriever extends UniProtMapperRetriever {

    public OMIMFileRetriever() {
        super("OMIM");
    }
}
