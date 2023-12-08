package org.reactome.resource.refseq.peptide;

import org.reactome.resource.UniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class RefSeqPeptideFileRetriever extends UniProtMapperRetriever {

    public RefSeqPeptideFileRetriever() {
        super("RefSeqPeptide");
    }
}
