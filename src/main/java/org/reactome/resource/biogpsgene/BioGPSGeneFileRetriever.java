package org.reactome.resource.biogpsgene;


import org.reactome.retrievers.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class BioGPSGeneFileRetriever extends SingleFileUniProtMapperRetriever {

    public BioGPSGeneFileRetriever() {
        super("BioGPSGene");
    }
}
