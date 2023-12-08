package org.reactome.resource.ucsc;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class UCSCReferenceCreator extends ReferenceSequenceReferenceCreator {

    public UCSCReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("UCSC", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
