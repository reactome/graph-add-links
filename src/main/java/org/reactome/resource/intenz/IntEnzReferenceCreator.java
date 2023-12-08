package org.reactome.resource.intenz;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class IntEnzReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public IntEnzReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("IntEnz", uniProtToResourceIdentifiers);
    }
}
