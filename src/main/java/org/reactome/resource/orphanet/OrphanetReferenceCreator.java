package org.reactome.resource.orphanet;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class OrphanetReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public OrphanetReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("Orphanet", uniProtToResourceIdentifiers);
    }
}
