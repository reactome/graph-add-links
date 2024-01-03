package org.reactome.resource.hgnc;

import org.reactome.resource.BasicFileRetriever;
import org.reactome.resource.UniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/26/2023
 */
public class HGNCFileRetriever extends BasicFileRetriever {

    public HGNCFileRetriever() {
        super("HGNC");
    }
}
