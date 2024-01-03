package org.reactome.resource.hgnc;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/27/2023
 */
public class HGNCReferenceCreator extends ReferenceSequenceReferenceCreator {

    public HGNCReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("HGNC", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
