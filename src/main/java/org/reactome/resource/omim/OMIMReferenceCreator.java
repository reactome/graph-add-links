package org.reactome.resource.omim;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class OMIMReferenceCreator extends ReferenceSequenceReferenceCreator {

    public OMIMReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("OMIM", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
