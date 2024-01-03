package org.reactome.resource.pharostargets;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class PharosTargetsReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {
    public PharosTargetsReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("PharosTargets", uniProtToResourceIdentifiers);
    }
}
