package org.reactome.resource.refseqpeptide;

import org.reactome.retrievers.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class RefSeqPeptideFileRetriever extends SingleFileUniProtMapperRetriever {

    public RefSeqPeptideFileRetriever() {
        super("RefSeqPeptide");
    }
}
