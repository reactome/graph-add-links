package org.reactome.resource.ensemblpeptide;

import org.reactome.retrievers.MultipleFileEnsEMBLBioMartUniProtRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLPeptideFileRetriever extends MultipleFileEnsEMBLBioMartUniProtRetriever {

    public EnsEMBLPeptideFileRetriever() {
        super("EnsEMBLPeptide");
    }
}
