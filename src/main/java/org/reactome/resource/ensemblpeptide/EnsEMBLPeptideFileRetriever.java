package org.reactome.resource.ensemblpeptide;

import org.reactome.otheridentifiers.retrievers.EnsEMBLBioMartUniProtRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLPeptideFileRetriever extends EnsEMBLBioMartUniProtRetriever {

    public EnsEMBLPeptideFileRetriever() {
        super("EnsEMBLPeptide");
    }
}
