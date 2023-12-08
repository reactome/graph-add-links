package org.reactome.resource.wormbase;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class WormbaseReferenceCreator extends ReferenceSequenceReferenceCreator {

    public WormbaseReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("Wormbase", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
