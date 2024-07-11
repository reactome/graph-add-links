package org.reactome.resource.ensembl;

import org.reactome.otheridentifiers.retrievers.EnsEMBLBioMartUniProtRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class ENSEMBLFileRetriever extends EnsEMBLBioMartUniProtRetriever {

    public ENSEMBLFileRetriever() {
        super("ENSEMBL");
    }
}
