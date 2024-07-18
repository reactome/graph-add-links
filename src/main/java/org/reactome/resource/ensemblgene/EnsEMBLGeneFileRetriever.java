package org.reactome.resource.ensemblgene;

import org.reactome.resource.MultipleFileEnsEMBLBioMartUniProtRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 7/8/2024
 */
public class EnsEMBLGeneFileRetriever extends MultipleFileEnsEMBLBioMartUniProtRetriever {

    public EnsEMBLGeneFileRetriever() {
        super("EnsEMBLGene");
    }
}
