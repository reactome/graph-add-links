package org.reactome.resource.kegg;

import org.reactome.graphnodes.ReferenceSequence;
import org.reactome.referencecreators.ReferenceSequenceReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 12/27/2023
 */
public class KEGGReferenceCreator extends ReferenceSequenceReferenceCreator {

    public KEGGReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("KEGG", uniProtToResourceIdentifiers, ReferenceSequence.ReferenceSequenceType.DNA);
    }
}
