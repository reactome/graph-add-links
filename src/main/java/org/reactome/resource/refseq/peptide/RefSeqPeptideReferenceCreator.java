package org.reactome.resource.refseq.peptide;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class RefSeqPeptideReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public RefSeqPeptideReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("RefSeqPeptide", uniProtToResourceIdentifiers);
    }
}
