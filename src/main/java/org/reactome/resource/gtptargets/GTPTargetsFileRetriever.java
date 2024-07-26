package org.reactome.resource.gtptargets;

import org.reactome.retrievers.SingleFileBasicFileRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/20/2023
 */
public class GTPTargetsFileRetriever extends SingleFileBasicFileRetriever {

    public GTPTargetsFileRetriever() {
        super("GTPTargets");
    }
}
