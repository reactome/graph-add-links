package org.reactome.resource.hpa;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class HPAReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public HPAReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("HPA", uniProtToResourceIdentifiers);
    }
}
