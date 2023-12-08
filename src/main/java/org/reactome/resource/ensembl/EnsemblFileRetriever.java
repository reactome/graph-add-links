package org.reactome.resource.ensembl;

import org.reactome.resource.UniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class EnsemblFileRetriever extends UniProtMapperRetriever {

    public EnsemblFileRetriever() {
        super("ENSEMBL");
    }
}
