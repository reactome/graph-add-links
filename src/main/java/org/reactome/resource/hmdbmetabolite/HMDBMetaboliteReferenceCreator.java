package org.reactome.resource.hmdbmetabolite;

import org.reactome.referencecreators.UniProtDatabaseIdentifierReferenceCreator;

import java.util.Map;
import java.util.Set;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 *         Created 11/19/2023
 */
public class HMDBMetaboliteReferenceCreator extends UniProtDatabaseIdentifierReferenceCreator {

    public HMDBMetaboliteReferenceCreator(Map<String, Set<String>> uniProtToResourceIdentifiers) {
        super("HMDBMetabolite", uniProtToResourceIdentifiers);
    }
}
