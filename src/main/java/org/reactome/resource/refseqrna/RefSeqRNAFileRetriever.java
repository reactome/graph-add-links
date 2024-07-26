package org.reactome.resource.refseqrna;

import org.reactome.retrievers.SingleFileUniProtMapperRetriever;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class RefSeqRNAFileRetriever extends SingleFileUniProtMapperRetriever {

    public RefSeqRNAFileRetriever() {
        super("RefSeqRNA");
    }
}
