package org.reactome.resource.monarch;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class MonarchReferenceCreator extends ReferenceSequenceReferenceCreator {

    public MonarchReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("Monarch", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
