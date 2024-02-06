package org.reactome.resource.refseqrna;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.DatabaseIdentifierReferenceCreator;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class RefSeqRNAReferenceCreator extends ReferenceSequenceReferenceCreator {

    public RefSeqRNAReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("RefSeqRNA", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.RNA);
    }
}
