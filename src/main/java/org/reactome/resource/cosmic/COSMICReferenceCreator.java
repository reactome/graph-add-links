package org.reactome.resource.cosmic;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 1/2/2024
 */
public class COSMICReferenceCreator extends ReferenceSequenceReferenceCreator {
    public COSMICReferenceCreator(Map<String, Set<String>> uniprotIdentifierToReferenceIdentifiers) {
        super("COSMIC", uniprotIdentifierToReferenceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
