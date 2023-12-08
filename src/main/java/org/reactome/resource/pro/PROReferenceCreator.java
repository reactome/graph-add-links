package org.reactome.resource.pro;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class PROReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {
    public PROReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("PRO", uniProtToResourceIdentifiers);
    }
}
