package org.reactome.resource.ensemblgene;

import org.reactome.otheridentifiers.retrievers.EnsEMBLBioMartUniProtRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLGeneFileRetriever extends EnsEMBLBioMartUniProtRetriever {

    public EnsEMBLGeneFileRetriever() {
        super("EnsEMBLGene");
    }
}
